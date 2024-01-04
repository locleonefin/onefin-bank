package com.onefin.ewallet.bank.dto.cms.bankTransfer;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Data
public class CMSTransInquiryRequest {


	@NotNull(message = "transactionId can't be null")
	@NotEmpty(message = "transactionId can't be empty")
	@JsonProperty("transactionId")
	private String transactionId;

	@NotNull(message = "bankCode can't be null")
	@NotEmpty(message = "bankCode can't be empty")
	@JsonProperty("bankCode")
	private String bankCode;

}
