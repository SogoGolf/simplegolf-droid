#!/bin/bash

# Exit on any error
set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color

# Release notes file path
RELEASE_NOTES_FILE="app/src/main/java/com/sogo/golf/msl/docs/release-notes.md"

echo -e "${GREEN}🚀 Android Release Process${NC}"
echo "======================================="

# Show current version
echo -e "\n${BLUE}Current version:${NC}"
cat version.properties
echo ""

# Step 1: Ask for version update type
echo -e "${YELLOW}How would you like to update the version?${NC}"
echo "1) Increment build number only (timestamp)"
echo "2) Set new release version (e.g., 3.1.0)"
echo -n "Enter choice [1-2]: "
read -r VERSION_CHOICE

case $VERSION_CHOICE in
    1)
        echo -e "\n${YELLOW}Incrementing build number...${NC}"
        ./gradlew setTimestampBuild
        echo -e "${GREEN}✓ Build number incremented${NC}"
        ;;
    2)
        echo -n -e "\n${YELLOW}Enter new version (e.g., 3.1.0): ${NC}"
        read -r NEW_VERSION
        
        # Validate version format
        if [[ ! "$NEW_VERSION" =~ ^[0-9]+\.[0-9]+(\.[0-9]+)?$ ]]; then
            echo -e "${RED}❌ Invalid version format. Please use format like 3.1 or 3.1.0${NC}"
            exit 1
        fi
        
        # Parse version components
        IFS='.' read -ra VERSION_PARTS <<< "$NEW_VERSION"
        MAJOR="${VERSION_PARTS[0]}"
        MINOR="${VERSION_PARTS[1]}"
        PATCH="${VERSION_PARTS[2]:-0}"  # Default to 0 if not provided
        
        echo -e "\n${YELLOW}Setting version to $MAJOR.$MINOR.$PATCH...${NC}"
        
        # Update version using gradle properties
        ./gradlew \
            -PversionMajor="$MAJOR" \
            -PversionMinor="$MINOR" \
            -PversionPatch="$PATCH" \
            setTimestampBuild
        
        echo -e "${GREEN}✓ Version set to $MAJOR.$MINOR.$PATCH with new build number${NC}"
        ;;
    *)
        echo -e "${RED}❌ Invalid choice. Exiting.${NC}"
        exit 1
        ;;
esac

# Display updated version
echo -e "\n${BLUE}Updated version:${NC}"
cat version.properties
echo ""

# Step 2: Release Notes
echo -e "\n${CYAN}📝 Release Notes${NC}"
echo "======================================="

# Check if release notes file exists
if [ ! -f "$RELEASE_NOTES_FILE" ]; then
    echo -e "${YELLOW}Creating release notes file...${NC}"
    mkdir -p release-notes
    cat > "$RELEASE_NOTES_FILE" << 'EOF'
# Release Notes

## Latest Release

### What's New
- 

### Bug Fixes
- 

### Known Issues
- None

---

## Previous Releases
EOF
    echo -e "${GREEN}✓ Created release notes template at $RELEASE_NOTES_FILE${NC}"
fi

# Show current release notes
echo -e "\n${YELLOW}Current release notes (Latest Release section):${NC}"
echo "---"
# Extract just the Latest Release section
awk '/^## Latest Release/,/^---/' "$RELEASE_NOTES_FILE" | sed '$d'
echo "---"

# Ask if user wants to edit release notes
echo -n -e "\n${YELLOW}Do you want to edit the release notes now? [y/N]: ${NC}"
read -r EDIT_NOTES

if [[ "$EDIT_NOTES" =~ ^[Yy]$ ]]; then
    # Check for available editors
    if command -v code &> /dev/null; then
        echo -e "${BLUE}Opening release notes in VS Code...${NC}"
        code "$RELEASE_NOTES_FILE"
    elif command -v nano &> /dev/null; then
        nano "$RELEASE_NOTES_FILE"
    elif command -v vim &> /dev/null; then
        vim "$RELEASE_NOTES_FILE"
    else
        echo -e "${YELLOW}Please edit $RELEASE_NOTES_FILE manually and press Enter when done${NC}"
    fi
    
    echo -n -e "\n${YELLOW}Press Enter when you've finished editing the release notes...${NC}"
    read -r
    
    # Show updated release notes
    echo -e "\n${GREEN}Updated release notes:${NC}"
    echo "---"
    awk '/^## Latest Release/,/^---/' "$RELEASE_NOTES_FILE" | sed '$d'
    echo "---"
fi

# Extract release notes for upload (removing markdown headers and empty lines)
RELEASE_NOTES_TEXT=$(awk '/^## Latest Release/,/^---/' "$RELEASE_NOTES_FILE" | \
    grep -v '^##' | \
    grep -v '^---' | \
    sed '/^$/d' | \
    sed 's/^### //' | \
    sed 's/^- /• /')

if [ -z "$RELEASE_NOTES_TEXT" ]; then
    RELEASE_NOTES_TEXT="Bug fixes and performance improvements"
fi

# Step 3: Confirmation
echo -n -e "\n${YELLOW}Do you want to continue with the release? [y/N]: ${NC}"
read -r CONFIRM
if [[ ! "$CONFIRM" =~ ^[Yy]$ ]]; then
    echo -e "${RED}Release cancelled.${NC}"
    exit 0
fi

# Step 4: Clean previous builds
echo -e "\n${YELLOW}Cleaning previous builds...${NC}"
./gradlew clean
echo -e "${GREEN}✓ Build cleaned${NC}"

# Step 5: Create signed release bundle
echo -e "\n${YELLOW}Building signed release bundle...${NC}"
./gradlew bundleRelease --stacktrace
echo -e "${GREEN}✓ Signed release bundle created${NC}"

# Step 6: Find the generated AAB file
AAB_PATH=$(find app/build/outputs/bundle -name "*.aab" -type f | head -1)
if [ -z "$AAB_PATH" ]; then
    echo -e "${RED}❌ Error: Could not find AAB file${NC}"
    exit 1
fi
echo -e "${GREEN}✓ Found AAB at: $AAB_PATH${NC}"

# Step 7: Upload to Google Play Console
echo -e "\n${YELLOW}Uploading to Google Play Store (Open Testing)...${NC}"

# Check if we have the necessary Play Store credentials
if [ -z "$PLAY_STORE_CREDENTIALS_FILE" ]; then
    echo -e "${RED}❌ Error: PLAY_STORE_CREDENTIALS_FILE environment variable not set${NC}"
    echo "Please set the path to your Google Play API JSON key file"
    echo "Example: export PLAY_STORE_CREDENTIALS_FILE=/path/to/credentials.json"
    exit 1
fi

# Check if credentials file exists
if [ ! -f "$PLAY_STORE_CREDENTIALS_FILE" ]; then
    echo -e "${RED}❌ Error: Credentials file not found at: $PLAY_STORE_CREDENTIALS_FILE${NC}"
    exit 1
fi

# Check if Python and required packages are available
if ! command -v python3 &> /dev/null; then
    echo -e "${RED}❌ Python 3 is required but not installed${NC}"
    echo "Please install Python 3 to continue"
    exit 1
fi

# Check for required Python packages
python3 -c "import googleapiclient, google.auth" 2>/dev/null || {
    echo -e "${YELLOW}Installing required Python packages...${NC}"
    python3 -m pip install --user google-api-python-client google-auth
}

echo "Using Google Play Developer API to upload..."
echo -e "${BLUE}Release notes:${NC}"
echo "$RELEASE_NOTES_TEXT"
echo ""

# Upload to Google Play with release notes
python3 - <<EOF
import sys
import os
import json
from googleapiclient.discovery import build
from google.oauth2 import service_account
from googleapiclient.http import MediaFileUpload

SCOPES = ['https://www.googleapis.com/auth/androidpublisher']
PACKAGE_NAME = 'com.sogo.golf.msl'
AAB_FILE = '$AAB_PATH'
TRACK = 'beta'  # Open testing track
RELEASE_NOTES = '''$RELEASE_NOTES_TEXT'''

try:
    print(f"Loading credentials from: {os.environ['PLAY_STORE_CREDENTIALS_FILE']}")
    
    # Load credentials
    credentials = service_account.Credentials.from_service_account_file(
        os.environ['PLAY_STORE_CREDENTIALS_FILE'], scopes=SCOPES)
    
    print("Building Android Publisher service...")
    
    # Build the service
    service = build('androidpublisher', 'v3', credentials=credentials)
    
    print(f"Creating edit for package: {PACKAGE_NAME}")
    
    # Create a new edit
    edit_request = service.edits().insert(body={}, packageName=PACKAGE_NAME)
    result = edit_request.execute()
    edit_id = result['id']
    print(f"Edit created with ID: {edit_id}")
    
    print(f"Uploading AAB file: {AAB_FILE}")
    
    # Upload the AAB
    upload_request = service.edits().bundles().upload(
        editId=edit_id,
        packageName=PACKAGE_NAME,
        media_body=MediaFileUpload(AAB_FILE, mimetype='application/octet-stream')
    )
    upload_result = upload_request.execute()
    version_code = upload_result['versionCode']
    print(f"✓ AAB uploaded successfully. Version code: {version_code}")
    
    print(f"Assigning to {TRACK} track with release notes...")
    
    # Prepare release notes in multiple languages (default to en-US)
    release_notes_list = [
        {
            'language': 'en-US',
            'text': RELEASE_NOTES
        }
    ]
    
    # Assign to open testing track with release notes
    track_request = service.edits().tracks().update(
        editId=edit_id,
        track=TRACK,
        packageName=PACKAGE_NAME,
        body={
            'track': TRACK,
            'releases': [{
                'status': 'completed',
                'versionCodes': [version_code],
                'releaseNotes': release_notes_list
            }]
        }
    )
    track_result = track_request.execute()
    print(f"✓ Assigned to {TRACK} track with release notes")
    
    print("Committing changes...")
    
    # Commit the edit
    commit_request = service.edits().commit(
        editId=edit_id,
        packageName=PACKAGE_NAME
    )
    commit_request.execute()
    print("✓ Changes committed successfully")
    
    # Success - exit with 0
    sys.exit(0)
    
except FileNotFoundError as e:
    print(f"❌ Error: Credentials file not found: {e}")
    sys.exit(1)
except Exception as e:
    print(f"❌ Error uploading to Play Store: {e}")
    import traceback
    traceback.print_exc()
    sys.exit(1)
EOF

if [ $? -eq 0 ]; then
    echo -e "\n${GREEN}✅ Successfully uploaded to Google Play Store Open Testing!${NC}"
    
    # Archive current release notes
    VERSION=$(grep VERSION_MAJOR version.properties | cut -d'=' -f2).$(grep VERSION_MINOR version.properties | cut -d'=' -f2).$(grep VERSION_PATCH version.properties | cut -d'=' -f2)
    
    # Ask if user wants to archive the release notes
    echo -n -e "\n${YELLOW}Archive current release notes for version $VERSION? [Y/n]: ${NC}"
    read -r ARCHIVE_NOTES
    
    if [[ ! "$ARCHIVE_NOTES" =~ ^[Nn]$ ]]; then
        # Move current release notes to previous releases section
        echo -e "${YELLOW}Archiving release notes...${NC}"
        
        # Create a temporary file with the new structure
        TEMP_FILE=$(mktemp)
        
        # Write the header and empty latest release section
        cat > "$TEMP_FILE" << 'EOF'
# Release Notes

## Latest Release

### What's New
- 

### Bug Fixes
- 

### Known Issues
- None

---

## Previous Releases

EOF
        
        # Add current release to previous releases
        echo "### Version $VERSION" >> "$TEMP_FILE"
        awk '/^## Latest Release/,/^---/' "$RELEASE_NOTES_FILE" | \
            grep -v '^## Latest Release' | \
            grep -v '^---' | \
            sed '/^$/d' >> "$TEMP_FILE"
        echo "" >> "$TEMP_FILE"
        
        # Append existing previous releases
        awk '/^## Previous Releases/,EOF' "$RELEASE_NOTES_FILE" | \
            grep -v '^## Previous Releases' >> "$TEMP_FILE"
        
        # Replace the original file
        mv "$TEMP_FILE" "$RELEASE_NOTES_FILE"
        
        echo -e "${GREEN}✓ Release notes archived${NC}"
    fi
    
    # Show final version info
    echo -e "\n${BLUE}Release Summary:${NC}"
    echo "======================================="
    grep -E "VERSION_MAJOR|VERSION_MINOR|VERSION_PATCH|VERSION_BUILD" version.properties
    echo "--------------------------------------"
    echo "Release Notes:"
    echo "$RELEASE_NOTES_TEXT"
    echo "======================================="
else
    echo -e "${RED}❌ Failed to upload to Play Store${NC}"
    exit 1
fi

echo -e "\n${GREEN}🎉 Release process completed successfully!${NC}"
echo "Next steps:"
echo "1. Check Google Play Console for the new release in Open Testing"
echo "2. Test the release with your testing group"
echo "3. Promote to production when ready"