package com.onefin.ewallet.bank.dto.bvb;


import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class BVBVirtualAcctReopenResponse
		extends BVBVirtualAcctCommonResponse {

	private BVBVirtualAcctCommonPartnerResponse data;

	public BVBVirtualAcctReopenResponse() {
	}

	@Override
	public String toString() {
		return "BVBVirtualAcctReopenResponse{" +
				"data=" + data + "," + super.toString() +
				'}';
	}
}
