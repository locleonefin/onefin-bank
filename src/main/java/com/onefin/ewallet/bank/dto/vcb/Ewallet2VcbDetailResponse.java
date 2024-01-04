package com.onefin.ewallet.bank.dto.vcb;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.onefin.ewallet.bank.common.VcbConstants;
import com.onefin.ewallet.common.domain.constants.DomainConstants;
import lombok.Data;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.Date;

@Data
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Ewallet2VcbDetailResponse {

	private static final Logger LOGGER = LoggerFactory.getLogger(Ewallet2VcbDetailResponse.class);

	private String transID;

	private String transStatus;

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DomainConstants.DATE_FORMAT_yyyyMMDDHHmmssSSSSSS, timezone = DomainConstants.HO_CHI_MINH_TIME_ZONE)
	private Date transTime;

	@JsonProperty("transTime")
	public void setTransTime(String transTime) {
		DateTimeZone zone = DateTimeZone.forID(VcbConstants.HO_CHI_MINH_TIME_ZONE);
		DateTime dateTime = null;
		try {
			dateTime = DateTime.parse(transTime, DateTimeFormat.forPattern(DomainConstants.DATE_FORMAT_yyyyMMDDHHmmssSSSSSS)).withZone(zone);
		} catch (Exception e) {

		} finally {
			try {
				dateTime = DateTime.parse(transTime, DateTimeFormat.forPattern(DomainConstants.DATE_FORMAT_yyyyMMDDHHmmss_T)).withZone(zone);
			} catch (Exception e) {

			}
			if (dateTime == null) {
				LOGGER.warn("Can't parse datetime => use current date");
				dateTime = new DateTime(DateTimeZone.UTC).withZone(zone);
			}
			LOGGER.info("Current datetime: {}", dateTime.toDate());
			this.transTime = dateTime.toDate();
		}
	}

	private BigDecimal transAmount;

	@JsonProperty("transAmount")
	public void setTransAmount(String transAmount) {
		this.transAmount = new BigDecimal(transAmount).compareTo(BigDecimal.ZERO) == 0 ? null : new BigDecimal(transAmount);
	}

	private String transCode;

	private String otp;

	@JsonProperty("OTP")
	public void setOtp(String otp) {
		this.otp = otp;
	}

	private String otpTtl;

	@JsonProperty(value = "OTPTTL", access = JsonProperty.Access.WRITE_ONLY)
	public void setOtpTtl(String otpTtl) {
		this.otpTtl = otpTtl;
	}

	private String vcbId;

	@JsonProperty("vcbId")
	public String getVcbId() {
		return vcbId;
	}

	@JsonProperty("vcb_id")
	public void setVcbId(String vcbId) {
		this.vcbId = vcbId;
	}

	private String checkProfileResult;

	@JsonProperty("check_profile_result")
	public void setCheckProfileResult(String checkProfileResult) {
		this.checkProfileResult = checkProfileResult;
	}

	private String partnerAccBalance;

}
