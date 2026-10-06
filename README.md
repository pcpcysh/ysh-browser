# YSH Browser

> 极致轻量的 Android 浏览器，Material 风格，多标签页，暗黑模式。

## ✨ 特性

- 🗂️ **多标签页** - 真正的多 WebView，切换不重新加载
- 🖼️ **标签缩略图** - 卡片式标签管理，实时截图
- 🎨 **Material 风格 UI** - 深色配色，圆角卡片，紫青渐变
- 🚀 **极致轻量** - APK 仅 128KB，零第三方依赖
- 📱 **电视遥控适配** - 方向键导航，焦点管理
- 🌙 **强制深色** - 无需手动切换，所有界面和网页统一深色
- 🔍 **智能地址栏** - 自动识别网址/搜索词
- 📚 **书签管理** - SQLite 存储，首页动态展示
- 📜 **历史记录** - 自动记录，可清空
- 🕶️ **无痕模式** - 一键开关，不记录历史/Cookie
- ⬇️ **下载支持** - 调用系统下载管理器
- 🔗 **分享网页** - 一键分享到其他 App
- 📌 **桌面快捷方式** - Android 8.0+ 支持
- 🖼️ **书签图标自动抓取** - favicon.im 接口
- ✨ **涟漪反馈** - 所有按钮 Material 涟漪效果

## 📸 截图

*(待添加)*

## 🛠️ 技术栈

- Java
- Android SDK (compileSdk 30, minSdk 21)
- WebView / SQLite
- **零第三方库依赖**（极致轻量）

## 📦 构建

### AIDE Pro（手机端）

1. 用 AIDE Pro 打开项目
2. 点击锤子编译
3. 点击运行

### Android Studio（电脑端）

1. 克隆项目
2. Sync Gradle
3. Run

## 📁 项目结构

```

app/src/main/
├── java/com/ysh/browser/
│   ├── MainActivity.java          # 主界面 + 多标签
│   ├── TabsActivity.java          # 标签管理页面
│   ├── TabManager.java            # 标签状态存储
│   ├── SettingsActivity.java      # 设置
│   ├── HistoryActivity.java       # 历史记录
│   ├── BookmarkActivity.java      # 书签
│   ├── HistoryHelper.java         # 历史数据库
│   └── BookmarkHelper.java        # 书签数据库
└── res/
├── layout/                    # 布局
├── drawable/                  # 圆角背景、矢量图标
├── values/                    # 颜色、主题
└── anim/                      # 菜单动画

```

## 📄 License

MIT License
