package org.yechan.remittance.ledger;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Import;

@AutoConfiguration
@Import(IdempotencyKeyController.class)
public class IdempotencyKeyApiAutoConfiguration {

}
