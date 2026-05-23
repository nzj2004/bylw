# 文件上传配置与存储说明

> 📅 更新时间：2026-04-26  
> 📌 版本：v1.2

---

## 📁 文件存储位置

### 当前存储路径

文件存储在**后端项目根目录下的 `uploads/` 文件夹**中：

```
job-recruitment-backend/
└── uploads/                    # 上传文件根目录
    ├── images/                 # 图片文件
    │   └── 2026/              # 年份文件夹
    │       └── 04/            # 月份文件夹
    │           └── 26/        # 日期文件夹
    │               ├── 张三_1730000000000.jpg
    │               ├── 李四_1730000001000.png
    │               └── 王五_1730000002000.gif
    └── pdfs/                   # PDF文件
        └── 2026/
            └── 04/
                └── 26/
                    └── 张三_1730000003000.pdf
```

### 实际路径示例

```
绝对路径：C:\Users\20927\Desktop\demo\job-recruitment-backend\uploads\images\2026\04\26\张三_1730000000000.jpg
相对路径：images/2026/04/26/张三_1730000000000.jpg
数据库存储：images/2026/04/26/张三_1730000000000.jpg
```

---

## ⚙️ 配置文件说明

### application.yml 配置项

在 `application.yml` 中新增了文件上传配置：

```yaml
# 文件上传配置
file:
  upload:
    # 上传基础路径（可以使用绝对路径或相对路径）
    base-path: ${user.dir}/uploads/
    # 图片子路径
    image-path: images/
    # PDF子路径
    pdf-path: pdfs/
    # 文件大小限制（字节）
    max-image-size: 5242880  # 5MB
    max-pdf-size: 10485760   # 10MB
```

### 配置项说明

| 配置项 | 说明 | 默认值 | 修改建议 |
|--------|------|--------|---------|
| `base-path` | 上传基础路径 | `${user.dir}/uploads/` | 可改为绝对路径 |
| `image-path` | 图片子路径 | `images/` | 一般不需修改 |
| `pdf-path` | PDF子路径 | `pdfs/` | 一般不需修改 |
| `max-image-size` | 图片大小限制 | `5242880` (5MB) | 根据需要调整 |
| `max-pdf-size` | PDF大小限制 | `10485760` (10MB) | 根据需要调整 |

---

## 🎯 自定义存储路径

### 方式1：使用相对路径（推荐）

```yaml
file:
  upload:
    base-path: ${user.dir}/uploads/  # 项目根目录下的uploads文件夹
```

### 方式2：使用绝对路径

**Windows系统：**
```yaml
file:
  upload:
    base-path: D:/file-uploads/  # 注意使用正斜杠
```

**Linux系统：**
```yaml
file:
  upload:
    base-path: /var/uploads/
```

### 方式3：使用环境变量

```yaml
file:
  upload:
    base-path: ${UPLOAD_PATH:/uploads/}  # 优先使用环境变量，否则使用默认值
```

然后在系统环境变量中设置：
```bash
# Windows
set UPLOAD_PATH=D:/my-uploads/

# Linux/Mac
export UPLOAD_PATH=/var/uploads/
```

---

## 📝 文件命名规则

### 命名格式

```
{用户名前3个字}_{时间戳}.{扩展名}
```

### 示例

| 用户名 | 原文件名 | 新文件名 | 说明 |
|--------|---------|---------|------|
| 张三 | photo.jpg | 张三_1730000000000.jpg | 中文用户名 |
| 李四王五 | image.png | 李四王_1730000001000.png | 取前3个字 |
| admin | test.gif | adm_1730000002000.gif | 英文用户名 |
| user1 | pic.webp | use_1730000003000.webp | 字母+数字 |
| 王 | avatar.jpg | 王_1730000004000.jpg | 单字用户名 |

### 时间戳说明

- 使用 `System.currentTimeMillis()` 生成
- 精确到毫秒，避免文件名冲突
- 示例：`1730000000000`

---

## 📂 目录自动生成

### 按日期分类

系统会**自动按日期创建文件夹**：

```
格式：年/月/日/
示例：2026/04/26/
```

### 自动创建机制

1. **首次上传时**：自动创建完整的目录结构
2. **跨月/跨年时**：自动创建新的月份/年份文件夹
3. **每天首次上传**：自动创建当天的日期文件夹

### 日志示例

```
2026-04-26 16:30:00 INFO  - 创建上传目录: C:\...\uploads\images\2026\04\26\
2026-04-26 16:30:00 INFO  - 图片文件保存成功: 2026/04/26/张三_1730000000000.jpg
```

---

## 🔧 配置类说明

### FileUploadConfig.java

配置类用于读取 `application.yml` 中的配置：

```java
@Data
@Component
@ConfigurationProperties(prefix = "file.upload")
public class FileUploadConfig {
    private String basePath;        // 基础路径
    private String imagePath;       // 图片子路径
    private String pdfPath;         // PDF子路径
    private Long maxImageSize;      // 图片大小限制
    private Long maxPdfSize;        // PDF大小限制
    
    // 获取完整路径的方法
    public String getFullImagePath() {
        return basePath + imagePath;
    }
    
    public String getFullPdfPath() {
        return basePath + pdfPath;
    }
}
```

---

## 🛡️ 安全措施

### 1. 文件类型验证
- ✅ Content-Type白名单验证
- ✅ 文件扩展名白名单验证
- ✅ 双重验证防止伪造

### 2. 文件大小限制
- ✅ 图片：最大5MB（可配置）
- ✅ PDF：最大10MB（可配置）

### 3. 文件名安全
- ✅ 用户名前缀（自动清理特殊字符）
- ✅ 时间戳命名（避免冲突）
- ✅ 禁止路径遍历字符

### 4. 存储安全
- ✅ 目录隔离（图片和PDF分开）
- ✅ 按日期分类（便于管理）
- ✅ 删除权限验证（防止越权）

---

## 📊 文件访问

### 数据库存储

数据库中存储的是**相对路径**：

```sql
-- resume表
avatar_url: 'images/2026/04/26/张三_1730000000000.jpg'
pdf_url: 'pdfs/2026/04/26/张三_1730000001000.pdf'

-- company_info表
logo_url: 'images/2026/04/26/公司名_1730000002000.jpg'
```

### 前端访问

前端通过完整URL访问文件：

```javascript
// 拼接方式
const fileUrl = 'http://localhost:8080/uploads/' + relativePath;

// 示例
const avatarUrl = 'http://localhost:8080/uploads/images/2026/04/26/张三_1730000000000.jpg';
```

### 静态资源映射

需要在Spring Boot中配置静态资源映射（如果还未配置）：

```java
@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Value("${file.upload.base-path}")
    private String uploadPath;
    
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + uploadPath);
    }
}
```

---

## 🔍 文件管理

### 目录结构示例

```
uploads/
├── images/
│   ├── 2026/
│   │   ├── 04/
│   │   │   ├── 25/
│   │   │   │   ├── 张三_1730000000000.jpg
│   │   │   │   └── 李四_1730000001000.png
│   │   │   └── 26/
│   │   │       ├── 王五_1730000002000.jpg
│   │   │       └── 赵六_1730000003000.gif
│   │   └── 05/
│   │       └── 01/
│   │           └── 测试_1730000004000.jpg
│   └── 2027/
│       └── 01/
│           └── 01/
│               └── 新年_1730000005000.jpg
└── pdfs/
    └── 2026/
        └── 04/
            └── 26/
                └── 张三_1730000006000.pdf
```

### 文件大小统计

可以使用以下命令统计文件大小：

**Windows (PowerShell):**
```powershell
# 统计uploads文件夹总大小
Get-ChildItem -Path "uploads" -Recurse | Measure-Object -Property Length -Sum

# 按日期查看
Get-ChildItem -Path "uploads\images\2026\04" -Recurse | Measure-Object -Property Length -Sum
```

**Linux:**
```bash
# 查看总大小
du -sh uploads/

# 查看各月份大小
du -sh uploads/images/2026/*/
```

---

## 🚀 修改配置后的操作

### 1. 修改配置文件

编辑 `application.yml`，修改相关配置项。

### 2. 重启后端服务

```bash
cd job-recruitment-backend
.\start.ps1
```

### 3. 验证配置

查看启动日志：
```
创建上传目录: C:\Users\20927\Desktop\demo\job-recruitment-backend\uploads\images\
创建上传目录: C:\Users\20927\Desktop\demo\job-recruitment-backend\uploads\pdfs\
```

### 4. 测试上传

1. 登录系统
2. 进入简历页面或企业信息页面
3. 上传图片
4. 检查文件是否保存到新路径

---

## ⚠️ 注意事项

### 1. 路径分隔符

- **Windows**: 使用 `/` 或 `\\`（推荐 `/`）
- **Linux**: 使用 `/`
- **配置文件**: 统一使用 `/`

### 2. 权限问题

- 确保上传目录有**写入权限**
- Linux系统可能需要：`chmod 755 uploads/`

### 3. 磁盘空间

- 定期检查磁盘空间
- 建议定期清理旧文件
- 可以设置定时任务自动清理

### 4. 备份策略

- 定期备份uploads文件夹
- 可以使用云存储（如OSS、S3）
- 建议异地备份

### 5. 迁移文件

如果修改了存储路径，需要：
1. 迁移已有文件到新路径
2. 更新数据库中的文件路径
3. 测试文件访问是否正常

---

## 📞 常见问题

### Q1: 如何修改上传文件大小限制？

修改 `application.yml`：
```yaml
file:
  upload:
    max-image-size: 10485760  # 改为10MB
    max-pdf-size: 20971520    # 改为20MB
```

同时修改Spring Boot的限制：
```yaml
spring:
  servlet:
    multipart:
      max-file-size: 20MB
      max-request-size: 50MB
```

### Q2: 如何更改文件名格式？

修改 `FileUploadUtil.java` 中的 `generateFilePrefix()` 方法：
```java
private String generateFilePrefix(String username) {
    // 自定义逻辑
    return username; // 使用完整用户名
}
```

### Q3: 如何关闭按日期分类？

修改 `FileUploadUtil.java` 中的 `getDatePath()` 方法：
```java
private String getDatePath() {
    return ""; // 返回空字符串，不按日期分类
}
```

### Q4: 文件上传后无法访问？

检查：
1. 静态资源映射是否配置
2. 文件路径是否正确
3. 文件是否存在
4. 权限是否正确

---

**文档版本**: v1.2  
**更新时间**: 2026-04-26  
**维护人员**: 开发团队
