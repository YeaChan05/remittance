package org.yechan.remittance;

import java.util.Arrays;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

  private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

  @ExceptionHandler(BusinessException.class)
  public ResponseEntity<?> handleBusinessException(BusinessException e) {
    log.error(Arrays.toString(e.getStackTrace()));
    return ResponseEntity.status(e.getStatus().toHttpStatus()).body(e.getMessage());
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  ResponseEntity<?> handleValidationException(MethodArgumentNotValidException e) {
    log.error(Arrays.toString(e.getStackTrace()));
    return ResponseEntity.badRequest().body(e.getMessage());
  }
}
