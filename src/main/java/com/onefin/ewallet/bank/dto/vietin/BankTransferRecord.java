package com.onefin.ewallet.bank.dto.vietin;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.onefin.ewallet.bank.common.VietinConstants;
import lombok.Data;

import javax.validation.constraints.*;
import java.math.BigDecimal;

@Data
public class BankTransferRecord {

	private String transId;

	@NotNull(message = "Not empty priority")
	@Min(1)
	private Integer priority;

	@Size(max = 100)
	private String senderBankId;

	@Size(max = 100)
	private String senderBranchId;

	@Size(max = 100)
	private String senderAcctId; // DEPEND ON PARTNER

	@Size(max = 100)
	private String senderAcctName;

	@Size(max = 255)
	private String senderAddr;

	@Size(max = 100)
	private String senderCity;

	@Size(max = 100)
	private String senderCountry;

	@NotNull(message = "Not empty senderAccountOrder")
	@Min(0)
	private Integer senderAccountOrder;

	@Size(max = 100)
	@NotEmpty(message = "Not empty recvBankId")
	private String recvBankId;

	@Size(max = 100)
	private String recvBranchId;

	@Size(max = 100)
	@NotEmpty(message = "Not empty recvAcctId")
	private String recvAcctId;

	@Size(max = 100)
	@NotEmpty(message = "Not empty recvAcctName")
	private String recvAcctName;

	@Size(max = 255)
	private String recvAddr;

	@Size(max = 100)
	private String recvCity;

	@Size(max = 100)
	private String recvCountry;

	@Size(max = 30)
	private String recvIdCard;

	@Size(max = 30)
	private String recvIdIssueDate;

	@Size(max = 30)
	private String recvIdIssueBy;

	@Size(max = 30)
	private String recvPhoneNo;

	@Size(max = 100)
	private String recvEmail;

	@DecimalMin(value = "0.0", inclusive = false)
	private BigDecimal amount;

	private String currencyCode;

	@Size(max = 50)
	@NotEmpty(message = "Not empty payRefNo")
	private String payRefNo;

	@Size(max = 100)
	private String payRefInfor;

	@Size(max = 100)
	@NotEmpty(message = "Not empty remark")
	private String remark;

	private Boolean isCard=false;

}
