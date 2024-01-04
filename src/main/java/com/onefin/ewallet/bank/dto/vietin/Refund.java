package com.onefin.ewallet.bank.dto.vietin;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

import lombok.Data;

@Data
public class Refund {

	@Size(max = 30)
	@NotEmpty(message = "Not empty amount")
	private String amount;

	@Size(max = 30)
	// @NotEmpty(message = "Not empty")
	private String currencyCode;

	@Size(max = 12)
	@NotEmpty(message = "Not empty refundTransactionId")
	private String refundTransactionId;

	@Size(max = 14)
	@NotEmpty(message = "Not empty transTime")
	private String transTime;

	@Size(max = 16)
	private String clientIP;

	@Size(max = 12)
	@NotEmpty(message = "Not empty requestId")
	private String requestId;

	private String providerId;

	private String merchantId;

	@Size(max = 15)
	@NotEmpty(message = "Not empty channel")
	private String channel;

	private String version;

	@Size(max = 3)
	private String language;

	@Size(max = 30)
	private String mac;

	private String signature;

	public Refund() {
		this.amount = new String();
		this.refundTransactionId = new String();
		this.clientIP = new String();
		this.language = new String();
		this.mac = new String();
	}

}
