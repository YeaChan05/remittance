package org.yechan.remittance;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;

@Configuration
public class AuthSecurityConfiguration {

  @Bean(name = "authorizeHttpRequestsCustomizer")
  AuthorizeHttpRequestsCustomizer authorizeHttpRequestsCustomizer() {
    return registry -> registry
        .requestMatchers(HttpMethod.POST, "/login").permitAll()
        .anyRequest().authenticated();
  }
}
