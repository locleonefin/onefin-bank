package com.onefin.ewallet.bank.dto.bankCollection;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.onefin.ewallet.bank.service.bvb.ExtractVarNameSerializedJson;
import com.onefin.ewallet.common.domain.constants.DomainConstants;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
public class UpdateVirtualAcctResponse {

	@JsonProperty("virtualAcc")
	private String accNo;

	@JsonProperty("virtualAccName")
//	@JsonSerialize(using = ExtractVarNameSerializedJson.class)
	private String virtualAccName;

	@JsonProperty("currency")
	private String ccy;

	@JsonProperty("status")
	private String accStat;

	@JsonProperty("merchantCode")
	private String clientUserID;

	@JsonProperty("amount")
	private BigDecimal fixedAmount;

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DomainConstants.DATE_FORMAT_TRANS, timezone = DomainConstants.HO_CHI_MINH_TIME_ZONE)
	private Date updateDate;


}
