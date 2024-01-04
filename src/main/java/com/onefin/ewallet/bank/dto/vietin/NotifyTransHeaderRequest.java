package com.onefin.ewallet.bank.dto.vietin;

import lombok.Data;

import java.util.Map;

@Data
public class NotifyTransHeaderRequest {

	private String msgId;
	private String msgType;
	private String channelId;
	private String providerId;
	private String merchantId;
	private String productId;
	private String timestamp;
	private String recordNum;
	private String signature;
	private String encrypt;
	private String gatewayId;
	private String username;

	public NotifyTransHeaderRequest() {
	}

}
