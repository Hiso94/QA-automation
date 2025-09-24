package com.example.util;

import java.security.SecureRandom;
import java.util.Locale;

public final class RandomData {

    private static final String ALPHANUM = "abcdefghijklmnopqrstuvwxyz0123456789";
    private static final SecureRandom RNG = new SecureRandom();

    private RandomData() {}

    public static String randomEmail() {
        return randomString(10) + "+" + System.currentTimeMillis() + "@example.test";
    }

    public static String randomPhone() {
        StringBuilder sb = new StringBuilder("+1");
        for (int i = 0; i < 10; i++) {
            sb.append(RNG.nextInt(10));
        }
        return sb.toString();
    }

    public static String randomString(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            int idx = RNG.nextInt(ALPHANUM.length());
            sb.append(ALPHANUM.charAt(idx));
        }
        return sb.toString().toLowerCase(Locale.ROOT);
    }
}


