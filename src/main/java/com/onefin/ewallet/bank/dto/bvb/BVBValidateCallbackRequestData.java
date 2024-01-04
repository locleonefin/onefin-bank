package com.onefin.ewallet.bank.dto.bvb;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class BVBValidateCallbackRequestData {
	public BVBValidateCallbackRequestData() {

	}

	@JsonProperty("trnRefNo")
	private String trnRefNo;

	@JsonProperty("partnerCode")
	private String partnerCode;
	
}
