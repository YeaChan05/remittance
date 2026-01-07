package org.yechan.remittance;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.lang.reflect.Method;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.method.support.ModelAndViewContainer;

class LoginUserIdArgumentResolverIntegrationTest {

  private final LoginUserIdArgumentResolver resolver = new LoginUserIdArgumentResolver();

  @AfterEach
  void clearContext() {
    SecurityContextHolder.clearContext();
  }

  @Test
  void supportsParameterWhenAnnotatedLong() throws Exception {
    MethodParameter parameter = new MethodParameter(method("handlerWithLong", Long.class), 0);

    assertThat(resolver.supportsParameter(parameter)).isTrue();
  }

  @Test
  void supportsParameterWhenAnnotatedPrimitiveLong() throws Exception {
    MethodParameter parameter = new MethodParameter(method("handlerWithPrimitive", long.class), 0);

    assertThat(resolver.supportsParameter(parameter)).isTrue();
  }

  @Test
  void doesNotSupportWithoutAnnotation() throws Exception {
    MethodParameter parameter = new MethodParameter(method("handlerWithoutAnnotation", Long.class), 0);

    assertThat(resolver.supportsParameter(parameter)).isFalse();
  }

  @Test
  void resolvesUserIdWhenAuthenticated() throws Exception {
    SecurityContextHolder.getContext().setAuthentication(
        new UsernamePasswordAuthenticationToken(
            "42",
            "credentials",
            List.of(new SimpleGrantedAuthority("ROLE_USER"))
        )
    );
    MethodParameter parameter = new MethodParameter(method("handlerWithLong", Long.class), 0);

    Object result = resolver.resolveArgument(parameter, new ModelAndViewContainer(), null, null);

    assertThat(result).isEqualTo(42L);
  }

  @Test
  void throwsWhenAuthenticationMissing() throws Exception {
    MethodParameter parameter = new MethodParameter(method("handlerWithLong", Long.class), 0);

    assertThatThrownBy(() -> resolver.resolveArgument(parameter, new ModelAndViewContainer(), null, null))
        .isInstanceOf(BusinessException.class)
        .hasMessage("Unauthorized");
  }

  @Test
  void throwsWhenAnonymousAuthentication() throws Exception {
    SecurityContextHolder.getContext().setAuthentication(
        new AnonymousAuthenticationToken(
            "key",
            "anonymous",
            List.of(new SimpleGrantedAuthority("ROLE_ANONYMOUS"))
        )
    );
    MethodParameter parameter = new MethodParameter(method("handlerWithLong", Long.class), 0);

    assertThatThrownBy(() -> resolver.resolveArgument(parameter, new ModelAndViewContainer(), null, null))
        .isInstanceOf(BusinessException.class)
        .hasMessage("Unauthorized");
  }

  @Test
  void throwsWhenUserIdIsNotNumeric() throws Exception {
    SecurityContextHolder.getContext().setAuthentication(
        new UsernamePasswordAuthenticationToken(
            "not-a-number",
            "credentials",
            List.of(new SimpleGrantedAuthority("ROLE_USER"))
        )
    );
    MethodParameter parameter = new MethodParameter(method("handlerWithLong", Long.class), 0);

    assertThatThrownBy(() -> resolver.resolveArgument(parameter, new ModelAndViewContainer(), null, null))
        .isInstanceOf(BusinessException.class)
        .hasMessage("Invalid user id");
  }

  private static Method method(String name, Class<?> parameterType) throws NoSuchMethodException {
    return TestController.class.getDeclaredMethod(name, parameterType);
  }

  private static class TestController {

    void handlerWithLong(@LoginUserId Long userId) {
    }

    void handlerWithPrimitive(@LoginUserId long userId) {
    }

    void handlerWithoutAnnotation(Long userId) {
    }
  }
}
