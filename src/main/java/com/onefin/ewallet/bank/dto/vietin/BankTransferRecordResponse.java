package com.onefin.ewallet.bank.dto.vietin;

import lombok.Data;

import javax.validation.constraints.Size;

@Data
public class BankTransferRecordResponse {

	private String transId;

	private String status;

	@Size(max = 30)
	private String feeAmount;

	@Size(max = 30)
	private String vatAmount;

	@Size(max = 30)
	private String bankTransactionId;

	private String description;

	@Size(max = 5)
	private String currencyCode;

	private String remark;

}
