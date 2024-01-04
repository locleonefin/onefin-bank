package com.onefin.ewallet.bank.repository.jpa;

import com.onefin.ewallet.common.base.repository.mariadb.IBaseTransactionRepository;

import com.onefin.ewallet.common.domain.bank.common.LinkBankTransaction;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.util.List;

@RepositoryRestResource(collectionResourceRel = "metaDatas", exported = false)
public interface LinkBankTransRepo<T extends LinkBankTransaction> extends IBaseTransactionRepository<T> {

	@Query(value = "SELECT * FROM link_bank_transaction e WHERE e.bank = :bank AND e.phone_num = :phoneNum AND e.tran_status = :trxStatus ORDER BY e.created_date DESC", nativeQuery = true)
	List<LinkBankTransaction> findByBankAndPhoneNumAndTranStatus(@Param("bank") String bank, @Param("phoneNum") String phoneNum, @Param("trxStatus") String trxStatus);

	@Query(value = "SELECT * FROM link_bank_transaction e WHERE e.phone_num = :phoneNum AND e.tran_status = :trxStatus AND e.api_operation = :apiOperation ORDER BY e.created_date DESC", nativeQuery = true)
	List<LinkBankTransaction> findByPhoneNumAndTranStatusAndApiOperation(@Param("phoneNum") String phoneNum, @Param("trxStatus") String trxStatus, @Param("apiOperation") String apiOperation);

	@Query(value = "SELECT * FROM link_bank_transaction e WHERE e.phone_num = :phoneNum AND e.vcb_request_id = :ssVcbRequestId AND e.api_operation = :apiOperation ORDER BY e.created_date DESC", nativeQuery = true)
	List<LinkBankTransaction> findByPhoneNumAndVcbRequestIdAndApiOperation(@Param("phoneNum") String phoneNum, @Param("ssVcbRequestId") String ssVcbRequestId, @Param("apiOperation") String apiOperation);

	LinkBankTransaction findByBankAndBankRequestTransAndPhoneNum(String bank, String vcbTrans, String PhoneNum);

	LinkBankTransaction findByBankAndSsRequestIdAndPhoneNum(String bank, String ssRequestId, String phoneNum);

	LinkBankTransaction findByBankAndRequestIdAndTranStatus(String bank, String requestId, String transStatus);

	LinkBankTransaction findByBankAndSsRequestIdAndTranStatus(String bank, String requestId, String transStatus);

	List<LinkBankTransaction> findBySsRequestId(String requestId);

	List<LinkBankTransaction> findByBankAndSsRequestIdAndLinkType(String bank, String requestId, String linkType);

	LinkBankTransaction findByBankAndSsRequestIdAndApiOperation(String bank, String requestId, String apiOperation);

	LinkBankTransaction findByBankAndTranStatusAndTokenStateAndTokenId(String bank, String transStatus, String tokenState, String tokenId);

	@Query(value = "SELECT * FROM link_bank_transaction e WHERE e.bank = :bank AND e.api_operation IN (:linkStatusList) AND e.token_number = :tokenNumber ORDER BY e.created_date DESC", nativeQuery = true)
	List<LinkBankTransaction> findByLinkActionAndToken(@Param("bank") String bank, @Param("linkStatusList") List<String> linkStatusList, @Param("tokenNumber") String tokenNumber);


}
