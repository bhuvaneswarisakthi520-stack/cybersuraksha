import { FieldValue, getFirestore } from "firebase-admin/firestore";
import { HttpsError, onCall } from "firebase-functions/v2/https";

export const createIncident = onCall(async (request) => {
  const db = getFirestore();

  const { triggerType, location, networkState = "unknown" } = request.data;

  if (!triggerType) {
    throw new HttpsError(
      "invalid-argument",
      "triggerType is required.",
    );
  }

  const incidentRef = db.collection("incidents").doc();

  await incidentRef.set({
    incidentId: incidentRef.id,
    userId: request.auth?.uid ?? "demo-user",
    triggerType,
    state: "EMERGENCY_ACTIVATED",
    location: location ?? null,
    networkState,
    alertSent: false,
    createdAt: FieldValue.serverTimestamp(),
    updatedAt: FieldValue.serverTimestamp(),
  });

  await incidentRef.collection("events").add({
    type: "EMERGENCY_ACTIVATED",
    message: `Emergency started using ${triggerType}`,
    createdAt: FieldValue.serverTimestamp(),
  });

  return {
    success: true,
    incidentId: incidentRef.id,
    state: "EMERGENCY_ACTIVATED",
  };
});

const allowedTransitions: Record<string, string[]> = {
  EMERGENCY_ACTIVATED: ["SAFETY_CHECK", "RESOLVED"],
  SAFETY_CHECK: ["CONTACT_ALERTED", "RESOLVED"],
  CONTACT_ALERTED: ["EMERGENCY_ESCALATED", "RESOLVED"],
  EMERGENCY_ESCALATED: ["RESOLVED"],
  RESOLVED: [],
};

export const updateIncidentState = onCall(async (request) => {
  const db = getFirestore();

  const { incidentId, nextState } = request.data;
  const userId = request.auth?.uid ?? "demo-user";

  if (!incidentId || !nextState) {
    throw new HttpsError(
      "invalid-argument",
      "incidentId and nextState are required.",
    );
  }

  const incidentRef = db.collection("incidents").doc(incidentId);
  const incidentSnapshot = await incidentRef.get();

  if (!incidentSnapshot.exists) {
    throw new HttpsError("not-found", "Incident not found.");
  }

  const incident = incidentSnapshot.data();

  if (incident?.userId !== userId) {
    throw new HttpsError(
      "permission-denied",
      "You cannot update this incident.",
    );
  }

  const currentState = incident.state;

  if (!allowedTransitions[currentState]?.includes(nextState)) {
    throw new HttpsError(
      "failed-precondition",
      `Cannot change ${currentState} to ${nextState}.`,
    );
  }

  await incidentRef.update({
    state: nextState,
    updatedAt: FieldValue.serverTimestamp(),
    resolvedAt:
      nextState === "RESOLVED"
        ? FieldValue.serverTimestamp()
        : null,
  });

  await incidentRef.collection("events").add({
    type: nextState,
    message: `State changed from ${currentState} to ${nextState}`,
    createdAt: FieldValue.serverTimestamp(),
  });

  return {
    success: true,
    incidentId,
    previousState: currentState,
    state: nextState,
  };
});
export const updateIncidentLocation = onCall(async (request) => {
  const db = getFirestore();

  const { incidentId, location } = request.data;
  const userId = request.auth?.uid ?? "demo-user";

  if (!incidentId || !location) {
    throw new HttpsError(
      "invalid-argument",
      "incidentId and location are required.",
    );
  }

  if (
    typeof location.latitude !== "number" ||
    typeof location.longitude !== "number"
  ) {
    throw new HttpsError(
      "invalid-argument",
      "location.latitude and location.longitude must be numbers.",
    );
  }

  const incidentRef = db.collection("incidents").doc(incidentId);
  const incidentSnapshot = await incidentRef.get();

  if (!incidentSnapshot.exists) {
    throw new HttpsError("not-found", "Incident not found.");
  }

  const incident = incidentSnapshot.data();

  if (incident?.userId !== userId) {
    throw new HttpsError(
      "permission-denied",
      "You cannot update this incident location.",
    );
  }

  const locationPoint = {
    latitude: location.latitude,
    longitude: location.longitude,
    accuracyMeters: location.accuracyMeters ?? null,
    capturedAt: FieldValue.serverTimestamp(),
  };

  await incidentRef.update({
    location: locationPoint,
    updatedAt: FieldValue.serverTimestamp(),
  });

  await incidentRef.collection("locationUpdates").add(locationPoint);

  await incidentRef.collection("events").add({
    type: "LOCATION_UPDATED",
    message: "Latest emergency location updated.",
    createdAt: FieldValue.serverTimestamp(),
  });

  return {
    success: true,
    incidentId,
    location: {
      latitude: location.latitude,
      longitude: location.longitude,
      accuracyMeters: location.accuracyMeters ?? null,
    },
  };
});