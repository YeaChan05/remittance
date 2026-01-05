package org.yechan.remittance;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PasswordGenerator {

  private static final String LETTERS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
  private static final String DIGITS = "0123456789";
  private static final String SPECIALS = "!@#$%^&*";
  private static final String ALL = LETTERS + DIGITS + SPECIALS;

  private static final SecureRandom random = new SecureRandom();

  public static String generate() {
    List<Character> chars = new ArrayList<>();

    // 정규식의 lookahead 조건을 먼저 강제
    chars.add(LETTERS.charAt(random.nextInt(LETTERS.length())));
    chars.add(DIGITS.charAt(random.nextInt(DIGITS.length())));
    chars.add(SPECIALS.charAt(random.nextInt(SPECIALS.length())));

    // 나머지 채우기 (총 10자)
    while (chars.size() < 10) {
      chars.add(ALL.charAt(random.nextInt(ALL.length())));
    }

    // 순서 랜덤화
    Collections.shuffle(chars, random);

    StringBuilder sb = new StringBuilder();
    for (char c : chars) {
      sb.append(c);
    }
    return sb.toString();
  }
}
