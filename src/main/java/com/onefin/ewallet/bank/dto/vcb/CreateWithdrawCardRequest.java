package com.onefin.ewallet.bank.dto.vcb;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.math.BigDecimal;

@Data
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class CreateWithdrawCardRequest {

	private BigDecimal amount;

	private Terminal terminal;

	private String currency;

	private CardTokenPayment token;

	private String order_info;

	private String invoice_ref;

	private String merchant_id;

	private String merchant_txn_ref;

	private String user_id;

	private String user_mpin;

}
