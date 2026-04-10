# DramaMore 屏幕适配说明

## 概述
本项目已完成全面的屏幕适配，支持从小屏手机到大屏平板的各种设备。

## 适配方案

### 1. 资源限定符适配
使用 Android 的 `values-swXXXdp` 资源限定符，为不同屏幕宽度提供不同的尺寸资源。

#### 支持的屏幕尺寸
- **values** (默认): 基准尺寸，适用于所有未匹配的设备
- **values-sw360dp**: 小屏手机 (如 Galaxy S5, Pixel 2)
- **values-sw411dp**: 标准手机 (如 Pixel 3, Galaxy S10)
- **values-sw480dp**: 大屏手机 (如 Pixel 3 XL, Galaxy Note)
- **values-sw600dp**: 小平板 (7英寸平板)
- **values-sw720dp**: 大平板 (10英寸平板)

### 2. 尺寸资源定义

#### 首页相关尺寸
- `home_header_container_height`: 首页头部容器高度
- `home_header_scrim_height`: 首页头部渐变高度
- `home_search_height`: 搜索框高度
- `home_banner_height`: Banner 高度

#### 关注页封面尺寸
- `follow_cover_width`: 封面宽度
- `follow_cover_height`: 封面高度
- `follow_cover_bottom_mask_height`: 底部遮罩高度

#### 通用间距
- `spacing_tiny`: 极小间距 (3-6dp)
- `spacing_small`: 小间距 (6-12dp)
- `spacing_medium`: 中等间距 (8-16dp)
- `spacing_normal`: 标准间距 (10-18dp)
- `spacing_large`: 大间距 (12-24dp)
- `spacing_xlarge`: 超大间距 (16-32dp)

#### 图标尺寸
- `icon_tiny`: 极小图标 (12-20dp)
- `icon_small`: 小图标 (16-26dp)
- `icon_medium`: 中等图标 (22-32dp)
- `icon_large`: 大图标 (32-48dp)

#### 文字大小
- `text_size_tiny`: 极小文字 (9-13sp)
- `text_size_small`: 小文字 (12-16sp)
- `text_size_normal`: 标准文字 (13-17sp)
- `text_size_medium`: 中等文字 (14-18sp)
- `text_size_large`: 大文字 (16-20sp)
- `text_size_xlarge`: 超大文字 (17-22sp)

#### 圆角
- `corner_radius_small`: 小圆角 (3-6dp)
- `corner_radius_medium`: 中等圆角 (6-12dp)
- `corner_radius_large`: 大圆角 (10-18dp)

#### 其他
- `card_elevation`: 卡片阴影高度
- `card_corner_radius`: 卡片圆角
- `bottom_nav_height`: 底部导航栏高度

### 3. 工具类使用

#### ScreenUtils 工具类
提供了丰富的屏幕相关工具方法：

```java
// 获取屏幕宽度（像素）
int width = ScreenUtils.getScreenWidth(context);

// 获取屏幕高度（像素）
int height = ScreenUtils.getScreenHeight(context);

// 获取状态栏高度
int statusBarHeight = ScreenUtils.getStatusBarHeight(context);

// dp 转 px
int px = ScreenUtils.dp2px(context, 16);

// px 转 dp
float dp = ScreenUtils.px2dp(context, 48);

// sp 转 px
int px = ScreenUtils.sp2px(context, 14);

// 判断是否为平板
boolean isTablet = ScreenUtils.isTablet(context);

// 获取屏幕宽度（dp）
int widthDp = ScreenUtils.getScreenWidthDp(context);

// 获取屏幕信息
String info = ScreenUtils.getScreenInfo(context);
```

## 使用建议

### 1. 布局文件中使用
在 XML 布局文件中，优先使用 dimens 资源而不是硬编码数值：

```xml
<!-- 推荐 -->
<TextView
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    android:textSize="@dimen/text_size_normal"
    android:padding="@dimen/spacing_medium" />

<!-- 不推荐 -->
<TextView
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    android:textSize="14sp"
    android:padding="10dp" />
```

### 2. 代码中使用
在 Java/Kotlin 代码中，使用 Resources 获取尺寸：

```java
// 获取 dimen 资源
int spacing = getResources().getDimensionPixelSize(R.dimen.spacing_medium);

// 或使用工具类
int px = ScreenUtils.dp2px(context, 16);
```

### 3. 新增尺寸资源
如果需要新增尺寸资源：
1. 在 `values/dimens.xml` 中定义基准值
2. 在各个 `values-swXXXdp/dimens.xml` 中定义对应的适配值
3. 保持比例关系，确保在不同屏幕上视觉效果一致

## 测试建议

### 测试设备
建议在以下设备/模拟器上测试：
- 小屏手机: 360dp 宽度 (如 Pixel 2)
- 标准手机: 411dp 宽度 (如 Pixel 3)
- 大屏手机: 480dp 宽度 (如 Pixel 3 XL)
- 小平板: 600dp 宽度 (7英寸平板)
- 大平板: 720dp 宽度 (10英寸平板)

### 检查要点
1. 文字大小是否合适，不会过大或过小
2. 间距是否协调，不会过于拥挤或稀疏
3. 图片和图标是否清晰，比例是否合适
4. 布局是否充分利用屏幕空间
5. 触摸区域是否足够大（建议最小 48dp）

## 注意事项

1. **不要硬编码尺寸**: 始终使用 dimens 资源
2. **保持一致性**: 相同功能的元素使用相同的尺寸资源
3. **测试多设备**: 在不同屏幕尺寸上充分测试
4. **考虑横屏**: 如果应用支持横屏，需要额外适配
5. **字体缩放**: 考虑用户设置的字体大小

## 维护

当需要调整 UI 尺寸时：
1. 首先在基准文件 `values/dimens.xml` 中调整
2. 然后按比例调整各个 `values-swXXXdp/dimens.xml` 中的值
3. 在多个设备上测试效果
4. 确保视觉效果在所有设备上保持一致

## 参考资料

- [Android 官方文档 - 支持不同的屏幕尺寸](https://developer.android.com/training/multiscreen/screensizes)
- [Android 官方文档 - 提供资源](https://developer.android.com/guide/topics/resources/providing-resources)
- [Material Design - 布局](https://material.io/design/layout/understanding-layout.html)
