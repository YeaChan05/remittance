package org.yechan.remittance.transfer;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Import;

@AutoConfiguration
@Import(TransferController.class)
public class TransferApiAutoConfiguration {

}
