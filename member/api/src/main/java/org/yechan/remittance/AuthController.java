package org.yechan.remittance;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.yechan.remittance.dto.MemberLoginRequest;
import org.yechan.remittance.dto.MemberLoginResponse;

@RestController
record AuthController(
    MemberQueryUseCase memberQueryUseCase
) {

  @PostMapping("/login")
  ResponseEntity<MemberLoginResponse> login(@RequestBody @Valid MemberLoginRequest request) {
    var token = memberQueryUseCase.login(request);
    var response = new MemberLoginResponse(token.accessToken(), token.refreshToken(), token.expiresIn());
    return ResponseEntity.ok(response);
  }
}
