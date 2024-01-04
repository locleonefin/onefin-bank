package com.onefin.ewallet.bank.dto.cms.bankTransfer;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.onefin.ewallet.bank.dto.vietin.Status;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.math.BigInteger;

@Data
public class CMSBankTransferResponse {

	@NotNull(message = "trxRefNo can't be null")
	@JsonProperty("status")
	private Status status;

	@NotNull(message = "transactionId can't be null")
	@NotEmpty(message = "transactionId can't be empty")
	@JsonProperty("transactionId")
	private String transactionId;

	private String accountName;

	private String bankId;

	private String branchId;

	private String bankName;

	private BigInteger processingFee;

	private String transDescription;

	private String currency;

	private int processedRecord;

	private String bankTransactionId;

}
