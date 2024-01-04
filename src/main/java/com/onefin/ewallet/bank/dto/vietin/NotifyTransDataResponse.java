package com.onefin.ewallet.bank.dto.vietin;

import lombok.Data;

import java.util.List;

@Data
public class NotifyTransDataResponse {

	private NotifyErrorsResponse errors;
	private List<NotifyTransRecordsResponse> records;
	private InquiryBillResponseDetail details;

	public NotifyTransDataResponse() {
	}

}
