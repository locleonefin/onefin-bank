package com.onefin.ewallet.bank.dto.bvb;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
public class BVBCallbackAPIBatchTransData {


	@JsonProperty("amount")
	private BigDecimal amount;

	@JsonProperty("ccy")
	private String ccy = "";

	@JsonProperty("channel")
	private String channel = "";

	@JsonProperty("crAcc")
	private String crAcc = "";

	@JsonProperty("externalRefNo")
	private String externalRefNo = "";

	@JsonProperty("fromAccName")
	private String fromAccName = "";

	@JsonProperty("fromAccNo")
	private String fromAccNo = "";

	@JsonProperty("fromBankCode")
	private String fromBankCode = "";

	@JsonProperty("fromBankName")
	private String fromBankName = "";

	@JsonProperty("narrative")
	private String narrative = "";

	@JsonProperty("partnerCode")
	private String partnerCode = "";

	@JsonProperty("partnerId")
	private String partnerId = "";

	@JsonProperty("relatedAcc")
	private String relatedAcc = "";

	@JsonProperty("relatedAccount")
	private String relatedAccount = "";

	@JsonProperty("relatedAccName")
	private String relatedAccName = "";

	@JsonProperty("traceId")
	private String traceId = "";

	@JsonProperty("trnRefNo")
	private String trnRefNo = "";

	@JsonProperty("valueDt")
	private String valueDt;

	@JsonProperty("txnInitDt")
	private String txnInitDt;

	@JsonProperty("drcr")
	private String drcr = "";

	@JsonProperty("clientUserID")
	private String clientUserID = "";

	@JsonProperty("creditAccount")
	private String creditAccount = "";

	@JsonProperty("transactionDate")
	private String transactionDate;

	@JsonProperty("source")
	private String source = "";

	@JsonProperty("napasTraceId")
	private String napasTraceId;

	public BVBCallbackAPIBatchTransData() {

	}
}
