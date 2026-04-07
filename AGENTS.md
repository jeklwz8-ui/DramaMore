## 1. 项目概览

`DramaMore` 是一个 Android 短剧应用，当前核心能力由 **ByteDance PSSDK（短剧 SDK）** 驱动，主要包含：

- 首页（Home）：网格流 + 头部多区块内容
- 推荐页（Recommend）：类似哗哗流/短视频流的纵向播放
- 关注页（Follow）：已收藏内容列表
- 我的页（Mine）：历史、语言、协议、清缓存等
- 播放页（DramaPlayActivity）：完整剧集播放、解锁、广告、收藏、历史记录等
- 历史页（HistoryActivity）

当前广告实现主要是 **Pangle 直连**，尚未看到 **Firebase** 或 **TOPON** 的正式接入代码。

---

## 2. 技术栈与现状

### 2.1 语言与构建
- 主要业务代码：**Java**
- 构建系统：**Gradle**
- Android 插件管理方式：**Version Catalog**
- 存在 `Compose` 依赖，但主要页面和业务仍是传统 `Activity / Fragment / XML`

### 2.2 关键第三方 SDK
- `com.bytedance.dramaverse:pssdk:1.9.0.0`
- `com.pangle.global:pag-sdk:+`
- `Room 2.2.6`
- `Glide`
- `OkHttp`
- `Gson`

### 2.3 当前未正式接入的能力
以下能力目前在仓库内**尚未完整接入**，如果任务涉及，需要新增：
- Firebase Analytics
- Firebase Crashlytics
- Firebase Cloud Messaging（FCM）
- TOPON 中介
- AdMob / Unity / Facebook 聚合链路

---

## 3. 目录与关键文件地图

> 以下路径为当前仓库中最重要的入口文件。

### 3.1 应用入口与配置
- `app/src/main/java/com/dramamore/shorts/yanqin/App.java`
- `app/src/main/AndroidManifest.xml`
- `app/build.gradle`
- `gradle/libs.versions.toml`
- `settings.gradle`

### 3.2 页面入口
- `app/src/main/java/com/dramamore/shorts/yanqin/activity/MainActivity.java`

### 3.3 主要 Fragment
- `fragment/HomeFragment.java`
- `fragment/RecommendFragment.java`
- `fragment/FollowFragment.java`
- `fragment/MineFragment.java`

### 3.4 主要 Activity
- `activity/DramaPlayActivity.java`
- `activity/LanguageActivity.java`
- `activity/HistoryActivity.java`
- `activity/WebViewActivity.java`
- `activity/MoreActivity.java`
- `activity/SearchActivity.java`

### 3.5 数据层
- `database/HistoryDatabase.java`
- `dao/HistoryDao.java`
- `entity/HistoryDaoEntity.java`
- `database/FollowDatabase.java`（存在于仓库中时与关注功能有关）
- `dao/FollowDao.java`
- `entity/FollowDaoEntity.java`

### 3.6 工具类
- `utils/ShortUtils.java`
- `utils/PlayHistoryHelper.java`
- `utils/FragmentUtils.java`
- `utils/SPUtils.java`
- `utils/Logs.java`

---

## 4. 启动流程与页面流转

### 4.1 Application 启动
`App.java` 当前主要做两件事：

1. 初始化 `PSSDK`
2. 初始化 `Pangle SDK`

当前还没有：
- Firebase 初始化辅助逻辑
- TOPON 初始化逻辑
- AdMob 初始化配置
- 全局埋点工具初始化

### 4.2 Manifest 入口
`MainActivity` 是 Launcher Activity。

### 4.3 主页面结构
`MainActivity` 使用底部导航切换 4 个 Fragment：

- HomeFragment
- RecommendFragment
- FollowFragment
- MineFragment

### 4.4 详情/播放页
`DramaPlayActivity` 是最核心、最复杂的业务页面：

- 进入完整播放页
- 处理剧集切换
- 处理进度、分辨率、倍速
- 插入 Banner / Feed / Reward 广告
- 管理解锁逻辑
- 写入历史与收藏

---

## 5. 各页面职责说明

### 5.1 MainActivity
职责：
- 作为首页容器
- 管理底部 Tab
- 切换 4 个主 Fragment

如果要加以下能力，可优先考虑这里：
- 留存事件检测（如 `Thirdday_open`、`Seven_open`）
- Android 13 通知权限请求
- 获取 FCM token

---

### 5.2 HomeFragment
职责：
- 首页推荐流
- 头部 Banner / 热门 / 配音 / 收藏最多 / 动态漫剧等区块
- Feed 分页加载
- 缓存首页数据到 SP

特点：
- 通过 `PSSDK.requestFeedList(...)`、`requestNewDrama(...)`、`requestPopularDrama(...)` 等接口拉内容
- 对首页多个区块使用本地缓存
- 通过 `HomeAdapter` 承载首页 UI

修改建议：
- 如果增加首页埋点，优先放在数据加载成功、点击事件、进入播放页等位置
- 不要随意删掉缓存逻辑，否则会影响冷启动体验

---

### 5.3 RecommendFragment
职责：
- 哗哗流/纵向单剧流播放
- 使用 `ViewPager2` 垂直播放多个 `ShortPlayFragment`
- 切页时预加载下一个视频
- 页面隐藏/暂停时停止播放，避免后台继续出声

特点：
- 这是最接近“推荐流”的实现
- 内部 `FeedListAdapter` 会创建 `PSSDK.createDetailFragment(...)`
- 已针对“页面切走仍播放”做了防护：`onPause` / `onStop` / `onHiddenChanged` / `onDestroyView` 都会停止播放

修改建议：
- 若加入哗哗流广告，不要破坏现有停止播放逻辑
- 推荐页目前未接广告 SDK 代码；若新增，要特别注意 fragment 生命周期

---

### 5.4 FollowFragment
职责：
- 展示已收藏的短剧
- 通过远端 feed + 本地收藏状态筛出关注内容
- 加载更多时跳过空页，尽量填满首屏

特点：
- 数据不是纯本地表驱动，而是从 `PSSDK.requestFeedList(...)` 拉内容后，再根据 `shortPlay.isCollected` 和本地历史组合成 UI 数据
- 依赖 `HistoryDao` 查最近播放集数

修改建议：
- 如果要把关注页改成纯本地收藏页，需要连同 `FollowDatabase` / `FollowDao` 逻辑一起改
- 当前实现可能依赖服务端 feed 覆盖面，改动需谨慎

---

### 5.5 MineFragment
职责：
- 历史入口
- 语言设置入口
- 协议页入口
- 清缓存
- 版本号显示
- 展示最近 3 条历史剧集

特点：
- 最近观看数据来自 `HistoryDatabase`
- 语言设置通过 `LanguageChooseDialog` 回调后调用 `PSSDK.setContentLanguages(...)`
- 改语言后会重启当前 Activity

修改建议：
- 如果在“我的页”增加埋点，这是一个适合加轻量行为日志的页面
- 不建议在这里加强打断型广告

---

### 5.6 LanguageActivity
职责：
- 展示语言勾选列表
- 确认后回传选中语言

关键点：
- 当前布局 `activity_language.xml` 已有 `ad_container`
- 这个容器适合作为后续 Banner / 原生广告容器
- 目前 Java 逻辑还没有接广告代码

修改建议：
- 语言页如果加广告，应优先使用底部容器，不要影响语言勾选和确认按钮交互
- 先确保 SDK 依赖和初始化都已完成，再向该容器 `addView`

---

### 5.7 HistoryActivity
职责：
- 展示观看历史
- 基于 Room 的 `HistoryDao` 分页加载

注意：
- 当前 `offset` 计算有可疑逻辑：`(currentPage - 1) * offset`
- 如果未来修复分页，需补回归测试，避免历史页重复/丢页

---

### 5.8 DramaPlayActivity（最核心）
这是最重要的业务页面，修改时优先保护稳定性。

职责包括：
- 创建播放页 Fragment
- 默认解锁前 5 集
- 自动播放下一集
- 处理底部扩展内容（Banner / 默认 View）
- Reward 激励广告解锁
- Feed 原生广告预加载
- Banner 加载与展示
- 播放历史保存
- 收藏/点赞/分享/倍速/分辨率 UI
- 退出挽留弹窗
- 收益上报 `PSSDK.reportRevenueInfo(...)`

### 已存在的关键回调
- `onShortPlayPlayed(...)`
- `onVideoPlayCompleted(...)`
- `onVideoInfoFetched(...)`
- `onItemSelected(...)`
- `showAdIfNeed(...)`
- `onProgressChange(...)`

### 已存在的广告相关方法
- `loadRewardAd(...)`
- `loadPangleFeedAd()`
- `loadPangleBannerAd()`
- `showUnlockAd(...)`

### 强烈建议
如无必要，不要重构该类的整体结构；应采用“局部插入”的方式新增：
- Firebase 打点
- Crashlytics 非致命异常记录
- 新广告位回调
- 小范围空指针保护

---

## 6. 数据层说明

### 6.1 HistoryDatabase
- 使用 Room
- 单例数据库
- 单线程 `ExecutorService` 处理 DB 操作

### 6.2 HistoryDao
提供：
- `insert(...)`
- `getEntityByShortId(...)`
- `deleteByShortId(...)`
- `getPagedHistories(limit, offset)`

### 6.3 HistoryDaoEntity
字段：
- `id`
- `short_id`
- `short_json`
- `play_index`

说明：
- `short_json` 直接存序列化后的 `ShortPlay`
- `play_index` 代表播放到第几集

### 6.4 ShortUtils
重要能力：
- `shortPlayToJson(...)`
- `jsonToShortPlay(...)`
- `historyInsert(...)`
- `followInsertOrDelete(...)`
- `convertToK(...)`

说明：
- 如果要调整历史/收藏记录方式，优先在 `ShortUtils` 统一改，避免页面层重复逻辑

---

## 7. 当前广告实现现状

### 7.1 当前接入方式
仓库里当前广告能力主要是 **Pangle 直连**：

- Rewarded
- Native Feed
- Banner

### 7.2 当前未完成项
- `App.REWARDAD_ID`
- `App.NATIVEAD_ID`
- `App.BANNERAD_ID`

这些常量当前可能仍为空，需要在真正接广告时填入。

### 7.3 现有广告行为
- 播放页切到视频 item 时，会尝试显示底部 Banner
- Reward 广告主要用于解锁更多剧集
- Feed 广告有预加载逻辑，但某些视图装配代码可能仍保留注释/半成品状态

### 7.4 中介现状
当前仓库尚未看到：
- TOPON SDK 依赖
- TOPON 初始化
- AdMob / Unity / Facebook 的接入代码

---

## 8. Firebase 接入需求（用户明确给出的业务需求）

如果任务是接入 Firebase，请遵循下列事件名与含义，不要私自改名。

### 8.1 留存事件
- `Thirdday_open`
  - 含义：距离用户首次打开 24–48 小时内的首次打开
  - 只记录一次
- `Seven_open`
  - 含义：距离用户首次打开 144–168 小时内的首次打开
  - 只记录一次

### 8.2 剧集事件
- `Play_drama_id`
  - 含义：用户进入播放页面时记录剧集 ID
- `Playover_drama_id`
  - 含义：用户播放完一部剧之后，记录剧 ID

### 8.3 广告监听事件
#### 插屏
- `Req_intads`
- `Req_intads_suc`
- `Show_intads`
- `Click_intads`

#### 开屏
- `Req_openads`
- `Req_openads_suc`
- `Show_openads`
- `Click_openads`

#### 原生
- `Req_ysads`
- `Req_ysads_suc`
- `Show_ysads`
- `Click_ysads`

#### 横幅
- `Req_bannerads`
- `Req_bannerads_suc`
- `Show_bannerads`
- `Click_bannerads`

### 8.4 建议的事件挂载位置
#### 留存
- `MainActivity.onResume()` 或应用首页显示后

#### 剧集
- `DramaPlayActivity.onShortPlayPlayed(...)`
- `DramaPlayActivity.onVideoPlayCompleted(...)`

#### Banner
- `loadPangleBannerAd()`

#### 原生
- `loadPangleFeedAd()`

#### 激励/插屏/开屏
- 根据未来接入的真实广告回调挂载，不要伪造

---


## 9. Codex 修改本仓库时的约束

以下是建议直接给 Codex 的工作约束。

### 9.1 总体原则
- 不重命名现有核心类
- 不随意改播放链路
- 不破坏 Room 表结构，除非任务明确要求迁移
- 不擅自改事件名
- 对所有新增 SDK 先确保 Gradle 能编译通过

### 9.2 修改播放页时
- 优先局部插入代码
- 保留原有回调名与业务流程
- 所有新增空值保护写清楚原因
- 广告失败不能阻塞视频播放
- 退出/暂停时不能让推荐流继续播放

### 9.3 修改首页与推荐页时
- 保留分页加载逻辑
- 保留缓存逻辑
- 不要让多次请求造成重复插入数据
- 推荐页需要格外注意 `onPause/onStop/onHiddenChanged`

### 9.4 修改数据层时
- 数据库读写继续走 `Executor`
- 若调整分页，补注释说明
- 不要在主线程直接查 Room

### 9.5 新增工具类时
建议新增到：
- `utils/`
- `push/`
- `analytics/`（若需要单独包）

---

## 10. Codex 可直接使用的任务模板

以下模板可直接作为 Codex prompt 使用。
复制任一模板块并粘贴给 Codex，即可直接执行对应任务。

### 10.1 接 Firebase Analytics
> 请在不引入 TOPON 的前提下，为本仓库接入 Firebase Analytics。要求：
> 1. 使用 `google-services.json`
> 2. 添加 `firebase-bom`、`firebase-analytics`
> 3. 新建统一 `EventTracker`
> 4. 在 `MainActivity.onResume()` 挂留存检测
> 5. 在 `DramaPlayActivity.onShortPlayPlayed()` 和 `onVideoPlayCompleted()` 挂剧集事件

### 10.2 接 Crashlytics
> 请在本仓库中接入 Firebase Crashlytics，要求：
> 1. 添加必要插件与依赖
> 2. 在 `App.java` 增加最小初始化辅助代码
> 3. 保持现有 PSSDK 与 Pangle 初始化不变
> 4. 给出一个测试崩溃入口，但不要默认在生产逻辑中触发
> 5. 说明修改文件及原因
> 6. 每次修改新增新的功能时，请务必在 `index.md` 中记录
> 7. 每次修改新增新的功能时，请务必增加一个完整日志来描述功能是否正常
> 8. 每次修改时不要影响其他正常的功能不要随意改事件名不要出现逻辑冲突适配器冲突监听器冲突等
> 9. 每次修改不要影响其他正常的功能更不能增加新的错误逻辑
> 10. 每次修改新增时不要动其他任何的正常功能的代码

### 10.3 接 FCM
> 请在本仓库中接入 Firebase Cloud Messaging：
> 1. 添加 `firebase-messaging`
> 2. 新建 `MyFirebaseMessagingService`
> 3. 在 Manifest 注册 service
> 4. 在 `MainActivity` 请求 Android 13 通知权限并获取 token

### 10.4 给语言页加 Banner 广告容器逻辑
> 请基于现有 `LanguageActivity.java` 与 `activity_language.xml`，把广告展示逻辑接入已有 `ad_container`，要求：
> 1. 不影响语言选择逻辑
> 2. 广告加载失败不崩溃
> 3. 页面销毁时释放广告对象
> 4. 加入对应广告事件埋点

---

## 11. 已知风险与坑位

### 11.1 播放页类过大
`DramaPlayActivity` 体量很大，功能高度耦合。不要一次性大重构。

### 11.2 推荐页生命周期敏感
推荐页使用 `ViewPager2 + ShortPlayFragment`，稍不注意就会出现页面切走仍播放的问题。

### 11.3 历史分页疑似存在 offset 逻辑问题
`HistoryActivity` 的分页偏移可能存在错误，需要谨慎验证后再改。

### 11.4 Follow 页数据并非纯本地
关注页不是单纯从本地收藏表拉完整数据，而是混合远端 feed 与收藏状态。

### 11.5 广告位常量可能未填
不要默认广告一定能展示，代码里要对空 ID 和加载失败做兜底。

---

## 12. 推荐的改动输出格式（让 Codex 按这个执行）

当 Codex 完成任务后，建议输出：
1. 修改文件列表
2. 每个文件的改动目的
3. 关键代码片段
4. 验证步骤
5. 风险说明

---

## 13. 给 Codex 的最后提醒

- 先读 `App.java`、`MainActivity.java`、`DramaPlayActivity.java`
- 播放链路优先保护稳定，不要抢着重构
- 先让构建通过，再接业务逻辑
- 对所有 SDK 接入，优先遵循官方文档
- 对所有埋点，优先遵循业务方给定事件名
- 如果无法确认版本号，不要盲猜，先保留占位并明确标注需要用户补充
- 每次修改新增新的功能时，请务必在 `index.md` 中记录
- 每次修改新增新的功能时，请务必增加一个完整日志来描述功能是否正常
- 每次修改时不要影响其他正常的功能不要随意改事件名不要出现逻辑冲突适配器冲突监听器冲突等

## 14. 执行效率与防遗漏清单（强制）


1. 先确认本次只修改 Git 跟踪的主工程目录（默认 `app/`），避免误改重复副本目录。
2. 先列出将要改动的文件与插入点，再开始改代码。
3. 涉及 `DramaPlayActivity` 时，先加生命周期防护再加功能逻辑。
4. 播放页新增弹窗/对话框必须满足：
   - 使用 Activity 上下文创建（不要用不稳定的 View Context）。
   - `show()` 前检查 `isFinishing()` / `isDestroyed()`。
   - 在视图 `onDetachedFromWindow()` 或页面销毁时主动 `dismiss()`。
5. 广告、弹窗、埋点失败都不能阻塞播放主链路。
6. 列表筛选/分页改动要保持“原分页逻辑 + 原缓存逻辑”不被破坏。
7. 改动完成后必须执行至少一次构建验证：`./gradlew.bat :app:assembleDebug`。
8. 最终输出必须包含“已对照 AGENTS.md 检查”的结论与剩余风险。

---
