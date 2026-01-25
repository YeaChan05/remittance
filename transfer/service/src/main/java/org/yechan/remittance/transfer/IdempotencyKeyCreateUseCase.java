package org.yechan.remittance.transfer;

import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;

public interface IdempotencyKeyCreateUseCase {

  IdempotencyKeyModel create(IdempotencyKeyCreateProps props);
}

interface IdempotencyKeyCreateProps {

  long memberId();

  IdempotencyKeyProps.IdempotencyScopeValue scope();
}

@Slf4j
record IdempotencyKeyService(
    IdempotencyKeyRepository repository,
    Clock clock,
    Duration expiresIn
) implements IdempotencyKeyCreateUseCase {

  @Override
  public IdempotencyKeyModel create(IdempotencyKeyCreateProps props) {
     log.info("idempotency.create.start memberId={} scope={}", props.memberId(),
         props.scope());
    var now = LocalDateTime.now(clock);
    var key = UUID.randomUUID().toString();
     log.info("idempotency.create.persist memberId={} scope={}", props.memberId(),
         props.scope());
    return repository.save(new GeneratedIdempotencyKeyProps(props, key, now));
  }

  private class GeneratedIdempotencyKeyProps implements IdempotencyKeyProps {

    private final IdempotencyKeyCreateProps props;
    private final String key;
    private final LocalDateTime now;

    public GeneratedIdempotencyKeyProps(IdempotencyKeyCreateProps props, String key,
        LocalDateTime now) {
      this.props = props;
      this.key = key;
      this.now = now;
    }

    @Override
    public Long memberId() {
      return props.memberId();
    }

    @Override
    public String idempotencyKey() {
      return key;
    }

    @Override
    public LocalDateTime expiresAt() {
      return now.plus(expiresIn);
    }

    @Override
    public IdempotencyScopeValue scope() {
      return props.scope();
    }

    @Override
    public IdempotencyKeyStatusValue status() {
      return IdempotencyKeyStatusValue.BEFORE_START;
    }

    @Override
    public String requestHash() {
      return null;
    }

    @Override
    public String responseSnapshot() {
      return null;
    }

    @Override
    public LocalDateTime startedAt() {
      return null;
    }

    @Override
    public LocalDateTime completedAt() {
      return null;
    }
  }
}
