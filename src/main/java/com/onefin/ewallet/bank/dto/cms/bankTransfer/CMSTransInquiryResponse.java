package com.onefin.ewallet.bank.dto.cms.bankTransfer;


import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.onefin.ewallet.bank.dto.vietin.Status;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.math.BigInteger;

@Data
public class CMSTransInquiryResponse {

	@NotNull(message = "productCode can't be null")
	@NotEmpty(message = "productCode can't be empty")
	@JsonProperty("productCode")
	private String productCode;

	@NotNull(message = "trxRefNo can't be null")
	@JsonProperty("status")
	private Status status;

	@NotNull(message = "transactionId can't be null")
	@NotEmpty(message = "transactionId can't be empty")
	@JsonProperty("transactionId")
	private String transactionId;

	private int processedRecord;

	private String accountName;

	private String bankId;

	private String branchId;

	private String bankName;

	@JsonSerialize(using = ToStringSerializer.class)
	private BigInteger processingFee;

	private String transDescription;

	private String currency;

	private String bankTransId;

}
