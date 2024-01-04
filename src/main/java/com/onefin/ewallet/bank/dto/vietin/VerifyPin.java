package com.onefin.ewallet.bank.dto.vietin;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

import lombok.Data;

@Data
public class VerifyPin {

	@Size(max = 20)
	@NotEmpty(message = "Not empty")
	private String otp;

	@Size(max = 12)
	@NotEmpty(message = "Not empty")
	private String verifyTransactionId;

	@Size(max = 12)
	@NotEmpty(message = "Not empty")
	private String verifyBy;

	@Size(max = 14)
	@NotEmpty(message = "Not empty")
	private String transTime;

	@Size(max = 16)
	private String clientIP;

	@Size(max = 12)
	@NotEmpty(message = "Not empty")
	private String requestId;

	@Size(max = 15)
	@NotEmpty(message = "Not empty")
	private String channel;

	@Size(max = 3)
	private String language;

	@Size(max = 30)
	private String mac;

	private String providerId;

	private String merchantId;

	private String version;

	private String signature;

	public VerifyPin() {
		this.clientIP = new String();
		this.language = new String();
		this.mac = new String();
	}

}
