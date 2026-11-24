# 调研问卷系统

一期 MVP：管理端登录、问卷编辑与发布、公开链接填答、基础统计与 CSV 导出。

## 技术栈

- 后端：Spring Boot 3、PostgreSQL、Flyway、JWT
- 前端：Vue 3 + Vite + TypeScript

## 本地运行

```bash
# 数据库与 API
docker compose up -d db
cd backend && mvn spring-boot:run

# 前端（另开终端）
cd frontend && npm install && npm run dev
```

- 管理端：http://localhost:5173 （默认账号 `admin` / `admin123`）
- API：http://localhost:8080

也可一键：`docker compose up --build`

## 目录

- `backend/` REST API
- `frontend/` 管理台与 H5 填答页
- `docs/superpowers/` 设计与实施文档