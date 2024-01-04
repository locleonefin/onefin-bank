package com.onefin.ewallet.bank.dto.bvb;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class BVBValidateCallbackRequest extends BVBVirtualAcctCommonRequest {

	@JsonProperty("msgId")
	private String msgId;

	@JsonProperty("data")
	private BVBValidateCallbackRequestData data;

	public BVBValidateCallbackRequest() {

	}

	@Override
	public String toString() {
		return "BVBValidateCallbackRequest{" +
				"msgId='" + msgId + '\'' +
				", data=" + data + "," + super.toString() +
				'}';
	}
}
