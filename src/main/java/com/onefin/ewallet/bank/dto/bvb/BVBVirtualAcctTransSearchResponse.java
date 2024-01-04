package com.onefin.ewallet.bank.dto.bvb;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;


@Data
public class BVBVirtualAcctTransSearchResponse extends BVBVirtualAcctCommonResponse {


	@JsonProperty("data")
	private BVBVirtualAcctTransSearchResponseData data;

	public BVBVirtualAcctTransSearchResponse() {
	}

	@Override
	public String toString() {
		return "BVBVirtualAcctTransSearchResponse{" +
				"data=" + data + "," + super.toString() +
				'}';
	}
}
