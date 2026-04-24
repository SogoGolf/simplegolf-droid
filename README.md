# SimpleGolf Android App

## 🚀 DEPLOYING TO GOOGLE PLAY

For the exact release workflow, including:

- the default `internal` testing deploy
- the interactive script path
- the manual upload path when the build number was already bumped
- the exact credentials and signing checks
- the exact Google Play API upload command
- the release-notes format used for `en-US`

use [PLAY_STORE_RELEASE.md](/Users/angusjohnston/src-android/simplegolf-droid/PLAY_STORE_RELEASE.md).

### Quick Deploy (Recommended)
When the user asks to send Android to the store for testing, treat [PLAY_STORE_RELEASE.md](/Users/angusjohnston/src-android/simplegolf-droid/PLAY_STORE_RELEASE.md) as the source of truth. The default testing target is Google Play `internal` unless the user explicitly asks for another track.

To deploy with the interactive script:

```bash
./scripts/release_to_play_store.sh
```

This script will:
1. Prompt you for the new version number
2. Update the version in `version.properties`
3. Build a release AAB (Android App Bundle)
4. Sign the AAB with the release keystore
5. Upload to the Google Play track you choose in the script
6. Upload your release notes as `en-US`
7. Handle the configuration automatically

### Prerequisites
- Ensure you have the release keystore configured
- Make sure you have Google Play Console API access set up
- Export `PLAY_STORE_CREDENTIALS_FILE` to the service-account JSON key file path

### What Happens After Deployment
- The app usually appears in Play Console within a few minutes
- `internal` builds are usually visible to testers faster than `beta`
- You can monitor the rollout status in Google Play Console

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
