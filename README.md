# SimpleGolf Android App

## 🚀 DEPLOYING TO GOOGLE PLAY OPEN TESTING

### Quick Deploy (Recommended)
To deploy a new update to the Open Testing channel on Google Play:

```bash
./scripts/release_to_play_store.sh
```

This script will:
1. Prompt you for the new version number
2. Update the version in the app's build.gradle
3. Build a release AAB (Android App Bundle)
4. Sign the AAB with the release keystore
5. Upload to Google Play Console's Open Testing track
6. Handle all the configuration automatically

### Prerequisites
- Ensure you have the release keystore configured
- Make sure you have Google Play Console API access set up
- Have your service account JSON key file in place

### What Happens After Deployment
- The app will be available in Open Testing within a few minutes
- Testers enrolled in Open Testing will see the update within 2-4 hours
- You can monitor the rollout status in the Google Play Console

---

## Project Overview
SimpleGolf is an Android application for golf scoring and competition management, integrated with MyScorecard Live (MSL) and SOGO Golf platforms.

## Development Setup

### Requirements
- Android Studio (latest stable version)
- JDK 11 or higher
- Android SDK with minimum API level 24

### Building the Project
1. Clone the repository
2. Open the project in Android Studio
3. Sync Gradle files
4. Run the app on an emulator or physical device

### Build Variants
- **Debug**: For development and testing
- **Release**: For production deployment (requires signing keys)

## Architecture
The app follows MVVM architecture with:
- Jetpack Compose for UI
- Hilt for dependency injection
- Coroutines and Flow for async operations
- Room for local database
- Retrofit for network calls

## Key Features
- MSL integration for live scoring
- SOGO Golf leaderboards and token management
- Competition round tracking
- Playing partner management
- Offline support with data synchronization

## Testing
Run tests using:
```bash
./gradlew test
```

## Support
For issues or questions, please contact the development team.