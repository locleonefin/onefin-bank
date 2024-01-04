package com.onefin.ewallet.bank.dto.vcb;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.onefin.ewallet.common.domain.constants.DomainConstants;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;
import java.util.Date;

@Data
@JsonInclude(Include.NON_EMPTY)
public class CheckTrxEWallet2VcbRequest {

	@NotEmpty(message = "Not empty")
	private String requestId;

	private String partnerId;

	@NotEmpty(message = "Not empty")
	private String userId;

	private String txnCode;

	private String txnAmount;

	private String txnCurrency;

	private String txnId;

	private String txnDesc;

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DomainConstants.DATE_FORMAT_yyyyMMDDHHmmss_T, timezone = DomainConstants.HO_CHI_MINH_TIME_ZONE)
	private Date txnTime;

	private String fullname;

	private String bankAccount;

	private String address;

	private String id_number;

	@JsonProperty("idNumber")
	public void setIdNumber(String idNumber) {
		this.id_number = idNumber;
	}

	//@JsonProperty(value = "idNumberType")
	private String id_number_type;

	@JsonProperty("idNumberType")
	public void setIdNumberType(String idNumberType) {
		this.id_number_type = idNumberType;
	}

	//@JsonProperty(value = "idPicFilename")
	private String id_pic_filename;

	private String txnStatus;

	private String otp;

	private String vcbtransID;

	@JsonProperty("vcbTransId")
	public void setVcbTransId(String vcbTransId) {
		this.vcbtransID = vcbTransId;
	}

	private String partnerAcctID;

	@Size(max = 2)
	@NotEmpty(message = "Not empty")
	@JsonProperty(value = "lang")
	private String lang;

}
