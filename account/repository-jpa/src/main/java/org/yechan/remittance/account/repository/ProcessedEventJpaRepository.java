package org.yechan.remittance.account.repository;

import org.springframework.data.jpa.repository.JpaRepository;

interface ProcessedEventJpaRepository extends JpaRepository<ProcessedEventEntity, Long> {

  boolean existsByEventId(Long eventId);
}
