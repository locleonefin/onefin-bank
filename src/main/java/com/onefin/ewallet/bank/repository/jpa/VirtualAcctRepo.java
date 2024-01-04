package com.onefin.ewallet.bank.repository.jpa;

import com.onefin.ewallet.common.domain.bank.vietin.VietinVirtualAcctTable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface VirtualAcctRepo extends JpaRepository<VietinVirtualAcctTable, UUID> {

	@Modifying(clearAutomatically = true, flushAutomatically = true)
	@Transactional
	@Query(value = "Update vietin_virtual_acct Set virtual_acct_status = :virtual_acct_status, updated_date = CURRENT_TIMESTAMP Where virtual_acct_var = :virtual_acct_var AND bank_code = :bankCode", nativeQuery = true)
	public void updateVietinVirtualAcctStatus(@Param("virtual_acct_var") String virtual_acct_var, @Param("virtual_acct_status") String virtual_acct_status, @Param("bankCode") String bankCode);

	@Query(value = "SELECT COUNT(*) FROM vietin_virtual_acct WHERE pool_name = :poolName AND bank_code = :bankCode", nativeQuery = true)
	int countVirtualAcct(@Param("poolName") String poolName, @Param("bankCode") String bankCode);

	@Query(value = "SELECT COUNT(*) FROM vietin_virtual_acct WHERE inUse = :inUse AND bank_code = :bankCode", nativeQuery = true)
	int countVirtualAcctByInUse(@Param("inUse") boolean inUse, @Param("bankCode") String bankCode);

	@Query(value = "SELECT * FROM vietin_virtual_acct WHERE virtual_acct_var = :virtualAcctVar AND bank_code = :bankCode ORDER BY created_date desc LIMIT 1", nativeQuery = true)
	Optional<VietinVirtualAcctTable> findFirstByVirtualAcctVar(@Param("virtualAcctVar") String virtualAcctVar, @Param("bankCode") String bankCode);

	@Query(value = "SELECT * FROM vietin_virtual_acct WHERE virtual_acct_id = :virtualAcctVarId AND bank_code = :bankCode ORDER BY created_date desc LIMIT 1", nativeQuery = true)
	Optional<VietinVirtualAcctTable> findFirstByVirtualAcctVarId(@Param("virtualAcctVarId") String virtualAcctVarId, @Param("bankCode") String bankCode);


	@Query(value = "SELECT * FROM vietin_virtual_acct e WHERE e.virtual_acct_id = :virtualAcctId AND e.in_use  = :inUse AND e.bank_code = :bankCode", nativeQuery = true)
	Optional<VietinVirtualAcctTable> findFirstByVirtualAcctIdAndInUse(@Param("virtualAcctId") String virtualAcctId, @Param("inUse") boolean inUse, @Param("bankCode") String bankCode);

	@Query(value = "SELECT * FROM vietin_virtual_acct e WHERE e.virtual_acct_id = :virtualAcctId AND e.in_use  = :inUse AND e.bank_code = :bankCode", nativeQuery = true)
	Optional<VietinVirtualAcctTable> findByVirtualAcctIdAndInUse(@Param("virtualAcctId") String virtualAcctId, @Param("inUse") boolean inUse, @Param("bankCode") String bankCode);

	@Query(value = "SELECT * FROM vietin_virtual_acct e WHERE e.in_use  = :inUse AND e.pool_name  = :poolName AND e.bank_code = :bankCode ORDER BY e.release_time ASC LIMIT 1", nativeQuery = true)
	Optional<VietinVirtualAcctTable> findFirstByInUseAndPoolName(@Param("inUse") boolean inUse, @Param("poolName") String poolName, @Param("bankCode") String bankCode);

	@Query(value = "SELECT * FROM vietin_virtual_acct e WHERE e.in_use  = :inUse AND bank_code = :bankCode AND e.release_time + interval :expireBuffer minute <= :currDate", nativeQuery = true)
	List<VietinVirtualAcctTable> findByInUseAndBufferExpired(@Param("inUse") boolean inUse, @Param("currDate") Date currDate, @Param("expireBuffer") int expireBuffer, @Param("bankCode") String bankCode);

	@Query(value = "SELECT * FROM vietin_virtual_acct e WHERE e.virtual_acct_id =:virtualAcctId AND e.in_use  = :inUse AND e.bank_code = :bankCode AND :currDate <= e.release_time", nativeQuery = true)
	VietinVirtualAcctTable findByVirtualAcctIdAndInUseAndNotExpired(@Param("virtualAcctId") String virtualAcctId, @Param("inUse") boolean inUse, @Param("currDate") Date currDate, @Param("bankCode") String bankCode);

	@Modifying(clearAutomatically = true, flushAutomatically = true)
	@Transactional
	@Query(value = "Update vietin_virtual_acct Set in_use = :inUse, updated_date = :currDate, release_time = :releaseDate Where virtual_acct_var = :virtualAcctVar and in_use != :inUse AND bank_code = :bankCode", nativeQuery = true)
	void updateVietinVirtualAcctInUse(@Param("virtualAcctVar") String virtual_acct_var, @Param("inUse") boolean inUse, @Param("currDate") Date currDate, @Param("releaseDate") Date releaseDate, @Param("bankCode") String bankCode);

	@Modifying(clearAutomatically = true, flushAutomatically = true)
	@Transactional
	@Query(value = "Update vietin_virtual_acct Set in_use = :inUse, updated_date = :currDate, release_time = :releaseDate Where virtual_acct_id = :virtualAcctId AND bank_code = :bankCode", nativeQuery = true)
	void updateVietinVirtualAcctIdInUse(@Param("virtualAcctId") String virtualAcctId, @Param("inUse") boolean inUse, @Param("currDate") Date currDate, @Param("releaseDate") Date releaseDate, @Param("bankCode") String bankCode);

	@Modifying(clearAutomatically = true, flushAutomatically = true)
	@Transactional
	@Query(value = "Update vietin_virtual_acct Set virtual_acct_status = :virtualAcctStatus, re_open_date = :reOpenDate, updated_date = :currDate Where virtual_acct_id = :virtualAcctId AND bank_code = :bankCode", nativeQuery = true)
	void updateBVBVirtualAcctReopenDate(@Param("virtualAcctId") String virtualAcctId,
										@Param("reOpenDate") Date reOpenDate,
										@Param("virtualAcctStatus") String virtualAcctStatus,
										@Param("currDate") Date currDate,
										@Param("bankCode") String bankCode);

	@Modifying(clearAutomatically = true, flushAutomatically = true)
	@Transactional
	@Query(value = "Update vietin_virtual_acct Set  virtual_acct_status = :virtualAcctStatus, close_date = :closeDate, updated_date = :currDate Where virtual_acct_id = :virtualAcctId AND bank_code = :bankCode", nativeQuery = true)
	void updateBVBVirtualAcctCloseDate(@Param("virtualAcctId") String virtualAcctId,
									   @Param("closeDate") Date closeDate,
									   @Param("virtualAcctStatus") String virtualAcctStatus,
									   @Param("currDate") Date currDate,
									   @Param("bankCode") String bankCode);

	@Modifying(clearAutomatically = true, flushAutomatically = true)
	@Transactional
	@Query(value = "Update vietin_virtual_acct Set virtual_acct_status = :virtualAcctStatus, re_open_date = :reOpenDate, in_use = :inUse, updated_date = :currDate, release_time = :releaseDate Where virtual_acct_var = :virtualAcctVar and in_use != :inUse AND bank_code = :bankCode", nativeQuery = true)
	void updateBVBVirtualAcctInUseReopenDate(@Param("virtualAcctVar") String virtual_acct_var,
											 @Param("inUse") boolean inUse,
											 @Param("virtualAcctStatus") String virtualAcctStatus,
											 @Param("reOpenDate") Date reOpenDate,
											 @Param("currDate") Date currDate,
											 @Param("releaseDate") Date releaseDate,
											 @Param("bankCode") String bankCode);

	@Modifying(clearAutomatically = true, flushAutomatically = true)
	@Transactional
	@Query(value = "Update vietin_virtual_acct Set virtual_acct_name = :virtualAcctName, fixed_amount = :fixedAmount, updated_date = :currDate Where virtual_acct_id = :virtualAcctId AND bank_code = :bankCode", nativeQuery = true)
	void updateBVBVirtualAcctNameAndFixAmount(@Param("virtualAcctId") String virtualAcctId,
											  @Param("virtualAcctName") String virtualAcctName,
											  @Param("fixedAmount") BigDecimal fixedAmount,
											  @Param("currDate") Date currDate,
											  @Param("bankCode") String bankCode);

	@Modifying(clearAutomatically = true, flushAutomatically = true)
	@Transactional
	@Query(value = "Update vietin_virtual_acct Set acc_name = :accName, customer_name = :clientUserId, virtual_acct_name = :virtualAcctName, fixed_amount = :fixedAmount, updated_date = :currDate Where virtual_acct_id = :virtualAcctId AND bank_code = :bankCode", nativeQuery = true)
	void updateBVBVirtualAcctUpdateApi(@Param("virtualAcctId") String virtualAcctId,
									   @Param("virtualAcctName") String virtualAcctName,
									   @Param("fixedAmount") BigDecimal fixedAmount,
									   @Param("clientUserId") String clientUserId,
									   @Param("accName") String accName,
									   @Param("currDate") Date currDate,
									   @Param("bankCode") String bankCode);

	@Modifying(clearAutomatically = true, flushAutomatically = true)
	@Transactional
	@Query(value = "Update vietin_virtual_acct Set acc_name = :accName, customer_name = :clientUserId, virtual_acct_name = :virtualAcctName, fixed_amount = :fixedAmount, virtual_acct_status = :virtualAcctStatus, re_open_date = :reOpenDate, updated_date = :currDate Where virtual_acct_id = :virtualAcctId AND bank_code = :bankCode", nativeQuery = true)
	void updateBVBVirtualAcctReopenApi(@Param("virtualAcctId") String virtualAcctId,
									   @Param("reOpenDate") Date reOpenDate,
									   @Param("virtualAcctStatus") String virtualAcctStatus,
									   @Param("fixedAmount") BigDecimal fixedAmount,
									   @Param("clientUserId") String clientUserId,
									   @Param("virtualAcctName") String virtualAcctName,
									   @Param("accName") String accName,
									   @Param("currDate") Date currDate,
									   @Param("bankCode") String bankCode);

	@Modifying(clearAutomatically = true, flushAutomatically = true)
	@Transactional
	@Query(value = "Update vietin_virtual_acct Set acc_name = :accName, close_date = :closeDate, customer_name = :clientUserId, virtual_acct_name = :virtualAcctName, fixed_amount = :fixedAmount, virtual_acct_status = :virtualAcctStatus, re_open_date = :reOpenDate, updated_date = :currDate Where virtual_acct_id = :virtualAcctId AND bank_code = :bankCode", nativeQuery = true)
	void updateBVBVirtualAcctNotifyApi(@Param("virtualAcctId") String virtualAcctId,
									   @Param("reOpenDate") Date reOpenDate,
									   @Param("closeDate") Date closeDate,
									   @Param("virtualAcctStatus") String virtualAcctStatus,
									   @Param("fixedAmount") BigDecimal fixedAmount,
									   @Param("clientUserId") String clientUserId,
									   @Param("virtualAcctName") String virtualAcctName,
									   @Param("accName") String accName,
									   @Param("currDate") Date currDate,
									   @Param("bankCode") String bankCode);

	@Modifying(clearAutomatically = true, flushAutomatically = true)
	@Transactional
	@Query(value = "Update vietin_virtual_acct Set acc_name = :accName, customer_name = :clientUserId, virtual_acct_name = :virtualAcctName, fixed_amount = :fixedAmount, virtual_acct_status = :virtualAcctStatus, re_open_date = :reOpenDate, updated_date = :currDate Where virtual_acct_id = :virtualAcctId AND bank_code = :bankCode", nativeQuery = true)
	void updateBVBVirtualAcctReopenNotifyApi(@Param("virtualAcctId") String virtualAcctId,
											 @Param("reOpenDate") Date reOpenDate,
											 @Param("virtualAcctStatus") String virtualAcctStatus,
											 @Param("fixedAmount") BigDecimal fixedAmount,
											 @Param("clientUserId") String clientUserId,
											 @Param("virtualAcctName") String virtualAcctName,
											 @Param("accName") String accName,
											 @Param("currDate") Date currDate,
											 @Param("bankCode") String bankCode);

	@Modifying(clearAutomatically = true, flushAutomatically = true)
	@Transactional
	@Query(value = "Update vietin_virtual_acct Set acc_name = :accName, close_date = :closeDate, customer_name = :clientUserId, virtual_acct_name = :virtualAcctName, fixed_amount = :fixedAmount, virtual_acct_status = :virtualAcctStatus, updated_date = :currDate Where virtual_acct_id = :virtualAcctId AND bank_code = :bankCode", nativeQuery = true)
	void updateBVBVirtualAcctCloseNotifyApi(@Param("virtualAcctId") String virtualAcctId,
											@Param("closeDate") Date closeDate,
											@Param("virtualAcctStatus") String virtualAcctStatus,
											@Param("fixedAmount") BigDecimal fixedAmount,
											@Param("clientUserId") String clientUserId,
											@Param("virtualAcctName") String virtualAcctName,
											@Param("accName") String accName,
											@Param("currDate") Date currDate,
											@Param("bankCode") String bankCode);

	@Modifying(clearAutomatically = true, flushAutomatically = true)
	@Transactional
	@Query(value = "Update vietin_virtual_acct Set in_use = :inUse, updated_date = :currDate, release_time = :releaseDate Where virtual_acct_id = :virtualAcctId and in_use != :inUse AND bank_code = :bankCode", nativeQuery = true)
	void updateVirtualAcctInUse(@Param("virtualAcctId") String virtual_acct_id, @Param("inUse") boolean inUse, @Param("currDate") Date currDate, @Param("releaseDate") Date releaseDate, @Param("bankCode") String bankCode);

	@Query(value = "SELECT * FROM vietin_virtual_acct WHERE virtual_acct_id = :virtualAcctId AND bank_code = :bankCode ORDER BY created_date desc LIMIT 1", nativeQuery = true)
	Optional<VietinVirtualAcctTable> findFirstByVirtualAcctId(@Param("virtualAcctId") String virtualAcctId, @Param("bankCode") String bankCode);

	@Modifying(clearAutomatically = true, flushAutomatically = true)
	@Transactional
	@Query(value = "Update vietin_virtual_acct Set acc_name = :accName, customer_name = :clientUserId, virtual_acct_name = :virtualAcctName, fixed_amount = :fixedAmount, virtual_acct_status = :virtualAcctStatus, updated_date = :currDate Where virtual_acct_id = :virtualAcctId AND bank_code = :bankCode", nativeQuery = true)
	void updateBVBVirtualAcctOpenNotifyApi(@Param("virtualAcctId") String virtualAcctId,
										   @Param("virtualAcctStatus") String virtualAcctStatus,
										   @Param("fixedAmount") BigDecimal fixedAmount,
										   @Param("clientUserId") String clientUserId,
										   @Param("virtualAcctName") String virtualAcctName,
										   @Param("accName") String accName,
										   @Param("currDate") Date currDate,
										   @Param("bankCode") String bankCode);

	@Query(value = "SELECT * FROM vietin_virtual_acct e WHERE e.in_use  = :inUse AND e.release_time + interval :expireBuffer minute <= :currDate", nativeQuery = true)
	List<VietinVirtualAcctTable> findAllByInUseAndBufferExpired(@Param("inUse") boolean inUse, @Param("currDate") Date currDate, @Param("expireBuffer") int expireBuffer);

}
