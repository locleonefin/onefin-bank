package com.onefin.ewallet.bank.dto.bvb;


import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
public class BVBVirtualAcctUpdateResponse
		extends BVBVirtualAcctCommonResponse {
	private BVBVirtualAcctCommonPartnerResponse data;

	public BVBVirtualAcctUpdateResponse() {
	}

	@Override
	public String toString() {
		return "BVBVirtualAcctUpdateResponse{" +
				"data=" + data + "," + super.toString() +
				'}';
	}
}
