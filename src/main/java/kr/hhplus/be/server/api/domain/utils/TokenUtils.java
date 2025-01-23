package kr.hhplus.be.server.api.domain.utils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

public class TokenUtils {

    /**
     * 사용자 ID를 기반으로 UUID 토큰 생성
     * @param userId 사용자 ID
     * @return 생성된 토큰 문자열
     */
    public static String generateTokenString(Long userId) {
        try {
            String input = String.valueOf(userId);
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));

            long mostSigBits = bytesToLong(hash, 0);
            long leastSigBits = bytesToLong(hash, 8);

            return new UUID(mostSigBits, leastSigBits).toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("UUID 생성에 실패했습니다.", e);
        }
    }

    /**
     * 바이트 배열을 long 값으로 변환
     * @param bytes 바이트 배열
     * @param offset 시작 위치
     * @return 변환된 long 값
     */
    private static long bytesToLong(byte[] bytes, int offset) {
        long value = 0;
        for (int i = 0; i < 8; i++) {
            value = (value << 8) | (bytes[offset + i] & 0xFF);
        }
        return value;
    }
}
