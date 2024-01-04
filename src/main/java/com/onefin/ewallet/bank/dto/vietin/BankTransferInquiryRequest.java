package com.onefin.ewallet.bank.dto.vietin;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.onefin.ewallet.bank.common.VietinConstants;
import com.onefin.ewallet.common.base.constants.OneFinConstants;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

@Data
public class BankTransferInquiryRequest {

	private String requestId;

	@NotEmpty(message = "Not empty queryRequestId")
	private String queryRequestId;

	private String providerId;

	private String merchantId;

	private String originalRequestId;

	private String transTime;                // yyyyMMddHHmmss

	@Size(max = 16)
	private String clientIP;

	private String channel;

	private String bankCode = "CTG";

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

}
