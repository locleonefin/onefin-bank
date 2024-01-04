package com.onefin.ewallet.bank.dto.vietin;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
@JsonInclude(Include.NON_EMPTY)
public class LinkBankBaseResponse {

	@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
	private String signature;

	private String providerId;

	private String merchantId;

	private String requestId;

	private Status status;

	private String bankTransactionId;

	private String description;

	private String token;

	private String tokenIssueDate;

	private String tokenExpireDate;

	private String cardMask;

	private List<Balance> balances;

}
