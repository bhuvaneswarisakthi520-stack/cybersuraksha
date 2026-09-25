import { initializeApp } from "firebase-admin/app";
import { onCall } from "firebase-functions/v2/https";

import {
  createIncident,
  updateIncidentLocation,
  updateIncidentState,
} from "./incidents";
import { sendSOSAlert } from "./alerts";

initializeApp();

export const healthCheck = onCall(() => {
  return {
    success: true,
    message: "CyberSuraksha backend is running",
  };
});

export {
  createIncident,
  updateIncidentLocation,
  updateIncidentState,
  sendSOSAlert,
};