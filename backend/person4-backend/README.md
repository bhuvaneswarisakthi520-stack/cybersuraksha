# CyberSuraksha — Person 4 demo API

Small Node/Express prototype for temporary journey links, location sharing, safe points, encrypted evidence storage, SHA-256 integrity records, and PDF reports.

## Run locally

```powershell
npm.cmd start
```

Health check: `http://localhost:3000/health`

Data is stored under `data/` and excluded from Git. This is a local hackathon prototype, not a production deployment. The JSON database contains sensitive location metadata in plaintext; protect the computer and do not commit or share the `data/` folder.

## API

### Create temporary journey

`POST /api/journeys` with JSON body `{"expiresInMinutes":60}`.

Returns `journeyId`, `ownerToken`, `shareToken`, and `expiresAt`. The owner token is for app uploads/location updates/revocation. The separate share token is for the receiver. Only token hashes are stored. Keep both tokens secret; the response is the only time they are returned.

### Revoke journey

`DELETE /api/journeys/:id` with header `x-owner-token: <ownerToken>`.

### Update/read live location

`PUT /api/journeys/:id/location` with `x-owner-token` and JSON `{ "latitude": 13.08, "longitude": 80.27, "accuracyMeters": 18 }`.

`GET /api/journeys/:id/location` with `x-share-token: <shareToken>`.

### Safe points

`GET /api/safe-points`, optionally `?latitude=13.08&longitude=80.27` to sort the demo records approximately by distance. Replace the seed point with verified and maintained local service data before any real use.

### Encrypted evidence

`POST /api/journeys/:id/evidence` expects raw encrypted bytes (`Content-Type: application/octet-stream`), `x-owner-token`, `x-emergency-activated: true`, `x-content-encrypted: true`, `x-file-name`, and an `x-media-type` such as `video/mp4` or `audio/aac`. Maximum 25 MB per upload. The phone app must capture only after emergency activation and encrypt locally before uploading. The server stores ciphertext, metadata, and SHA-256; the server cannot verify that the client truthfully encrypted it. The decryption key must be delivered to approved contacts separately and securely.

`GET /api/journeys/:id/evidence` with `x-share-token` returns metadata and hashes.

`GET /api/journeys/:id/evidence/:evidenceId` with `x-share-token` downloads ciphertext.

`DELETE /api/journeys/:id/evidence/:evidenceId` with `x-owner-token` deletes the stored file and records a deletion audit event.

### PDF report

`GET /api/journeys/:id/report.pdf` with `x-share-token` generates a report containing location timeline, evidence hashes, and audit hashes. It is a demo summary, not a forensic certification.

## Important integration boundaries

- This backend never opens the phone camera or microphone. Person 1/2's Android app owns permissions, trigger, recording, encryption, and user consent.
- Do not record or upload before intentional emergency activation. Show the OS foreground recording indicator and provide a stop/delete path.
- This prototype uses a local JSON database and filesystem. It does not include Person 3's authentication, authorization, notifications, or incident APIs; agree on API contracts before merging.
- Local tokens are capability secrets. Never log, commit, or publish them. Use HTTPS and a managed secret/storage service before deployment.
- Demo safe-point data is fictional and must not be represented as a verified safety service.
