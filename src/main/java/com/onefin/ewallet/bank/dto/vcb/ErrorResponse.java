package com.onefin.ewallet.bank.dto.vcb;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import org.springframework.http.HttpStatus;

@Data
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class ErrorResponse {

	private String requestId;

	private String code;

	private String message;

	private String name;

	private String details;

	private String information_link;

	@JsonIgnore
	private HttpStatus httpCode;

}
