package com.onefin.ewallet.bank.dto.vietin;

import lombok.Data;

@Data
public class NotifyTransRecordsResponse {
	private String transId;
	private String originalId;
	private String channelId;
	private String priority;
	private String recordNo;
	private String transTime;
	private String transType;
	private String serviceType;
	private String paymentType;
	private String paymentMethod;
	private String custCode;
	private String custName;
	private String custAcct;
	private String idCard;
	private String idCardType;
	private String phoneNo;
	private String email;
	private String sendBankId;
	private String sendBranchId;
	private String sendAcctId;
	private String sendAcctName;
	private String recvBankId;
	private String recvBranchId;
	private String recvAcctId;
	private String recvAcctName;
	private String amount;
	private String fee;
	private String vat;
	private String payRefNo;
	private String payRefInfo;
	private String bankTransId;
	private String remark;
	private Status status;
	private String currencyCode;
	private String addInfo;
	private String preseve1;
	private String preseve2;
	private String preseve3;

	public NotifyTransRecordsResponse() {
	}

}
