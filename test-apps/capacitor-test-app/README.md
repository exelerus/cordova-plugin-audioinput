# Capacitor Test App for audioinput Plugin

This is a minimal Capacitor app for testing the cordova-plugin-audioinput on real devices.

## Prerequisites

- Node.js and npm installed
- For Android: Android Studio and Android SDK
- For iOS: Xcode (macOS only)

## Setup

1. **Install dependencies:**
   ```bash
   npm install
   ```

2. **Add platform(s):**
   ```bash
   # For Android
   npx cap add android

   # For iOS (macOS only)
   npx cap add ios
   ```

3. **Install the plugin:**

   **Option A: From GitHub (published plugin)**
   ```bash
   npm install https://github.com/exelerus/cordova-plugin-audioinput.git#BRANCH_NAME
   npx cap sync
   ```

   **Option B: From Local Plugin (for testing local changes)**
   ```bash
   # First, build the plugin from the plugin root directory
   cd ../..
   npm install
   npm run build

   # Then install it in the test app
   cd test-apps/capacitor-test-app
   npm install ../..
   ```

   **Note:** To test a different branch, replace BRANCH_NAME with your actual branch name.

4. **Build the web assets:**
   ```bash
   npm run build
   ```

5. **Sync with native platforms:**
   ```bash
   npx cap sync
   ```

## Build and Run

### Android

1. **Connect your Android device** via USB with USB debugging enabled

2. **Run the app:**
   ```bash
   npm run android
   ```

   Or open in Android Studio:
   ```bash
   npx cap open android
   ```

   Then build and run from Android Studio.

### iOS (macOS only)

1. **Connect your iOS device** via USB or use simulator

2. **Run the app:**
   ```bash
   npm run ios
   ```

   Or open in Xcode:
   ```bash
   npx cap open ios
   ```

   Then build and run from Xcode.

## Development Workflow

For faster development with live reload:

1. **Start the dev server:**
   ```bash
   npm run dev
   ```

2. **In another terminal, sync and run:**
   ```bash
   npx cap sync
   npx cap run android  # or ios
   ```

Note: The plugin functionality requires running on a real device or emulator - it won't work in the browser.

## Testing the Plugin

The test app provides buttons to:

1. **Request Permission** - Request microphone access
2. **Check Permission** - Verify current permission status
3. **Start Capture** - Begin audio capture
4. **Stop Capture** - Stop audio capture
5. **Clear Log** - Clear the status log

The status area will show:
- Platform information
- Plugin loading status
- Permission status
- Audio capture events
- Any errors that occur

## Updating the Plugin

To test changes to the plugin:

1. **Remove the old plugin:**
   ```bash
   npm uninstall cordova-plugin-audioinput
   ```

2. **Re-add from your branch:**
   ```bash
   npm install https://github.com/exelerus/cordova-plugin-audioinput.git#YOUR_BRANCH_NAME
   ```

3. **Sync and rebuild:**
   ```bash
   npm run build
   npx cap sync
   npx cap run android  # or ios
   ```

## Testing Local Plugin Changes

If you want to test local changes to the plugin without pushing to GitHub:

1. **Build the plugin first:**
   ```bash
   cd /path/to/cordova-plugin-audioinput
   npm install
   npm run build
   ```

2. **Create a tarball (optional) or install directly:**
   ```bash
   # Option A: Install directly from parent directory
   cd test-apps/capacitor-test-app
   npm uninstall cordova-plugin-audioinput
   npm install ../..

   # Option B: Create and install tarball
   cd /path/to/cordova-plugin-audioinput
   npm pack
   ```

3. **In the test app, install the local tarball (if using Option B):**
   ```bash
   npm install /path/to/cordova-plugin-audioinput-1.0.3.tgz
   npx cap sync
   ```

## Troubleshooting

### Android Issues

- **Plugin not loading:**
  - Check `android/app/src/main/assets/public/` for web files
  - Check `android/capacitor.settings.gradle` for the plugin
  - Try: `npx cap sync android --force`

- **Permission denied:** Grant microphone permission in app settings

- **Build errors:**
  - Clean build: `cd android && ./gradlew clean`
  - Invalidate caches in Android Studio

### iOS Issues

- **Signing errors:** Configure your development team in Xcode
- **Permission errors:** The plugin should automatically add the required Info.plist entry
- **Plugin not loading:**
  - Check the Podfile in the ios/App directory
  - Try: `cd ios/App && pod install`

### General Issues

- **Plugin not found in app:**
  ```bash
  npm run build
  npx cap sync
  ```

- **Stale cache:**
  ```bash
  rm -rf dist/ node_modules/
  npm install
  npm run build
  npx cap sync
  ```

## Project Structure

```
capacitor-test-app/
├── index.html          # Main HTML file
├── main.js            # Application logic
├── package.json       # Dependencies
├── capacitor.config.json  # Capacitor configuration
├── vite.config.js     # Vite bundler config
├── dist/              # Built web assets (generated)
├── android/           # Android native project (generated)
└── ios/              # iOS native project (generated)
```

## Additional Resources

- [Capacitor Documentation](https://capacitorjs.com/docs)
- [Capacitor Plugin Development](https://capacitorjs.com/docs/plugins)
- [Cordova to Capacitor Migration](https://capacitorjs.com/docs/cordova/migrating-from-cordova-to-capacitor)
