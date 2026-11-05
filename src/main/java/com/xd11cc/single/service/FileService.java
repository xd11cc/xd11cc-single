package com.xd11cc.single.service;

import com.xd11cc.single.entity.base.FileResultVO;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.List;

/**
 * 文件服务。
 * <p>
 * 接口只描述「做什么」，不绑定传输层：上传接受流，下载返回流，
 * 由调用方（Controller）负责与 HTTP 打交道。
 * </p>
 *
 * @author xd11cc
 * @date 2026-05-08 11:07:40
 */
public interface FileService {

    /**
     * 上传浏览器提交的文件
     */
    FileResultVO upload(MultipartFile file);

    /**
     * 上传任意输入流，供服务端生成的文件（导出、转换结果）复用。
     * <p>
     * 流的生命周期由调用方负责：本方法不关闭传入的流。
     * </p>
     *
     * @param in          文件内容
     * @param size        字节数
     * @param filename    原始文件名，用于展示与下载命名
     * @param contentType MIME 类型，为空时按二进制流处理
     */
    FileResultVO upload(InputStream in, long size, String filename, String contentType);

    /**
     * 打开文件流并返回元数据，调用方负责关闭。
     */
    FileContent open(String fileId);

    /**
     * 预览地址，带签名且有过期时间
     */
    String getPreviewUrl(String fileId);

    /**
     * 批量删除。
     *
     * @return 删除失败的 fileId；全部成功时返回空列表
     */
    List<String> batchDelete(List<String> fileIds);
}
