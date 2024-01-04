package com.onefin.ewallet.bank.dto.cms.bankTransfer;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.onefin.ewallet.bank.common.VietinConstants;
import com.onefin.ewallet.common.base.anotation.EnumValidator;
import com.onefin.ewallet.common.base.anotation.MinMaxFieldLengthConstraint;
import com.onefin.ewallet.common.base.constants.OneFinEnum;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.math.BigDecimal;
import java.math.BigInteger;

@Data
public class CMSBankTransferRequest {

	@NotNull(message = "amount can't be null")
	private BigInteger amount;

	@EnumValidator(enumClass = VietinConstants.FeeType.class, checkNull = true, message = "Invalid FeeType")
	private String feeType;

	@NotNull(message = "memberId can't be null")
	@NotEmpty(message = "memberId can't be empty")
	@JsonProperty("memberId")
	private String memberId;

	@NotNull(message = "recvAcctId can't be null")
	@NotEmpty(message = "recvAcctId can't be empty")
	@JsonProperty("recvAcctId")
	private String recvAcctId;

	@NotNull(message = "recvBankId can't be null")
	@NotEmpty(message = "recvBankId can't be empty")
	@JsonProperty("recvBankId")
	private String recvBankId;

	@JsonProperty("transactionMethod")
	@EnumValidator(enumClass = VietinConstants.RemittanceType.class, checkNull = true, message = "Invalid transactionMethod")
	private String transactionMethod;

	@NotNull(message = "recvAcctName can't be null")
	@NotEmpty(message = "recvAcctName can't be empty")
	@JsonProperty("recvAcctName")
	private String recvAcctName;

	private String address;

	private String city;

	private String currency = OneFinEnum.CurrencyCode.VND.getValue();

	private String email;

	private String firstname;

	private Boolean isCard;

	private String lastname;

	private String postcode;

	private String transDescription;

	@MinMaxFieldLengthConstraint(message = "invalid transactionId")
	private String transactionId;

	@Size(max = 50)
	@NotEmpty(message = "Not empty payRefNo")
	@NotNull(message = "payRefNo can't be null")
	@JsonProperty("payRefNo")
	private String payRefNo;

	@Size(max = 100)
	@JsonProperty("payRefInfor")
	private String payRefInfor;

	private String bankCode;

	@NotNull(message = "senderAccountOrder can't be null")
	private Integer senderAccountOrder;


}
