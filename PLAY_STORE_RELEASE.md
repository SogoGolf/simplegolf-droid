# Google Play Release Workflow

This file is the source of truth for Android store uploads.

## Default Rule For Future Agents

When the user says "send Android to the store for testing" and does not give extra detail:

1. Work from `develop` unless the user explicitly says otherwise.
2. Make sure the intended release commit is already committed.
3. Bump the build number once.
4. Commit and push the build bump if the user asked for that.
5. Build a signed release AAB.
6. Upload to Google Play Internal Testing.

For this repo, "store testing" means the Play `internal` track unless the user explicitly asks for:

- `alpha` for Closed Testing
- `beta` for Open Testing

Do not default to `beta`.

## App-Specific Values

- Repo root: `/Users/angusjohnston/src-android/simplegolf-droid`
- Package name: `com.sogo.golf.msl`
- Version source of truth: `version.properties`
- Default testing track: `internal`

## Pre-Flight Checks

Run these from the repo root:

```bash
git branch --show-current
git status --short
cat version.properties
printf '%s\n' "$PLAY_STORE_CREDENTIALS_FILE"
printf '%s\n' "$SIMPLEGOLF_ANDROID_KEYSTORE_PATH"
printf '%s\n' "$SIMPLEGOLF_ANDROID_KEY_ALIAS"
```

Expected state before release:

- current branch is usually `develop`
- no unintended working tree changes
- `PLAY_STORE_CREDENTIALS_FILE` points to a valid Google Play service-account JSON key
- signing is configured for `bundleRelease`

## Build Number Bump

The app version lives in `version.properties`:

```properties
VERSION_MAJOR=<major>
VERSION_MINOR=<minor>
VERSION_PATCH=<patch>
VERSION_BUILD=<build>
```

Useful Gradle tasks:

```bash
./gradlew incrementVersionBuild
./gradlew incrementVersionPatch
./gradlew incrementVersionMinor
./gradlew incrementVersionMajor
```

For a normal testing redeploy where only the build number should change:

```bash
./gradlew incrementVersionBuild
cat version.properties
```

If the user asked for the build bump to be committed before upload:

```bash
git add version.properties
git commit -m "Bump Android build to <build>"
git push github develop
```

Important:

- `./scripts/release_to_play_store.sh` will bump the version or build for you.
- If the build number was already bumped and committed earlier in the conversation, do not restart that script from the top or it will bump again.

## Standard Interactive Release Script

Use this when the user wants the script to handle versioning and upload in one flow:

```bash
./scripts/release_to_play_store.sh
```

When prompted:

1. Choose how to update the version.
2. Enter release notes as plain text.
3. Confirm the release.
4. Choose the track:
   - `1` = Internal Testing
   - `2` = Closed Testing
   - `3` = Open Testing

What the script does:

1. Updates `version.properties`
2. Builds a signed release AAB
3. Uploads the AAB to Google Play
4. Publishes the build to the chosen track
5. Uploads the release notes as `en-US`

## Manual Upload When The Build Number Is Already Bumped

Use this path when versioning was already handled earlier and you only want to build and upload the existing version.

### 1. Build The Signed AAB

```bash
./gradlew clean bundleRelease --stacktrace
```

### 2. Find The AAB

```bash
AAB_PATH=$(find app/build/outputs/bundle/release -name "*.aab" -type f | head -1)
printf '%s\n' "$AAB_PATH"
```

### 3. Create Release Notes

Google Play receives release notes as plain text in `en-US`. Example:

```bash
cat >/tmp/simplegolf-play-release-notes.txt <<'EOF'
build refresh for testing
EOF
```

### 4. Prepare Python Dependencies

```bash
if [ ! -d ".venv" ]; then
  python3 -m venv .venv
fi

source .venv/bin/activate
python3 -c "import googleapiclient, google.auth" 2>/dev/null || python3 -m pip install google-api-python-client google-auth
```

### 5. Upload To Google Play

For Internal Testing:

```bash
export TRACK_ENV=internal
```

For Closed Testing:

```bash
export TRACK_ENV=alpha
```

For Open Testing:

```bash
export TRACK_ENV=beta
```

Then run:

```bash
export AAB_PATH_ENV="$AAB_PATH"
export RELEASE_NOTES_FILE_PATH=/tmp/simplegolf-play-release-notes.txt

python3 - <<'PYTHON_EOF'
import os
from googleapiclient.discovery import build
from google.oauth2 import service_account
from googleapiclient.http import MediaFileUpload

SCOPES = ['https://www.googleapis.com/auth/androidpublisher']
PACKAGE_NAME = 'com.sogo.golf.msl'
AAB_FILE = os.environ['AAB_PATH_ENV']
TRACK = os.environ['TRACK_ENV']

with open(os.environ['RELEASE_NOTES_FILE_PATH'], 'r') as f:
    release_notes = f.read().strip()

credentials = service_account.Credentials.from_service_account_file(
    os.environ['PLAY_STORE_CREDENTIALS_FILE'],
    scopes=SCOPES,
)

service = build('androidpublisher', 'v3', credentials=credentials)
edit = service.edits().insert(body={}, packageName=PACKAGE_NAME).execute()
edit_id = edit['id']

upload_result = service.edits().bundles().upload(
    editId=edit_id,
    packageName=PACKAGE_NAME,
    media_body=MediaFileUpload(AAB_FILE, mimetype='application/octet-stream'),
).execute()

version_code = upload_result['versionCode']

service.edits().tracks().update(
    editId=edit_id,
    track=TRACK,
    packageName=PACKAGE_NAME,
    body={
        'track': TRACK,
        'releases': [{
            'status': 'completed',
            'versionCodes': [version_code],
            'releaseNotes': [{
                'language': 'en-US',
                'text': release_notes,
            }],
        }],
    },
).execute()

service.edits().commit(editId=edit_id, packageName=PACKAGE_NAME).execute()
print(f'Uploaded versionCode {version_code} to track {TRACK}')
PYTHON_EOF
```

## After Upload

- The build usually appears in Play Console within a few minutes.
- `internal` testers usually see the update quickly.
- Confirm the uploaded version code, track, and release notes in Play Console after the API upload completes.
