package com.onefin.ewallet.bank.dto.bvb;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class BVBCallbackAPIBatchTrans {


	@JsonProperty("number")
	private String number;

	@JsonProperty("last")
	private String last;

	private Long size;

	@JsonProperty("length")
	private String length;

	@JsonProperty("totalPages")
	private String totalPages;

	@JsonProperty("first")
	private String first;

	@JsonProperty("empty")
	private String empty;

	@JsonProperty("totalElements")
	private String totalElements;

	private List<BVBCallbackAPIBatchTransData> content;

	public BVBCallbackAPIBatchTrans() {
	}
}
