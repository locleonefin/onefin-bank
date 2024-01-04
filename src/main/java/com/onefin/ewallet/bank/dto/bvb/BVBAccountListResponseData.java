package com.onefin.ewallet.bank.dto.bvb;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BVBAccountListResponseData {

	@JsonProperty("size")
	private Long size;

	@JsonProperty("totalElements")
	private String totalElements;

	@JsonProperty("number")
	private String number;

	@JsonProperty("totalPages")
	private String totalPages;

	@JsonProperty("length")
	private String length;

	@JsonProperty("content")
	private List<BVBVirtualAcctCommonPartnerResponse> content;

	@JsonProperty("first")
	private String first;

	@JsonProperty("last")
	private String last;

	@JsonProperty("empty")
	private String empty;

	@JsonProperty("next")
	private BVBAccountListRequestData next;

	@JsonProperty("previous")
	private BVBAccountListRequestData previous;

	@JsonProperty("error")
	@JsonInclude(JsonInclude.Include.NON_EMPTY)
	private String error = "";

	@JsonProperty("message")
	@JsonInclude(JsonInclude.Include.NON_EMPTY)
	private String message = "";

}
