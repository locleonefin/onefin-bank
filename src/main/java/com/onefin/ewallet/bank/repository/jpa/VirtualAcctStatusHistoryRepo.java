package com.onefin.ewallet.bank.repository.jpa;

import com.onefin.ewallet.common.domain.bank.vietin.VietinVirtualAcctStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface VirtualAcctStatusHistoryRepo extends JpaRepository<VietinVirtualAcctStatusHistory, UUID> {
}
