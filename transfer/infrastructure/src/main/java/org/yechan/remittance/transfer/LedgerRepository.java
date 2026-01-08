package org.yechan.remittance.transfer;

public interface LedgerRepository {

  LedgerModel save(LedgerProps props);

}
