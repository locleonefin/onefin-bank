package com.onefin.ewallet.bank.dto.vcb;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.math.BigDecimal;

@Data
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class CreatePaymentCardRequest {

	private BigDecimal amount;

	private Terminal terminal;

	private String currency;

	private CardTokenPayment token;

	private CardOrder order;

	private String merchant_id;

	private String merchant_txn_ref;

	private String user_id;

	private String user_mpin;

}
