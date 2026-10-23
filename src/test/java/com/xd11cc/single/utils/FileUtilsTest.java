package com.xd11cc.single.utils;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class FileUtilsTest {

    @Test
    void createTempFile_传入内容_文件存在且内容正确(@TempDir Path tempDir) throws Exception {
        File file = FileUtils.createTempFile("hello-world");

        assertThat(file).exists();
        String content = new String(Files.readAllBytes(file.toPath()));
        assertThat(content).isEqualTo("hello-world");
    }

    @Test
    void createTempFile_空字符串_文件存在且为空(@TempDir Path tempDir) throws Exception {
        File file = FileUtils.createTempFile("");

        assertThat(file).exists();
        String content = new String(Files.readAllBytes(file.toPath()));
        assertThat(content).isEmpty();
    }

    @Test
    void createTempFile_字节数组_文件存在且内容正确(@TempDir Path tempDir) throws Exception {
        byte[] data = new byte[]{0x00, 0x01, 0x02, (byte) 0xFF};
        File file = FileUtils.createTempFile(data);

        assertThat(file).exists();
        byte[] readBack = Files.readAllBytes(file.toPath());
        assertThat(readBack).isEqualTo(data);
    }

    @Test
    void createTempFile_无参_文件存在且为空(@TempDir Path tempDir) throws Exception {
        File file = FileUtils.createTempFile();

        assertThat(file).exists();
        assertThat(Files.size(file.toPath())).isZero();
    }

    @Test
    void createTempFile_每次生成不同文件() {
        File f1 = FileUtils.createTempFile();
        File f2 = FileUtils.createTempFile();

        assertThat(f1).exists();
        assertThat(f2).exists();
        assertThat(f1.getAbsolutePath()).isNotEqualTo(f2.getAbsolutePath());
    }
}
