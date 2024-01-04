package com.onefin.ewallet.bank.dto.vietin;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.onefin.ewallet.common.utility.string.StringHelper;

import lombok.Data;

@Data
public class TokenIssue {

	@Size(max = 20)
	@NotEmpty(message = "Not empty cardNumber")
	private String cardNumber;

	@Size(max = 6)
	// @NotEmpty(message = "Not empty")
	private String cardIssueDate;

	@Size(max = 50)
	@NotEmpty(message = "Not empty cardHolderName")
	private String cardHolderName;

	@JsonProperty("cardHolderName")
	public void setCardHolderName(String cardHolderName) {
		try {
			this.cardHolderName = StringHelper.deAccent(cardHolderName.toUpperCase());
		} catch (Exception e) {
			this.cardHolderName = cardHolderName;
		}
	}

	@Size(max = 30)
	@NotEmpty(message = "Not empty providerCustId")
	private String providerCustId;

	@Size(max = 30)
	@NotEmpty(message = "Not empty custPhoneNo")
	private String custPhoneNo;

	@Size(max = 30)
	@NotEmpty(message = "Not empty custIDNo")
	private String custIDNo;

	@Size(max = 16)
	private String clientIP;

	@Size(max = 14)
	@NotEmpty(message = "Not empty transTime")
	private String transTime;

	@Size(max = 12)
	@NotEmpty(message = "Not empty requestId")
	private String requestId;

	@Size(max = 15)
	@NotEmpty(message = "Not empty channel")
	private String channel;

	@Size(max = 3)
	private String language;

	@Size(max = 30)
	private String mac;

	private String providerId;

	private String merchantId;

	private String version;

	private String signature;

	public TokenIssue() {
		this.cardIssueDate = new String();
		this.custPhoneNo = new String();
		this.custIDNo = new String();
		this.clientIP = new String();
		this.language = new String();
		this.mac = new String();
	}

}
