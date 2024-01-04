package com.onefin.ewallet.bank.dto.bvb;


import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class BVBVirtualAcctReopenRequest extends BVBVirtualAcctCommonRequest {

	private BVBVirtualAcctCommonPartnerRequest data;

	public BVBVirtualAcctReopenRequest() {
	}

	@Override
	public String toString() {
		return "BVBVirtualAcctReopenRequest{" +
				"data=" + data + "," + super.toString() +
				'}';
	}
}
