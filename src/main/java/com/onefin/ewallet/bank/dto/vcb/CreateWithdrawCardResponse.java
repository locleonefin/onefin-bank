package com.onefin.ewallet.bank.dto.vcb;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Map;

@Data
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class CreateWithdrawCardResponse extends ErrorResponse {

	@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
	private String id;

	@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
	private String type;

	@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
	private String partner_id;

	@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
	private String group_id;

	@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
	private String user_id;

	@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
	private String merchant_id;

	@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
	private String merchant_name;

	@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
	private String merchant_txn_ref;

	@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
	private String terminal_id;

	@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
	private Map<String, Object> instrument;

	@JsonProperty("instrument")
	public void setInstrument(Map<String, Object> instrument) {
		this.instrument = instrument;
	}

	@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
	private BigDecimal amount;

	@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
	private String currency;

	@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
	private String order_info;

	@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
	private String pay_time;

	@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
	private String batch_no;

	@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
	private String trace_no;

	@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
	private String state;

	@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
	private String response_code;

	@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
	private Map<String, Object> settlement;

	@JsonProperty("settlement")
	public void setSettlement(Map<String, Object> settlement) {
		this.settlement = settlement;
	}

	private String tokenId;
}
