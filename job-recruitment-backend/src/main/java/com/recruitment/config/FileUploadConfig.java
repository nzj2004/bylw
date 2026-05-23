package com.recruitment.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 文件上传配置类
 */
@Data
@Component
@ConfigurationProperties(prefix = "file.upload")
public class FileUploadConfig {

    /**
     * 上传基础路径
     */
    private String basePath = System.getProperty("user.dir") + "/uploads/";

    /**
     * 图片子路径
     */
    private String imagePath = "images/";

    /**
     * PDF子路径
     */
    private String pdfPath = "pdfs/";

    /**
     * 图片最大大小（字节）
     */
    private Long maxImageSize = 5 * 1024 * 1024L; // 5MB

    /**
     * PDF最大大小（字节）
     */
    private Long maxPdfSize = 10 * 1024 * 1024L; // 10MB

    /**
     * 获取完整的图片上传路径
     */
    public String getFullImagePath() {
        return basePath + imagePath;
    }

    /**
     * 获取完整的PDF上传路径
     */
    public String getFullPdfPath() {
        return basePath + pdfPath;
    }
}
