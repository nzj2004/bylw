# 毕业生招聘信息发布与管理系统 - 功能总结 v1.1

> 📅 版本：1.1  
> 📅 更新日期：2026-04-26  
> 📊 状态：生产就绪

---

## 🎯 项目概述

基于 **Vue 3 + Spring Boot 3.1.6 + MySQL 8.0** 的毕业生招聘信息管理系统，支持**四角色权限管理**：管理员、运营、企业、求职者。

---

## 🚀 核心功能模块

### 1️⃣ 用户认证模块
- ✅ 用户注册（求职者/企业）
- ✅ 用户登录（弹窗模式，无需跳转）
- ✅ JWT Token无状态认证
- ✅ 基于角色的访问控制（RBAC）
- ✅ 密码BCrypt加密

### 2️⃣ 管理员功能（admin）
| 功能模块 | 主要功能 |
|---------|---------|
| **用户管理** | 增删改查、启用/禁用、重置密码（用户名+123456） |
| **职位管理** | 查看所有职位、编辑、删除 |
| **批量发布** | Excel导入、批量导入招聘信息 |
| **操作日志** | 多条件筛选（操作人/类型/模块/状态/时间范围） |
| **数据看板** | 系统统计数据、可视化展示 |

### 3️⃣ 运营功能（operator）
| 功能模块 | 主要功能 |
|---------|---------|
| **企业审核** | 状态筛选、查看详情、审核通过/拒绝 |
| **职位审核** | 状态筛选、批量审核、单个审核、分页显示 |
| **职位管理** | 查看所有已发布职位、搜索筛选 |

### 4️⃣ 企业功能（company）
| 功能模块 | 主要功能 |
|---------|---------|
| **企业信息** | 编辑信息、**上传Logo**、提交审核、查看审核状态 |
| **职位管理** | 发布职位、编辑、删除、查看状态 |
| **应聘管理** | 查看投递列表、简历详情、筛选应聘者 |

### 5️⃣ 求职者功能（user）
| 功能模块 | 主要功能 |
|---------|---------|
| **职位浏览** | 首页推荐、列表分页、详情查看 |
| **职位搜索** | 关键词搜索、多条件筛选、搜索历史 |
| **简历管理** | 编辑简历、**上传个人照片**、**上传PDF简历** |
| **投递管理** | 查看投递记录、投递状态、企业反馈 |
| **职位收藏** | 收藏/取消、收藏列表、直接投递 |

### 6️⃣ 系统功能
- ✅ 操作日志自动记录（AOP切面）
- ✅ 时间字段自动填充（MetaObjectHandler）
- ✅ 文件上传安全管理
- ✅ 接口级权限保护

---

## 🆕 v1.1 新增功能

### 📸 文件上传功能（重要）

#### 求职者简历
- ✅ **个人照片上传**
  - 支持格式：JPG、PNG、GIF、WEBP
  - 文件大小：最大5MB
  - 实时预览
  
- ✅ **PDF简历上传**
  - 支持格式：PDF
  - 文件大小：最大10MB
  - 在线查看

#### 企业信息
- ✅ **企业Logo上传**
  - 支持格式：JPG、PNG、GIF、WEBP
  - 文件大小：最大5MB
  - 编辑模式下可上传

#### 🔒 安全防护措施
```
✅ 文件类型白名单验证（Content-Type + 扩展名）
✅ 文件大小限制（图片5MB，PDF10MB）
✅ UUID安全文件名（防止冲突和注入）
✅ 路径遍历攻击防护（过滤危险字符）
✅ 存储目录隔离（images/ 和 pdfs/）
✅ 删除权限验证（限制在上传目录内）
✅ 详细日志记录
```

### 🎨 界面优化
- ✅ 保存成功提示优化（仅显示"保存成功"）
- ✅ 职位审核状态筛选按钮组（与企业审核一致）
- ✅ 企业审核完善（分页、详情、状态筛选）
- ✅ 职位审核完善（批量操作、分页）

### 🔧 技术优化
- ✅ MyBatis-Plus时间字段自动填充
- ✅ 运营端真实API对接（非演示功能）
- ✅ 企业审核完整流程实现

---

## 🛠️ 技术栈

### 后端
| 技术 | 版本 | 用途 |
|------|------|------|
| Spring Boot | 3.1.6 | 核心框架 |
| Spring Security | 6.1.5 | 安全框架 |
| JWT | 0.12.3 | 身份认证 |
| MyBatis-Plus | 3.5.7 | ORM框架 |
| MySQL | 8.0.18 | 数据库 |
| Lombok | 1.18.30 | 代码简化 |

### 前端
| 技术 | 版本 | 用途 |
|------|------|------|
| Vue | 3.3.11 | 前端框架 |
| Vite | 5.0.8 | 构建工具 |
| Element Plus | 2.5.1 | UI组件库 |
| Pinia | 2.1.7 | 状态管理 |
| Vue Router | 4.2.5 | 路由管理 |
| Axios | 1.6.5 | HTTP客户端 |

---

## 📁 项目结构

```
demo/
├── job-recruitment-backend/          # 后端（Spring Boot）
│   ├── controller/                   # 9个控制器
│   │   ├── AuthController            # 认证
│   │   ├── UserController            # 用户管理
│   │   ├── JobController             # 职位管理
│   │   ├── JobFavoriteController     # 收藏管理
│   │   ├── OperationLogController    # 日志管理
│   │   ├── StatisticsController      # 数据统计
│   │   ├── OperatorController        # 运营端 ⭐新增
│   │   ├── FileUploadController      # 文件上传 ⭐新增
│   │   └── ResumeController          # 简历管理 ⭐新增
│   ├── service/                      # 6个服务
│   ├── mapper/                       # 8个Mapper
│   ├── entity/                       # 8个实体
│   ├── config/                       # 3个配置类
│   ├── security/                     # 安全相关
│   └── utils/                        # 工具类（含FileUploadUtil）
│
├── job-recruitment-frontend/         # 前端（Vue 3）
│   └── src/
│       ├── api/                      # 9个API模块
│       │   ├── auth.js               # 认证API
│       │   ├── user.js               # 用户API
│       │   ├── job.js                # 职位API
│       │   ├── favorite.js           # 收藏API
│       │   ├── log.js                # 日志API
│       │   ├── statistics.js         # 统计API
│       │   ├── company.js            # 企业审核 ⭐新增
│       │   ├── upload.js             # 文件上传 ⭐新增
│       │   └── resume.js             # 简历 ⭐新增
│       └── views/                    # 16个页面
│           ├── admin/                # 5个管理端页面
│           ├── operator/             # 2个运营端页面
│           ├── company/              # 3个企业端页面
│           └── user/                 # 6个求职者端页面
│
└── README.md                         # 项目文档
```

---

## 🗄️ 数据库设计

### 表概览（8张表）

| 表名 | 说明 | 关键字段 |
|------|------|---------|
| sys_user | 用户表 | username, password, role, status |
| company_info | 企业信息表 | company_name, logo_url, status |
| job_info | 职位信息表 | title, status, reject_reason |
| resume | 简历表 | **avatar_url, pdf_url** ⭐新增 |
| application | 投递记录表 | job_id, resume_id, status |
| sys_operation_log | 操作日志表 | user_id, operation_type, status |
| job_favorite | 职位收藏表 | user_id, job_id |

### 数据库特性
- ✅ 逻辑删除（deleted字段）
- ✅ 时间自动填充（create_time, update_time）
- ✅ 外键约束
- ✅ 索引优化

---

## 🔐 权限设计

### 角色定义

| 角色 | 编码 | 说明 |
|------|------|------|
| 管理员 | ADMIN | 系统管理、用户管理 |
| 运营 | OPERATOR | 企业审核、职位审核 |
| 企业 | COMPANY | 发布职位、管理应聘 |
| 求职者 | USER | 浏览职位、投递简历 |

### 权限控制
- ✅ 前端路由拦截
- ✅ 后端接口鉴权（@PreAuthorize）
- ✅ 数据权限隔离
- ✅ JWT Token验证

---

## 🚦 快速启动

### 环境要求
- JDK 17+
- Node.js 18+
- MySQL 8.0+

### 启动后端
```bash
cd job-recruitment-backend
.\start.ps1
# 访问：http://localhost:8080
```

### 启动前端
```bash
cd job-recruitment-frontend
npm run dev
# 访问：http://localhost:3000
```

### 数据库初始化
```bash
# 1. 执行基础脚本
mysql -u root -p < src/main/resources/db/schema.sql

# 2. 执行更新脚本（v1.1新增）
mysql -u root -p < src/main/resources/db/add_resume_fields.sql
```

---

## 📊 项目统计

| 指标 | 数量 |
|------|------|
| 后端控制器 | 9个 |
| 服务接口 | 6个 |
| 前端页面 | 16个 |
| API模块 | 9个 |
| 数据库表 | 8张 |
| 代码行数 | ~15,000+ |

---

## ⚠️ 重要提示

### 数据库更新
v1.1版本新增了resume表的字段，**必须执行更新脚本**：
```sql
ALTER TABLE resume 
ADD COLUMN avatar_url VARCHAR(200) COMMENT '个人照片URL',
ADD COLUMN pdf_url VARCHAR(200) COMMENT 'PDF简历URL';
```

### 文件上传目录
系统会自动创建以下目录：
```
uploads/
├── images/    # 图片文件
└── pdfs/      # PDF文件
```

---

## 📚 相关文档

- [README.md](./README.md) - 完整项目文档
- [FILE_UPLOAD_UPDATE.md](./FILE_UPLOAD_UPDATE.md) - 文件上传功能详细说明
- [README_v1.1_项目验证报告.md](./README_v1.1_项目验证报告.md) - 项目一致性检查报告

---

## ✅ 版本检查清单

### 代码与文档一致性：85%

**已验证一致：**
- ✅ 技术栈版本
- ✅ 核心功能模块
- ✅ 权限系统设计
- ✅ 数据库表结构（大部分）
- ✅ 项目目录结构

**需要更新文档：**
- ⚠️ 控制器列表（缺3个）
- ⚠️ 服务层列表（缺1个）
- ⚠️ API模块列表（缺3个）
- ⚠️ 文件上传功能说明
- ⚠️ resume表字段说明

**详细说明请参考：** [README_v1.1_项目验证报告.md](./README_v1.1_项目验证报告.md)

---

## 🎉 总结

v1.1版本在原有基础上增加了：
1. **完整的文件上传功能**（含安全防护）
2. **简历管理增强**（照片+PDF）
3. **企业Logo上传**
4. **运营端功能完善**
5. **用户体验优化**

系统现已具备**生产环境部署条件**，功能完整、安全可靠！

---

**版本**：v1.1  
**更新日期**：2026-04-26  
**维护状态**：✅ 活跃维护
