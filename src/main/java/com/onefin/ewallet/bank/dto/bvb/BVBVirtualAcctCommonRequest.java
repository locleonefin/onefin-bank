package com.onefin.ewallet.bank.dto.bvb;


import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BVBVirtualAcctCommonRequest {

	private String from;

	@NotNull
	private String requestId;

	public BVBVirtualAcctCommonRequest() {

	}


}
