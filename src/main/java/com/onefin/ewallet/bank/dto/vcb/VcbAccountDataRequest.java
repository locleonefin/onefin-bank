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
public class VcbAccountDataRequest {

	private static final Logger LOGGER = LoggerFactory.getLogger(VcbAccountDataRequest.class);

	private String messType;

	private String vcbTrans;

	@JsonProperty("vcbTrans")
	public String getVcbTrans() {
		return vcbTrans;
	}

	@JsonProperty("vcb_trans")
	public void setVcb_trans(String vcb_trans) {
		this.vcbTrans = vcb_trans;
	}

	private String customerId;

	@JsonProperty("customerId")
	public String getCustomerId() {
		return customerId;
	}

	@JsonProperty("customerID")
	public void setCustomerID(String customerID) {

		this.customerId = customerID.replaceFirst("0", "84");
	}

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DomainConstants.DATE_FORMAT_yyyyMMDDHHmmssSSSSSS, timezone = DomainConstants.HO_CHI_MINH_TIME_ZONE)
	private Date transDatetime;

	@JsonProperty("trans_datetime")
	public void setTransDateTime(String transDateTime) {
		DateTimeZone zone = DateTimeZone.forID(VcbConstants.HO_CHI_MINH_TIME_ZONE);
		DateTime dateTime = null;
		try {
			dateTime = DateTime.parse(transDateTime, DateTimeFormat.forPattern(DomainConstants.DATE_FORMAT_yyyyMMDDHHmmssSSSSSS)).withZone(zone);
		} catch (Exception e) {

		} finally {
			try {
				dateTime = DateTime.parse(transDateTime, DateTimeFormat.forPattern(DomainConstants.DATE_FORMAT_yyyyMMDDHHmmss_T)).withZone(zone);
			} catch (Exception e) {

			}
			if (dateTime == null) {
				LOGGER.warn("Can't parse datetime => use current date");
				dateTime = new DateTime(DateTimeZone.UTC).withZone(zone);
			}
			LOGGER.info("Current datetime: {}", dateTime.toDate());
			this.transDatetime = dateTime.toDate();
		}
	}

	@JsonProperty(value = "money")
	private BigDecimal money;

	private String vcbId; // act as token

	@JsonProperty("vcbId")
	public String getVcbId() {
		return vcbId;
	}

	@JsonProperty("vcb_id")
	public void setVcb_id(String vcb_id) {
		this.vcbId = vcb_id;
	}

	private boolean registerAutoCashin;

	@JsonProperty("registerAutoCashin")
	public boolean getRegisterAutoCashin() {
		return registerAutoCashin;
	}

	@JsonProperty("register_auto_cashin")
	public void setRegister_auto_cashin(boolean register_auto_cashin) {
		this.registerAutoCashin = register_auto_cashin;
	}

	private BigDecimal minBalance;

	@JsonProperty("minBalance")
	public BigDecimal getMinBalance() {
		return minBalance;
	}

	@JsonProperty("min_balance")
	public void setMin_balance(BigDecimal min_balance) {
		this.minBalance = min_balance;
	}

	private BigDecimal amountAutoCashin;

	@JsonProperty("amountAutoCashin")
	public BigDecimal getAmountAutoCashin() {
		return amountAutoCashin;
	}

	@JsonProperty("amount_auto_cashin")
	public void setAmount_auto_cashin(BigDecimal amount_auto_cashin) {
		this.amountAutoCashin = amount_auto_cashin;
	}

	private String referralCode;

	private boolean isEkyc;

	@JsonProperty("isEKYC")
	public void setIsEKYC(boolean isEKYC) {
		this.isEkyc = isEKYC;
	}

}
