package com.onefin.ewallet.bank.dto.bvb;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class BVBVirtualAcctTransSearchResponseContent {
	public BVBVirtualAcctTransSearchResponseContent() {

	}

	@JsonProperty("amount")
	private BigDecimal amount;

	@JsonProperty("ccy")
	private String ccy;

	@JsonProperty("channel")
	private String channel;

	@JsonProperty("creditAccount")
	private String creditAccount;

	@JsonProperty("drcr")
	private String drcr;

	@JsonProperty("externalRefNo")
	private String externalRefNo;

	@JsonProperty("fromAccName")
	private String fromAccName;

	@JsonProperty("fromAccNo")
	private String fromAccNo;

	@JsonProperty("fromBankCode")
	private String fromBankCode;

	@JsonProperty("narrative")
	private String narrative;

	@JsonProperty("partnerCode")
	private String partnerCode;

	@JsonProperty("relatedAccName")
	private String relatedAccName;

	@JsonProperty("relatedAccount")
	private String relatedAccount;

	@JsonProperty("source")
	private String source;

	@JsonProperty("traceId")
	private String traceId;

	@JsonProperty("transactionDate")
	private String transactionDate;

	@JsonProperty("trnRefNo")
	private String trnRefNo;

	@JsonProperty("txnInitDt")
	private String txnInitDt;

	@JsonProperty("valueDt")
	private String valueDt;
}
