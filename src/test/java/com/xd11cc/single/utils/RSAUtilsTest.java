package com.xd11cc.single.utils;

import org.junit.jupiter.api.Test;

import java.security.KeyPair;
import java.security.KeyPairGenerator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RSAUtilsTest {

    private static KeyPair generateKeyPair() throws Exception {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(2048);
        return keyPairGenerator.generateKeyPair();
    }

    private static String publicKeyToBase64(KeyPair keyPair) {
        return java.util.Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded());
    }

    private static String privateKeyToBase64(KeyPair keyPair) {
        return java.util.Base64.getEncoder().encodeToString(keyPair.getPrivate().getEncoded());
    }

    @Test
    void roundTrip_加解密后原文不变() throws Exception {
        KeyPair keyPair = generateKeyPair();
        String plainText = "Hello RSA 测试中文";
        String publicKey = publicKeyToBase64(keyPair);
        String privateKey = privateKeyToBase64(keyPair);

        String cipherText = RSAUtils.encryptByPublicKey(plainText, publicKey);
        String decrypted = RSAUtils.decryptByPrivateKey(cipherText, privateKey);

        assertThat(decrypted).isEqualTo(plainText);
    }

    @Test
    void encryptByPublicKey_不同明文_密文不同() throws Exception {
        KeyPair keyPair = generateKeyPair();
        String publicKey = publicKeyToBase64(keyPair);
        String cipher1 = RSAUtils.encryptByPublicKey("msg1", publicKey);
        String cipher2 = RSAUtils.encryptByPublicKey("msg2", publicKey);
        assertThat(cipher1).isNotEqualTo(cipher2);
    }

    @Test
    void encryptByPublicKey_空字符串_可正常加解密() throws Exception {
        KeyPair keyPair = generateKeyPair();
        String publicKey = publicKeyToBase64(keyPair);
        String privateKey = privateKeyToBase64(keyPair);
        String cipherText = RSAUtils.encryptByPublicKey("", publicKey);
        assertThat(cipherText).isNotBlank();
        assertThat(RSAUtils.decryptByPrivateKey(cipherText, privateKey)).isEqualTo("");
    }

    @Test
    void encryptByPublicKey_非法base64密钥_抛异常() {
        assertThrows(Exception.class,
                () -> RSAUtils.encryptByPublicKey("plain text", "not-a-base64-key!!!"));
    }

    @Test
    void decryptByPrivateKey_非法密文_抛异常() throws Exception {
        KeyPair keyPair = generateKeyPair();
        String publicKey = publicKeyToBase64(keyPair);
        String privateKey = privateKeyToBase64(keyPair);
        String cipherText = RSAUtils.encryptByPublicKey("test", publicKey);

        // 篡改密文
        String tampered = cipherText.substring(0, cipherText.length() - 2) + "xx";
        assertThrows(Exception.class,
                () -> RSAUtils.decryptByPrivateKey(tampered, privateKey));
    }
}
