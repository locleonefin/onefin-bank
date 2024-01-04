package com.onefin.ewallet.bank.dto.vietin;

import lombok.Data;

@Data
public class NotifyTransResponse {

	private NotifyTransHeaderResponse header;

	private NotifyTransDataResponse data;

	public NotifyTransResponse() {
	}

}
