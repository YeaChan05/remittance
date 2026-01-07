package org.yechan.remittance.auth;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
record AuthApiController(
    AuthLoginUseCase authLoginUseCase
) {

  @PostMapping("/login")
  ResponseEntity<AuthLoginResponse> login(@RequestBody @Valid AuthLoginRequest request) {
    var token = authLoginUseCase.login(request);
    var response = new AuthLoginResponse(token.accessToken(), token.refreshToken(),
        token.expiresIn());
    return ResponseEntity.ok(response);
  }
}
