package com.xd11cc.single.controller;

import com.xd11cc.single.entity.base.FileResultVO;
import com.xd11cc.single.entity.base.ResponseVO;
import com.xd11cc.single.service.FileContent;
import com.xd11cc.single.service.FileService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * @author xd11cc
 * @date 2026-05-08 10:19:22
 * @description 文件管理。传输层细节（响应流、响应头）留在这一层，文件服务只处理内容与元数据
 */
@RestController
@RequestMapping("/file")
@Api(tags = "文件管理")
public class FileController {

    private static final int STREAM_BUFFER_SIZE = 8192;

    private final FileService fileService;

    public FileController(FileService fileService) {
        this.fileService = fileService;
    }

    @PostMapping("/upload")
    @ApiOperation("上传文件")
    public ResponseVO<FileResultVO> upload(@RequestParam("file") MultipartFile file) {
        return ResponseVO.success(fileService.upload(file));
    }

    @PostMapping("/upload/batch")
    @ApiOperation("批量上传文件")
    public ResponseVO<List<FileResultVO>> uploadBatch(@RequestParam("file") MultipartFile[] files) {
        List<FileResultVO> list = new ArrayList<>();
        if (files != null) {
            for (MultipartFile file : files) {
                list.add(fileService.upload(file));
            }
        }
        return ResponseVO.success(list);
    }

    @PostMapping("/batchDelete")
    @ApiOperation("批量删除文件，返回删除失败的 fileId")
    public ResponseVO<List<String>> batchDelete(@RequestParam("fileIds") List<String> fileIds) {
        return ResponseVO.success(fileService.batchDelete(fileIds));
    }

    @PostMapping("/download")
    @ApiOperation("下载文件")
    public void download(@RequestParam("fileId") String fileId, HttpServletResponse response)
            throws IOException {
        try (FileContent content = fileService.open(fileId)) {
            response.setContentType(content.getContentType());
            // RFC 5987：中文/空格文件名用 filename* 传，避免 URLEncoder 把空格变成 +
            String encoded = URLEncoder.encode(content.getOriginalName(), StandardCharsets.UTF_8.name())
                    .replace("+", "%20");
            response.setHeader("Content-Disposition",
                    "attachment; filename*=UTF-8''" + encoded);

            try (InputStream in = content.getStream();
                 OutputStream out = response.getOutputStream()) {
                byte[] buffer = new byte[STREAM_BUFFER_SIZE];
                int read;
                while ((read = in.read(buffer)) != -1) {
                    out.write(buffer, 0, read);
                }
                out.flush();
            }
        }
    }
}
