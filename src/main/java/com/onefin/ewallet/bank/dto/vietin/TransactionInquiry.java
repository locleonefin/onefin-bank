package com.onefin.ewallet.bank.dto.vietin;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

import lombok.Data;

@Data
public class TransactionInquiry {

	@Size(max = 20)
	@NotEmpty(message = "Not empty")
	private String queryTransactionId;

	@Size(max = 6)
	@NotEmpty(message = "Not empty")
	private String queryType;

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

	public TransactionInquiry() {
		this.language = new String();
		this.mac = new String();
	}

}
