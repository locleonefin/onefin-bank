package com.onefin.ewallet.bank.dto.bvb;


import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class BVBVirtualAcctInfoDetailRequest extends BVBVirtualAcctCommonRequest {

	private BVBVirtualAcctCommonPartnerRequest data;

	public BVBVirtualAcctInfoDetailRequest() {
	}

	@Override
	public String toString() {
		return "BVBVirtualAcctInfoDetailRequest{" +
				"data=" + data + "," + super.toString() +
				'}';
	}
}
