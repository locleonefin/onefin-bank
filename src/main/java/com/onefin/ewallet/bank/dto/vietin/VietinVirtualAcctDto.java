package com.onefin.ewallet.bank.dto.vietin;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.onefin.ewallet.common.base.model.BaseConnResponse;
import com.onefin.ewallet.common.domain.constants.DomainConstants;
import lombok.Data;

import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import java.util.Date;

@Data
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class VietinVirtualAcctDto extends BaseConnResponse {

	private String virtualAcctCode;

	private String virtualAcctVar;

	private String maxCredit;

	private String minCredit;

	private String creditExpireDate;

	private String debitExpireDate;

	private String maxDebit;

	private String minDebit;

	private String effectiveDate;

	private String expireDate;

	private String virtualAcctName;

	private String virtualAcctId;

	private String qrURL;

	private String poolName;

	private String bank;

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DomainConstants.DATE_FORMAT_TRANS, timezone = DomainConstants.HO_CHI_MINH_TIME_ZONE)
	@Temporal(TemporalType.TIMESTAMP)
	private Date releaseTime;

	private int period; // Second

}
