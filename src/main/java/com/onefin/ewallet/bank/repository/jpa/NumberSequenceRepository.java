package com.onefin.ewallet.bank.repository.jpa;

import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import com.onefin.ewallet.common.base.repository.sequenceTrans.INumberSequenceRepository;

@RepositoryRestResource(collectionResourceRel = "metaDatas", exported = false)
public interface NumberSequenceRepository extends INumberSequenceRepository {

}