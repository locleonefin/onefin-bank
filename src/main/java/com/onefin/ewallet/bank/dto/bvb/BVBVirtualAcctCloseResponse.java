package com.onefin.ewallet.bank.dto.bvb;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BVBVirtualAcctCloseResponse extends BVBVirtualAcctCommonResponse {

	@JsonProperty("data")
	private BVBVirtualAcctCommonPartnerResponse data;

	public BVBVirtualAcctCloseResponse() {
	}

	@Override
	public String toString() {
		return "BVBVirtualAcctCloseResponse{" +
				"data=" + data + "," + super.toString() +
				'}';
	}
}
