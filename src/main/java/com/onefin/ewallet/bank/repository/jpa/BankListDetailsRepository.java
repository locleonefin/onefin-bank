package com.onefin.ewallet.bank.repository.jpa;

import com.onefin.ewallet.common.domain.bank.common.BankListDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface BankListDetailsRepository extends JpaRepository<BankListDetails, UUID> {

	@Query(value = "SELECT * FROM bank_list_details e WHERE e.citad_id <> '' ORDER BY e.updated_date", nativeQuery = true)
	List<BankListDetails> findByCitadNotEmpty();

	@Query(value = "SELECT * FROM bank_list_details e WHERE e.napas_bin <> '' AND e.citad_id <> '' ORDER BY e.updated_date", nativeQuery = true)
	List<BankListDetails> findByNapasBinNotEmpty();

	BankListDetails findByBranchCode(String branchCode);

	@Query(value = "SELECT * FROM bank_list_details e WHERE e.bank_list_code = :citad", nativeQuery = true)
	List<BankListDetails> findAllBranchByCitadCode(@Param("citad") String citad);

	@Query(value = "SELECT * FROM bank_list_details e WHERE e.bank_list_code IN (:citad) AND e.province = :province", nativeQuery = true)
	List<BankListDetails> findAllBranchByBankListCodeAndProvince(@Param("citad") List<String> citad, @Param("province") String province);

	@Query(value = "SELECT MAX(id) FROM bank_list_details", nativeQuery = true)
	int findMaxInColumnId();

}
