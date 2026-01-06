package org.yechan.remittance;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.web.filter.OncePerRequestFilter;

class JwtAuthenticationFilter extends OncePerRequestFilter {

  private final TokenParser parser;
  private final TokenVerifier verifier;
  private final AuthenticationEntryPoint authenticationEntryPoint;

  JwtAuthenticationFilter(
      TokenParser parser,
      TokenVerifier verifier,
      AuthenticationEntryPoint authenticationEntryPoint
  ) {
    this.parser = parser;
    this.verifier = verifier;
    this.authenticationEntryPoint = authenticationEntryPoint;
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest request,
      HttpServletResponse response,
      FilterChain filterChain
  ) throws ServletException, IOException {
    var token = parser.parse(request);
    if (token.isEmpty()) {
      filterChain.doFilter(request, response);
      return;
    }

    try {
      var authentication = verifier.verify(token.get());
      var context = SecurityContextHolder.createEmptyContext();
      context.setAuthentication(authentication);
      SecurityContextHolder.setContext(context);
      filterChain.doFilter(request, response);
    } catch (AuthenticationException ex) {
      SecurityContextHolder.clearContext();
      authenticationEntryPoint.commence(request, response, ex);
    } finally {
      SecurityContextHolder.clearContext();
    }
  }
}
