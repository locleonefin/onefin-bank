package com.onefin.ewallet.bank.dto.bvb;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class BVBAccountListRequest extends BVBVirtualAcctCommonRequest {

	@JsonProperty("data")
	private BVBAccountListRequestData data;

	public BVBAccountListRequest() {
	}

	@Override
	public String toString() {
		return "BVBAccountListRequest{" +
				"data=" + data + "," + super.toString() +
				'}';
	}
}
