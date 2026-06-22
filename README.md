# Massage Application

An Android SMS and messaging manager built with Java. The app supports sending and receiving messages, contact browsing, search, blocking, archiving, scheduling, language selection, and a dark/light theme toggle.

## Features

- SMS inbox and conversation view
- Send messages from contacts or phone numbers
- Read contacts and search contacts
- Search messages
- Block contacts and blocked-message management
- Archive conversations
- Schedule SMS delivery
- Language selection screen with multiple languages
- Light and dark theme support
- Custom splash screen and drawer navigation

## Permissions Used

- `READ_SMS`
- `SEND_SMS`
- `RECEIVE_SMS`
- `READ_CONTACTS`
- `CALL_PHONE`
- `POST_NOTIFICATIONS`
- `INTERNET`
- `ACCESS_NETWORK_STATE`

## Project Structure

- `app/src/main/java/com/example/massageapplication/` - activities, receivers, services, and adapters
- `app/src/main/res/layout/` - XML layouts for all screens
- `app/src/main/res/drawable/` - icons, backgrounds, and UI assets
- `app/src/main/res/values/` - strings, colors, themes, and styles

## Screenshots

Put the screenshot files in a `screenshots/` folder at the project root and keep the filenames below. Each image is displayed at `200x400`.

### Splash Screen

<img src="screenshots/splash-screen.png" alt="Splash Screen" width="200" height="400" />

### Language Selection

<img src="screenshots/language-selection.png" alt="Language Selection Screen" width="200" height="400" />

### Main Messages Screen

<img src="screenshots/main-screen.png" alt="Main Messages Screen" width="200" height="400" />

### Message Chat Screen

<img src="screenshots/message-screen.png" alt="Message Chat Screen" width="200" height="400" />

### Contacts Screen

<img src="screenshots/contacts-screen.png" alt="Contacts Screen" width="200" height="400" />

### Contact Details Screen

<img src="screenshots/contact-details-screen.png" alt="Contact Details Screen" width="200" height="400" />

### Search Screen

<img src="screenshots/search-screen.png" alt="Search Screen" width="200" height="400" />

### Schedule Screen

<img src="screenshots/schedule-screen.png" alt="Schedule Screen" width="200" height="400" />

### Block Screen

<img src="screenshots/block-screen.png" alt="Block Screen" width="200" height="400" />

### Block Messages Screen

<img src="screenshots/block-messages-screen.png" alt="Block Messages Screen" width="200" height="400" />

### Archive Screen

<img src="screenshots/archive-screen.png" alt="Archive Screen" width="200" height="400" />

## Setup

1. Open the project in Android Studio.
2. Sync Gradle.
3. Run the app on a device or emulator.
4. Grant the SMS and contacts permissions when prompted.
5. If needed, set the app as the default SMS app to enable full message access.

## Notes

- The project name in the app is `Massage Application`.
- The app uses Google Mobile Ads test IDs in the language selection screen.
- Some screens depend on runtime permissions and default-SMS behavior.

