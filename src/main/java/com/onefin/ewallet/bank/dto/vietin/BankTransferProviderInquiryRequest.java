package com.onefin.ewallet.bank.dto.vietin;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.onefin.ewallet.bank.common.VietinConstants;
import com.onefin.ewallet.common.base.constants.OneFinConstants;
import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Data
public class BankTransferProviderInquiryRequest {

	private String requestId;

	private String providerId;

	private String merchantId;

	private String accountId;

	@NotNull(message = "Not empty senderAccountOrder")
	@Min(0)
	private Integer inquiryAccountOrder;

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

	private String language;

	@JsonProperty("language")
	private void initLanguage(String language) {
		this.language = OneFinConstants.LANGUAGE.fromText(language).getValue();
	}

	private String signature;

}
