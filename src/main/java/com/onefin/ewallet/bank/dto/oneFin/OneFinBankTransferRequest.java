package com.onefin.ewallet.bank.dto.oneFin;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class OneFinBankTransferRequest {

	@NotNull
	@JsonProperty("transId")
	private String transId;

	@NotNull
	@JsonProperty("transDateTime")
	private long transDateTime;

	@NotNull
	@JsonProperty("amount")
	private Long amount;
	
	@NotNull
	@JsonProperty("comment")
	private String comment;
	
	@JsonProperty("fromAccount")
	private String fromAccount;
	
	@JsonProperty("userName")
	private String userName;
	
	@JsonProperty("userPhone")
	private String userPhone;

    @NotNull
    @JsonProperty("toAccount")
    private String toAccount;

}