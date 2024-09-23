package top.hcode.hoj.utils;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.crypto.Cipher;
import org.apache.commons.codec.binary.Hex;

public class AESUtils {

    /**
     * 使用 AES 加密给定的文本，返回加密后的字符串，格式为 "IV(16字节):加密后的数据"
     *
     * @param text      需要加密的原始文本
     * @param secretKey 用于加密的密钥 (必须为 16 字节、24 字节或 32 字节的字符串)
     * @return 加密后的字符串，格式为 IV(16字节):Ciphertext
     * @throws Exception 如果加密过程中发生错误，抛出异常
     */
    public static String encrypt(String text, String secretKey) throws Exception {
        // 生成16字节的随机IV（初始化向量）
        byte[] iv = new byte[16];
        SecureRandom random = new SecureRandom();
        random.nextBytes(iv);

        // 将密钥转换为字节数组，并通过 SecretKeySpec 生成 AES 密钥规范
        SecretKeySpec keySpec = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), "AES");

        // 初始化 AES 加密模式，使用 CBC 模式和 PKCS5 填充方式
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        IvParameterSpec ivSpec = new IvParameterSpec(iv);
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);

        // 执行加密，将原始文本加密为字节数组
        byte[] encrypted = cipher.doFinal(text.getBytes(StandardCharsets.UTF_8));

        // 将 IV 和加密后的数据转换为十六进制格式，并拼接返回
        return Hex.encodeHexString(iv) + ":" + Hex.encodeHexString(encrypted);
    }

    /**
     * 使用 AES 解密给定的加密字符串，返回解密后的原始文本
     *
     * @param encryptedText 加密后的文本，格式为 IV(16字节):Ciphertext
     * @param secretKey     用于解密的密钥 (必须为 16 字节、24 字节或 32 字节的字符串)
     * @return 解密后的原始字符串
     * @throws Exception 如果解密过程中发生错误，抛出异常
     */
    public static String decrypt(String encryptedText, String secretKey) throws Exception {
        // 分割加密文本，获取 IV 和加密后的数据
        String[] parts = encryptedText.split(":");

        // 校验加密文本的格式是否正确
        if (parts.length != 2) {
            throw new IllegalArgumentException("Invalid encrypted text format. Expected format: IV:Ciphertext");
        }

        // 将 IV 和加密后的数据从十六进制字符串转换为字节数组
        byte[] iv = Hex.decodeHex(parts[0]);
        byte[] encrypted = Hex.decodeHex(parts[1]);

        // 将密钥转换为字节数组，并通过 SecretKeySpec 生成 AES 密钥规范
        SecretKeySpec keySpec = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), "AES");

        // 初始化 AES 解密模式，使用 CBC 模式和 PKCS5 填充方式
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        IvParameterSpec ivSpec = new IvParameterSpec(iv);
        cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);

        // 执行解密，将加密的数据解密为字节数组
        byte[] decrypted = cipher.doFinal(encrypted);

        // 返回解密后的原始字符串
        return new String(decrypted, StandardCharsets.UTF_8);
    }

}
