package com.xd11cc.single.service;

import com.xd11cc.single.entity.base.FileResultVO;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * @author xd11cc
 * @date 2026-05-08 11:07:40
 * @description
 */
public interface FileService {

    String upload(MultipartFile file);

    String getPreviewUrl(String fileId);

    void batchDelete(List<String> fileIds);

    void download(HttpServletResponse response, String fileId);
}
