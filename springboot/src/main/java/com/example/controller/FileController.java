package com.example.controller;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.lang.Dict;
import com.example.annotation.AuditLogRecord;
import com.example.common.R;
import com.example.config.FileUploadConfig;
import com.example.exception.CustomerException;
import io.swagger.annotations.ApiOperation;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
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

    /**
     * 文件上传
     */
    @ApiOperation("文件上传")
    @AuditLogRecord(action = "文件上传", resource = "文件")
    @PostMapping("/upload")
    public R upload(@RequestParam("file") MultipartFile file) throws Exception {
        // 找到文件的位置
        String filePath = fileUploadConfig.getFilePath();
        if (!FileUtil.isDirectory(filePath)) {
            FileUtil.mkdir(filePath);
        }
        byte[] bytes = file.getBytes();
        String originalFileName = file.getOriginalFilename();
        String fileExtension = "";
        if (originalFileName != null && originalFileName.contains(".")) {
            fileExtension = originalFileName.substring(originalFileName.lastIndexOf("."));
        }
        // 使用纯时间戳作为文件名
        String fileName = System.currentTimeMillis() + fileExtension;
        // 写入文件
        FileUtil.writeBytes(bytes, filePath + fileName);
        String url = fileUploadConfig.getBaseUrl() + "/files/download/" + fileName;
        return R.success(url);
    }

    /**
     * 文件下载
     * 下载路径："http://localhost:9999/files/download/404.jpg"
     */
    @ApiOperation("文件下载")
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
     * wang-editor编辑器文件上传接口
     */
    @ApiOperation("wang-editor编辑器文件上传接口")
    @AuditLogRecord(action = "wang-editor编辑器文件上传接口", resource = "文件上传")
    @PostMapping("/wang/upload")
    public R wangEditorUpload(MultipartFile file) {
        String flag = System.currentTimeMillis() + "";
        String fileName = file.getOriginalFilename();
        String fileExtension = "";
        if (fileName != null && fileName.contains(".")) {
            fileExtension = fileName.substring(fileName.lastIndexOf("."));
        }
        String newFileName = flag + fileExtension;
        try {
            String filePath = fileUploadConfig.getFilePath();
            // 文件存储形式：时间戳
            FileUtil.writeBytes(file.getBytes(), filePath + newFileName);
            System.out.println(newFileName + "--上传成功");
            Thread.sleep(1L);
        } catch (Exception e) {
            System.err.println(newFileName + "--文件上传失败");
        }
        String http = fileUploadConfig.getBaseUrl() + "/files/download/";
        Map<String, Object> resMap = new HashMap<>();
        // wangEditor上传图片成功后， 需要返回的参数
        resMap.put("errno", 0);
        resMap.put("data", CollUtil.newArrayList(Dict.create().set("url", http + newFileName)));
        return R.success(resMap);
    }

}