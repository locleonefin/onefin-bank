package com.onefin.ewallet.bank.dto.bvb;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BVBTestUnitResponseData extends BVBVirtualAcctCommonPartnerResponse {

	@JsonProperty("traceId")
	private String traceId;

	public BVBTestUnitResponseData() {
	}

	@Override
	public String toString() {
		return "BVBTestUnitResponseData{" +
				"traceId='" + traceId + '\'' + "," + super.toString() +
				'}';
	}
}
