# 🔧 CI/CD Pipeline Setup (Azure DevOps + Firebase App Distribution)

This document outlines the full CI/CD process for the **BucketList Android App** using **Azure DevOps** and **Firebase App Distribution**.

## 📦 Pipeline Overview
This CI/CD setup includes:
- ✅ Gradle build automation
- ✅ Unit test execution (JUnit + MockK)
- ✅ Firebase App Distribution delivery

## 📁 Folder Structure
```
app/
├── src/
│   ├── main/
│   ├── test/          → Unit tests (ViewModel, Repository, Logic)
│   └── androidTest/   → UI tests (Espresso)
azure-pipelines.yml    → Pipeline definition file
```

## ⚙ Prerequisites

### 1. Firebase Project Setup
- Create a Firebase project: https://console.firebase.google.com
- Add your Android app (`applicationId = "edu.vt.cs5254.bucketlist"`)
- Enable **App Distribution** → Click **"Get Started"**
- Download Firebase **Admin SDK service account JSON key**

### 2. Upload Firebase Credentials to Azure DevOps
- Go to **Azure DevOps → Pipelines → Library → Secure Files**
- Upload the service account key (e.g., `firebase-adminsdk.json`)
- Authorize it for use in pipelines

### 3. Add Required Pipeline Variables
| Variable Name       | Example Value                           | Secret |
|--------------------|------------------------------------------|--------|
| `FIREBASE_APP_ID`  | 1:1234567890:android:abc1234567890abc     | ❌ No  |
| `FIREBASE_TOKEN`   | (generated via `firebase login:ci`)      | ✅ Yes |
| `TESTER_EMAILS`    | you@example.com,tester@example.com       | ❌ No  |

## 🔁 Pipeline Steps Summary

1. Trigger on `main` branch push
2. Install Java 17 and Android SDK
3. Set required environment variables
4. Build APK using Gradle `assembleDebug`
5. Run Unit Tests using Gradle `testDebugUnitTest`
6. Copy APK to artifacts directory
7. Distribute APK to Firebase App Distribution

## 💡 Firebase CD Notes
- Make sure `GOOGLE_APPLICATION_CREDENTIALS` is exported to point to the service account file path.
- `--token` is deprecated but still temporarily supported.
- Firebase CLI uses `.apk` from `app/build/outputs/apk/debug/app-debug.apk`.

## 📩 For Testers
Testers will receive an email invite from Firebase.
They must:
- Log in with the email used in `TESTER_EMAILS`
- Click the invite link to download & install the APK

---
## 🔗 References
- [Firebase App Distribution](https://firebase.google.com/docs/app-distribution)
- [Azure Pipelines Docs](https://learn.microsoft.com/en-us/azure/devops/pipelines/)