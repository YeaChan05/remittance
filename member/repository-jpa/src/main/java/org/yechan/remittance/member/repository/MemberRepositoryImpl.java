package org.yechan.remittance.member.repository;

import java.util.Optional;
import org.yechan.remittance.member.MemberIdentifier;
import org.yechan.remittance.member.MemberModel;
import org.yechan.remittance.member.MemberProps;
import org.yechan.remittance.member.MemberRepository;

public class MemberRepositoryImpl implements MemberRepository {

  private final MemberJpaRepository repository;

  public MemberRepositoryImpl(MemberJpaRepository repository) {
    this.repository = repository;
  }

  @Override
  public MemberModel save(MemberProps props) {
    MemberEntity entity = MemberEntity.create(props);
    return repository.save(entity);
  }

  @Override
  public Optional<MemberModel> findById(MemberIdentifier identifier) {
    return repository.findById(identifier.memberId())
        .map(memberEntity -> memberEntity);
  }

  @Override
  public Optional<MemberModel> findByEmail(String email) {
    return repository.findByEmail(email)
        .map(memberEntity -> memberEntity);
  }
}
