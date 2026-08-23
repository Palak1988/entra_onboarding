package com.example.oidc.Security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;
import java.util.regex.Pattern;

@Configuration
public class AESHelper {
    public Key secretKeySpec;
    public static final String START_WITH = "ENC--";

    private byte[] generateIV() {
        SecureRandom secureRandom = new SecureRandom();
        byte[] iv = new byte[12];
        secureRandom.nextBytes(iv);
        return iv;
    }

    private static String asHexString(byte[] buf) {
        StringBuilder strbuf = new StringBuilder(buf.length * 2);

        for (int i = 0; i < buf.length; ++i) {
            if ((buf[i] & 255) < 16) {
                strbuf.append("0");
            }

            strbuf.append(Long.toString(buf[i] & 255, 16));
        }

        return START_WITH + strbuf;
    }

    public Key generateKey(String key, String salt) throws NoSuchAlgorithmException {
        if (key != null) {
            try {
                byte[] secretKeyBytes = key.getBytes(StandardCharsets.UTF_8);
                byte[] saltDecode = Base64.getDecoder().decode(salt);
                MessageDigest sha = MessageDigest.getInstance("SHA-256");
                sha.reset();
                sha.update(saltDecode);
                byte[] keySharDig = sha.digest(secretKeyBytes);
                keySharDig = Arrays.copyOfRange(keySharDig, 0, 16);
                return new SecretKeySpec(keySharDig, "AES");
            } catch (Exception e) {
                throw e;
            }
        }
        return null;
    }

    public AESHelper(@Value("${jasypt.encryptor.key}") String key, @Value("${jasypt.encryptor.salt}") String salt) throws Exception {
        this.secretKeySpec = this.generateKey(key, salt);
    }

    public String decryptPlugin(String encryptedString) throws Exception {
        try {
            if (encryptedString.startsWith(START_WITH)) {
                encryptedString = encryptedString.split(Pattern.quote(START_WITH))[1];
                byte[] cipherMessage = toByteArray(encryptedString);
                ByteBuffer byteBuffer = ByteBuffer.wrap(cipherMessage);
                int ivLength = byteBuffer.getInt();
                byte[] iv = new byte[ivLength];
                byteBuffer.get(iv);

                byte[] cipherText = new byte[byteBuffer.remaining()];
                byteBuffer.get(cipherText);
                Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
                cipher.init(2, secretKeySpec, new GCMParameterSpec(128, iv));
                byte[] original = cipher.doFinal(cipherText);
                return new String(original, StandardCharsets.UTF_8);
            } else {
                return encryptedString;
            }
        } catch (Exception e) {
            throw e;
        }
    }

    private byte[] toBytes(char[] chars) {
        CharBuffer charBuffer = CharBuffer.wrap(chars);
        ByteBuffer byteBuffer = StandardCharsets.UTF_8.encode(charBuffer);
        byte[] bytes = Arrays.copyOfRange(byteBuffer.array(), byteBuffer.position(), byteBuffer.limit());
        Arrays.fill(byteBuffer.array(), (byte) 0);
        return bytes;
    }

    private static byte[] toByteArray(String hexString) {
        int arrLength = hexString.length() >> 1;
        byte[] buf = new byte[arrLength];

        for (int ii = 0; ii < arrLength; ++ii) {
            int index = ii << 1;
            String lDigit = hexString.substring(index, index + 2);
            buf[ii] = (byte) Integer.parseInt(lDigit, 16);
        }

        return buf;
    }

    public String decrypt(String encryptedString) throws Exception {
        try {
            if (encryptedString.startsWith(START_WITH)) {
                encryptedString = encryptedString.split(Pattern.quote(START_WITH))[1];
                byte[] cipherMessage = toByteArray(encryptedString);
                ByteBuffer byteBuffer = ByteBuffer.wrap(cipherMessage);
                int ivLength = byteBuffer.getInt();
                byte[] iv = new byte[ivLength];
                byteBuffer.get(iv);

                byte[] cipherText = new byte[byteBuffer.remaining()];
                byteBuffer.get(cipherText);
                Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
                cipher.init(2, secretKeySpec, new GCMParameterSpec(128, iv));
                byte[] original = cipher.doFinal(cipherText);
                return new String(original, StandardCharsets.UTF_8);
            } else {
                return encryptedString;
            }
        } catch (Exception e) {
            throw e;
        }
    }

    public String encrypt(String input) throws Exception {
        try {
            byte[] iv = this.generateIV();
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(1, secretKeySpec, new GCMParameterSpec(128, iv));
            byte[] concealByteArr = cipher.doFinal(this.toBytes(input.toCharArray()));
            ByteBuffer byteBuffer = ByteBuffer.allocate(4 + iv.length + concealByteArr.length);
            byteBuffer.putInt(iv.length);
            byteBuffer.put(iv);
            byteBuffer.put(concealByteArr);
            byte[] cipherMessage = byteBuffer.array();
            return asHexString(cipherMessage);
        } catch (Exception e) {
            throw e;
        }
    }
}