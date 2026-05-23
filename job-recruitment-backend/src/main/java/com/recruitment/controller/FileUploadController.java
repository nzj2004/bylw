package com.recruitment.controller;

import com.recruitment.dto.Result;
import com.recruitment.utils.FileUploadUtil;
import com.recruitment.utils.SecurityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * 文件上传控制器
 */
@RestController
@RequestMapping("/api/upload")
public class FileUploadController {

    @Autowired
    private FileUploadUtil fileUploadUtil;

    /**
     * 上传图片（通用）
     */
    @PostMapping("/image")
    @PreAuthorize("isAuthenticated()")
    public Result<Map<String, String>> uploadImage(@RequestParam("file") MultipartFile file) {
        try {
            // 验证文件
            fileUploadUtil.validateImageFile(file);
            
            // 获取当前用户名
            String username = SecurityUtil.getCurrentUsername();
            
            // 保存图片（使用用户名生成文件名）
            String filePath = fileUploadUtil.saveImage(file, username);
            
            Map<String, String> result = new HashMap<>();
            result.put("url", "/uploads/" + filePath);
            result.put("fileName", file.getOriginalFilename());
            
            return Result.success(result);
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        } catch (IOException e) {
            return Result.error("文件上传失败：" + e.getMessage());
        }
    }

    /**
     * 上传PDF文件
     */
    @PostMapping("/pdf")
    @PreAuthorize("isAuthenticated()")
    public Result<Map<String, String>> uploadPdf(@RequestParam("file") MultipartFile file) {
        try {
            // 验证文件
            fileUploadUtil.validatePdfFile(file);
            
            // 获取当前用户名
            String username = SecurityUtil.getCurrentUsername();
            
            // 保存PDF（使用用户名生成文件名）
            String filePath = fileUploadUtil.savePdf(file, username);
            
            Map<String, String> result = new HashMap<>();
            result.put("url", "/uploads/" + filePath);
            result.put("fileName", file.getOriginalFilename());
            
            return Result.success(result);
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        } catch (IOException e) {
            return Result.error("文件上传失败：" + e.getMessage());
        }
    }

    /**
     * 删除文件
     */
    @DeleteMapping("/file")
    @PreAuthorize("isAuthenticated()")
    public Result<Boolean> deleteFile(@RequestParam String filePath) {
        try {
            boolean deleted = fileUploadUtil.deleteFile(filePath);
            if (deleted) {
                return Result.success(true);
            } else {
                return Result.error("文件删除失败");
            }
        } catch (Exception e) {
            return Result.error("文件删除失败：" + e.getMessage());
        }
    }
}
