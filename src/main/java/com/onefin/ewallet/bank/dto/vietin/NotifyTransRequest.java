package com.onefin.ewallet.bank.dto.vietin;

import lombok.Data;
import lombok.ToString;

@Data
public class NotifyTransRequest {

	@ToString.Exclude
	private NotifyTransHeaderRequest header;

	@ToString.Exclude
	private NotifyTransDataRequest data;

	public NotifyTransRequest() {
	}

}
