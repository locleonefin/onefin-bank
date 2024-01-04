package com.onefin.ewallet.bank.repository.jpa;

import com.onefin.ewallet.common.domain.bank.vietin.VietinNotifyTransTable;
import com.onefin.ewallet.common.domain.bank.vietin.VietinVirtualAcctTransHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface VietinNotifyTransTableRepo extends JpaRepository<VietinNotifyTransTable, UUID> {

	@Query(value = "SELECT * FROM vietin_notify_trans WHERE msg_type = :msgType AND trans_id = :transId AND remark = :remark AND amount = :amount AND bank_code = :bankCode ORDER BY created_date DESC LIMIT 1", nativeQuery = true)
	Optional<VietinNotifyTransTable> findByTransIdAndAmountAndBankCodeAndMsgTypeAndLimit1(
			@Param("transId") String transId,
			@Param("amount") String amount,
			@Param("bankCode") String bankCode,
			@Param("remark") String remark,
			@Param("msgType") String msgType);

}
