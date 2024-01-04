package com.onefin.ewallet.bank.dto.vietin;

import lombok.Data;

@Data
public class NotifyTransRecordsRequest {
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
	private String sendVirtualAcctId;
	private String sendVirtualAcctName;
	private String sendAddr;
	private String sendCity;
	private String sendCountry;
	private String recvBankId;
	private String recvBranchId;
	private String recvAcctId;
	private String recvAcctName;
	private String recvVirtualAcctId;
	private String recvVirtualAcctName;
	private String recvAddr;
	private String recvCity;
	private String recvCountry;
	private String billId;
	private String billTerm;
	private String amount;
	private String fee;
	private String vat;
	private String balance;
	private String payRefNo;
	private String payRefInfo;
	private String bankTransId;
	private String remark;
	private Status status;
	private String currencyCode;

	public NotifyTransRecordsRequest() {
	}

	public String toStringWithoutBalance() {
		return "NotifyTransRecordsRequest{" +
				"transId='" + transId + '\'' +
				", originalId='" + originalId + '\'' +
				", channelId='" + channelId + '\'' +
				", priority='" + priority + '\'' +
				", recordNo='" + recordNo + '\'' +
				", transTime='" + transTime + '\'' +
				", transType='" + transType + '\'' +
				", serviceType='" + serviceType + '\'' +
				", paymentType='" + paymentType + '\'' +
				", paymentMethod='" + paymentMethod + '\'' +
				", custCode='" + custCode + '\'' +
				", custName='" + custName + '\'' +
				", custAcct='" + custAcct + '\'' +
				", idCard='" + idCard + '\'' +
				", idCardType='" + idCardType + '\'' +
				", phoneNo='" + phoneNo + '\'' +
				", email='" + email + '\'' +
				", sendBankId='" + sendBankId + '\'' +
				", sendBranchId='" + sendBranchId + '\'' +
				", sendAcctId='" + sendAcctId + '\'' +
				", sendAcctName='" + sendAcctName + '\'' +
				", sendVirtualAcctId='" + sendVirtualAcctId + '\'' +
				", sendVirtualAcctName='" + sendVirtualAcctName + '\'' +
				", sendAddr='" + sendAddr + '\'' +
				", sendCity='" + sendCity + '\'' +
				", sendCountry='" + sendCountry + '\'' +
				", recvBankId='" + recvBankId + '\'' +
				", recvBranchId='" + recvBranchId + '\'' +
				", recvAcctId='" + recvAcctId + '\'' +
				", recvAcctName='" + recvAcctName + '\'' +
				", recvVirtualAcctId='" + recvVirtualAcctId + '\'' +
				", recvVirtualAcctName='" + recvVirtualAcctName + '\'' +
				", recvAddr='" + recvAddr + '\'' +
				", recvCity='" + recvCity + '\'' +
				", recvCountry='" + recvCountry + '\'' +
				", billId='" + billId + '\'' +
				", billTerm='" + billTerm + '\'' +
				", amount='" + amount + '\'' +
				", fee='" + fee + '\'' +
				", vat='" + vat + '\'' +
				", payRefNo='" + payRefNo + '\'' +
				", payRefInfo='" + payRefInfo + '\'' +
				", bankTransId='" + bankTransId + '\'' +
				", remark='" + remark + '\'' +
				", status=" + status +
				", currencyCode='" + currencyCode + '\'' +
				'}';
	}
}
