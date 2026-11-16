# CyberSuraksha Mobile App (Android Native)

CyberSuraksha is a privacy-first, offline-capable personal safety and emergency response mobile application developed natively for Android using **Kotlin** and **Jetpack Compose (Material 3)**.

---

## 🌟 Key Features

### 1. 112 India National ERSS Emergency Hub
- Direct one-tap contact buttons for 8 specialized public emergency helplines:
  - **POLICE** (`100` / `112`)
  - **FIRE** (`101`)
  - **MEDICAL** (`108` / `102`)
  - **DISASTER** (`1077`)
  - **WOMAN** (`181` / `1091`)
  - **CHILD** (`1098` Childline)
  - **ELDERLY** (`14567` Senior Citizens)
  - **RAILWAY** (`139` Railway Protection)
- Live Situation Description box with speech-to-text / microphone voice input.
- Real-time GPS location pill (e.g. Kozhipathy).
- Interactive **Integrated Helpline Information** guide.

### 2. Camouflage Calculator Disguise & Stealth Mode
- Harmless-looking, fully functional calculator overlay for duress scenarios.
- **Unlock Pattern**: Type `7777=` to return to CyberSuraksha.
- **Silent Background SOS**: Type `9999=` to immediately transmit encrypted SOS alerts with GPS coordinates and tracking links to trusted contacts while the screen remains an innocuous calculator showing `0`.

### 3. Incident History & Cryptographic Evidence Vault
- Tamper-evident incident ledger recording timestamps, locations, and triggers.
- **SHA-256 Hash Verification**: Audio (`.m4a`) and video (`.mp4`) recordings are hashed on-device for verifiable chain-of-custody.
- **PIN-Protected Deletion**: Security PIN verification (Default: `1234`) prevents accidental or coerced evidence deletion; entering duress PIN `9999` fakes deletion while silently escalating emergency alerts.

### 4. Forensic Safety Audit Report Generator
- Automatically compiles comprehensive, timestamped incident summaries.
- One-tap sharing via **WhatsApp**, Android system share sheet (Email, Drive, Files), or clipboard copy.

### 5. Persistent User Profile & Emergency Contacts
- Backed by persistent storage (`UserProfileManager` & `SharedPreferences`), keeping user identity, primary emergency contacts, and demographic preferences intact across app restarts and device reboots.

### 6. Multi-Tiered Risk State Machine
- **🟢 GREEN**: Normal background monitoring.
- **🟡 YELLOW**: Pre-Alert / Silent check-in timer.
- **🟠 ORANGE**: High threat active, automated evidence recording.
- **🔴 RED**: Full emergency escalation, loud alarm siren, SMS + WhatsApp dispatch, and live location streaming.

---

## 🛠️ Tech Stack & Architecture

- **Language**: Kotlin 2.2.10
- **UI Toolkit**: Jetpack Compose with Material 3 (Material You dark safety theme)
- **Architecture**: Single Activity (`MainActivity`), Compose Navigation, Layered MVVM
- **Local Storage**: Room Database & SharedPreferences
- **Hardware Integrations**: FusedLocationProviderClient, SensorManager (Hard Shake Detection), AudioRecord & MediaRecorder, CameraX, Android Keystore (AES-GCM encryption & SHA-256 hashing)
- **Minimum SDK**: Android API 24 (Android 7.0+)
- **Target SDK**: Android API 37 (Android 16)

---

## 🚀 Building & Running

1. Open the `mobile-app` directory in Android Studio.
2. Build the project using Gradle:
   ```powershell
   ./gradlew assembleDebug
   ```
3. Install the APK to a connected device:
   ```powershell
   adb install -r app/build/outputs/apk/debug/app-debug.apk
   ```
