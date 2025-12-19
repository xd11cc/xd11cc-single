package com.xd11cc.single.service;

import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.crypto.Cipher;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * @author xd11cc
 * @date 2025-11-30 19:23:56
 */
@SpringBootTest
@RunWith(SpringRunner.class)
public class RSAEncryptionTest {

    /**
     * 生成RSA密钥对
     * @return 包含公钥和私钥的Map
     */
    public static Map<String, String> generateKeyPair() {
        try {
            KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance("RSA");
            keyPairGen.initialize(2048); // 密钥长度
            KeyPair keyPair = keyPairGen.generateKeyPair();

            PublicKey publicKey = keyPair.getPublic();
            PrivateKey privateKey = keyPair.getPrivate();

            Map<String, String> keyMap = new HashMap<>();
            keyMap.put("publicKey", Base64.getEncoder().encodeToString(publicKey.getEncoded()));
            keyMap.put("privateKey", Base64.getEncoder().encodeToString(privateKey.getEncoded()));

            return keyMap;
        } catch (Exception e) {
            throw new RuntimeException("生成密钥对失败", e);
        }
    }

    /**
     * RSA公钥加密
     * @param data 待加密数据
     * @param publicKey 公钥(Base64编码)
     * @return 加密后的Base64字符串
     */
    public static String encrypt(String data, String publicKey) {
        try {
            byte[] keyBytes = Base64.getDecoder().decode(publicKey);
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            PublicKey pubKey = keyFactory.generatePublic(keySpec);

            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(Cipher.ENCRYPT_MODE, pubKey);

            byte[] encryptedBytes = cipher.doFinal(data.getBytes());
            return Base64.getEncoder().encodeToString(encryptedBytes);
        } catch (Exception e) {
            throw new RuntimeException("RSA加密失败", e);
        }
    }

    /**
     * RSA私钥解密
     * @param encryptedData 加密数据(Base64编码)
     * @param privateKey 私钥(Base64编码)
     * @return 解密后的字符串
     */
    public static String decrypt(String encryptedData, String privateKey) {
        try {
            byte[] keyBytes = Base64.getDecoder().decode(privateKey);
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            PrivateKey priKey = keyFactory.generatePrivate(keySpec);

            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(Cipher.DECRYPT_MODE, priKey);

            byte[] encryptedBytes = Base64.getDecoder().decode(encryptedData);
            byte[] decryptedBytes = cipher.doFinal(encryptedBytes);
            return new String(decryptedBytes);
        } catch (Exception e) {
            throw new RuntimeException("RSA解密失败", e);
        }
    }

    /**
     * 使用私钥生成数字签名
     * @param data 原始数据
     * @param privateKey 私钥(Base64编码)
     * @return 数字签名的Base64字符串
     */
    public static String sign(String data, String privateKey) {
        try {
            byte[] keyBytes = Base64.getDecoder().decode(privateKey);
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            PrivateKey priKey = keyFactory.generatePrivate(keySpec);

            Signature signature = Signature.getInstance("SHA256withRSA");
            signature.initSign(priKey);
            signature.update(data.getBytes());

            byte[] signBytes = signature.sign();
            return Base64.getEncoder().encodeToString(signBytes);
        } catch (Exception e) {
            throw new RuntimeException("生成数字签名失败", e);
        }
    }

    /**
     * 使用公钥验证数字签名
     * @param data 原始数据
     * @param sign 数字签名(Base64编码)
     * @param publicKey 公钥(Base64编码)
     * @return 验证是否成功
     */
    public static boolean verify(String data, String sign, String publicKey) {
        try {
            byte[] keyBytes = Base64.getDecoder().decode(publicKey);
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            PublicKey pubKey = keyFactory.generatePublic(keySpec);

            Signature signature = Signature.getInstance("SHA256withRSA");
            signature.initVerify(pubKey);
            signature.update(data.getBytes());

            byte[] signBytes = Base64.getDecoder().decode(sign);
            return signature.verify(signBytes);
        } catch (Exception e) {
            throw new RuntimeException("验证数字签名失败", e);
        }
    }

    public static void main(String[] args) {
        String encrypt = encrypt("Spyh135655**", "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCZpdShbdmAQ/SnzCx1cRc3xkQDIHU5Db9FtStiJo/2nQAl3HUDw4J0Hf/pSKTqJkQuw12xAAIzFq/lAqT0FfazhH0A1NrNtXm0oceO2iQIy0F4FzMFQGf0rv2eYEGm11u+Jbqrpf21INiNJavzzXYdMnoJ2JztuyYyaD7li7yFZwABIDA");
        System.out.println(encrypt);
    }
}
