package com.xd11cc.single.utils;

import javax.crypto.Cipher;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

/**
 * @author xd11cc
 * @date 2025-11-30 19:30:41
 */
public class RSAUtils {

    private static final String RSA_ALGORITHM = "RSA/ECB/PKCS1Padding";
    private static final String CHARSET = "UTF-8";

    public static String encryptByPublicKey(String plainText, String base64PublicKey) throws Exception {
        byte[] publicKeyBytes = Base64.getDecoder().decode(base64PublicKey);
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(publicKeyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PublicKey publicKey = keyFactory.generatePublic(keySpec);

        Cipher cipher = Cipher.getInstance(RSA_ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        byte[] encryptBytes = cipher.doFinal(plainText.getBytes(CHARSET));
        return Base64.getEncoder().encodeToString(encryptBytes);
    }

    public static String decryptByPrivateKey(String cipherText, String base64PrivateKey) throws Exception {
        byte[] privateKeyBytes = Base64.getDecoder().decode(base64PrivateKey);
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(privateKeyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PrivateKey privateKey = keyFactory.generatePrivate(keySpec);

        Cipher cipher = Cipher.getInstance(RSA_ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        byte[] decryptBytes = cipher.doFinal(Base64.getDecoder().decode(cipherText));
        return new String(decryptBytes, CHARSET);
    }
}
