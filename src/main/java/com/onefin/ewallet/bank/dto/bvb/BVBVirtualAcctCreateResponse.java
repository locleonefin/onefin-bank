package com.onefin.ewallet.bank.dto.bvb;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.onefin.ewallet.common.domain.bank.vietin.VietinVirtualAcctStatusHistory;
import lombok.*;


@Data
@EqualsAndHashCode(callSuper = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BVBVirtualAcctCreateResponse
		extends BVBVirtualAcctCommonResponse {

	private BVBVirtualAcctCommonPartnerResponse data;

	public BVBVirtualAcctCreateResponse() {
	}

	@Override
	public String toString() {
		return "BVBVirtualAcctCreateResponse{" +
				"data=" + data + "," + super.toString() +
				'}';
	}
}
