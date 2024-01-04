package com.onefin.ewallet.bank.dto.bvb.bankTransfer;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class BVBIBFTInquiryEscrowAccountResponseData {

	@NotNull(message = "balance cannot be null")
	@JsonProperty("balance")
	private String balance;

	@NotNull(message = "responseTime cannot be null")
	@JsonProperty("responseTime")
	private String responseTime;

	private String description;
}
