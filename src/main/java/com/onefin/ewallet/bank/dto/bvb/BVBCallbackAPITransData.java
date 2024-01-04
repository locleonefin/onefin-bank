package com.onefin.ewallet.bank.dto.bvb;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BVBCallbackAPITransData {

	@JsonProperty("externalRefNo")
	private String externalRefNo;

	@JsonProperty("trnRefNo")
	private String trnRefNo;

	@JsonProperty("acEntrySrNo")
	private String acEntrySrNo;

	@JsonProperty("accNo")
	private String accNo;

	@JsonProperty("source")
	private String source;

	@JsonProperty("ccy")
	private String ccy;

	@JsonProperty("drcr")
	private String drcr;

	@JsonProperty("lcyAmount")
	private String lcyAmount;

	@JsonProperty("valueDt")
	private String valueDt;

	@JsonProperty("txnInitDt")
	private String txnInitDt;

	@JsonProperty("relatedAccount")
	private String relatedAccount;

	@JsonProperty("relatedAccountName")
	private String relatedAccountName;

	@JsonProperty("narrative")
	private String narrative;

	@JsonProperty("clientUserID")
	private String clientUserID;

	@JsonProperty("channel")
	private String channel;

	@JsonProperty("fromAccNo")
	private String fromAccNo;

	@JsonProperty("fromAccName")
	private String fromAccName;

	@JsonProperty("fromBankCode")
	private String fromBankCode;

	@JsonProperty("fromBankName")
	private String fromBankName;

	@JsonProperty("napasTraceId")
	private String napasTraceId;

	public BVBCallbackAPITransData() {

	}
}
