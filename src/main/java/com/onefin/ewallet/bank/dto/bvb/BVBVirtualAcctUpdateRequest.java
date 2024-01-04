package com.onefin.ewallet.bank.dto.bvb;


import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class BVBVirtualAcctUpdateRequest
		extends BVBVirtualAcctCommonRequest {

	private BVBVirtualAcctCommonPartnerRequest data;

	public BVBVirtualAcctUpdateRequest() {
	}

	@Override
	public String toString() {
		return "BVBVirtualAcctUpdateRequest{" +
				"data=" + data + "," + super.toString() +
				'}';
	}
}
