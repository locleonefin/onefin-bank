package com.onefin.ewallet.bank.dto.bvb.bankTransfer;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Data
public class BVBIBFTInquiryRequestData {

	@NotNull(message = "accountNo cannot be null")
	private String accountNo;

	@NotNull(message = "cardNo cannot be null")
	private String cardNo;

	@NotNull(message = "bankCode cannot be null")
	private String bankCode;

	@Size(max = 1)
	@NotNull(message = "onus cannot be null")
	@JsonProperty("onus")
	private String onus;

	private String description;

}
