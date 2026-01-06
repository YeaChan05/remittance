package org.yechan.remittance;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import javax.crypto.SecretKey;

class JwtTokenGenerator implements TokenGenerator {

  private static final String SECRET = "member-token-secret-member-token-secret";

  private final SecretKey secretKey;
  private final long accessExpiresIn;
  private final long refreshExpiresIn;

  JwtTokenGenerator(String salt, long accessExpiresIn, long refreshExpiresIn) {
    var secret = SECRET + salt;
    this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    this.accessExpiresIn = accessExpiresIn;
    this.refreshExpiresIn = refreshExpiresIn;
  }

  @Override
  public AuthTokenValue generate(Long memberId) {
    var issuedAt = Instant.now();
    var accessToken = createToken(memberId, issuedAt, accessExpiresIn);
    var refreshToken = createToken(memberId, issuedAt, refreshExpiresIn);
    return new AuthTokenValue(accessToken, refreshToken, accessExpiresIn);
  }

  private String createToken(Long memberId, Instant issuedAt, long expiresInSeconds) {
    return Jwts.builder()
        .subject(memberId.toString())
        .issuedAt(Date.from(issuedAt))
        .expiration(Date.from(issuedAt.plusSeconds(expiresInSeconds)))
        .signWith(secretKey)
        .compact();
  }
}
