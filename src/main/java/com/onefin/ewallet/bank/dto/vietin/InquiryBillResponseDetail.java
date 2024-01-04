package com.onefin.ewallet.bank.dto.vietin;

import lombok.Data;

@Data
public class InquiryBillResponseDetail {

	private String transId;

	private String transTime;

	private String custCode;

	private String custName;

	private String billId;

	private String amount;

	private String amountMin;

	private String currencyCode;
}
