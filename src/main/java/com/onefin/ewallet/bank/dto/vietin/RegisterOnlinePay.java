package com.onefin.ewallet.bank.dto.vietin;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.onefin.ewallet.common.utility.string.StringHelper;

import lombok.Data;

@Data
public class RegisterOnlinePay {

	@Size(max = 20)
	@NotEmpty(message = "Not empty")
	private String cardNumber;

	@Size(max = 6)
	private String cardIssueDate;

	@Size(max = 50)
	@NotEmpty(message = "Not empty")
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
	@NotEmpty(message = "Not empty")
	private String providerCustId;

	@Size(max = 30)
	@NotEmpty(message = "Not empty")
	private String custIDNo;

	@Size(max = 30)
	private String custIDIssueDate;

	@Size(max = 30)
	private String custIDIssueBy;

	@Size(max = 30)
	@NotEmpty(message = "Not empty")
	private String custPhoneNo;

	@Size(max = 30)
	private String custGender;

	@Size(max = 30)
	private String custBirthday;

	@Size(max = 50)
	private String custEmail;

	@Size(max = 16)
	private String clientIP;

	@Size(max = 14)
	@NotEmpty(message = "Not empty")
	private String transTime;

	@Size(max = 12)
	@NotEmpty(message = "Not empty")
	private String requestId;

	private boolean acceptRegistered;

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

	public RegisterOnlinePay() {
		this.cardIssueDate = new String();
		this.custIDIssueDate = new String();
		this.custIDIssueBy = new String();
		this.custGender = new String();
		this.custBirthday = new String();
		this.custEmail = new String();
		this.clientIP = new String();
		this.language = new String();
		this.mac = new String();
	}

}
