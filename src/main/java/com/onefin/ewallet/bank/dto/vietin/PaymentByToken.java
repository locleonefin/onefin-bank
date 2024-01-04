package com.onefin.ewallet.bank.dto.vietin;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

import lombok.Data;

@Data
public class PaymentByToken {

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
	private String payMethod;

	@Size(max = 30)
	private String goodsType;

	@Size(max = 30)
	private String billNo;

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

	public PaymentByToken() {
		this.currencyCode = new String();
		this.payMethod = new String();
		this.goodsType = new String();
		this.billNo = new String();
		this.remark = new String();
		this.clientIP = new String();
		this.language = new String();
		this.mac = new String();
	}

}
