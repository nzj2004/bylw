# 文件上传功能更新说明

## 📝 更新时间
2026-04-24

## 🎯 新增功能

### 1. 求职者简历模块
- ✅ **个人照片上传**
  - 支持JPG、PNG、GIF、WEBP格式
  - 文件大小限制：5MB
  - 实时预览
  
- ✅ **PDF简历上传**
  - 仅支持PDF格式
  - 文件大小限制：10MB
  - 支持在线查看
  
- ✅ **保存提示优化**
  - 保存成功仅显示"保存成功"
  - 去除多余文字

### 2. 企业信息模块
- ✅ **企业Logo上传**
  - 支持JPG、PNG、GIF、WEBP格式
  - 文件大小限制：5MB
  - 实时预览
  - 仅在编辑模式下可上传

---

## 🔒 安全措施

### 文件上传安全防护

#### 1. 文件类型白名单验证
```java
// 图片类型白名单
private static final List<String> ALLOWED_IMAGE_TYPES = Arrays.asList(
    "image/jpeg", "image/jpg", "image/png", "image/gif", "image/webp"
);

// PDF类型验证
private static final String PDF_CONTENT_TYPE = "application/pdf";
```

#### 2. 文件大小限制
```java
// 图片：5MB
private static final long MAX_IMAGE_SIZE = 5 * 1024 * 1024;

// PDF：10MB  
private static final long MAX_PDF_SIZE = 10 * 1024 * 1024;
```

#### 3. 文件名安全处理
- ✅ 使用UUID生成安全文件名，防止文件名冲突
- ✅ 防止路径遍历攻击（检查`..`、`/`、`\`等危险字符）
- ✅ 过滤特殊字符：`< > : " | ? * \0`

```java
private boolean isUnsafeFilename(String filename) {
    return filename.contains("..") || 
           filename.contains("/") || 
           filename.contains("\\") ||
           filename.contains("\0") ||
           // ... 其他危险字符
}
```

#### 4. 文件存储隔离
```
uploads/
├── images/    # 图片文件
└── pdfs/      # PDF文件
```

#### 5. 删除文件安全防护
```java
// 确保文件路径在上传目录内
Path fullPath = Paths.get(UPLOAD_DIR + filePath).normalize();
if (!fullPath.startsWith(Paths.get(UPLOAD_DIR))) {
    log.warn("尝试删除上传目录外的文件: {}", filePath);
    return false;
}
```

---

## 📦 新增文件

### 后端文件

| 文件路径 | 说明 |
|---------|------|
| `controller/FileUploadController.java` | 文件上传控制器 |
| `utils/FileUploadUtil.java` | 文件上传工具类（含安全验证） |
| `service/ResumeService.java` | 简历服务接口 |
| `service/impl/ResumeServiceImpl.java` | 简历服务实现 |
| `controller/ResumeController.java` | 简历控制器 |
| `config/SecurityConfig.java` | 添加静态资源访问配置 |
| `entity/Resume.java` | 添加avatarUrl和pdfUrl字段 |
| `db/add_resume_fields.sql` | 数据库更新脚本 |

### 前端文件

| 文件路径 | 说明 |
|---------|------|
| `api/upload.js` | 文件上传API |
| `api/resume.js` | 简历API |
| `views/user/Resume.vue` | 更新简历页面 |
| `views/company/Info.vue` | 更新企业信息页面 |

---

## 🔧 数据库更新

### 执行SQL脚本

```sql
-- 为简历表添加头像和PDF字段
ALTER TABLE resume 
ADD COLUMN avatar_url VARCHAR(200) COMMENT '个人照片URL' AFTER attachment_url,
ADD COLUMN pdf_url VARCHAR(200) COMMENT 'PDF简历URL' AFTER avatar_url;
```

**注意**：企业表的`logo_url`字段已存在，无需更新。

---

## 🚀 使用说明

### 求职者上传简历照片和PDF

1. 登录求职者账号
2. 进入"我的简历"页面
3. **上传个人照片**：
   - 点击照片区域
   - 选择图片文件
   - 自动上传并显示
4. **上传PDF简历**：
   - 点击"上传PDF简历"按钮
   - 选择PDF文件
   - 上传成功后显示"查看已上传的PDF简历"链接
5. 点击"保存简历"按钮
6. 提示"保存成功"

### 企业上传Logo

1. 登录企业账号
2. 进入"企业信息"页面
3. 点击"修改"按钮进入编辑模式
4. 点击Logo区域上传企业Logo
5. 点击"保存"按钮保存企业信息

---

## ⚠️ 重要提示

### 重启后端服务

更新代码后需要重启后端服务：

```bash
# 停止旧进程
netstat -ano | findstr :8080
taskkill /F /PID <进程ID>

# 启动新服务
cd job-recruitment-backend
.\start.ps1
```

### 前端热更新

前端支持热更新，保存文件后自动刷新，无需手动重启。

---

## 🛡️ 安全特性总结

| 安全措施 | 说明 | 状态 |
|---------|------|------|
| 文件类型白名单 | 仅允许指定格式 | ✅ |
| 文件大小限制 | 图片5MB，PDF10MB | ✅ |
| 文件名UUID化 | 防止冲突和注入 | ✅ |
| 路径遍历防护 | 检查危险字符 | ✅ |
| 存储目录隔离 | 图片和PDF分开存储 | ✅ |
| 删除权限验证 | 仅能删除上传目录内文件 | ✅ |
| Content-Type验证 | 验证文件MIME类型 | ✅ |
| 扩展名验证 | 双重验证文件类型 | ✅ |

---

## 📋 API接口

### 文件上传

```
POST /api/upload/image     # 上传图片
POST /api/upload/pdf       # 上传PDF
DELETE /api/upload/file    # 删除文件
```

### 简历管理

```
GET  /api/user/resume/info    # 获取简历
POST /api/user/resume/info    # 保存简历
```

---

## ✨ 后续优化建议

1. **图片压缩**：上传前压缩图片，减少存储空间
2. **CDN加速**：使用CDN分发静态文件
3. **病毒扫描**：对上传文件进行病毒扫描
4. **水印处理**：为企业Logo添加水印
5. **缩略图生成**：自动生成图片缩略图
6. **存储优化**：集成OSS对象存储服务
