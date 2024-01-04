package com.onefin.ewallet.bank.dto.bvb;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class BVBVirtualAcctTransSearchRequest extends BVBVirtualAcctCommonRequest {

	@JsonProperty("msgId")
	private String msgId;


	private BVBVirtualAcctTransSearchRequestData data;

	@Override
	public String toString() {
		return "BVBVirtualAcctTransSearchRequest{" +
				"msgId='" + msgId + '\'' +
				", data=" + data + "," + super.toString() +
				'}';
	}
}
