package org.yechan.remittance.member;

import java.util.Optional;

public interface MemberRepository {

  MemberModel save(MemberProps props);

  Optional<MemberModel> findById(MemberIdentifier identifier);

  Optional<MemberModel> findByEmail(String email);
}
