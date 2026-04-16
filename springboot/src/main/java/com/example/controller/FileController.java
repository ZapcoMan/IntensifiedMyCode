package com.example.controller;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.lang.Dict;
import com.example.annotation.AuditLogRecord;
import com.example.common.R;
import com.example.config.FileUploadConfig;
import com.example.exception.CustomerException;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*
处理文件上传下载的接口
*/
@RestController
@RequestMapping("/files")
public class FileController {

    @Autowired
    private FileUploadConfig fileUploadConfig;
    private static final Log log = LogFactory.getLog(FileController.class);

    // 允许的文件扩展名白名单
    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList(
            ".jpg", ".jpeg", ".png", ".gif", ".bmp", ".webp",  // 图片
            ".pdf", ".doc", ".docx", ".xls", ".xlsx", ".ppt", ".pptx",  // 文档
            ".txt", ".csv", ".zip", ".rar", ".7z"  // 其他
    );

    // 最大文件大小：10MB
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024;

    /**
     * 文件上传（带安全校验）
     */
    @Operation(summary = "文件上传")
    @AuditLogRecord(action = "文件上传", resource = "文件")
    @PostMapping("/upload")
    public R upload(@RequestParam("file") MultipartFile file) throws Exception {
        // 1. 检查文件是否为空
        if (file == null || file.isEmpty()) {
            throw new CustomerException("500", "上传文件不能为空");
        }

        // 2. 检查文件大小
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new CustomerException("500", "文件大小不能超过10MB");
        }

        // 3. 获取原始文件名和扩展名
        String originalFileName = file.getOriginalFilename();
        if (originalFileName == null || originalFileName.isEmpty()) {
            throw new CustomerException("500", "文件名不能为空");
        }

        // 4. 校验文件扩展名（白名单）
        String fileExtension = "";
        if (originalFileName.contains(".")) {
            fileExtension = originalFileName.substring(originalFileName.lastIndexOf(".")).toLowerCase();
        } else {
            throw new CustomerException("500", "文件必须有扩展名");
        }

        if (!ALLOWED_EXTENSIONS.contains(fileExtension)) {
            throw new CustomerException("500", 
                "不支持的文件类型: " + fileExtension + "。允许的类型: " + String.join(", ", ALLOWED_EXTENSIONS));
        }

        // 5. 校验 Content-Type（防止 MIME 类型伪造）
        String contentType = file.getContentType();
        if (contentType == null || !isValidContentType(contentType, fileExtension)) {
            log.warn("可疑的 Content-Type: " + contentType + ", 文件: " + originalFileName);
            // 注意：这里不直接拒绝，因为某些浏览器可能发送错误的 Content-Type
            // 但会记录日志供后续审计
        }

        // 6. 找到文件的位置
        String filePath = fileUploadConfig.getFilePath();
        if (!FileUtil.isDirectory(filePath)) {
            FileUtil.mkdir(filePath);
        }

        // 7. 生成安全的文件名（使用时间戳 + 随机数）
        String fileName = System.currentTimeMillis() + "_" + (int)(Math.random() * 10000) + fileExtension;

        // 8. 写入文件
        byte[] bytes = file.getBytes();
        FileUtil.writeBytes(bytes, filePath + fileName);
        
        log.info("文件上传成功: " + fileName + ", 大小: " + bytes.length + " bytes");

        String url = fileUploadConfig.getBaseUrl() + "/files/download/" + fileName;
        return R.success(url);
    }

    /**
     * 校验 Content-Type 是否与文件扩展名匹配
     */
    private boolean isValidContentType(String contentType, String extension) {
        switch (extension) {
            case ".jpg":
            case ".jpeg":
                return contentType.startsWith("image/jpeg");
            case ".png":
                return contentType.startsWith("image/png");
            case ".gif":
                return contentType.startsWith("image/gif");
            case ".pdf":
                return contentType.equals("application/pdf");
            case ".doc":
                return contentType.equals("application/msword");
            case ".docx":
                return contentType.equals("application/vnd.openxmlformats-officedocument.wordprocessingml.document");
            case ".xls":
                return contentType.equals("application/vnd.ms-excel");
            case ".xlsx":
                return contentType.equals("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            default:
                return true; // 其他类型不做严格校验
        }
    }

    /**
     * 文件下载
     * 下载路径："http://localhost:9999/files/download/404.jpg"
     */
    @Operation(summary = "文件下载")
    @AuditLogRecord(action = "文件下载", resource = "文件")
    @GetMapping("/download/{fileName}")
    public R download(@PathVariable String fileName, HttpServletResponse response) throws Exception {
        // 找到文件的位置
        String filePath = fileUploadConfig.getFilePath();  // 获取配置的文件路径
        String realPath = filePath + fileName;  //  D:\IdeaProjects\mycode\files\xxx.jpg
        boolean exist = FileUtil.exist(realPath);
        if (!exist) {
            log.error("文件不存在: " + realPath);
            return R.error("文件不存在");
        }
        // 读取文件的字节流
        byte[] bytes = FileUtil.readBytes(realPath);
        ServletOutputStream os = response.getOutputStream();
        // 设置响应头
        response.setContentType("application/octet-stream");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");
        // 输出流对象把文件写出到客户端
        os.write(bytes);
        os.flush();
        os.close();
        return R.ok();
    }


    /**
     * wang-editor编辑器文件上传接口（带安全校验）
     */
    @Operation(summary = "wang-editor编辑器文件上传接口")
    @AuditLogRecord(action = "wang-editor编辑器文件上传接口", resource = "文件上传")
    @PostMapping("/wang/upload")
    public R wangEditorUpload(MultipartFile file) {
        try {
            // 1. 检查文件是否为空
            if (file == null || file.isEmpty()) {
                throw new CustomerException("500", "上传文件不能为空");
            }

            // 2. 检查文件大小
            if (file.getSize() > MAX_FILE_SIZE) {
                throw new CustomerException("500", "文件大小不能超过10MB");
            }

            // 3. 获取原始文件名和扩展名
            String fileName = file.getOriginalFilename();
            if (fileName == null || fileName.isEmpty()) {
                throw new CustomerException("500", "文件名不能为空");
            }

            // 4. 校验文件扩展名（白名单）
            String fileExtension = "";
            if (fileName.contains(".")) {
                fileExtension = fileName.substring(fileName.lastIndexOf(".")).toLowerCase();
            } else {
                throw new CustomerException("500", "文件必须有扩展名");
            }

            if (!ALLOWED_EXTENSIONS.contains(fileExtension)) {
                throw new CustomerException("500", 
                    "不支持的文件类型: " + fileExtension);
            }

            // 5. 生成安全的文件名
            String flag = System.currentTimeMillis() + "_" + (int)(Math.random() * 10000);
            String newFileName = flag + fileExtension;

            // 6. 写入文件
            String filePath = fileUploadConfig.getFilePath();
            FileUtil.writeBytes(file.getBytes(), filePath + newFileName);
            
            log.info("wang-editor 文件上传成功: " + newFileName);

            String http = fileUploadConfig.getBaseUrl() + "/files/download/";
            Map<String, Object> resMap = new HashMap<>();
            resMap.put("errno", 0);
            resMap.put("data", CollUtil.newArrayList(Dict.create().set("url", http + newFileName)));
            return R.success(resMap);
        } catch (CustomerException e) {
            log.error("wang-editor 文件上传失败: " + e.getMsg());
            throw e;
        } catch (Exception e) {
            log.error("wang-editor 文件上传失败", e);
            throw new CustomerException("500", "文件上传失败: " + e.getMessage());
        }
    }

}