package com.onefin.ewallet.bank.repository.jpa;

import com.onefin.ewallet.common.domain.bank.vietin.VietinEfastTransactionView;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface VietinEfastTransactionViewRepo extends JpaRepository<VietinEfastTransactionView, UUID> {

	Page<VietinEfastTransactionView> findAll(Specification<VietinEfastTransactionView> spec, Pageable pageable);

}
