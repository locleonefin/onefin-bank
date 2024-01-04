package com.onefin.ewallet.bank.dto.bvb;


import lombok.Data;

@Data
public class BVBVirtualAcctCloseRequest
		extends BVBVirtualAcctCommonRequest {
	private BVBVirtualAcctCommonPartnerRequest data;

	public BVBVirtualAcctCloseRequest() {

	}

	@Override
	public String toString() {
		return "BVBVirtualAcctCloseRequest{" +
				"data=" + data + "," + super.toString() +
				'}';
	}
}
