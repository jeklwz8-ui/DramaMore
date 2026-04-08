# DramaMore Project Index

## Overview
DramaMore is an innovative platform designed to provide users with an extensive catalog of dramatic works, including plays, scripts, and performances.

## Table of Contents
- [Installation](#installation)
- [Usage](#usage)
- [Features](#features)
- [Contributing](#contributing)
- [License](#license)

## Installation
To set up the project, follow these steps:
1. Clone the repository:
   ```bash
   git clone https://github.com/jeklwz8-ui/DramaMore.git
   ```
2. Navigate to the project directory:
   ```bash
   cd DramaMore
   ```
3. Install the necessary dependencies:
   ```bash
   npm install
   ```

## Usage
After installation, you can run the application with:
```bash
npm start
```

## Features
- Comprehensive database of dramatic works
- User-friendly interface
- Search and filter functionality
- Contribution guidelines for authors and actors

## Update Log
- 2026-04-08: Recommend page custom progress bar visibility is now deferred until valid playback progress is received (`duration > 0`), so the bar appears only after video content is actually rendered.
- 2026-04-08: Validation log: executed `./gradlew.bat :app:assembleDebug` successfully after deferred progress-bar visibility optimization.
- 2026-04-08: Recommend page custom progress bar (`sb_recommend_progress`) was moved slightly downward (`layout_marginBottom` adjusted to `-2dp`) for better visual alignment.
- 2026-04-08: Validation log: executed `./gradlew.bat :app:assembleDebug` successfully after the progress-bar position tweak.
- 2026-04-08: Recommend page replaced SDK progress display with a custom page-level progress bar (`sb_recommend_progress`) at the bottom of the video area, using `onProgressChange(...)` callbacks to update UI and seek.
- 2026-04-08: Validation log: executed `./gradlew.bat :app:assembleDebug` successfully after custom recommend progress-bar implementation.
- 2026-04-08: Recommend page added a custom bottom episode bar below the player (`fragment_recommend.xml`), and episode selection now uses local `IndexChooseDialog` + `ShortPlayFragment.startPlayIndex()` instead of the SDK overlay choose bar.
- 2026-04-08: Validation log: executed `./gradlew.bat :app:assembleDebug` successfully after custom recommend choose-bar implementation.
- 2026-04-08: Fix recommend page episode bar visibility regression. Restored `ll_bottom_actions` to overlay rendering path and kept bottom spacer logic for player area separation.
- 2026-04-08: Validation log: re-ran `./gradlew.bat :app:assembleDebug`, build passed successfully after visibility fix.
- 2026-04-07: Mine page added two new entries between language settings and privacy protocol: "评价我们" and "分享我们".
- 2026-04-07: "评价我们" opens Google Play rating page (`market://details`) with web fallback.
- 2026-04-07: "分享我们" opens Android share panel with app link text.
- 2026-04-07: Standardized icons for "评价我们", "分享我们", and "隐私协议" in Mine page.

## Contributing
We welcome contributions! Please read our [CONTRIBUTING.md](CONTRIBUTING.md) for guidelines on how to contribute to this project.

## License
This project is licensed under the MIT License. See the [LICENSE](LICENSE) file for more details.
