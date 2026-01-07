package org.yechan.remittance;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import javax.crypto.SecretKey;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

class JwtTokenVerifier implements TokenVerifier {

  private static final String SECRET = "member-token-secret-member-token-secret";

  private final SecretKey secretKey;

  JwtTokenVerifier(String salt) {
    var secret = SECRET + salt;
    this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
  }

  @Override
  public Authentication verify(String token) {
    try {
      var claims = Jwts.parser()
          .verifyWith(secretKey)
          .build()
          .parseSignedClaims(token)
          .getPayload();
      String subject = claims.getSubject();
      if (subject == null) {
        throw new BadCredentialsException("Invalid token subject");
      }
      return new UsernamePasswordAuthenticationToken(subject, token, Collections.emptyList());
    } catch (JwtException | IllegalArgumentException e) {
      throw new BadCredentialsException("Invalid token", e);
    }
  }
}
