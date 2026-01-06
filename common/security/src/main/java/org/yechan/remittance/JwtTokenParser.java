package org.yechan.remittance;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Optional;
import org.springframework.http.HttpHeaders;
import org.springframework.util.StringUtils;

class JwtTokenParser implements TokenParser {

  private static final String BEARER = "Bearer ";

  @Override
  public Optional<String> parse(HttpServletRequest request) {
    String header = request.getHeader(HttpHeaders.AUTHORIZATION);
    if (!StringUtils.hasText(header) || !header.startsWith(BEARER)) {
      return Optional.empty();
    }
    return Optional.of(header.substring(BEARER.length()));
  }
}
