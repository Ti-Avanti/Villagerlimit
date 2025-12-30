# VillagerLimit

<div align="center">

![Version](https://img.shields.io/badge/version-2.1.1-blue)
![Minecraft](https://img.shields.io/badge/minecraft-1.8--1.21.4-green)
![Java](https://img.shields.io/badge/java-21-orange)
![License](https://img.shields.io/badge/license-MIT-yellow)

一个功能强大的 Minecraft Paper 插件，用于管理村民数量、交易限制和寿命系统

[功能特性](#功能特性) • [安装](#安装) • [配置](#配置) • [命令](#命令) • [更新日志](#更新日志)

</div>

---

## 📖 简介

VillagerLimit 是一款专为生存服务器设计的村民管理插件，旨在解决村民泛滥、交易刷物品导致的服务器经济崩溃问题。通过灵活的配置系统，服主可以精确控制村民数量、交易行为，同时保持游戏平衡性。

## ✨ 功能特性

### 🏘️ 村民数量控制
- 按区块范围限制村民数量
- 禁止村民自然生成
- 允许通过治愈僵尸村民获得（受数量限制）
- 一键清除所有村民命令

### 💰 交易限制系统
- **双模式经验消耗** - 支持等级消耗和经验值消耗
- **成本递增系统** - 重复交易成本递增，支持倍率/固定/阶梯三种模式
- **交易冷却系统** - 交易后进入冷却期，可按职业/物品配置
- **次数限制系统** - 每日/每周/每月交易次数限制
- **经济平衡机制** - 高价值物品需额外消耗绿宝石

### ⏰ 村民寿命系统
- 治愈获得的村民自动设置寿命
- 头顶实时显示剩余时间
- 版本自适应显示（1.19.4+ 使用 TextDisplay，低版本使用 ArmorStand）
- 寿命到期自动移除
- 可配置通知范围

### 👥 权限组差异化
- 支持自定义权限组
- 可配置经验消耗倍率
- 可配置冷却时间倍率
- 可配置每日交易次数加成

### ⚡ 性能优化
- 智能缓存系统
- 异步任务处理
- 村民区块追踪优化
- 内存自动管理
- 性能实时监控

### 💾 数据持久化
- SQLite 数据库存储
- 重启后数据不丢失
- 自动保存机制

### 🌍 多语言支持
- 完整的中英文翻译
- 所有消息可自定义
- 易于扩展其他语言

### 🖥️ GUI 界面系统
- 精美的统计 GUI 界面
- 交易排行榜 GUI
- 点击式操作，简单直观

## 📦 安装

### 要求
- **服务器**: Paper 1.8+ (推荐 1.21.4)
- **Java**: 21+
- **可选**: PlaceholderAPI

### 步骤
1. 下载最新版本的 `Villagerlimit-2.1.1.jar`
2. 将 jar 文件放入服务器的 `plugins` 文件夹
3. 重启服务器
4. 编辑 `plugins/Villagerlimit/config.yml` 配置文件
5. 使用 `/vlreload` 重载配置

## ⚙️ 配置

详细配置请查看 [config.yml](src/main/resources/config.yml)

### 快速配置示例

```yaml
# 村民寿命设置
villager-lifespan:
  enabled: true
  days: 7
  notify-range: 16

# 交易经验消耗
trade-control:
  exp-cost:
    enabled: true
    cost-mode: LEVEL  # LEVEL 或 POINTS
    base-cost: 5
```

## 🎮 命令

| 命令 | 描述 | 权限 |
|------|------|------|
| `/killvillagers` | 移除所有已加载的村民 | `villagerlimit.kill` |
| `/vlreload` | 重载插件配置 | `villagerlimit.admin` |
| `/vlstats [玩家]` | 查看交易统计 | `villagerlimit.stats` |
| `/vltop` | 查看交易排行榜 | `villagerlimit.top` |
| `/vladmin <reset\|clear>` | 管理命令 | `villagerlimit.admin` |
| `/vlperf [选项]` | 查看性能统计 | `villagerlimit.admin` |

## 📊 PlaceholderAPI 变量

```
%villagerlimit_trades% - 玩家总交易次数
%villagerlimit_exp_spent% - 玩家总消耗经验
%villagerlimit_rank% - 玩家排名
%villagerlimit_group% - 玩家权限组
%villagerlimit_daily_limit% - 每日交易限制
%villagerlimit_daily_used% - 今日已用次数
%villagerlimit_daily_remaining% - 今日剩余次数
```

## 📝 更新日志

### v2.1.1 (2025-12-30)
- ✨ 新增村民寿命系统
- ✨ 新增版本自适应显示（TextDisplay/ArmorStand）
- ✨ 新增双模式经验消耗（LEVEL/POINTS）
- ✨ 新增可配置通知范围
- 🐛 修复经验值模式缺少最低等级保护
- 🐛 优化经验计算逻辑

### v2.1.0 (2025-12-30)
- ✨ 新增性能优化系统
- ✨ 新增缓存管理器
- ✨ 新增异步任务管理
- ✨ 新增性能监控命令

[查看完整更新日志](UPDATE_LOG.md)

## 🛠️ 技术栈

- **核心**: Paper API 1.21.4
- **语言**: Java 21
- **数据库**: SQLite
- **构建工具**: Maven
- **架构**: 模块化设计

## 📄 许可证

本项目采用 [MIT License](LICENSE) 许可证

## 👤 作者

**Ti_Avanti**

## 🤝 贡献

欢迎提交 Issue 和 Pull Request！

## 📮 反馈与支持

如果您在使用过程中遇到问题或有建议，欢迎：
- 提交 [Issue](../../issues)
- 查看 [Wiki](../../wiki)
- 阅读 [论坛帖子](FORUM_POST.md)

---

<div align="center">

**如果觉得这个项目不错，请给个 ⭐ Star 支持一下！**

Made with ❤️ by Ti_Avanti

</div>
