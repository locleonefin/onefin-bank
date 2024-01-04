package com.onefin.ewallet.bank.dto.vietin;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.onefin.ewallet.common.base.model.BaseConnResponse;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class ConnResponse extends BaseConnResponse {

	// old version
	private Object vtbResponse;

	// new version
	private Object response;

	private String version;

	private String type;
}
