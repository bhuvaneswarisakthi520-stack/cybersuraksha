import { FieldValue, getFirestore } from "firebase-admin/firestore";
import { HttpsError, onCall } from "firebase-functions/v2/https";

export const sendSOSAlert = onCall(async (request) => {
  const db = getFirestore();

  const { incidentId, alertType = "SOS" } = request.data;
  const userId = request.auth?.uid ?? "demo-user";

  if (!incidentId) {
    throw new HttpsError(
      "invalid-argument",
      "incidentId is required.",
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
      "You cannot alert contacts for this incident.",
    );
  }

  if (incident.alertSent) {
    return {
      success: true,
      alreadySent: true,
      message: "SOS alert was already recorded.",
    };
  }

  const location = incident.location;
  const mapLink = location
    ? `https://www.google.com/maps?q=${location.latitude},${location.longitude}`
    : "Location unavailable";

  await incidentRef.collection("alerts").add({
    alertType,
    recipient: "trusted_contacts_demo",
    message: `CyberSuraksha ${alertType}: emergency assistance may be needed.`,
    mapLink,
    createdAt: FieldValue.serverTimestamp(),
    status: "SIMULATED_DELIVERED",
  });

  await incidentRef.update({
    alertSent: true,
    lastAlertType: alertType,
    updatedAt: FieldValue.serverTimestamp(),
  });

  await incidentRef.collection("events").add({
    type: "TRUSTED_CONTACT_ALERT",
    message: `${alertType} alert recorded for trusted contacts.`,
    createdAt: FieldValue.serverTimestamp(),
  });

  return {
    success: true,
    incidentId,
    alertType,
    mapLink,
    deliveryStatus: "SIMULATED_DELIVERED",
  };
});