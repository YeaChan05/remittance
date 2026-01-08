package org.yechan.remittance.transfer;

import java.time.LocalDateTime;

public record TransferQueryCondition(
    LocalDateTime from,
    LocalDateTime to,
    Integer limit
) {

}
