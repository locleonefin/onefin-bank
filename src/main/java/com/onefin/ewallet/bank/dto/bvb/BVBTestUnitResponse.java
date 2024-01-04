package com.onefin.ewallet.bank.dto.bvb;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;


@Data
public class BVBTestUnitResponse extends BVBVirtualAcctCommonResponse {

	@JsonProperty("data")
	private BVBTestUnitResponseData data;

	@Override
	public String toString() {
		return "BVBTestUnitResponse{" +
				"data=" + data + "," + super.toString() +
				'}';
	}
}
