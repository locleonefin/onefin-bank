package com.onefin.ewallet.bank.dto.bvb;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;


@Data
public class BVBAccountListRequestData {

	@JsonProperty("partnerCode")
	private String partnerCode;

	@JsonProperty("page")
	private String page = "";

	@JsonProperty("size")
	private String size = "";

	public BVBAccountListRequestData() {
	}
}
