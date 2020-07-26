package com.wzp.util.jwt;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.wzp.util.time.TimeUtils;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class AccessToken {

    private static final String[] ARRAY = new String[]{};

    public static AccessToken verify(String token, String secret) {
        return verify(token, secret.getBytes(StandardCharsets.UTF_8));
    }

    public static AccessToken verify(String token, byte[] secret) {
        DecodedJWT jwt = JWT.require(Algorithm.HMAC256(secret))
                .acceptLeeway(0)
                .build().verify(token);
        return decode(jwt);
    }

    public static AccessToken decode(String token) {
        DecodedJWT jwt = JWT.decode(token);
        return decode(jwt);
    }

    public static AccessToken of(String tenantId, String userId, LocalDateTime expiresAt, List<String> accessResources) {
        return new AccessToken(tenantId, userId, expiresAt, null, null, accessResources);
    }

    private static AccessToken decode(DecodedJWT jwt) {
        return new AccessToken(
                jwt.getClaim("t").asString(),
                jwt.getClaim("u").asString(),
                jwt.getExpiresAt() != null ? TimeUtils.asLocalDateTime(jwt.getExpiresAt()) : null,
                jwt.getNotBefore() != null ? TimeUtils.asLocalDateTime(jwt.getNotBefore()) : null,
                jwt.getIssuedAt() != null ? TimeUtils.asLocalDateTime(jwt.getIssuedAt()) : null,
                jwt.getAudience());
    }

    private final String tenantId;

    private final String userId;

    private final LocalDateTime issuedAt;

    private final LocalDateTime notBefore;

    private final LocalDateTime expiresAt;

    private final List<String> access;

    private AccessToken(String tenantId, String userId,
                        LocalDateTime expiresAt, LocalDateTime notBefore,
                        LocalDateTime issuedAt, List<String> access) {
        Objects.requireNonNull(tenantId);
        Objects.requireNonNull(expiresAt);

        this.tenantId = tenantId;
        this.userId = userId;
        this.issuedAt = issuedAt;
        this.notBefore = notBefore;
        this.expiresAt = expiresAt;
        this.access = access != null ? Collections.unmodifiableList(access) : null;
    }

    public String getTenantId() {
        return tenantId;
    }

    public String getUserId() {
        return userId;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public LocalDateTime getIssuedAt() {
        return issuedAt;
    }

    public LocalDateTime getNotBefore() {
        return notBefore;
    }

    public List<String> getAccess() {
        return access;
    }

    @Override
    public String toString() {
        return "AccessToken{" +
                "tenantId='" + tenantId + '\'' +
                ", userId='" + userId + '\'' +
                ", issuedAt=" + issuedAt +
                ", notBefore=" + notBefore +
                ", expiresAt=" + expiresAt +
                ", access=" + access +
                '}';
    }

    public String sign(String secret) {
        return sign(Algorithm.HMAC256(secret));
    }

    public String sign(byte[] secret) {
        return sign(Algorithm.HMAC256(secret));
    }

    private String sign(Algorithm algorithm) {
        return JWT.create()
                .withClaim("t", tenantId)
                .withClaim("u", userId)
                .withExpiresAt(expiresAt != null ? TimeUtils.asDate(expiresAt) : null)
                .withNotBefore(notBefore != null ? TimeUtils.asDate(notBefore) : null)
                .withIssuedAt(issuedAt != null ? TimeUtils.asDate(issuedAt) : null)
                .withAudience(access != null ? access.toArray(ARRAY) : null)
                .sign(algorithm);
    }

    public static void main(String[] args) {
        AccessToken accessToken = AccessToken.of("t", "u",
                LocalDateTime.now().plusDays(1), null);
        String secret = UUID.randomUUID().toString().replaceAll("-", "");
        String token = accessToken.sign(secret);
        System.out.println(token);

        AccessToken decode = AccessToken.decode(token);
        System.out.println(decode);

        AccessToken verify = AccessToken.verify(token, secret);
        System.out.println(verify);
    }
}
