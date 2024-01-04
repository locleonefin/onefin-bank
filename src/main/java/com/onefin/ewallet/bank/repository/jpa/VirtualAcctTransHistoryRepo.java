package com.onefin.ewallet.bank.repository.jpa;

import com.onefin.ewallet.common.domain.bank.vietin.VietinVirtualAcctTransHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@RepositoryRestResource(collectionResourceRel = "metaDatas", exported = false)
public interface VirtualAcctTransHistoryRepo extends JpaRepository<VietinVirtualAcctTransHistory, UUID> {

	@Query(value = "SELECT * FROM vietin_virtual_acct_trans_history WHERE virtual_acct_var = :virtualAcctVar AND tran_status = :tranStatus AND bank_code = :bankCode", nativeQuery = true)
	List<VietinVirtualAcctTransHistory> findByVirtualAcctVarAndTranStatus(@Param("virtualAcctVar") String virtualAcctVar, @Param("tranStatus") String tranStatus, @Param("bankCode") String bankCode);

	@Query(value = "SELECT * FROM vietin_virtual_acct_trans_history WHERE virtual_acct_id = :virtualAcctId AND tran_status = :tranStatus AND amount = :amount AND bank_code = :bankCode", nativeQuery = true)
	List<VietinVirtualAcctTransHistory> findByVirtualAcctIdAndTranStatusAndAmount(@Param("virtualAcctId") String virtualAcctId, @Param("tranStatus") String tranStatus, @Param("amount") BigDecimal amount, @Param("bankCode") String bankCode);

	@Query(value = "SELECT * FROM vietin_virtual_acct_trans_history WHERE virtual_acct_id = :virtualAcctId AND tran_status = :tranStatus AND amount = :amount AND bank_code = :bankCode AND CURRENT_TIMESTAMP < created_date +  INTERVAL :hourInterval HOUR  ORDER BY created_date DESC LIMIT 1", nativeQuery = true)
	List<VietinVirtualAcctTransHistory> findByVirtualAcctIdAndTranStatusAndAmountAndLimit1(@Param("virtualAcctId") String virtualAcctId, @Param("tranStatus") String tranStatus, @Param("amount") BigDecimal amount, @Param("hourInterval") int hourInterval, @Param("bankCode") String bankCode);

	@Modifying(clearAutomatically = true, flushAutomatically = true)
	@Transactional
	@Query(value = "Update vietin_virtual_acct_trans_history Set tran_status = :tranStatus, updated_date = :currDate, vietin_noti_uuid = :vietinNotiUuid Where virtual_acct_id = :virtualAcctId AND tran_status = :queryTranStatus AND bank_code = :bankCode AND trans_unique_key = :transUniqueKey", nativeQuery = true)
	void updateVietinVirtualAcctByStatus(@Param("tranStatus") String tranStatus,
										 @Param("currDate") Date currDate,
										 @Param("vietinNotiUuid") UUID vietinNotiUuid,
										 @Param("virtualAcctId") String virtualAcctId,
										 @Param("queryTranStatus") String queryTranStatus,
										 @Param("bankCode") String bankCode,
										 @Param("transUniqueKey") String transUniqueKey

	);

	@Modifying(clearAutomatically = true, flushAutomatically = true)
	@Transactional
	@Query(value = "Update vietin_virtual_acct_trans_history Set tran_status = :tranStatus, updated_date = :currDate, vietin_noti_uuid = :vietinNotiUuid Where virtual_acct_id = :virtualAcctId AND tran_status = :queryTranStatus AND bank_code = :bankCode AND amount = :amount", nativeQuery = true)
	void updateVietinVirtualAcctByStatus(@Param("tranStatus") String tranStatus,
										 @Param("currDate") Date currDate,
										 @Param("vietinNotiUuid") UUID vietinNotiUuid,
										 @Param("virtualAcctId") String virtualAcctId,
										 @Param("queryTranStatus") String queryTranStatus,
										 @Param("bankCode") String bankCode,
										 @Param("amount") BigDecimal amount
	);

	@Modifying(clearAutomatically = true, flushAutomatically = true)
	@Transactional
	@Query(value = "Update vietin_virtual_acct_trans_history Set tran_status = :tranStatus, updated_date = :currDate, vietin_noti_uuid = :vietinNotiUuid Where id = :id  AND bank_code = :bankCode", nativeQuery = true)
	void updateVirtualAcctById(@Param("tranStatus") String tranStatus, @Param("currDate") Date currDate, @Param("vietinNotiUuid") UUID vietinNotiUuid, @Param("id") UUID id, @Param("bankCode") String bankCode);

	@Query(value = "SELECT * FROM vietin_virtual_acct_trans_history WHERE trans_unique_key = :transUniqueKey AND merchant_code =:merchantCode AND expire_time >= :currDate AND tran_status =:tranStatus", nativeQuery = true)
	VietinVirtualAcctTransHistory findByTransUniqueKeyAndMerchantCodeAndNotExpireAndTranStatus(@Param("transUniqueKey") String transUniqueKey, @Param("merchantCode") String merchantCode, @Param("currDate") Date currDate, @Param("tranStatus") String tranStatus);

	@Query(value = "SELECT * FROM vietin_virtual_acct_trans_history WHERE trans_unique_key = :transUniqueKey AND merchant_code =:merchantCode", nativeQuery = true)
	VietinVirtualAcctTransHistory findByTransUniqueKeyAndMerchantCode(@Param("transUniqueKey") String transUniqueKey, @Param("merchantCode") String merchantCode);

	@Query(value = "SELECT * FROM vietin_virtual_acct_trans_history WHERE tran_status = :transStatus AND virtual_acct_id = :virtualAcctId AND bank_code = :bankCode", nativeQuery = true)
	VietinVirtualAcctTransHistory findByTransStatusAndVirtualAcctId(@Param("virtualAcctId") String virtualAcctId, @Param("transStatus") String transStatus, @Param("bankCode") String bankCode);

	@Modifying(clearAutomatically = true, flushAutomatically = true)
	@Transactional
	@Query(value = "Update vietin_virtual_acct_trans_history Set customer_inquiry = :customerInquiry, updated_date = :currDate Where virtual_acct_id = :virtualAcctId AND bank_code = :bankCode AND tran_status = :queryTranStatus", nativeQuery = true)
	void updateCustomerInquiryVietinVirtualAcct(@Param("customerInquiry") boolean customerInquiry, @Param("currDate") Date currDate, @Param("virtualAcctId") String virtualAcctId, @Param("queryTranStatus") String queryTranStatus, @Param("bankCode") String bankCode);

	@Query(value = "SELECT * FROM vietin_virtual_acct_trans_history WHERE virtual_acct_id = :virtualAcctId AND tran_status = :tranStatus AND bank_code = :bankCode", nativeQuery = true)
	List<VietinVirtualAcctTransHistory> findByVirtualAcctVarIdAndTranStatus(@Param("virtualAcctId") String virtualAcctId, @Param("tranStatus") String tranStatus, @Param("bankCode") String bankCode);

}
