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
- 2026-04-08：已补充系统通知栏权限支持：Manifest 新增 `android.permission.POST_NOTIFICATIONS`，并在 `MainActivity` 中增加 Android 13+ 的运行时通知权限请求；Android 12 及以下不会弹此权限，因为系统本身不要求运行时授权。
- 2026-04-08：功能验证日志：已执行 `./gradlew.bat :app:assembleDebug`，用于验证“通知栏权限声明与 Android 13+ 动态申请”的本次改动是否编译通过。
- 2026-04-08：再次修正首页“动漫短剧”标题容器位置，确认 `fl_cartoon_section` 真实绑定到“动漫短剧”标题块本身，不再误挂到“收藏最多”标题块，确保无数据时标题不会残留。
- 2026-04-08：功能验证日志：已执行 `./gradlew.bat :app:assembleDebug`，用于验证“动漫短剧标题容器位置修正后无数据不显示标题”的本次修复是否编译通过。
- 2026-04-08：修正首页“动漫短剧”标题容器绑定错误，之前误把显示/隐藏控制挂到了其他标题块上，导致无数据时仍可能残留“动漫短剧”标题；现已改为真实绑定“动漫短剧”标题容器本身。
- 2026-04-08：功能验证日志：已执行 `./gradlew.bat :app:assembleDebug`，用于验证“动漫短剧标题与内容严格同显同隐”的本次修正是否编译通过。
- 2026-04-08：首页“动漫短剧”已改为和“配音剧”一致的显示逻辑：标题栏与内容区默认隐藏，只有接口成功且实际返回了动漫短剧数据后才显示；空数据时会清掉该栏缓存并整块隐藏。
- 2026-04-08：功能验证日志：已执行 `./gradlew.bat :app:assembleDebug`，用于验证“动漫短剧无数据时整栏隐藏、有数据时再显示”的本次改动是否编译通过。
- 2026-04-08：首页主推荐流及头部 Banner/热门/收藏最多/动态漫剧缓存已改为按当前内容语言隔离，切换语言后不再复用上一种语言的首页缓存数据。
- 2026-04-08：首页主推荐流第一页在当前语言下返回空结果时，现会主动清空旧缓存和旧列表，避免出现“语言已切换，但首页推荐仍显示旧语言内容”的问题。
- 2026-04-08：功能验证日志：已执行 `./gradlew.bat :app:assembleDebug`，用于验证“首页推荐内容跟随当前语言且不复用旧语言缓存”的本次修复是否编译通过。
- 2026-04-08：已新增统一内容语言 helper，首次打开应用且未设置语言时默认写入简体中文 `zh_hans`，避免首页首次进入语言为空导致语种相关模块不稳定。
- 2026-04-08：语言选择页面已修复为单选逻辑，点击任一语言时会自动取消其他勾选；当前已选语言会默认高亮勾选在对应项上，不再出现默认选中状态错位或多选问题。
- 2026-04-08：功能验证日志：已执行 `./gradlew.bat :app:assembleDebug`，用于验证“首次默认简体中文 + 语言页单选”的本次改动是否编译通过。
- 2026-04-08：修正首页“配音剧”标题容器绑定错误，之前误把显示/隐藏控制挂到了“热门短剧”标题块上，导致首次无配音剧数据时仍可能看到“配音剧”标题但下方为空；现已改为真实绑定“配音剧”标题块本身。
- 2026-04-08：功能验证日志：已执行 `./gradlew.bat :app:assembleDebug`，用于验证“配音剧标题与内容严格同显同隐”的修正是否编译通过。
- 2026-04-08：首页“配音剧”标题栏与内容区默认改为 `GONE`，等待加载过程中不再提前显示；只有请求成功且实际返回了配音剧数据后才显示，失败或空数据时保持隐藏。
- 2026-04-08：功能验证日志：已执行 `./gradlew.bat :app:assembleDebug`，用于验证“配音剧标题栏加载前隐藏、成功有数据后显示”的本次调整是否编译通过。
- 2026-04-08：首页“配音剧”已删除按 IP/地区映射语言的旧请求逻辑，改为严格跟随“我的页”语言设置 `PSSDK.getContentLanguages()` 取数；并按 1900 音频语种文档仅向 `PSSDK.setVoiceLanguages()` 传支持的编码，印尼语从内容语种 `in` 统一映射为音频语种 `id`。
- 2026-04-08：`HomeFragment` 的“配音剧”缓存已改为按当前所选语言单独隔离，切换语言后不会再复用上一种语言的首页配音剧缓存；当当前所选语言没有任何配音剧数据时，会清空当前语言对应缓存并隐藏首页“配音剧”整栏。
- 2026-04-08：`MoreActivity` 的“配音剧更多”页也已同步改为按“我的页”已选语言请求，不再走地区逻辑；若当前未选语言，则直接展示空列表，不再回退到其他地区语言数据。
- 2026-04-08：功能验证日志：已执行 `./gradlew.bat :app:assembleDebug`，用于验证“配音剧按我的页语言返回数据、无数据时隐藏整栏，且音频语种编码符合 1900 文档”的本次改动是否编译通过。
- 2026-04-08：已按 TopOn 官方 Android 接入要求补充 SDK 仓库源、TopOn Core/横幅/原生/插屏/激励/开屏依赖及 UnityAds、Pangle、AdMob、Tramini 适配器依赖，并将现有 Pangle 直连依赖固定到 `7.6.0.2`，避免继续使用 `+` 带来的版本漂移。
- 2026-04-08：`App.java` 已新增 TopOn 初始化入口，加入主进程判断、Debug 日志开关以及空 `App Key` 兜底；`AndroidManifest.xml` 已补充网络权限、`org.apache.http.legacy`、AdMob `APPLICATION_ID` 以及 `AD_MANAGER_APP=true` 元数据。
- 2026-04-08：`proguard-rules.pro` 已显式补充 `-keep class com.bytedance.sdk.** { *; }`；当前工程未开启 `shrinkResources`，因此本次无需额外放置 `res/raw/keep.xml`。
- 2026-04-08：功能验证日志：已执行 `./gradlew.bat :app:assembleDebug`，当前工程构建通过；运行时仍需补充 `TOPON_APP_KEY` 后，继续在真机 Logcat 中确认 `anythink` 初始化日志与各适配器加载状态。
- 2026-04-08：我的页“评价我们”评分弹窗的五颗星图标已替换为 `collect_1 / collect_2`，未选中显示 `collect_1`，选中后显示 `collect_2`。
- 2026-04-08：功能验证日志：已执行 `./gradlew.bat :app:assembleDebug`，评分弹窗星级图标替换为 `collect_1 / collect_2` 后的工程构建通过。
- 2026-04-08：我的页“评价我们”评分弹窗已按设计图继续精修，增强了底部面板的深色渐变、顶部圆角、标题层级、星级尺寸、输入框和提交按钮的视觉比例与间距。
- 2026-04-08：功能验证日志：已执行 `./gradlew.bat :app:assembleDebug`，我的页“评价我们”评分弹窗视觉精修后的工程构建通过。
- 2026-04-08：我的页“评价我们”按钮已删除原先跳转应用商店的实现，改为弹出页面内自定义评分弹窗；弹窗按设计图增加标题、五档星级、反馈输入框和提交按钮。
- 2026-04-08：功能验证日志：已执行 `./gradlew.bat :app:assembleDebug`，我的页“评价我们”自定义评分弹窗替换旧逻辑后的工程构建通过。
- 2026-04-08：我的页已在头像信息下方、观看记录上方恢复并重绘“会员权益”卡片，位置与设计图一致，展示“会员权益 / 未充值”标题及两列权益文案内容。
- 2026-04-08：功能验证日志：已执行 `./gradlew.bat :app:assembleDebug`，我的页新增会员权益卡片后的工程构建通过。
- 2026-04-08：修复播放页自定义右侧操作区闪退问题：右侧“收藏/倍速/清晰度”入口改为按“容器 + 子文案/图标”绑定，避免把 `LinearLayout` 误当成 `TextView` 强转；同时播放页自定义控件改用 Activity 上下文创建，并移除 SDK 点赞残留注入。
- 2026-04-08：功能验证日志：已执行 `./gradlew.bat :app:assembleDebug`，播放页右侧自定义操作区闪退修复后的工程构建通过。
- 2026-04-08：播放页右侧首个自定义按钮已由点赞功能切换为收藏功能，继续保持当前图标样式不变；点击后改为调用 `PSSDK.setCollected(...)`，并同步本地收藏状态。
- 2026-04-08：功能验证日志：已执行 `./gradlew.bat :app:assembleDebug`，播放页自定义按钮由点赞改为收藏后的工程构建通过。
- 2026-04-08：播放页点赞入口已彻底切换为页面自定义 overlay 按钮，不再使用 SDK 默认点赞图标；右侧三项现为自定义“点赞、倍速、清晰度”操作区，点赞点击直接走 `PSSDK.setLike(...)`。
- 2026-04-08：功能验证日志：已执行 `./gradlew.bat :app:assembleDebug`，播放页改为自定义点赞按钮并移除 SDK 点赞控件后的工程构建通过。
- 2026-04-08：播放页右侧操作区已按设计图重排为“点赞、倍速、清晰度”三项竖排结构；倍速和清晰度入口改为播放器右侧中下区域展示，点赞位置同步对齐到同一列。
- 2026-04-08：功能验证日志：已执行 `./gradlew.bat :app:assembleDebug`，播放页右侧操作区按设计图重排后的工程构建通过。
- 2026-04-08：播放页已移除右侧分享与收藏图标按钮，仅保留当前页面需要的播放控制项；分享入口和收藏入口不再在 `DramaPlayActivity` 播放界面展示。
- 2026-04-08：功能验证日志：已执行 `./gradlew.bat :app:assembleDebug`，播放页移除分享与收藏按钮后的工程构建通过。
- 2026-04-08：播放页已隐藏 SDK 默认左上返回按钮，改为使用播放页自定义返回箭头，并将箭头顶部偏移下调，方便单独控制返回图标位置。
- 2026-04-08：功能验证日志：已执行 `./gradlew.bat :app:assembleDebug`，播放页返回箭头改为自定义并下移后的工程构建通过。
- 2026-04-08：播放页主布局已按推荐页播放器骨架重新对齐，恢复为“上方视频区 + 下方独立底栏”的同款结构；沉浸式时底栏改为 `INVISIBLE` 保持占位，避免位置跳动。
- 2026-04-08：播放页 overlay 已进一步贴近推荐页样式，底部简介区不再额外展示语种按钮视觉占位，整体观感更接近推荐页播放器。
- 2026-04-08：功能验证日志：已执行 `./gradlew.bat :app:assembleDebug`，播放页按推荐页播放器骨架复刻后的工程构建通过。
- 2026-04-08：播放页播放器容器已继续整体上移，并同步对底部可见区域做压缩处理，进一步提升画面重心，减少下半部分占比。
- 2026-04-08：功能验证日志：已执行 `./gradlew.bat :app:assembleDebug`，播放页继续上移并压缩下方可见区域后的工程构建通过。
- 2026-04-08：播放页根布局顶部安全区内边距已从播放器本体移除，视频画面整体上移；顶部倍速、清晰度菜单改为单独按状态栏安全区做偏移，避免上移后遮挡。
- 2026-04-08：功能验证日志：已执行 `./gradlew.bat :app:assembleDebug`，播放页整体上移并保持顶部控制安全区后的工程构建通过。
- 2026-04-08：播放页底部操作栏已从根布局独立占位改为覆盖在播放器上的悬浮层，进入沉浸式时仅隐藏悬浮层，不再触发播放器区域重新测量和位置跳变。
- 2026-04-08：播放页底部进度条位置已改为随悬浮底部栏和系统安全区动态计算，保证非沉浸式时进度条贴着悬浮栏顶部，沉浸式切换时播放器位置保持稳定。
- 2026-04-08：功能验证日志：已执行 `./gradlew.bat :app:assembleDebug`，播放页底部栏改为悬浮层后的工程构建通过。
- 2026-04-08：播放页已关闭 SDK 底部额外内容保留区 `displayBottomExtraView(false)`，视频画面现在会继续向下填充到自定义进度条所在位置。
- 2026-04-08：功能验证日志：已执行 `./gradlew.bat :app:assembleDebug`，播放页视频区域下探到进度条位置后的工程构建通过。
- 2026-04-08：功能验证日志：已执行 `./gradlew.bat :app:assembleDebug`，播放页新增专用播放器覆盖层并切换为推荐页同款横向铺满显示模式后，工程构建通过。
- 2026-04-08：播放页新增专用播放器覆盖层布局 `player_overlay_for_play.xml`，用于和推荐页播放器样式解耦，单独承载播放页的倍速、清晰度、语种与底部信息展示。
- 2026-04-08：播放页播放器显示模式已切换为与推荐页一致的横向铺满模式，优先填满左右宽度，并对上下超出区域做裁切填充处理。
- 2026-04-08：播放页 `DramaPlayActivity` 底部布局已改为与推荐页一致，改为页面级自定义底部选集条与页面级自定义进度条，不再使用播放页原有的独立固定选集布局和 SDK 默认进度条展示方式。
- 2026-04-08：播放页选集入口已切换为本地 `IndexChooseDialog`，并保留倍速、清晰度、语种切换、广告与历史记录等原有能力；右侧“播放全集”按钮用于快速回到第 1 集重新播放。
- 2026-04-08：功能验证日志：已执行 `./gradlew.bat :app:assembleDebug`，播放页改为推荐页同款底部布局后的工程构建通过。
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
- 2026-04-07: Mine page added two new entries between language settings and privacy protocol: "璇勪环鎴戜滑" and "鍒嗕韩鎴戜滑".
- 2026-04-07: "璇勪环鎴戜滑" opens Google Play rating page (`market://details`) with web fallback.
- 2026-04-07: "鍒嗕韩鎴戜滑" opens Android share panel with app link text.
- 2026-04-07: Standardized icons for "璇勪环鎴戜滑", "鍒嗕韩鎴戜滑", and "闅愮鍗忚" in Mine page.

## Contributing
We welcome contributions! Please read our [CONTRIBUTING.md](CONTRIBUTING.md) for guidelines on how to contribute to this project.

## License
This project is licensed under the MIT License. See the [LICENSE](LICENSE) file for more details.
