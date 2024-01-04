package com.onefin.ewallet.bank.dto.vietin;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.onefin.ewallet.bank.common.VietinConstants;
import com.onefin.ewallet.common.base.constants.OneFinConstants;
import com.onefin.ewallet.common.base.errorhandler.RuntimeBadRequestException;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;

@Data
public class BankTransferRequest {

	private String requestId;

	@NotEmpty(message = "Not empty requestId")
	private String clientRequestId;

	@JsonProperty(value = "requestId")
	public void initClientRequestId(String requestId) {
		this.clientRequestId = requestId;
	}

	private String providerId;

	private String merchantId;

	private String transTime;

	private int sendRecord;

	private List<@Valid BankTransferRecord> records;

	@JsonProperty(value = "records")
	public void setRecords(List<BankTransferRecord> records) {
		if (records == null || records.size() == 0) {
			throw new RuntimeBadRequestException();
		}
		this.sendRecord = records.size();
		this.records = records;
	}

	@NotEmpty(message = "Not empty remittanceType")
	private String remittanceType;

	@JsonProperty("remittanceType")
	private void initRemittanceType(String remittanceType) {
		this.remittanceType = VietinConstants.RemittanceType.fromText(remittanceType).getValue();
	}

	@NotEmpty(message = "Not empty feeType")
	private String feeType;

	@JsonProperty("feeType")
	private void initFeeType(String feeType) {
		this.feeType = VietinConstants.FeeType.fromText(feeType).getValue();
	}

	private String verifyByBank;

	@JsonProperty("verifyByBank")
	private void initVerifyByBank(String verifyByBank) {
		this.verifyByBank = VietinConstants.VerifyByBank.fromText(verifyByBank).getValue();
	}

	@Size(max = 30)
	private String execUserID;

	@Size(max = 30)
	private String verifyMethod;

	@Size(max = 30)
	private String verifyInfor;

	@Size(max = 16)
	private String clientIP;

	@NotEmpty(message = "Not empty channel")
	private String channel;

	@JsonProperty("channel")
	private void initChannel(String channel) {
		this.channel = VietinConstants.Channel.fromText(channel).getValue();
	}

	@Size(max = 5)
	private String version;

	@NotEmpty(message = "Not empty language")
	private String language;

	@JsonProperty("language")
	private void initLanguage(String language) {
		this.language = OneFinConstants.LANGUAGE.fromText(language).getValue();
	}

	private String signature;

	private String bankCode = "CTG";

	public void addRecord(BankTransferRecord record) {
		if (records == null || records.size() == 0) {
			records = new ArrayList<>();
		}
		records.add(record);
		this.sendRecord = records.size();
	}

}
