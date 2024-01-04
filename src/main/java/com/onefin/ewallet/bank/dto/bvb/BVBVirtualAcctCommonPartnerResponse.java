package com.onefin.ewallet.bank.dto.bvb;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NonNull;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.math.BigDecimal;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BVBVirtualAcctCommonPartnerResponse {

	@JsonProperty("accName")
	private String accName;

	@JsonProperty("accNo")
	private String accNo;

	@NotEmpty()
	@Size(max = 1)
	@Pattern(regexp = "C|O", message = "accStat must be C or O")
	@NonNull
	@JsonProperty("accStat")
	private String accStat;

	@NotEmpty()
	@Size(max = 1)
	@NonNull
	@Pattern(regexp = "M|O", message = "accType must be M or O")
	@JsonProperty("accType")
	private String accType;

	@JsonProperty("ccy")
	private String ccy;

	@JsonProperty("checkerId")
	private String checkerId;

	@JsonProperty("makerId")
	private String makerId;

	@JsonProperty("partnerAccNo")
	private String partnerAccNo;

	@JsonProperty("partnerCode")
	@NonNull
	private String partnerCode;

	@JsonProperty("clientUserID")
	private String clientUserId = "";

	@JsonProperty("fixedAmount")
	private BigDecimal fixedAmount;

	//	@JsonFormat(shape = JsonFormat.Shape.STRING,
//			pattern = DomainConstants.DATE_FORMAT_TRANS5,
//			timezone = DomainConstants.HO_CHI_MINH_TIME_ZONE)
//	@Temporal(TemporalType.TIMESTAMP)
	@JsonProperty("closeDate")
	@JsonInclude(JsonInclude.Include.NON_EMPTY)
	private String closeDate = "";

	@JsonProperty("openDate")
	@JsonInclude(JsonInclude.Include.NON_EMPTY)
	private String openDate = "";

	@JsonProperty("error")
	@JsonInclude(JsonInclude.Include.NON_EMPTY)
	private String error = "";

	@JsonProperty("message")
	@JsonInclude(JsonInclude.Include.NON_EMPTY)
	private String message = "";

	@JsonProperty("reOpenDate")
	@JsonInclude(JsonInclude.Include.NON_EMPTY)
	private String reOpenDate = "";


	public BVBVirtualAcctCommonPartnerResponse() {

	}
}
