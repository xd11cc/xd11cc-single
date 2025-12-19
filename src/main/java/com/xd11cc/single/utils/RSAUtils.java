package com.xd11cc.single.utils;


import javax.crypto.Cipher;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

/**
 * @author xd11cc
 * @date 2025-11-30 19:30:41
 */
public class RSAUtils {

    // 固定RSA算法（指定PKCS1Padding填充，避免跨语言兼容问题）
    private static final String RSA_ALGORITHM = "RSA/ECB/PKCS1Padding";
    // 字符编码（统一UTF-8）
    private static final String CHARSET = "UTF-8";

    /**
     * RSA公钥加密（最终返回Base64编码的密文）
     * @param plainText 待加密的明文（字符串）
     * @param base64PublicKey Base64格式的RSA公钥字符串
     * @return 加密后Base64编码的密文字符串
     * @throws Exception 加密异常（算法/密钥/编码问题）
     */
    public static String encryptByPublicKey(String plainText, String base64PublicKey) throws Exception {
        // 1. 解码Base64格式的公钥字符串 → 字节数组
        byte[] publicKeyBytes = Base64.getDecoder().decode(base64PublicKey);

        // 2. 构建X509公钥规范（RSA公钥标准格式）
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(publicKeyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PublicKey publicKey = keyFactory.generatePublic(keySpec);

        // 3. 初始化RSA加密器（加密模式+公钥）
        Cipher cipher = Cipher.getInstance(RSA_ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);

        // 4. 加密明文（UTF-8编码）→ 加密字节数组
        byte[] encryptBytes = cipher.doFinal(plainText.getBytes(CHARSET));

        // 5. 加密字节数组 → Base64编码字符串（返回）
        return Base64.getEncoder().encodeToString(encryptBytes);
    }

    // ------------------- 以下为测试辅助方法 -------------------
    /**
     * 生成测试用RSA密钥对（返回Base64格式的公钥+私钥）
     * 实际生产中，公钥由服务端生成并提供，私钥保密
     */
    public static void generateTestKeyPair() throws Exception {
        java.security.KeyPairGenerator keyGen = java.security.KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048); // 密钥长度2048（1024已不安全）
        java.security.KeyPair keyPair = keyGen.generateKeyPair();

        // 公钥→Base64字符串
        String publicKeyBase64 = Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded());
        // 私钥→Base64字符串
        String privateKeyBase64 = Base64.getEncoder().encodeToString(keyPair.getPrivate().getEncoded());

        System.out.println("测试用Base64公钥：\n" + publicKeyBase64);
        System.out.println("\n测试用Base64私钥：\n" + privateKeyBase64);
    }

    // 测试主方法
    public static void main(String[] args) {
        try {
            // 1. 生成测试密钥对（首次运行先获取公钥）
            generateTestKeyPair();

            // 2. 待加密的明文
            String plainText = "Spyh135655**";
            System.out.println("\n明文：" + plainText);

            // 3. 替换为上面生成的Base64公钥
            String base64PublicKey = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCXAs5Kza/ynDijaUMSPeWYCRAr3S5N0DWlvq1SIPxjdT5ksi+BmLVDcxVmvcEGXMrQD4XPxc3s4X368CePqY6B/sCpoh9oxi2wWUSjHMWzhU6/3cNdGm8gYIgMqWOolohl+neLBz875y+PysNJxEDIUPw7WB5GAbWqlgN13IHsrQIDAQAB";

            // 4. 公钥加密
            String cipherText = encryptByPublicKey(plainText, base64PublicKey);
            System.out.println("\n加密后Base64密文：\n" + cipherText);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
