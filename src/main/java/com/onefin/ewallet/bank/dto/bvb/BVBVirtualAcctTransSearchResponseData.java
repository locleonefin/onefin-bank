package com.onefin.ewallet.bank.dto.bvb;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class BVBVirtualAcctTransSearchResponseData {

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

	@JsonProperty("first")
	private String first;

	@JsonProperty("last")
	private String last;

	@JsonProperty("content")
	private List<BVBVirtualAcctTransSearchResponseContent> content;


}
