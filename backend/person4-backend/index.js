const express = require("express");
const helmet = require("helmet");
const cors = require("cors");
const crypto = require("node:crypto");
const fs = require("node:fs/promises");
const path = require("node:path");
const PDFDocument = require("pdfkit");

const app = express();
const PORT = Number(process.env.PORT || 3000);
const DATA_DIR = path.join(__dirname, "data");
const EVIDENCE_DIR = path.join(DATA_DIR, "private-evidence");
const DB_FILE = path.join(DATA_DIR, "db.json");
const MAX_EVIDENCE_BYTES = 25 * 1024 * 1024;

app.disable("x-powered-by");
app.use(helmet());
app.use(cors({
  origin(origin, callback) {
    // Mobile apps do not send an Origin header. Local web demos use localhost.
    if (!origin || /^https?:\/\/(localhost|127\.0\.0\.1)(:\d+)?$/.test(origin)) {
      return callback(null, true);
    }
    return callback(new Error("Origin not allowed"));
  },
}));
app.use(express.json({ limit: "32kb" }));

let database = { journeys: {}, safePoints: [], audit: [] };
let writeQueue = Promise.resolve();

async function loadDatabase() {
  await fs.mkdir(EVIDENCE_DIR, { recursive: true });
  try {
    database = JSON.parse(await fs.readFile(DB_FILE, "utf8"));
  } catch (error) {
    if (error.code !== "ENOENT") throw error;
    database.safePoints = [
      { id: "safe-demo-1", name: "Demo Community Help Point", latitude: 13.0827, longitude: 80.2707, address: "Replace with verified local data" },
    ];
    await saveDatabase();
  }
  database.journeys ||= {};
  database.safePoints ||= [];
  database.audit ||= [];
}

function saveDatabase() {
  writeQueue = writeQueue.then(async () => {
    const temporaryFile = `${DB_FILE}.tmp`;
    await fs.writeFile(temporaryFile, JSON.stringify(database, null, 2), { encoding: "utf8" });
    await fs.rename(temporaryFile, DB_FILE);
  });
  return writeQueue;
}

function sha256(value) {
  return crypto.createHash("sha256").update(value).digest("hex");
}

function tokenMatches(presented, expectedHash) {
  if (typeof presented !== "string" || !expectedHash) return false;
  const actual = Buffer.from(sha256(presented), "hex");
  const expected = Buffer.from(expectedHash, "hex");
  return actual.length === expected.length && crypto.timingSafeEqual(actual, expected);
}

async function addAudit(journey, action, details = {}) {
  const previousHash = database.audit.at(-1)?.hash || "0".repeat(64);
  const entry = {
    sequence: database.audit.length + 1,
    journeyId: journey.id,
    action,
    occurredAt: new Date().toISOString(),
    details,
    previousHash,
  };
  entry.hash = sha256(JSON.stringify(entry));
  database.audit.push(entry);
  journey.auditHashes.push(entry.hash);
}

function getJourney(req, res, tokenHeader, tokenField) {
  const journey = database.journeys[req.params.id];
  if (!journey || journey.revokedAt || Date.parse(journey.expiresAt) <= Date.now()) {
    res.status(404).json({ error: "Journey link is invalid, expired, or revoked." });
    return null;
  }
  if (!tokenMatches(req.get(tokenHeader), journey[tokenField])) {
    res.status(401).json({ error: "Access token is invalid." });
    return null;
  }
  return journey;
}

function safeFileName(name) {
  return path.basename(String(name || "evidence.bin")).replace(/[^a-zA-Z0-9._-]/g, "_").slice(0, 100);
}

app.get("/health", (req, res) => {
  res.json({ status: "ok", service: "CyberSuraksha Person 4 API" });
});

// Create a temporary journey. Save the returned owner/share tokens securely; only their hashes are stored.
app.post("/api/journeys", async (req, res, next) => {
  try {
    const expiresInMinutes = Number(req.body.expiresInMinutes ?? 60);
    if (!Number.isInteger(expiresInMinutes) || expiresInMinutes < 1 || expiresInMinutes > 1440) {
      return res.status(400).json({ error: "Expiry must be between 1 and 1440 minutes." });
    }
    const id = crypto.randomUUID();
    const ownerToken = crypto.randomBytes(32).toString("base64url");
    const shareToken = crypto.randomBytes(32).toString("base64url");
    const journey = {
      id,
      ownerTokenHash: sha256(ownerToken),
      shareTokenHash: sha256(shareToken),
      createdAt: new Date().toISOString(),
      expiresAt: new Date(Date.now() + expiresInMinutes * 60_000).toISOString(),
      revokedAt: null,
      latestLocation: null,
      locationHistory: [],
      evidence: [],
      auditHashes: [],
    };
    database.journeys[id] = journey;
    await addAudit(journey, "journey.created", { expiresAt: journey.expiresAt });
    await saveDatabase();
    res.status(201).json({ journeyId: id, ownerToken, shareToken, expiresAt: journey.expiresAt,
      note: "Tokens are shown once. Store them safely; never put them in public screenshots." });
  } catch (error) { next(error); }
});

// Owner can revoke a link immediately.
app.delete("/api/journeys/:id", async (req, res, next) => {
  try {
    const journey = getJourney(req, res, "x-owner-token", "ownerTokenHash");
    if (!journey) return;
    journey.revokedAt = new Date().toISOString();
    await addAudit(journey, "journey.revoked");
    await saveDatabase();
    res.json({ status: "revoked", revokedAt: journey.revokedAt });
  } catch (error) { next(error); }
});

// Owner submits GPS updates. Recording/location collection is controlled by the phone app, not this API.
app.put("/api/journeys/:id/location", async (req, res, next) => {
  try {
    const journey = getJourney(req, res, "x-owner-token", "ownerTokenHash");
    if (!journey) return;
    const { latitude, longitude, accuracyMeters } = req.body;
    if (typeof latitude !== "number" || latitude < -90 || latitude > 90 ||
        typeof longitude !== "number" || longitude < -180 || longitude > 180 ||
        (accuracyMeters !== undefined && (typeof accuracyMeters !== "number" || accuracyMeters < 0))) {
      return res.status(400).json({ error: "Valid latitude/longitude and optional non-negative accuracyMeters are required." });
    }
    const point = { latitude, longitude, accuracyMeters: accuracyMeters ?? null, capturedAt: new Date().toISOString() };
    journey.latestLocation = point;
    journey.locationHistory.push(point);
    await addAudit(journey, "location.updated", { capturedAt: point.capturedAt });
    await saveDatabase();
    res.json({ status: "location saved", location: point });
  } catch (error) { next(error); }
});

// Trusted receiver reads location only with the separate, expiring share token.
app.get("/api/journeys/:id/location", (req, res) => {
  const journey = getJourney(req, res, "x-share-token", "shareTokenHash");
  if (!journey) return;
  res.json({ journeyId: journey.id, expiresAt: journey.expiresAt, location: journey.latestLocation });
});

app.get("/api/safe-points", (req, res) => {
  const latitude = Number(req.query.latitude);
  const longitude = Number(req.query.longitude);
  const safePoints = database.safePoints.map((point) => ({ ...point }));
  // Optional simple distance estimate for demo sorting; replace with a proper geo query at scale.
  if (Number.isFinite(latitude) && Number.isFinite(longitude)) {
    safePoints.sort((a, b) =>
      Math.hypot(a.latitude - latitude, a.longitude - longitude) -
      Math.hypot(b.latitude - latitude, b.longitude - longitude));
  }
  res.json({ safePoints, note: "Demo seed point must be replaced with verified, maintained local services." });
});

// Store encrypted media bytes only. The Flutter app must encrypt locally and call this only after emergency activation.
app.post("/api/journeys/:id/evidence", express.raw({ type: "application/octet-stream", limit: MAX_EVIDENCE_BYTES }), async (req, res, next) => {
  try {
    const journey = getJourney(req, res, "x-owner-token", "ownerTokenHash");
    if (!journey) return;
    if (req.get("x-emergency-activated") !== "true" || req.get("x-content-encrypted") !== "true") {
      return res.status(400).json({ error: "Upload requires emergency activation and client-side encryption confirmation." });
    }
    if (!Buffer.isBuffer(req.body) || req.body.length === 0) {
      return res.status(400).json({ error: "Send encrypted file bytes as application/octet-stream." });
    }
    const mediaType = req.get("x-media-type") || "application/octet-stream";
    if (!/^(audio|video)\/[a-zA-Z0-9.+-]+$/.test(mediaType)) {
      return res.status(400).json({ error: "Only audio/video evidence is accepted." });
    }
    const evidenceId = crypto.randomUUID();
    const storedName = `${evidenceId}.enc`;
    const evidencePath = path.join(EVIDENCE_DIR, storedName);
    const fileHash = sha256(req.body);
    await fs.writeFile(evidencePath, req.body, { flag: "wx" });
    const record = {
      id: evidenceId,
      fileName: safeFileName(req.get("x-file-name")),
      mediaType,
      encrypted: true,
      byteLength: req.body.length,
      sha256: fileHash,
      capturedAt: new Date().toISOString(),
      storageName: storedName,
    };
    journey.evidence.push(record);
    await addAudit(journey, "evidence.uploaded", { evidenceId, sha256: fileHash, byteLength: record.byteLength });
    await saveDatabase();
    res.status(201).json({ evidence: { ...record, storageName: undefined } });
  } catch (error) { next(error); }
});

app.get("/api/journeys/:id/evidence", (req, res) => {
  const journey = getJourney(req, res, "x-share-token", "shareTokenHash");
  if (!journey) return;
  res.json({ evidence: journey.evidence.map(({ storageName, ...record }) => record) });
});

// Receiver obtains ciphertext; decryption key must be delivered securely by the client app.
app.get("/api/journeys/:id/evidence/:evidenceId", async (req, res, next) => {
  try {
    const journey = getJourney(req, res, "x-share-token", "shareTokenHash");
    if (!journey) return;
    const record = journey.evidence.find((item) => item.id === req.params.evidenceId);
    if (!record) return res.status(404).json({ error: "Evidence not found." });
    const evidencePath = path.join(EVIDENCE_DIR, record.storageName);
    res.type("application/octet-stream").set("Content-Disposition", `attachment; filename="${record.id}.enc"`);
    res.send(await fs.readFile(evidencePath));
  } catch (error) { next(error); }
});

app.delete("/api/journeys/:id/evidence/:evidenceId", async (req, res, next) => {
  try {
    const journey = getJourney(req, res, "x-owner-token", "ownerTokenHash");
    if (!journey) return;
    const index = journey.evidence.findIndex((item) => item.id === req.params.evidenceId);
    if (index < 0) return res.status(404).json({ error: "Evidence not found." });
    const [record] = journey.evidence.splice(index, 1);
    await fs.rm(path.join(EVIDENCE_DIR, record.storageName), { force: true });
    await addAudit(journey, "evidence.deleted", { evidenceId: record.id, sha256: record.sha256 });
    await saveDatabase();
    res.json({ status: "deleted", evidenceId: record.id });
  } catch (error) { next(error); }
});

app.get("/api/journeys/:id/report.pdf", (req, res) => {
  const journey = getJourney(req, res, "x-share-token", "shareTokenHash");
  if (!journey) return;
  res.setHeader("Content-Type", "application/pdf");
  res.setHeader("Content-Disposition", `attachment; filename="cybersuraksha-${journey.id}.pdf"`);
  const pdf = new PDFDocument({ margin: 48 });
  pdf.pipe(res);
  pdf.fontSize(20).text("CyberSuraksha Incident Report", { underline: true });
  pdf.moveDown().fontSize(11).text("Demo report generated from server records. It is not a forensic certification.");
  pdf.moveDown().fontSize(12).text(`Journey ID: ${journey.id}`);
  pdf.text(`Created: ${journey.createdAt}`);
  pdf.text(`Expires: ${journey.expiresAt}`);
  pdf.text(`Revoked: ${journey.revokedAt || "No"}`);
  pdf.moveDown().fontSize(14).text("Location timeline");
  if (journey.locationHistory.length === 0) pdf.fontSize(10).text("No location updates recorded.");
  for (const point of journey.locationHistory) {
    pdf.fontSize(10).text(`${point.capturedAt} | ${point.latitude}, ${point.longitude} | accuracy: ${point.accuracyMeters ?? "unknown"} m`);
  }
  pdf.moveDown().fontSize(14).text("Evidence integrity records");
  if (journey.evidence.length === 0) pdf.fontSize(10).text("No evidence files recorded.");
  for (const item of journey.evidence) {
    pdf.fontSize(10).text(`${item.id} | ${item.mediaType} | ${item.capturedAt} | SHA-256: ${item.sha256}`);
  }
  pdf.moveDown().fontSize(14).text("Audit chain hashes");
  for (const hash of journey.auditHashes) pdf.fontSize(8).text(hash);
  pdf.end();
});

app.use((error, req, res, next) => {
  if (res.headersSent) return next(error);
  const status = error.status === 413 ? 413 : 500;
  res.status(status).json({ error: status === 413 ? "Upload too large (maximum 25 MB)." : "Request failed." });
});

loadDatabase().then(() => {
  app.listen(PORT, () => console.log(`CyberSuraksha Person 4 API running at http://localhost:${PORT}`));
}).catch((error) => {
  console.error("Could not start API:", error.message);
  process.exitCode = 1;
});
