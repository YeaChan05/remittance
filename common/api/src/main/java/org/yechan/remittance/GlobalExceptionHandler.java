package org.yechan.remittance;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
  @ExceptionHandler(BusinessException.class)
  public ResponseEntity<?> handleBusinessException(BusinessException e) {
    return ResponseEntity.internalServerError().body(e);
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  ResponseEntity<?> handleValidationException(MethodArgumentNotValidException e) {
    return ResponseEntity.badRequest().body(e.getMessage());
  }
}
