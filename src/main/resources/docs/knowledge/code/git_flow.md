---
title: Git 分支管理策略
version: 1.0
tags: [git, version-control]
author: AI Code Helper
create_time: 2023-10-20
---

# Git 分支管理策略

良好的分支管理是团队协作的基础。

## 1. Git Flow
经典的 Git 工作流：
- **master**: 主分支，用于发布。
- **develop**: 开发分支。
- **feature/**: 功能分支，从 develop 检出。
- **release/**: 发布分支，用于预发布测试。
- **hotfix/**: 紧急修复分支，从 master 检出。

## 2. GitHub Flow
适用于持续部署的简单工作流：
- 只有一个长期存在的 **main** 分支。
- 所有新功能都在 **feature** 分支开发。
- 提交 Pull Request (PR) 进行代码审查。
- 通过审查后合并回 main 并部署。
