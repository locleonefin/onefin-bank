package com.onefin.ewallet.bank.dto.vcb;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

@Data
public class CardTokenIssue {

	@NotEmpty(message = "Not empty requestId")
	private String requestId;

	@NotEmpty(message = "Not empty walletId")
	private String walletId;

	@NotEmpty(message = "Not empty mobile")
	private String phoneNo;

	@Size(max = 100)
	@NotEmpty(message = "Not empty cardNumber")
	private String cardNumber;

	@Size(max = 2)
	@NotEmpty(message = "Not empty cardIssueMonth")
	private String cardIssueMonth;

	@Size(max = 4)
	@NotEmpty(message = "Not empty cardIssueYear")
	private String cardIssueYear;

	@Size(max = 50)
	@NotEmpty(message = "Not empty cardHolderName")
	private String cardHolderName;

	@Size(max = 2)
	@NotEmpty(message = "Not empty lang")
	private String lang;

}
