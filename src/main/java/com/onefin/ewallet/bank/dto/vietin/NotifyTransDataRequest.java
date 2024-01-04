package com.onefin.ewallet.bank.dto.vietin;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class NotifyTransDataRequest {

	private List<NotifyTransRecordsRequest> records;

	private String transId;

	private String transTime;

	private String custCode;

	private Map<String, Object> additionalProperties;

	public NotifyTransDataRequest() {
	}

}
