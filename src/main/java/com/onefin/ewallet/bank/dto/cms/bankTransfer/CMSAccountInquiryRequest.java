package com.onefin.ewallet.bank.dto.cms.bankTransfer;

import com.onefin.ewallet.common.base.anotation.MinMaxFieldLengthConstraint;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Data
public class CMSAccountInquiryRequest {

	@MinMaxFieldLengthConstraint(message = "invalid account id")
	private String accountId;

	@NotNull(message = "transactionMethod can't be null")
	@NotEmpty(message = "transactionMethod can't be empty")
	private String transactionMethod;

	private String bankId;

	private Boolean isCard = false;

	@NotNull(message = "bankCode can't be null")
	@NotEmpty(message = "bankCode can't be empty")
	private String bankCode;
}
