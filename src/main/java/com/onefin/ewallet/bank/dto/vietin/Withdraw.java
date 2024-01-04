package com.onefin.ewallet.bank.dto.vietin;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

import lombok.Data;

@Data
public class Withdraw {

	@Size(max = 20)
	@NotEmpty(message = "Not empty")
	private String token;

	@Size(max = 6)
	@NotEmpty(message = "Not empty")
	private String tokenIssueDate;

	@Size(max = 31)
	@NotEmpty(message = "Not empty")
	private String amount;

	@Size(max = 30)
	private String currencyCode;

	@Size(max = 14)
	@NotEmpty(message = "Not empty")
	private String transTime;

	@Size(max = 16)
	private String clientIP;

	@Size(max = 30)
	private String benName;

	@Size(max = 30)
	private String benAcctNo;

	@Size(max = 30)
	private String benIDNo;

	@Size(max = 100)
	private String benAddInfo;

	@Size(max = 100)
	private String remark;

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

	public Withdraw() {
		this.currencyCode = new String();
		this.benName = new String();
		this.benAcctNo = new String();
		this.benIDNo = new String();
		this.benAddInfo = new String();
		this.remark = new String();
		this.clientIP = new String();
		this.language = new String();
		this.mac = new String();
	}

}
