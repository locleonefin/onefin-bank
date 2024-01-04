package com.onefin.ewallet.bank.dto.vietin;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
@JsonInclude(Include.NON_EMPTY)
public class VietinVirtualAcctCreateBaseResponse {

	private String providerId;

	private String merchantId;

	private String requestId;

	private Status status;

	private String virtualAcctId;

	private String qrURL;

}
