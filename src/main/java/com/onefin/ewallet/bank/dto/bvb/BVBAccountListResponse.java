package com.onefin.ewallet.bank.dto.bvb;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class BVBAccountListResponse extends BVBVirtualAcctCommonResponse {

	@JsonProperty("data")
	private BVBAccountListResponseData data;

	@Override
	public String toString() {
		return "BVBAccountListResponse{" +
				"data=" + data + "," + super.toString() +
				'}';
	}
}
