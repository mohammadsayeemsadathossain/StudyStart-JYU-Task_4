package com.studystart.jyu.security;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

final class Base64Url {
    private static final Base64.Encoder ENC = Base64.getUrlEncoder().withoutPadding();
    private static final Base64.Decoder DEC = Base64.getUrlDecoder();

    static String encode(byte[] bytes) { return ENC.encodeToString(bytes); }
    static byte[] decode(String s) { return DEC.decode(s); }
    static String encodeUtf8(String s) { return encode(s.getBytes(StandardCharsets.UTF_8)); }
    static String toUtf8(byte[] b) { return new String(b, StandardCharsets.UTF_8); }
}
