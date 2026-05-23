package com.recruitment.utils;

import com.recruitment.config.FileUploadConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

/**
 * 文件上传工具类 - 包含完整的安全措施
 * 支持按日期分类存储和自定义文件名
 */
@Slf4j
@Component
public class FileUploadUtil {

    @Autowired
    private FileUploadConfig fileUploadConfig;

    // 允许的图片类型（白名单）
    private static final List<String> ALLOWED_IMAGE_TYPES = Arrays.asList(
        "image/jpeg", "image/jpg", "image/png", "image/gif", "image/webp"
    );
    
    // 允许的图片扩展名
    private static final List<String> ALLOWED_IMAGE_EXTENSIONS = Arrays.asList(
        ".jpg", ".jpeg", ".png", ".gif", ".webp"
    );
    
    // 允许的PDF类型
    private static final String PDF_CONTENT_TYPE = "application/pdf";

    /**
     * 验证图片文件
     */
    public void validateImageFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("文件不能为空");
        }
        
        // 1. 验证文件类型（Content-Type）
        String contentType = file.getContentType();
        if (!ALLOWED_IMAGE_TYPES.contains(contentType)) {
            throw new IllegalArgumentException("只允许上传图片文件（JPG、PNG、GIF、WEBP）");
        }
        
        // 2. 验证文件扩展名
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !hasValidExtension(originalFilename, ALLOWED_IMAGE_EXTENSIONS)) {
            throw new IllegalArgumentException("文件扩展名不正确");
        }
        
        // 3. 验证文件大小
        if (file.getSize() > fileUploadConfig.getMaxImageSize()) {
            throw new IllegalArgumentException("图片文件大小不能超过" + (fileUploadConfig.getMaxImageSize() / 1024 / 1024) + "MB");
        }
        
        // 4. 验证文件名安全性（防止路径遍历攻击）
        if (isUnsafeFilename(originalFilename)) {
            throw new IllegalArgumentException("文件名包含非法字符");
        }
        
        log.info("图片文件验证通过: {}", originalFilename);
    }
    
    /**
     * 验证PDF文件
     */
    public void validatePdfFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("文件不能为空");
        }
        
        // 1. 验证文件类型
        String contentType = file.getContentType();
        if (!PDF_CONTENT_TYPE.equals(contentType)) {
            throw new IllegalArgumentException("只允许上传PDF文件");
        }
        
        // 2. 验证文件扩展名
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !originalFilename.toLowerCase().endsWith(".pdf")) {
            throw new IllegalArgumentException("文件必须是PDF格式");
        }
        
        // 3. 验证文件大小
        if (file.getSize() > fileUploadConfig.getMaxPdfSize()) {
            throw new IllegalArgumentException("PDF文件大小不能超过" + (fileUploadConfig.getMaxPdfSize() / 1024 / 1024) + "MB");
        }
        
        // 4. 验证文件名安全性
        if (isUnsafeFilename(originalFilename)) {
            throw new IllegalArgumentException("文件名包含非法字符");
        }
        
        log.info("PDF文件验证通过: {}", originalFilename);
    }
    
    /**
     * 保存图片文件（使用默认命名：时间戳）
     */
    public String saveImage(MultipartFile file) throws IOException {
        return saveImage(file, null);
    }
    
    /**
     * 保存图片文件（自定义用户名前缀）
     * @param file 文件
     * @param username 用户名（用于生成文件名）
     */
    public String saveImage(MultipartFile file, String username) throws IOException {
        // 获取今天的日期路径：yyyy/MM/dd
        String datePath = getDatePath();
        String targetDir = fileUploadConfig.getFullImagePath() + datePath;
        
        // 创建目录
        createDirectoryIfNotExists(targetDir);
        
        // 生成文件名
        String fileName = generateImageFileName(username, file.getOriginalFilename());
        
        // 保存文件
        Path targetPath = Paths.get(targetDir + fileName);
        Files.copy(file.getInputStream(), targetPath);
        
        log.info("图片文件保存成功: {}{}", datePath, fileName);
        
        // 返回相对路径（用于数据库存储和访问）
        return fileUploadConfig.getImagePath() + datePath + fileName;
    }
    
    /**
     * 保存PDF文件
     */
    public String savePdf(MultipartFile file) throws IOException {
        return savePdf(file, null);
    }
    
    /**
     * 保存PDF文件（自定义用户名前缀）
     */
    public String savePdf(MultipartFile file, String username) throws IOException {
        // 获取今天的日期路径：yyyy/MM/dd
        String datePath = getDatePath();
        String targetDir = fileUploadConfig.getFullPdfPath() + datePath;
        
        // 创建目录
        createDirectoryIfNotExists(targetDir);
        
        // 生成文件名
        String fileName = generatePdfFileName(username, file.getOriginalFilename());
        
        // 保存文件
        Path targetPath = Paths.get(targetDir + fileName);
        Files.copy(file.getInputStream(), targetPath);
        
        log.info("PDF文件保存成功: {}{}", datePath, fileName);
        
        // 返回相对路径
        return fileUploadConfig.getPdfPath() + datePath + fileName;
    }
    
    /**
     * 生成图片文件名
     * 格式：用户名前3位_时间戳.扩展名
     */
    private String generateImageFileName(String username, String originalFilename) {
        String prefix = generateFilePrefix(username);
        String timestamp = String.valueOf(System.currentTimeMillis());
        String extension = getFileExtension(originalFilename);
        return prefix + "_" + timestamp + extension;
    }
    
    /**
     * 生成PDF文件名
     * 格式：用户名前3位_时间戳.pdf
     */
    private String generatePdfFileName(String username, String originalFilename) {
        String prefix = generateFilePrefix(username);
        String timestamp = String.valueOf(System.currentTimeMillis());
        return prefix + "_" + timestamp + ".pdf";
    }
    
    /**
     * 生成文件名前缀
     * 规则：取用户名前3个字，如果不足3个则取全部
     */
    private String generateFilePrefix(String username) {
        if (username == null || username.trim().isEmpty()) {
            // 如果没有用户名，使用"user"作为默认前缀
            return "user";
        }
        
        // 去除空格和特殊字符
        String cleanName = username.trim()
                .replaceAll("[^a-zA-Z0-9\\u4e00-\\u9fa5]", "");
        
        if (cleanName.isEmpty()) {
            return "user";
        }
        
        // 取前3个字符（支持中文）
        int length = Math.min(3, cleanName.length());
        return cleanName.substring(0, length);
    }
    
    /**
     * 获取日期路径：yyyy/MM/dd/
     */
    private String getDatePath() {
        LocalDate now = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd/");
        return now.format(formatter);
    }
    
    /**
     * 获取文件扩展名
     */
    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return ".jpg"; // 默认扩展名
        }
        return filename.substring(filename.lastIndexOf("."));
    }
    
    /**
     * 删除文件
     */
    public boolean deleteFile(String filePath) {
        try {
            // 安全检查：确保文件路径在上传目录内
            String fullPath = fileUploadConfig.getBasePath() + filePath;
            Path normalizedPath = Paths.get(fullPath).normalize();
            Path basePath = Paths.get(fileUploadConfig.getBasePath()).normalize();
            
            if (!normalizedPath.startsWith(basePath)) {
                log.warn("尝试删除上传目录外的文件: {}", filePath);
                return false;
            }
            
            File file = normalizedPath.toFile();
            if (file.exists()) {
                boolean deleted = file.delete();
                if (deleted) {
                    log.info("文件删除成功: {}", filePath);
                }
                return deleted;
            }
            return false;
        } catch (Exception e) {
            log.error("删除文件失败: {}", filePath, e);
            return false;
        }
    }
    
    /**
     * 检查文件扩展名是否合法
     */
    private boolean hasValidExtension(String filename, List<String> allowedExtensions) {
        String lowerFilename = filename.toLowerCase();
        return allowedExtensions.stream().anyMatch(lowerFilename::endsWith);
    }
    
    /**
     * 检查文件名是否安全（防止路径遍历攻击）
     */
    private boolean isUnsafeFilename(String filename) {
        // 检查是否包含路径分隔符或其他危险字符
        return filename.contains("..") || 
               filename.contains("/") || 
               filename.contains("\\") ||
               filename.contains("\0") ||
               filename.contains("<") ||
               filename.contains(">") ||
               filename.contains(":") ||
               filename.contains("\"") ||
               filename.contains("|") ||
               filename.contains("?") ||
               filename.contains("*");
    }
    
    /**
     * 创建目录（如果不存在）
     */
    private void createDirectoryIfNotExists(String dirPath) {
        File dir = new File(dirPath);
        if (!dir.exists()) {
            boolean created = dir.mkdirs();
            if (created) {
                log.info("创建上传目录: {}", dirPath);
            }
        }
    }
}
