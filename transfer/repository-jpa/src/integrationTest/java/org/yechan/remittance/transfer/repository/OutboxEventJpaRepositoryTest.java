package org.yechan.remittance.transfer.repository;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.persistence.EntityManager;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestConstructor;
import org.yechan.remittance.transfer.OutboxEventProps;
import org.yechan.remittance.transfer.OutboxEventProps.OutboxEventStatusValue;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TransferRepositoryAutoConfiguration.class)
@ContextConfiguration(classes = TestApplication.class)
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
class OutboxEventJpaRepositoryTest {

  private final OutboxEventJpaRepository repository;
  private final EntityManager entityManager;

  @Autowired
  OutboxEventJpaRepositoryTest(
      OutboxEventJpaRepository repository,
      EntityManager entityManager
  ) {
    this.repository = repository;
    this.entityManager = entityManager;
  }

  @Test
  void findNewForPublishOrdersAndFilters() {
    var e1 = saveOutboxEvent(OutboxEventStatusValue.NEW);
    saveOutboxEvent(OutboxEventStatusValue.SENT);
    var e3 = saveOutboxEvent(OutboxEventStatusValue.NEW);
    flushClear();

    List<OutboxEventEntity> results = repository.findNewForPublish(
        OutboxEventStatusValue.NEW,
        null,
        Pageable.unpaged()
    );

    assertThat(results).extracting(OutboxEventEntity::eventId)
        .containsExactly(e1.eventId(), e3.eventId());
  }

  @Test
  void findNewForPublishRespectsLimit() {
    var e1 = saveOutboxEvent(OutboxEventStatusValue.NEW);
    saveOutboxEvent(OutboxEventStatusValue.NEW);
    flushClear();

    List<OutboxEventEntity> results = repository.findNewForPublish(
        OutboxEventStatusValue.NEW,
        null,
        PageRequest.of(0, 1)
    );

    assertThat(results).extracting(OutboxEventEntity::eventId)
        .containsExactly(e1.eventId());
  }

  @Test
  void markSentUpdatesStatus() {
    var e1 = saveOutboxEvent(OutboxEventStatusValue.NEW);
    flushClear();

    int updated = repository.markSent(e1.eventId(), OutboxEventStatusValue.SENT);
    flushClear();

    assertThat(updated).isEqualTo(1);
    var found = repository.findById(e1.eventId());
    assertThat(found).isPresent();
    assertThat(found.get().status()).isEqualTo(OutboxEventStatusValue.SENT);
  }

  private OutboxEventEntity saveOutboxEvent(
      OutboxEventStatusValue status
  ) {
    return repository.save(OutboxEventEntity.create(
        new TestOutboxEventProps(status)
    ));
  }

  private void flushClear() {
    entityManager.flush();
    entityManager.clear();
  }

  private record TestOutboxEventProps(
      OutboxEventStatusValue status
  ) implements OutboxEventProps {

    @Override
    public String aggregateType() {
      return "TRANSFER";
    }

    @Override
    public String aggregateId() {
      return UUID.randomUUID().toString();
    }

    @Override
    public String eventType() {
      return "TRANSFER_COMPLETED";
    }

    @Override
    public String payload() {
      return "{\"status\":\"SUCCEEDED\"}";
    }
  }
}
