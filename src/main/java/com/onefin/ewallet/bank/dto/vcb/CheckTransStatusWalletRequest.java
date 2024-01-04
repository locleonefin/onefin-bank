package com.onefin.ewallet.bank.dto.vcb;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.onefin.ewallet.bank.common.VcbConstants;
import lombok.Data;

import javax.validation.constraints.NotEmpty;

@Data
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class CheckTransStatusWalletRequest {

	@NotEmpty(message = "Not empty requestId")
	private String queryRequestId;

	private VcbConstants.TransQueryType queryType;

	@NotEmpty(message = "Not empty lang")
	private String lang;

}
