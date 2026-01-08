package org.yechan.remittance.transfer;

import java.time.Instant;

public record TransferQueryCondition(
    Instant from,
    Instant to,
    Integer limit
) {

}
