# VillagerLimit 更新公告

## v2.1.3 (2026-01-01) 🐣 村民繁殖寿命管理版

### 🎉 重大更新

#### 村民繁殖寿命系统
插件现在支持为通过繁殖产生的村民设置寿命限制！

**1. 繁殖村民寿命管理**
- 自动检测村民繁殖事件
- 为繁殖出的小村民设置寿命（与治愈村民相同）
- 显示剩余寿命的浮动文本
- 寿命到期自动移除村民

**2. 自动寿命检测任务** ⭐ 新增
- 自动扫描全服所有村民，为无寿命村民添加寿命
- 支持启动时自动检查
- 支持定时任务自动检查（可配置间隔）
- 详细的日志记录（总数、添加数、已有数）

**3. 配置选项**
- `spawn-control.allow-breeding` - 控制是否允许村民繁殖
- 复用现有的 `villager-lifespan` 配置
- `villager-lifespan.auto-add-lifespan` - 自动添加寿命配置
  - `enabled` - 是否启用自动添加
  - `check-interval` - 检查间隔（秒），0表示仅启动时检查
  - `check-on-startup` - 启动时是否检查
- 支持调试模式查看详细日志

### ✨ 新增功能

1. **繁殖事件检测**
   - 自动识别通过繁殖产生的村民
   - 性能优化的早期返回机制
   - 完整的错误处理和日志记录

2. **自动寿命检测系统** ⭐ 新增
   - 启动时自动扫描全服村民
   - 定时任务自动检测（默认5分钟）
   - 智能识别无寿命村民并自动添加
   - 支持全服扫描所有世界
   - 详细的统计日志输出

3. **配置灵活性**
   - 可单独禁用繁殖功能
   - 可单独禁用寿命系统
   - 可配置自动检测间隔
   - 支持配置热重载

4. **村民寿命管理命令** ⭐ 新增
   - `/vllifespan check` - 检查没有寿命的村民数量
   - `/vllifespan add [天数]` - 为所有无寿命村民添加寿命
   - `/vllifespan list` - 列出所有无寿命村民的位置
   - 支持 Tab 补全，方便快速输入

### 🔧 技术改进

1. **代码质量**
   - 添加属性测试框架（jqwik）
   - 完整的单元测试覆盖
   - 遵循最小侵入原则

2. **性能优化**
   - 早期返回避免不必要的处理
   - 复用现有的寿命管理逻辑
   - 无额外定时任务开销

### 🎮 使用指南

**检查无寿命村民**：
```
/vllifespan check
```
显示服务器中有多少村民没有设置寿命。

**批量添加寿命**：
```
/vllifespan add        # 使用配置的默认天数
/vllifespan add 7      # 添加7天寿命
/vllifespan add 30     # 添加30天寿命
```

**查看无寿命村民位置**：
```
/vllifespan list
```
显示前10个无寿命村民的坐标，方便定位。

### 📝 升级指南

1. 备份现有配置和数据
2. 替换插件jar文件为 `Villagerlimit-2.1.3.jar`
3. 重启服务器或使用 `/vlreload`
4. 使用 `/vllifespan check` 检查现有村民
5. 使用 `/vllifespan add` 为现有村民添加寿命

### ⚠️ 注意事项

- 从2.1.2升级后，现有村民不会自动获得寿命
- 需要使用 `/vllifespan add` 命令手动添加
- 新繁殖的村民会自动获得寿命
- 命令需要 `villagerlimit.admin` 权限

---

## v2.1.2 (2025-12-31) 🎨 GUI与交互增强版

### 🎉 重大更新

#### 全新 GUI 系统
插件现在提供精美的图形界面，让玩家更直观地查看统计信息！

**1. 统计 GUI 界面**
- 使用 `/vlstats` 自动打开个人统计界面
- 显示玩家头像、总交易次数、消耗经验
- 展示最常交易的物品（前7名）
- 精美的边框和图标设计

**2. 排行榜 GUI 界面** ⭐ 新增
- 使用 `/vltop` 打开交易排行榜
- 前三名特殊材质显示（金块/铁块/铜块）
- 显示玩家名、交易次数、消耗经验
- 支持点击查看详情（未来版本）

### ✨ 新增功能

1. **完整的 Tab 补全系统**
   - `/vlstats <Tab>` - 显示在线玩家列表
   - `/vladmin <Tab>` - 显示子命令（reset/clear/info）
   - `/vlperf <Tab>` - 显示性能选项
   - 智能过滤，输入时自动匹配

2. **排行榜 PlaceholderAPI 变量**
   ```
   %villagerlimit_top_1_name%    - 第1名玩家名
   %villagerlimit_top_1_trades%  - 第1名交易次数
   %villagerlimit_top_1_exp%     - 第1名消耗经验
   %villagerlimit_top_10_name%   - 第10名玩家名
   ```
   - 支持任意排名查询
   - 可用于计分板、全息显示
   - 自动更新，实时同步

3. **玩家经验和等级变量** ⭐ 新增
   ```
   %villagerlimit_player_exp%    - 玩家当前总经验值
   %villagerlimit_player_level%  - 玩家当前等级
   ```
   - 自动计算玩家所有等级的累计经验值
   - 包含当前等级的进度经验
   - 使用 Minecraft 原版经验公式计算
   - 可用于计分板、称号系统等

4. **智能命令执行**
   - 玩家执行命令：自动打开 GUI
   - 控制台执行命令：显示文本格式
   - 兼容性更好，体验更佳

5. **新增管理命令**
   - `/vladmin info` - 查看插件信息
   - 显示版本、作者、数据库状态
   - 方便服主快速诊断

### 🐛 Bug修复

1. **修复配置重载问题** ⭐ 重要修复
   - 问题：修改交易冷却配置后执行 `/vlreload` 不生效
   - 原因：Manager类的缓存数据未清空
   - 修复：重载时自动清空所有Manager缓存
   - 影响：TradeCooldownManager、TradeLimitManager、TradeDataManager

2. **优化重载机制**
   - 添加 `TradeCooldownManager.reload()` 方法
   - 添加 `VillagerTradeListener` 访问方法
   - 重载时清空冷却记录、交易数据、次数限制
   - 新配置立即生效，无需重启服务器

### 🎨 界面优化

**统计 GUI 布局**：
```
┌─────────────────────────────┐
│  [边框]  [玩家头像]  [边框]  │
│  [交易次数] [经验] [时间]    │
│  [最常交易物品展示区]        │
│         [关闭按钮]           │
└─────────────────────────────┘
```

**排行榜 GUI 布局**：
```
┌─────────────────────────────┐
│      [排行榜标题]            │
│  [#1金] [#2银] [#3铜]        │
│  [#4-10 玩家列表]            │
│         [关闭按钮]           │
└─────────────────────────────┘
```

### 🔧 技术改进

1. **模块化 GUI 系统**
   - BaseGUI 抽象基类
   - GUIManager 统一管理
   - 易于扩展新界面

2. **事件处理优化**
   - 自动取消点击事件
   - 防止物品被拿走
   - 关闭时自动清理

3. **Tab 补全架构**
   - 所有命令实现 TabCompleter
   - 智能过滤和匹配
   - 支持多级补全

4. **配置重载优化**
   - 清空冷却缓存（cooldowns Map）
   - 清空交易数据缓存（tradeRecords Map）
   - 清空次数限制缓存（limitRecords Map）
   - 确保新配置立即生效

### 📊 PlaceholderAPI 增强

**新增变量总览**：
```
玩家统计：
- %villagerlimit_trades%
- %villagerlimit_exp_spent%
- %villagerlimit_rank%
- %villagerlimit_player_exp%    ⭐ 新增
- %villagerlimit_player_level%  ⭐ 新增

排行榜：
- %villagerlimit_top_<排名>_name%
- %villagerlimit_top_<排名>_trades%
- %villagerlimit_top_<排名>_exp%
```

**应用示例**：
```yaml
# 计分板显示 - 交易排行榜
scoreboard:
  - "&6&l交易排行榜"
  - "&e1. %villagerlimit_top_1_name% &7- &6%villagerlimit_top_1_trades%"
  - "&e2. %villagerlimit_top_2_name% &7- &6%villagerlimit_top_2_trades%"
  - "&e3. %villagerlimit_top_3_name% &7- &6%villagerlimit_top_3_trades%"
```

### 🎮 使用指南

**查看统计**：
1. 输入 `/vlstats` 打开自己的统计
2. 输入 `/vlstats <玩家>` 查看其他玩家
3. 在 GUI 中查看详细信息

**查看排行榜**：
1. 输入 `/vltop` 打开排行榜
2. 查看前10名玩家
3. 金银铜牌特殊显示

**使用变量**：
1. 安装 PlaceholderAPI
2. 在计分板/全息中使用变量
3. 自动显示实时数据

**配置重载**：
1. 修改 `config.yml` 配置文件
2. 执行 `/vlreload` 命令
3. 所有缓存自动清空，新配置立即生效
4. 无需重启服务器

### 📝 升级指南

1. 备份现有配置和数据
2. 替换插件jar文件为 `Villagerlimit-2.1.2.jar`
3. 重启服务器或使用 `/vlreload`
4. 测试 GUI 和 Tab 补全功能
5. 测试配置重载功能（修改冷却时间后重载）
6. 配置 PlaceholderAPI 变量（可选）

### ⚠️ 注意事项

- 从2.1.1升级无需清空数据
- GUI 需要玩家在线才能打开
- 控制台执行命令仍显示文本格式
- PlaceholderAPI 变量需要安装 PlaceholderAPI 插件
- 配置重载会清空所有缓存数据（冷却、交易记录等）

### 🔗 相关链接

- 完整文档：查看 README.md
- 论坛帖子：查看 FORUM_POST.md
- 配置示例：查看 config.yml
- 问题反馈：联系作者 Ti_Avanti

---

**下载地址**：`target/Villagerlimit-2.1.2.jar`

感谢使用 VillagerLimit！

---

## v2.1.1 (2025-12-30) 🎯 经验系统增强版

### 🎉 重大更新

#### 双模式经验消耗系统
插件现在支持两种经验消耗方式，服主可以根据服务器需求自由选择：

**1. LEVEL 模式（等级消耗）**
- 直接扣除玩家等级
- 简单直观，玩家容易理解
- 适合休闲服务器
- 配置示例：`cost-mode: LEVEL`，`base-cost: 5` = 扣除5级

**2. POINTS 模式（经验值消耗）** ⭐ 新增
- 扣除具体经验值点数
- 精确控制，更加灵活
- 适合硬核服务器
- 配置示例：`cost-mode: POINTS`，`base-cost: 100` = 扣除100点经验

### ✨ 新增功能

1. **经验值精确计算系统**
   - 完整实现Minecraft经验公式
   - 支持跨等级经验扣除
   - 自动重新分配等级和经验条

2. **增强的最低等级保护**
   - LEVEL模式：防止等级低于设定值
   - POINTS模式：防止经验值低于最低等级所需经验
   - 双重保护机制，确保玩家体验

3. **独立的提示消息**
   - LEVEL模式：显示所需等级和当前等级
   - POINTS模式：显示所需经验点和当前经验点
   - 中英文完整翻译

### 🔧 配置更新

```yaml
trade-control:
  exp-cost:
    enabled: true
    # 消耗模式: LEVEL(等级), POINTS(经验值)
    cost-mode: LEVEL  # 或 POINTS
    base-cost: 5      # 等级模式=5级，经验值模式=5点
    
    # 新增：经验值不足提示
    insufficient-points-message: "§c交易需要 {required} 点经验，你当前只有 {current} 点！"
```

### 🐛 Bug修复

1. **修复经验值模式缺少最低等级保护**
   - 问题：POINTS模式下可能扣除到0级以下
   - 修复：添加最低等级经验值计算和检查

2. **优化经验计算逻辑**
   - 修正getTotalExperience()方法
   - 确保经验值计算准确无误

### 📊 性能优化

- 经验计算使用高效算法
- 避免重复计算
- 线程安全的经验操作

### 🎮 使用场景

**LEVEL模式适合**：
- 休闲生存服
- 新手友好服务器
- 简单经济系统

**POINTS模式适合**：
- 硬核生存服
- 精细经济控制
- 高级RPG服务器

### 📝 升级指南

1. 备份现有配置文件
2. 替换插件jar文件
3. 在config.yml中添加 `cost-mode` 配置项
4. 使用 `/vlreload` 重载配置
5. 测试经验消耗是否正常

### ⚠️ 注意事项

- 从2.1.0升级无需清空数据
- 默认使用LEVEL模式，保持向后兼容
- POINTS模式消耗值建议设置较大（如100-500点）
- 建议先在测试服测试后再应用到正式服

### 🔗 相关链接

- 完整文档：查看 FORUM_POST.md
- 配置示例：查看 config.yml
- 问题反馈：联系作者 Ti_Avanti

---

**下载地址**：`target/Villagerlimit-2.1.1.jar`

感谢使用 VillagerLimit！
