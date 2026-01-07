package org.yechan.remittance;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.security.autoconfigure.web.servlet.ServletWebSecurityAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer;
import org.springframework.security.config.annotation.web.configurers.FormLoginConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@AutoConfiguration(before = ServletWebSecurityAutoConfiguration.class)
@EnableConfigurationProperties(AuthTokenProperties.class)
public class CommonSecurityAutoConfiguration {

  @Bean
  TokenGenerator tokenGenerator(AuthTokenProperties authTokenProperties) {
    return new JwtTokenGenerator(
        authTokenProperties.salt(),
        authTokenProperties.accessExpiresIn(),
        authTokenProperties.refreshExpiresIn());
  }

  @Bean
  TokenParser tokenParser() {
    return new JwtTokenParser();
  }

  @Bean
  TokenVerifier tokenVerifier(AuthTokenProperties authTokenProperties) {
    return new JwtTokenVerifier(authTokenProperties.salt());
  }

  @Bean
  JwtAuthenticationFilter jwtAuthenticationFilter(
      TokenParser parser,
      TokenVerifier verifier,
      AuthenticationEntryPoint authenticationEntryPoint
  ) {
    return new JwtAuthenticationFilter(parser, verifier, authenticationEntryPoint);
  }

  @Bean
  AuthenticationEntryPoint authenticationEntryPoint() {
    return new DefaultAuthenticationEntryPoint();
  }

  @Bean
  AccessDeniedHandler accessDeniedHandler() {
    return new DefaultAccessDeniedHandler();
  }

  @Bean
  SecurityFilterChain securityFilterChain(
      HttpSecurity http,
      JwtAuthenticationFilter jwtAuthenticationFilter,
      AuthenticationEntryPoint authenticationEntryPoint,
      AccessDeniedHandler accessDeniedHandler,
      @Qualifier("authorizeHttpRequestsCustomizer")
      AuthorizeHttpRequestsCustomizer authorizeHttpRequestsCustomizer
  ) throws Exception {
    return http
        .formLogin(FormLoginConfigurer::disable)
        .csrf(CsrfConfigurer::disable)
        .sessionManagement(
            session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .exceptionHandling(handler -> handler
            .authenticationEntryPoint(authenticationEntryPoint)
            .accessDeniedHandler(accessDeniedHandler))
        .authorizeHttpRequests(authorizeHttpRequestsCustomizer::customize)
        .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
        .build();
  }

  @Bean(name = "authorizeHttpRequestsCustomizer")
  @ConditionalOnMissingBean(name = "authorizeHttpRequestsCustomizer")
  AuthorizeHttpRequestsCustomizer authorizeHttpRequestsCustomizer() {
    return registry -> registry.anyRequest().authenticated();
  }

}
