# BusAssistant — 智能通勤助手 (Android MVP)

> 打开 App 直接看到你常坐的几班车现在到哪了。无地图、零交互、纯本地。

## 核心特性

| 特性 | 说明 |
|------|------|
| **零交互看板** | 打开 App 3 秒内展示收藏线路的实时车辆位置和到站时间 |
| **无地图 SDK** | 纯进度条可视化，APK < 20MB，启动 < 2 秒 |
| **纯本地存储** | 所有数据存在手机本地，不上传任何服务器 |
| **习惯学习** | 自动记录查看行为，高频线路智能置顶（P1） |
| **用户标注** | 为线路打标签（上班/回家等），增强个性化 |

## 技术栈

- **Kotlin** + **Jetpack Compose**
- **Hilt** 依赖注入
- **Room** 本地数据库
- **Retrofit** 网络请求（预留）
- **Coroutines + Flow** 异步编程
- **Mock 数据** 开发阶段内置模拟公交数据

## 项目结构

```
com.saidi.busassistant/
├── MainActivity.kt              # 入口 + Navigation
├── BusAssistantApp.kt           # Application (Hilt)
├── data/
│   ├── local/                   # Room 数据库
│   │   ├── entity/
│   │   │   ├── BusLineEntity.kt         # 收藏线路
│   │   │   └── BehaviorLogEntity.kt     # 行为日志
│   │   ├── AppDatabase.kt
│   │   ├── BusLineDao.kt
│   │   └── BehaviorLogDao.kt
│   ├── remote/                  # Retrofit API
│   │   ├── BeijingBusApi.kt
│   │   └── dto/
│   │       └── BusRealTimeDto.kt
│   └── repository/
│       └── BusRepository.kt     # 数据整合 + Mock
├── di/
│   └── AppModule.kt             # Hilt 模块
├── ui/
│   ├── home/
│   │   └── HomeScreen.kt        # 首页实时看板
│   ├── addline/
│   │   └── AddLineScreen.kt     # 添加线路流程
│   ├── settings/
│   │   └── SettingsScreen.kt    # 设置页
│   ├── components/
│   │   ├── BusLineCard.kt       # 线路卡片
│   │   └── ProgressIndicator.kt # 进度条组件
│   ├── viewmodel/
│   │   └── HomeViewModel.kt     # 首页逻辑
│   └── theme/
│       ├── Color.kt
│       ├── Theme.kt
│       └── Type.kt
└── util/
    └── BusLabel.kt              # 标签工具 (预留)
```

## 开发阶段说明

### 当前状态：MVP（Mock 数据）

当前版本使用 **Mock 数据**模拟北京公交线路，无需真实 API 即可运行和体验交互流程。

### 切换到真实 API

修改 `BusRepository.kt` 中的 `getRealTimeData` 方法：

```kotlin
// 取消 Mock 数据注释，启用真实 API 调用：
val response = busApi.getRealTimeData(lineId, direction)
if (response.isSuccessful && response.body()?.status == 200) {
    val data = response.body()?.data
    if (data != null) {
        cache[cacheKey] = CacheEntry(data)
        lastRequestTime[cacheKey] = System.currentTimeMillis()
        return@withContext Result.success(data)
    }
}
```

同时修改 `BeijingBusApi.BASE_URL` 为实际的北京公交 API 地址。

## 构建和运行

### 环境要求
- Android Studio Hedgehog (2023.1.1) 或更新版本
- JDK 17
- Android SDK 34

### 构建步骤

1. 用 Android Studio 打开项目
2. 同步 Gradle（Sync Project with Gradle Files）
3. 连接设备或启动模拟器（API 26+）
4. 点击 Run

### 命令行构建

```bash
./gradlew assembleDebug
```

APK 输出路径：`app/build/outputs/apk/debug/app-debug.apk`

## 功能清单

### P0 — MVP 核心（已完成）
- [x] 手动添加公交线路
- [x] 实时看板（首页）
- [x] 实时数据网关（含 Mock）
- [x] 本地数据存储（Room）

### P1 — 智能层（框架已搭，待接入）
- [x] 用户路线标注
- [x] 行为自动记录
- [ ] 地理围栏区域判断（需位置权限）
- [x] 智能排序算法
- [x] 设置页

### P2 — 体验优化（后续迭代）
- [ ] 锁屏/桌面小组件
- [ ] 到站提醒通知
- [ ] 历史统计页

## 隐私说明

- **所有数据仅存储在设备本地**，使用 Room 数据库
- **不上传任何数据到服务器**
- 位置信息仅用于判断通勤方向，不在后台持续采集
- 用户可随时在设置中清除所有学习数据或关闭习惯记录

## License

MIT License — 自由使用和修改。
