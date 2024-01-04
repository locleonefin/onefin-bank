package com.onefin.ewallet.bank.dto.bvb;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class BVBVirtualAcctCommonPartnerRequest {

	@JsonProperty("accNo")
	private String accNo = "";

	@JsonProperty("accNameSuffix")
	private String accNameSuffix = "";

	@JsonProperty("partnerCode")
	private String partnerCode;

	@JsonProperty("fixedAmount")
	private BigDecimal fixedAmount;

	@JsonProperty("clientUserID")
	private String clientUserId = "";

	public BVBVirtualAcctCommonPartnerRequest() {
	}

}
