# DramaMore 修复清单

## 🔴 P0 - 安全问题（必须立即修复）

### ✅ 任务 1: 移除硬编码签名密钥
- [ ] 创建 `keystore.properties` 文件（添加到 .gitignore）
- [ ] 修改 `app/build.gradle` 使用环境变量
- [ ] 生成新的 release keystore（强密码）
- [ ] 更新 CI/CD 配置（如果有）

**文件**: `app/build.gradle`
**预计时间**: 30 分钟

---

### ✅ 任务 2: 启用 Release 混淆
- [ ] 修改 `app/build.gradle` 启用 `minifyEnabled`
- [ ] 启用 `shrinkResources`
- [ ] 测试混淆后的 APK

**文件**: `app/build.gradle`
**预计时间**: 15 分钟

---

### ✅ 任务 3: 完善 ProGuard 规则
- [ ] 添加 Gson 混淆规则
- [ ] 添加 Room 混淆规则
- [ ] 添加 Glide 混淆规则
- [ ] 添加 TOPON/AdMob 混淆规则
- [ ] 添加 PSSDK 混淆规则
- [ ] 测试混淆后功能

**文件**: `app/proguard-rules.pro`
**预计时间**: 1 小时

---

## 🟡 P1 - 稳定性问题（高优先级）

### ✅ 任务 4: 修复数据库分页逻辑
- [ ] 检查 `HistoryActivity` 分页代码
- [ ] 修复 offset 计算错误
- [ ] 添加分页测试用例
- [ ] 验证历史记录加载正确

**文件**: `HistoryActivity.java`
**预计时间**: 1 小时

---

### ✅ 任务 5: 修复内存泄漏
- [ ] `DramaPlayActivity`: 清理 Handler
- [ ] `RecommendFragment`: 清理 Runnable
- [ ] `HomeFragment`: 清理 Handler
- [ ] 使用 LeakCanary 验证

**文件**: 
- `DramaPlayActivity.java`
- `RecommendFragment.java`
- `HomeFragment.java`

**预计时间**: 2 小时

---

### ✅ 任务 6: 完善异常处理
- [ ] 替换所有 `catch (Throwable ignore)` 为具体异常
- [ ] 添加日志记录
- [ ] 添加用户友好的错误提示
- [ ] 关键异常上报到 Crashlytics

**文件**: 多个文件
**预计时间**: 2 小时

---

### ✅ 任务 7: 释放广告资源
- [ ] `DramaPlayActivity.onDestroy()` 释放广告
- [ ] 检查所有广告使用位置
- [ ] 添加空指针保护

**文件**: `DramaPlayActivity.java`
**预计时间**: 30 分钟

---

## 🟢 P2 - 代码质量（中优先级）

### ✅ 任务 8: 清理依赖
- [ ] 删除重复的 `viewpager2` 依赖
- [ ] 删除重复的 `glide` 依赖
- [ ] 删除过时的 `kotlin-stdlib-jdk7`
- [ ] 清理注释掉的依赖代码
- [ ] 更新 Room 到 2.6.x

**文件**: `app/build.gradle`
**预计时间**: 30 分钟

---

### ✅ 任务 9: 修复广告 ID 配置
- [ ] 将广告 ID 移到 BuildConfig
- [ ] 区分 debug/release 环境
- [ ] 添加空值检查
- [ ] 更新所有引用位置

**文件**: 
- `app/build.gradle`
- `App.java`

**预计时间**: 1 小时

---

### ✅ 任务 10: 修复注释乱码
- [ ] 修复 `DramaPlayActivity` 中的乱码注释
- [ ] 统一注释编码为 UTF-8
- [ ] 添加类级别 JavaDoc
- [ ] 添加关键方法 JavaDoc

**文件**: 
- `DramaPlayActivity.java`
- 其他包含乱码的文件

**预计时间**: 2 小时

---

### ✅ 任务 11: 消除魔法数字
- [ ] 定义常量 `DEFAULT_UNLOCKED_EPISODES = 5`
- [ ] 定义常量 `DEFAULT_PAGE_SIZE = 20`
- [ ] 定义常量 `MAX_RETRY_COUNT = 3`
- [ ] 替换所有硬编码数字

**文件**: 多个文件
**预计时间**: 1 小时

---

## 🔵 P3 - 架构优化（低优先级）

### ✅ 任务 12: 创建 Repository 层
- [ ] 创建 `ShortPlayRepository`
- [ ] 封装 PSSDK 调用
- [ ] 统一错误处理
- [ ] 添加缓存逻辑

**新文件**: `repository/ShortPlayRepository.java`
**预计时间**: 4 小时

---

### ✅ 任务 13: 引入 ViewModel
- [ ] 添加 ViewModel 依赖
- [ ] 创建 `HomeViewModel`
- [ ] 创建 `RecommendViewModel`
- [ ] 重构 Fragment 使用 ViewModel

**新文件**: 
- `viewmodel/HomeViewModel.java`
- `viewmodel/RecommendViewModel.java`

**预计时间**: 6 小时

---

### ✅ 任务 14: 创建 AdManager
- [ ] 设计 AdManager 接口
- [ ] 实现统一广告加载
- [ ] 实现广告缓存
- [ ] 实现广告预加载
- [ ] 重构现有广告代码

**新文件**: `ad/AdManager.java`
**预计时间**: 8 小时

---

### ✅ 任务 15: 添加单元测试
- [ ] 配置测试环境
- [ ] 测试 `ShortUtils` 序列化
- [ ] 测试数据库操作
- [ ] 测试工具类方法
- [ ] 目标覆盖率 > 30%

**新文件**: `test/` 目录下多个测试文件
**预计时间**: 8 小时

---

### ✅ 任务 16: 性能优化
- [ ] RecyclerView 优化（DiffUtil）
- [ ] 图片加载优化（占位图、缓存）
- [ ] 数据库查询优化（索引、LiveData）
- [ ] 使用 StrictMode 检测性能问题

**文件**: 多个文件
**预计时间**: 6 小时

---

## 🟣 Firebase 集成（按 AGENTS.md 要求）

### ✅ 任务 17: 接入 Firebase Analytics
- [ ] 添加 Firebase 依赖
- [ ] 添加 `google-services.json`
- [ ] 创建 `EventTracker` 工具类
- [ ] 实现留存事件（Thirdday_open, Seven_open）
- [ ] 实现剧集事件（Play_drama_id, Playover_drama_id）
- [ ] 实现广告事件（Req_*, Show_*, Click_*）

**新文件**: `analytics/EventTracker.java`
**预计时间**: 4 小时

---

### ✅ 任务 18: 接入 Firebase Crashlytics
- [ ] 添加 Crashlytics 依赖
- [ ] 配置 Crashlytics 插件
- [ ] 添加非致命异常记录
- [ ] 添加自定义键值对
- [ ] 测试崩溃上报

**文件**: 
- `app/build.gradle`
- 多个 Activity/Fragment

**预计时间**: 2 小时

---

### ✅ 任务 19: 接入 FCM（可选）
- [ ] 添加 FCM 依赖
- [ ] 创建 `MyFirebaseMessagingService`
- [ ] 请求通知权限（已有）
- [ ] 获取 FCM Token
- [ ] 处理推送消息

**新文件**: `push/MyFirebaseMessagingService.java`
**预计时间**: 3 小时

---

## 📊 进度跟踪

### 总体进度
- **P0 任务**: 0/3 完成
- **P1 任务**: 0/4 完成
- **P2 任务**: 0/4 完成
- **P3 任务**: 0/5 完成
- **Firebase**: 0/3 完成

### 预计总时间
- **P0**: 2.25 小时
- **P1**: 5.5 小时
- **P2**: 4.5 小时
- **P3**: 32 小时
- **Firebase**: 9 小时

**总计**: 约 53 小时（7 个工作日）

---

## 🎯 里程碑

### 里程碑 1: 安全修复（Day 1）
- [x] 完成所有 P0 任务
- [x] 生成 Release APK 测试
- [x] 安全审计通过

### 里程碑 2: 稳定性提升（Day 2-3）
- [ ] 完成所有 P1 任务
- [ ] 内存泄漏测试通过
- [ ] 异常处理完善

### 里程碑 3: 代码质量（Day 4-5）
- [ ] 完成所有 P2 任务
- [ ] 代码审查通过
- [ ] 依赖更新完成

### 里程碑 4: Firebase 集成（Day 6-7）
- [ ] Analytics 接入完成
- [ ] Crashlytics 接入完成
- [ ] 事件埋点验证

### 里程碑 5: 架构优化（Day 8-14）
- [ ] 完成所有 P3 任务
- [ ] 单元测试覆盖率 > 30%
- [ ] 性能基准测试通过

---

## 📝 注意事项

1. **每次修改后必须测试**：确保功能正常
2. **提交前代码审查**：至少一人 review
3. **保持向后兼容**：不要破坏现有功能
4. **文档同步更新**：修改后更新相关文档
5. **版本号管理**：每个里程碑更新版本号

---

## 🔗 相关文档

- [CODE_REVIEW_REPORT.md](./CODE_REVIEW_REPORT.md) - 详细审查报告
- [SCREEN_ADAPTATION.md](./SCREEN_ADAPTATION.md) - 屏幕适配说明
- [AGENTS.md](./AGENTS.md) - 项目架构文档

---

**创建时间**: 2026-04-10  
**最后更新**: 2026-04-10
