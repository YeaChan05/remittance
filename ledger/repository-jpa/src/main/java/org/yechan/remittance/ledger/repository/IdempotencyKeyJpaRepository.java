package org.yechan.remittance.ledger.repository;

import org.springframework.data.jpa.repository.JpaRepository;

interface IdempotencyKeyJpaRepository extends JpaRepository<IdempotencyKeyEntity, Long> {

}
