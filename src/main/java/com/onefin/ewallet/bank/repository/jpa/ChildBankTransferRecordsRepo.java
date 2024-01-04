package com.onefin.ewallet.bank.repository.jpa;

import com.onefin.ewallet.common.domain.bank.transfer.BankTransferChildRecords;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(collectionResourceRel = "metaDatas", exported = false)
public interface ChildBankTransferRecordsRepo extends JpaRepository<BankTransferChildRecords, String> {

	BankTransferChildRecords findByTransId(String transId);

}
