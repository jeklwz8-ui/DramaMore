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
- 2026-04-08: Unified the top hero scrim and the banner scrim to a single shared drawable (`bg_home_shared_scrim`) so both layers use the same background and no longer produce mismatched corner blocks.
- 2026-04-08: Validation log: executed `./gradlew.bat :app:assembleDebug` successfully after shared-scrim unification.
- 2026-04-08: Fixed small black corner blocks behind home carousel rounded top corners by replacing full-screen dark overlay with a top-only scrim (`bg_home_top_scrim`) and unifying bottom scrim to 4-corner radius.
- 2026-04-08: Validation log: executed `./gradlew.bat :app:assembleDebug` successfully after carousel corner black-block cleanup.
- 2026-04-08: Home carousel now enforces consistent 4-corner rounding via `ViewPager2Parent` path-based clipping; `ViewPager2` and its internal RecyclerView background are explicitly transparent to prevent black corner artifacts behind rounded edges.
- 2026-04-08: Validation log: executed `./gradlew.bat :app:assembleDebug` successfully after 4-corner carousel rounding and black-corner artifact fix.
- 2026-04-08: Fixed black corner artifacts above home carousel by removing inner image-level rounded clipping in `BannerAdapter`; carousel now uses unified container-level rounded clipping only.
- 2026-04-08: Validation log: executed `./gradlew.bat :app:assembleDebug` successfully after black-corner artifact fix on home carousel.
- 2026-04-08: Home banner `ViewPager2Parent` block now applies unified rounded clipping (`bg_banner_rounded` + `clipToOutline`), ensuring inner `ViewPager2` content and outer card corners stay visually consistent.
- 2026-04-08: Validation log: executed `./gradlew.bat :app:assembleDebug` successfully after banner unified-corner clipping adjustment.
- 2026-04-08: Fine-tuned home hero again: reduced search-to-banner gap, increased inner banner height, strengthened blur, and added pre-Android-12 fallback blur-like processing for carousel-backed background images.
- 2026-04-08: Softened bottom title-area backdrop further with lighter gradient/overlay opacity for a more transparent and less layered look.
- 2026-04-08: Validation log: executed `./gradlew.bat :app:assembleDebug` successfully after latest spacing/blur/height refinements.
- 2026-04-08: Further tuned home hero visuals: increased banner backdrop blur strength, increased hero/banner heights, and moved the search bar downward from the top for closer design alignment.
- 2026-04-08: Smoothed banner title background layering by using a softer full-card gradient (`bg_banner_bottom_scrim`) with reduced opacity to avoid hard separation artifacts.
- 2026-04-08: Validation log: executed `./gradlew.bat :app:assembleDebug` successfully after latest hero blur/height/search-offset refinements.
- 2026-04-08: Home banner blur was strengthened (Android 12+ RenderEffect radius increased), banner card height was slightly increased, and title-area layered background was softened by switching to full-card smooth scrim gradient.
- 2026-04-08: Validation log: executed `./gradlew.bat :app:assembleDebug` successfully after blur/height/title-background optimization.
- 2026-04-08: Home banner now drives a blurred backdrop layer behind the search bar using current carousel data (`iv_banner_bg_blur`), and the top hero section was merged to eliminate black gaps under rounded banner corners.
- 2026-04-08: Validation log: executed `./gradlew.bat :app:assembleDebug` successfully after home blurred-background and black-area fix.
- 2026-04-08: Home header search icon and banner layout were restyled to match the provided design: independent top search bar, updated spacing/radius/translucent background, and redesigned banner title/indicator positioning.
- 2026-04-08: Banner visuals were refined for design consistency: carousel image now uses center-crop with rounded corners, plus a bottom scrim overlay and compact dot indicators.
- 2026-04-08: Validation log: executed `./gradlew.bat :app:assembleDebug` successfully after home search/banner style updates.
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
