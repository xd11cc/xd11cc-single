package com.xd11cc.single.service;

import lombok.Getter;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;

/**
 * 文件内容与元数据。
 * <p>
 * 返回流而不是直接写响应，是为了让「文件模块」不依赖 Servlet 响应对象：
 * 调用方自己决定写响应、转存还是做别的处理，测试也只需要断言流的内容。
 * </p>
 *
 * @author xd11cc
 */
@Getter
public class FileContent implements Closeable {

    private final InputStream stream;

    private final String originalName;

    private final String contentType;

    public FileContent(InputStream stream, String originalName, String contentType) {
        this.stream = stream;
        this.originalName = originalName;
        this.contentType = contentType;
    }

    @Override
    public void close() throws IOException {
        stream.close();
    }
}
