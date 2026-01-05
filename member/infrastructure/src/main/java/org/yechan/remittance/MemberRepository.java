package org.yechan.remittance;

import java.util.Optional;

public interface MemberRepository {

  MemberModel save(MemberProps props);

  Optional<MemberModel> findById(MemberIdentifier identifier);
}
