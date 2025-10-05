package com.studystart.jyu.security;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.*;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class JwtUtil {
    private final byte[] secret;
    private final String issuer;
    private final long expMinutes;

    public JwtUtil(String secret, String issuer, long expMinutes) {
        this.secret = secret.getBytes(StandardCharsets.UTF_8);
        this.issuer = issuer;
        this.expMinutes = expMinutes;
    }

    public String generateToken(String username, List<Role> roles) {
        long now = Instant.now().getEpochSecond();
        long exp = now + expMinutes * 60;

        Map<String, Object> header = new LinkedHashMap<>();
        header.put("alg", "HS256");
        header.put("typ", "JWT");

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("iss", issuer);
        payload.put("sub", username);
        payload.put("iat", now);
        payload.put("exp", exp);
        payload.put("roles", roles == null ? Collections.emptyList() : roles);

        String headerB64 = Base64Url.encodeUtf8(toJson(header));
        String payloadB64 = Base64Url.encodeUtf8(toJson(payload));
        String signingInput = headerB64 + "." + payloadB64;
        String sigB64 = Base64Url.encode(hmacSha256(secret, signingInput));

        return signingInput + "." + sigB64;
    }

    public Decoded validateAndDecode(String token) {
        String[] parts = token.split("\\.");
        if (parts.length != 3) throw new JwtException("Invalid JWT structure");

        String signingInput = parts[0] + "." + parts[1];
        String expectedSig = Base64Url.encode(hmacSha256(secret, signingInput));
        if (!constantTimeEquals(expectedSig, parts[2])) {
            throw new JwtException("Invalid signature");
        }

        Map<String, Object> payload = parseJson(Base64Url.toUtf8(Base64Url.decode(parts[1])));
        String iss = (String) payload.get("iss");
        if (issuer != null && !issuer.equals(iss)) throw new JwtException("Invalid issuer");

        long now = Instant.now().getEpochSecond();
        Object expObj = payload.get("exp");
        if (expObj == null) throw new JwtException("Missing exp");
        long exp = (expObj instanceof Number) ? ((Number) expObj).longValue() : Long.parseLong(expObj.toString());
        if (now >= exp) throw new JwtExpiredException("Token expired");

        String username = (String) payload.get("sub");
        @SuppressWarnings("unchecked")
        List<Role> roles = (List<Role>) payload.get("roles");
        if (roles == null) roles = Collections.emptyList();

        return new Decoded(username, roles, exp);
    }

    public static final class Decoded {
        public final String username;
        public final List<Role> roles;
        public final long exp;
        public Decoded(String username, List<Role> roles, long exp) {
            this.username = username; this.roles = roles; this.exp = exp;
        }
    }

    public static class JwtException extends RuntimeException { public JwtException(String m) { super(m);} }
    public static class JwtExpiredException extends JwtException { public JwtExpiredException(String m){ super(m);} }


    private static byte[] hmacSha256(byte[] key, String data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(key, "HmacSHA256"));
            return mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            throw new RuntimeException("HMAC error", e);
        }
    }

    private static boolean constantTimeEquals(String a, String b) {
        if (a.length() != b.length()) return false;
        int res = 0;
        for (int i = 0; i < a.length(); i++) res |= a.charAt(i) ^ b.charAt(i);
        return res == 0;
    }


    private static String toJson(Map<String, Object> map) {
        StringBuilder sb = new StringBuilder("{");
        boolean first = true;
        for (Map.Entry<String, Object> e : map.entrySet()) {
            if (!first) sb.append(',');
            first = false;
            sb.append('"').append(e.getKey()).append('"').append(':');
            Object v = e.getValue();
            if (v instanceof Number || v instanceof Boolean) sb.append(v.toString());
            else if (v instanceof Collection) {
                sb.append('[');
                boolean f2 = true;
                for (Object o : (Collection<?>) v) {
                    if (!f2) sb.append(',');
                    f2 = false;
                    sb.append('"').append(escape(o.toString())).append('"');
                }
                sb.append(']');
            } else sb.append('"').append(escape(String.valueOf(v))).append('"');
        }
        sb.append('}');
        return sb.toString();
    }

    private static String escape(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private static Map<String, Object> parseJson(String json) {
        Map<String, Object> map = new LinkedHashMap<>();
        String t = json.trim();
        if (!t.startsWith("{") || !t.endsWith("}")) throw new IllegalArgumentException("Invalid JSON");
        String body = t.substring(1, t.length()-1).trim();
        if (body.isEmpty()) return map;
        List<String> parts = splitTopLevel(body);
        for (String part : parts) {
            int idx = part.indexOf(':');
            if (idx <= 0) throw new IllegalArgumentException("Bad pair: " + part);
            String key = unquote(part.substring(0, idx).trim());
            String val = part.substring(idx+1).trim();
            if (val.startsWith("\"")) map.put(key, unquote(val));
            else if (val.equals("true") || val.equals("false")) map.put(key, Boolean.parseBoolean(val));
            else if (val.startsWith("[")) map.put(key, parseStringArray(val));
            else map.put(key, Long.parseLong(val));
        }
        return map;
    }

    private static List<String> splitTopLevel(String s) {
        List<String> out = new ArrayList<>();
        int depth = 0; boolean inQ = false;
        StringBuilder cur = new StringBuilder();
        for (int i=0;i<s.length();i++) {
            char c = s.charAt(i);
            if (c=='"' && (i==0 || s.charAt(i-1)!='\\')) inQ = !inQ;
            if (!inQ) {
                if (c=='[') depth++;
                else if (c==']') depth--;
                else if (c==',' && depth==0) { out.add(cur.toString().trim()); cur.setLength(0); continue; }
            }
            cur.append(c);
        }
        if (cur.length()>0) out.add(cur.toString().trim());
        return out;
    }

    private static String unquote(String s) {
        String t = s.trim();
        if (t.startsWith("\"") && t.endsWith("\"")) t = t.substring(1, t.length()-1);
        return t.replace("\\\"", "\"").replace("\\\\", "\\");
    }

    private static List<String> parseStringArray(String s) {
        String t = s.trim();
        if (!t.startsWith("[") || !t.endsWith("]")) throw new IllegalArgumentException("Invalid array");
        String inner = t.substring(1, t.length()-1).trim();
        if (inner.isEmpty()) return Collections.emptyList();
        List<String> out = new ArrayList<>();
        List<String> parts = splitTopLevel(inner);
        for (String p : parts) out.add(unquote(p));
        return out;
    }
}
