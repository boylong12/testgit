# 调研问卷系统 — 设计规格

**日期：** 2026-11-24  
**一期范围：** 管理登录、问卷 CRUD、单选/多选/文本题、公开发布链接、发布后锁定、访客限填、统计与 CSV。

## 架构

前后端分离：`backend`（Spring Boot + PostgreSQL）、`frontend`（Vue 3）。公开 API 无鉴权；管理 API JWT。

## 核心规则

- 状态：DRAFT → PUBLISHED → CLOSED
- 发布后不可改题；`publicToken` 生成公开填答路径 `/s/{token}`
- 默认每 `visitorKey` 限填一次（可配置 `allowMultiple`）

## API

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | /api/auth/login | 管理员登录 |
| CRUD | /api/admin/surveys | 问卷管理 |
| POST | /api/admin/surveys/{id}/publish | 发布 |
| GET | /api/public/surveys/{token} | 公开问卷 |
| POST | /api/public/surveys/{token}/responses | 提交答卷 |
| GET | /api/admin/surveys/{id}/stats | 统计 |
| GET | /api/admin/surveys/{id}/export.csv | 导出 |