package com.onefin.ewallet.bank.dto.cms.bankTransfer;

import lombok.Data;

import java.math.BigInteger;

@Data
public class CMSCallbackMessage {

	private String productCode;

	private CMSCallbackStatus status;

	private String transactionId;

	private String trxRefNo;

	private String accountName;

	private String bankId;

	private String branchId;

	private String currency;

	private int processedRecord;

	private BigInteger processingFee;

	private String transDescription;
}
