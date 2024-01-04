package com.onefin.ewallet.bank.dto.vietin;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.onefin.ewallet.bank.common.VietinConstants;
import com.onefin.ewallet.common.base.constants.OneFinConstants;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Data
public class BankTransferAccountInquiryRequest {

	private String requestId;

	private String providerId;

	private String merchantId;

	@NotNull(message = "remittanceType can't be null")
	private String remittanceType;

	@JsonProperty("remittanceType")
	private void initRemittanceType(String remittanceType) {
		this.remittanceType = VietinConstants.BANK_TRANSFER_TYPE.fromText(remittanceType).getValue();
	}

	@Size(max = 100)
	@NotEmpty(message = "Not empty accountId")
	private String accountId;

	private String bankId;

	@Size(max = 100)
	private String branchId;

	private String transTime;

	@Size(max = 16)
	private String clientIP;

	private String channel;

	@JsonProperty("channel")
	private void initChannel(String channel) {
		this.channel = VietinConstants.Channel.fromText(channel).getValue();
	}

	@Size(max = 5)
	private String version;

	private String language = "vi";

	@JsonProperty("language")
	private void initLanguage(String language) {
		this.language = OneFinConstants.LANGUAGE.fromText(language).getValue();
	}

	private String signature;

	private Boolean isCard;

	private String bankCode = "CTG";

}
