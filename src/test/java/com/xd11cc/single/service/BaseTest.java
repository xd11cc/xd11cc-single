package com.xd11cc.single.service;

import cn.hutool.core.util.ZipUtil;
//import com.xd11cc.single.utils.FileConvertUtils;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;
import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.FileSystem;
import java.nio.file.*;
import java.security.Key;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * @author xd11cc
 * @date 2025-07-28 16:59:50
 */
@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class BaseTest {


    public static void main(String[] args) throws IOException {
        String sourceFile = "/Users/xudecheng/Desktop/xd11cc/work/hasone/1753694519952.zip";
        String targetFile = "/Users/xudecheng/Desktop/xd11cc/work/hasone/test";
        String baseFile = "/Users/xudecheng/Desktop/xd11cc/work/";
        ZipUtil.unzip(sourceFile, targetFile);
        File file = new File(sourceFile);
        File file1 = new File(targetFile);
//        extractNestedZip(file, file1);
//        unzipNio(Paths.get(sourceFile), Paths.get(targetFile));
//        Path path = Paths.get(baseFile);
//        Path resolve = path.relativize(Paths.get(sourceFile));
//        System.out.println(resolve);
    }

    public static void extractNestedZip(File zipFile, File outputDir) throws IOException {
        try (ZipFile zf = new ZipFile(zipFile, Charset.forName("GBK"))) {
            Enumeration<? extends ZipEntry> entries = zf.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                String entryName = entry.getName();
                File outputFile = new File(outputDir, entryName);
                if (entry.isDirectory()) {
                    // 创建输出目录
                    if (!outputFile.exists()) {
                        outputFile.mkdirs();
                    }
                } else {
                    // 创建输出文件的父目录
                    File parentDir = outputFile.getParentFile();
                    if (!parentDir.exists()) {
                        parentDir.mkdirs();
                    }
                    // 解压文件
                    try (InputStream is = zf.getInputStream(entry);
                         FileOutputStream fos = new FileOutputStream(outputFile)) {
                        byte[] buffer = new byte[1024];
                        int len;
                        while ((len = is.read(buffer)) > 0) {
                            fos.write(buffer, 0, len);
                        }
                    }
                }
            }
        }
    }

    public static void unzipNio(Path zipFile, Path destDir) throws IOException {
        try (FileSystem fs = FileSystems.newFileSystem(zipFile, null)) {
            for (Path root : fs.getRootDirectories()) {
                Files.walk(root).forEach(source -> {
                    try {
                        Path target = destDir.resolve(root.relativize(source).toString());
                        if (Files.isDirectory(source)) {
                            Files.createDirectories(target);
                        } else {
                            Files.createDirectories(target.getParent());
                            Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
                        }
                    } catch (IOException e) {
                        throw new UncheckedIOException(e);
                    }
                });
            }
        }
    }

    public static final String ALGORITHM = "PBEWithMD5AndDES";//加密算法
    private static final int ITERATIONCOUNT = 1000;

    private static Key getPBEKey(String password) {
        // 实例化使用的算法
        SecretKeyFactory keyFactory;
        SecretKey secretKey = null;
        try {
            keyFactory = SecretKeyFactory.getInstance(ALGORITHM);
            // 设置PBE密钥参数
            PBEKeySpec keySpec = new PBEKeySpec(password.toCharArray());
            // 生成密钥
            secretKey = keyFactory.generateSecret(keySpec);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return secretKey;
    }


    public static String encrypt(String plaintext, String password, String salt) {

        Key key = getPBEKey(password);
        byte[] encipheredData = null;
        PBEParameterSpec parameterSpec = new PBEParameterSpec(salt.getBytes(), ITERATIONCOUNT);
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM);

            cipher.init(Cipher.ENCRYPT_MODE, key, parameterSpec);
            //update-begin-author:sccott date:20180815 for:中文作为用户名时，加密的密码windows和linux会得到不同的结果 gitee/issues/IZUD7
            encipheredData = cipher.doFinal(plaintext.getBytes("utf-8"));
            //update-end-author:sccott date:20180815 for:中文作为用户名时，加密的密码windows和linux会得到不同的结果 gitee/issues/IZUD7
        } catch (Exception e) {
        }
        return bytesToHexString(encipheredData);
    }

    public static String bytesToHexString(byte[] src) {
        StringBuilder stringBuilder = new StringBuilder("");
        if (src == null || src.length <= 0) {
            return null;
        }
        for (int i = 0; i < src.length; i++) {
            int v = src[i] & 0xFF;
            String hv = Integer.toHexString(v);
            if (hv.length() < 2) {
                stringBuilder.append(0);
            }
            stringBuilder.append(hv);
        }
        return stringBuilder.toString();
    }

    @Test
    public void encode() {
        log.info("...........");
        // f12d2aaffd70ab696649131f6c0bec07
        String encrypt = encrypt("17312735670", "qwer123456", "kURHzjd4");
        log.info("encrypt:{}", encrypt);
        log.info("equal:{}", encrypt.equals("d669f77257a381d06853f57fe84c5c48"));
        log.info("...........");
    }

//    @Test
//    public void fileConvert() {
//        FileConvertUtils.PdfToWord("/Users/xudecheng/Desktop/xd11cc/file/许德承简历-Java.pdf", "/Users/xudecheng/Desktop/xd11cc/file/许德承简历-Java.docx");
//    }
}
