package com.onefin.ewallet.bank.dto.bvb;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class BVBVirtualAcctTransSearchRequestData {

	private String account;

	@JsonProperty("fromDate")
	private String fromDate;

	@JsonProperty("page")
	private int page;

	@JsonProperty("partnerCode")
	private String partnerCode;

	@JsonProperty("size")
	private int size;

	@JsonProperty("toDate")
	private String toDate;


}
