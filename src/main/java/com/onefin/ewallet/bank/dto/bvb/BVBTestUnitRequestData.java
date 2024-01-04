package com.onefin.ewallet.bank.dto.bvb;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class BVBTestUnitRequestData {

	@JsonProperty("accNo")
	private String accNo;

	@JsonProperty("amount")
	private String amount;
}
