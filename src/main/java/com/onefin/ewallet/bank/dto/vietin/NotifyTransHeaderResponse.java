package com.onefin.ewallet.bank.dto.vietin;

import lombok.Data;

@Data
public class NotifyTransHeaderResponse {

	private String msgId;
	private String msgType;
	private String channelId;
	private String gatewayId;
	private String providerId;
	private String merchantId;
	private String productId;
	private String timestamp;
	private String username;
	private String recordNum;
	private String signature;

	public NotifyTransHeaderResponse() {
	}

}
