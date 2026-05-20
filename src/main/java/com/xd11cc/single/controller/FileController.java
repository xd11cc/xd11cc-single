package com.xd11cc.single.controller;

import com.xd11cc.single.entity.base.FileResultVO;
import com.xd11cc.single.entity.base.ResponseVO;
import com.xd11cc.single.service.FileService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;

/**
 * @author xd11cc
 * @date 2026-05-08 10:19:22
 * @description
 */
@RestController
@RequestMapping("/file")
@Api(tags = "文件管理")
public class FileController {

    @Autowired
    private FileService fileService;

    @PostMapping("/upload")
    @ApiOperation("上传文件")
    public ResponseVO<FileResultVO> upload(@RequestParam("file") MultipartFile file) {
        String fileId = fileService.upload(file);
        FileResultVO fileResultVO = FileResultVO.builder()
                .fileId(fileId)
                .previewUrl(fileService.getPreviewUrl(fileId))
                .build();
        return ResponseVO.success(fileResultVO);
    }

    @PostMapping("/upload/batch")
    @ApiOperation("批量上传文件")
    public ResponseVO<List<FileResultVO>> uploadBatch(@RequestParam("file") MultipartFile[] files) {
        List<FileResultVO> list = new ArrayList<>();
        for (MultipartFile file : files) {
            String fileId = fileService.upload(file);
            FileResultVO fileResultVO = FileResultVO.builder()
                    .fileId(fileId)
                    .previewUrl(fileService.getPreviewUrl(fileId))
                    .build();
            list.add(fileResultVO);
        }

        return ResponseVO.success(list);
    }

    @PostMapping("/upload/anonymous")
    @ApiOperation("匿名上传文件")
    public ResponseVO<FileResultVO> uploadAnonymous(@RequestParam("file") MultipartFile file) {
        String fileId = fileService.upload(file);
        FileResultVO fileResultVO = FileResultVO.builder()
                .fileId(fileId)
                .previewUrl(fileService.getPreviewUrl(fileId))
                .build();
        return ResponseVO.success(fileResultVO);
    }

    @PostMapping("/batchDelete")
    @ApiOperation("批量删除文件")
    public ResponseVO batchDelete(@RequestParam("fileIds") List<String> fileIds) {
        fileService.batchDelete(fileIds);
        return ResponseVO.success();
    }

    @PostMapping("/download")
    @ApiOperation("下载文件")
    public ResponseVO download(HttpServletResponse response, @RequestParam("fileId") String fileId) {
        fileService.download(response, fileId);
        return ResponseVO.success();
    }

}
