package com.onefin.ewallet.bank.dto.bvb;


import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.onefin.ewallet.common.domain.constants.DomainConstants;
import lombok.Data;

import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import java.util.Date;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BVBVirtualAcctCommonResponse {

	@JsonProperty("bankTime")
	private Long bankTime;

	@JsonProperty("msgId")
	private String msgId;

	@JsonProperty("rCode")
	private String rCode;

	@JsonProperty("rMsg")
	@JsonInclude(JsonInclude.Include.NON_EMPTY)
	private String rMsg = "";

	@JsonProperty("requestId")
	private String requestId;

	@JsonProperty("to")
	private String to;

	public BVBVirtualAcctCommonResponse() {

	}

}
