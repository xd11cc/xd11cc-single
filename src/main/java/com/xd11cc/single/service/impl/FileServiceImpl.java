package com.xd11cc.single.service.impl;

import com.xd11cc.single.config.exception.ServiceException;
import com.xd11cc.single.config.properties.MinioProperties;
import com.xd11cc.single.enums.SystemErrorEnum;
import com.xd11cc.single.service.FileService;
import io.minio.*;
import io.minio.http.Method;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * @author xd11cc
 * @date 2026-05-08 11:07:40
 * @description
 */
@Service
public class FileServiceImpl implements FileService {

    @Autowired
    private MinioClient minioClient;
    @Autowired
    private MinioProperties minioProperties;

    @Override
    public String upload(MultipartFile file) {
        // 防重名
        String fileId = System.currentTimeMillis() + "_" + file.getOriginalFilename();
        String bucketName = minioProperties.getBucketName();

        try (InputStream is = file.getInputStream()){
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(fileId)
                            .stream(is, file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build()
            );
        }catch (Exception e){
            throw new ServiceException(SystemErrorEnum.MINIO_UPLOAD_FILE, e);
        }
        return fileId;
    }

    @Override
    public String getPreviewUrl(String fileId) {
        validateFileId(fileId);
        try {
            return minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .bucket(minioProperties.getBucketName())
                            .object(fileId)
                            .method(Method.GET)
                            .build()
            );
        } catch (Exception e) {
            throw new ServiceException(SystemErrorEnum.MINIO_GET_FILE_FILE, e);
        }
    }

    @Override
    public void batchDelete(List<String> fileIds) {
        fileIds.forEach(fileId -> {
            validateFileId(fileId);
            try {
                minioClient.removeObject(
                        RemoveObjectArgs.builder()
                                .bucket(minioProperties.getBucketName())
                                .object(fileId)
                                .build()
                );
            } catch (Exception e) {
                throw new ServiceException(SystemErrorEnum.MINIO_DELETE_FILE_FILE, e);
            }
        });
    }

    @Override
    public void download(HttpServletResponse response, String fileId) {
        validateFileId(fileId);

        try (InputStream is = minioClient.getObject(
                GetObjectArgs.builder()
                        .bucket(minioProperties.getBucketName())
                        .object(fileId)
                        .build());
            OutputStream os = response.getOutputStream()){

            // 设置响应头
            response.setContentType("application/octet-stream");
            response.setHeader("Content-Disposition", "attachment; filename=" + URLEncoder.encode(fileId, StandardCharsets.UTF_8.name()));

            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = is.read(buffer)) != -1) {
                os.write(buffer, 0, bytesRead);
            }
        }catch (Exception e){
            throw new ServiceException(SystemErrorEnum.MINIO_DOWNLOAD_FILE_FILE, e);
        }
    }

    private void validateFileId(String fileId) {
        if (fileId == null || fileId.contains("..") || fileId.contains("/") || fileId.contains("\\")) {
            throw new ServiceException(SystemErrorEnum.MINIO_DOWNLOAD_FILE_FILE);
        }
    }
}
