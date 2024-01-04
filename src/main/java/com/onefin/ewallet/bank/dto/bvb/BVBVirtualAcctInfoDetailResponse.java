package com.onefin.ewallet.bank.dto.bvb;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BVBVirtualAcctInfoDetailResponse
		extends BVBVirtualAcctCommonResponse {
	private BVBVirtualAcctCommonPartnerResponse data;

	public BVBVirtualAcctInfoDetailResponse() {
	}

	@Override
	public String toString() {
		return "BVBVirtualAcctInfoDetailResponse{" +
				"data=" + data + "," + super.toString() +
				'}';
	}
}
