package org.yechan.remittance;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;

@Configuration
public class MemberSecurityConfiguration {

  @Bean(name = "authorizeHttpRequestsCustomizer")
  AuthorizeHttpRequestsCustomizer authorizeHttpRequestsCustomizer() {
    return registry -> registry
        .requestMatchers(HttpMethod.POST, "/login", "/members").permitAll()
        .anyRequest().authenticated();
  }
}
