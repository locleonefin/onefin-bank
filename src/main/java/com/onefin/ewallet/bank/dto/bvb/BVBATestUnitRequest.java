package com.onefin.ewallet.bank.dto.bvb;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class BVBATestUnitRequest {

	private String from;

	@JsonProperty("msgId")
	private String msgId;

	@JsonProperty("requestId")
	private String requestId;


	@JsonProperty("data")
	private BVBTestUnitRequestData data;
}
