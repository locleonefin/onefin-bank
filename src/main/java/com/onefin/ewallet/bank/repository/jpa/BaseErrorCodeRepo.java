package com.onefin.ewallet.bank.repository.jpa;

import com.onefin.ewallet.common.domain.errorCode.BaseErrorCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(collectionResourceRel = "metaDatas", exported = false)
public interface BaseErrorCodeRepo extends JpaRepository<BaseErrorCode, String> {

	BaseErrorCode findByCode(String code);

}
