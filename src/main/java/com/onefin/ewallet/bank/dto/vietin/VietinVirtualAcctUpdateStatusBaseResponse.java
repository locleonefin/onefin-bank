package com.onefin.ewallet.bank.dto.vietin;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import lombok.Data;

@Data
@JsonInclude(Include.NON_EMPTY)
public class VietinVirtualAcctUpdateStatusBaseResponse {

	private String providerId;

	private String merchantId;

	private String requestId;

	private Status status;

}
