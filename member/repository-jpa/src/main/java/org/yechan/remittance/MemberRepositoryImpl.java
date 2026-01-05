package org.yechan.remittance;

import java.util.Optional;

public class MemberRepositoryImpl implements MemberRepository {

  private final MemberJpaRepository repository;

  public MemberRepositoryImpl(MemberJpaRepository repository) {
    this.repository = repository;
  }

  @Override
  public MemberModel save(MemberProps props) {
    MemberEntity entity = MemberEntity.create(props.name());
    return repository.save(entity);
  }

  @Override
  public Optional<MemberModel> findById(MemberIdentifier identifier) {
    return repository.findById(identifier.memberId())
        .map(memberEntity -> memberEntity);
  }
}
