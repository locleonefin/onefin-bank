package com.onefin.ewallet.bank.dto.bvb.bankTransfer;


import lombok.Data;

@Data
public class BVBSendReconciliationRequestData {

	private int processingDate;

	private String data;

	private String description;

	private Object extraInfo;
}
