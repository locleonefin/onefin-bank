package com.onefin.ewallet.bank.dto.oneFin;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class ErrorResponse {

	@JsonProperty("error_code")
	private int errorCode;

	@JsonProperty("error_message")
	private String errorMessage;
}
