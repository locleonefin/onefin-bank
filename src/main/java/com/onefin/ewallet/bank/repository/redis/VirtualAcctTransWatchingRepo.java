package com.onefin.ewallet.bank.repository.redis;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VirtualAcctTransWatchingRepo extends JpaRepository<VirtualAcctTransWatching, String> {

}
