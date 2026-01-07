package org.yechan.remittance.member;


import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.yechan.remittance.member.dto.MemberRegisterRequest;
import org.yechan.remittance.member.dto.MemberRegisterResponse;

@RestController
@RequestMapping("/members")
record MemberController(
    MemberCreateUseCase memberCreateUseCase
) {

  @PostMapping
  ResponseEntity<MemberRegisterResponse> register(
      @RequestBody @Valid MemberRegisterRequest request) {
    var model = memberCreateUseCase.register(request);
    var response = new MemberRegisterResponse(model.name());
    return ResponseEntity.ok(response);
  }
}
