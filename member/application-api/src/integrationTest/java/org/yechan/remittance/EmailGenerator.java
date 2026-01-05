package org.yechan.remittance;

import java.util.UUID;

public class EmailGenerator {

  public static String generate() {
    return UUID.randomUUID() + "@example.com";
  }
}
