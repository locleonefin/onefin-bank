package com.onefin.ewallet.bank.dto.bvb;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;


@JsonInclude(JsonInclude.Include.NON_NULL)
@EqualsAndHashCode(callSuper = true)
@Data
public class BVBVirtualAcctCreateRequest extends
		BVBVirtualAcctCommonRequest {

	@JsonProperty("data")
	private BVBVirtualAcctCreateRequestData data;

	public BVBVirtualAcctCreateRequest() {
	}

	@Override
	public String toString() {
		return "BVBVirtualAcctCreateRequest{" +
				"data=" + data + "," + super.toString() +
				'}';
	}
}

