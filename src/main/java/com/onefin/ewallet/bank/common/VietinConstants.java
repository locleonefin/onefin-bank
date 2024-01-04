package com.onefin.ewallet.bank.common;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.onefin.ewallet.common.base.constants.BankConstants;
import com.onefin.ewallet.common.base.constants.OneFinConstants;
import com.onefin.ewallet.common.base.errorhandler.RuntimeBadRequestException;

import java.util.stream.Stream;

public class VietinConstants extends OneFinConstants {

	// Vietin parameters
	public static final String VTB_REQUESTID = "requestId";
	public static final String VTB_PROVIDERID = "providerId";
	public static final String VTB_MERCHANTID = "merchantId";
	public static final String VTB_SIGNATURE = "signature";
	public static final String VTB_STATUS = "status";
	public static final String VTB_CODE = "code";

	// Vietin link bank error code
	public static final String VTB_SUCCESS_CODE = "00";
	public static final String VTB_INSUFFICIENT_MASTER_ACCOUNT_BALANCE = "27";
	public static final String VTB_DUPLICATE_REQUESTID_CODE = "07";
	public static final String VTB_PAY_BY_OTP_CODE = "20";

	// Vietin bank transfer error code
	public static final String VTB_BT_SUCCESS_CODE = "00";
	public static final String VTB_BT_PENDING_CODE = "01";

	// Vietin bank code
	public static final String VTB_BANK_CODE = "CTG";
	public static final String VTB_VIRTUAL_ACCT_SUCCESS_CODE = "1";

	// Vietin virtual account connector code
	public static final String VIR_ACCT_ORDER_EXIST = "VIR_ACCT_ORDER_EXIST";

	public enum VirtualAcctDebitCredit {

		DEBIT("D"),
		CREDIT("C");

		private final String value;

		VirtualAcctDebitCredit(String value) {
			this.value = value;
		}

		public String getValue() {
			return value;
		}

		@Override
		public String toString() {
			return this.value;
		}

		public static Stream<VirtualAcctDebitCredit> stream() {
			return Stream.of(VirtualAcctDebitCredit.values());
		}

	}

	public enum VirtualAcctOnOff {

		ON("0"),
		OFF("4");

		private final String value;

		VirtualAcctOnOff(String value) {
			this.value = value;
		}

		public String getValue() {
			return value;
		}

		@Override
		public String toString() {
			return this.value;
		}

		public static Stream<VirtualAcctOnOff> stream() {
			return Stream.of(VirtualAcctOnOff.values());
		}

	}

	public enum CurrencyCode {

		VND("VND"),
		USD("USD");

		private final String value;

		CurrencyCode(String value) {
			this.value = value;
		}

		public String getValue() {
			return value;
		}

		@JsonCreator(mode = JsonCreator.Mode.DELEGATING)
		public static String fromText(String text) {
			for (CurrencyCode r : CurrencyCode.values()) {
				if (r.getValue().equals(text)) {
					return r.getValue();
				}
			}
			return VND.getValue();
		}

		@Override
		public String toString() {
			return this.value;
		}

		public static Stream<CurrencyCode> stream() {
			return Stream.of(CurrencyCode.values());
		}

	}

	public enum FeeType {

		BENEFICIARY("BEN"),
		OUR("OUR"),

		// BVB FeeType
		B2C(BankConstants.BVBFeeModel.B2C.getValue()),
		B2B(BankConstants.BVBFeeModel.B2B.getValue());


		private final String value;

		FeeType(String value) {
			this.value = value;
		}

		public String getValue() {
			return value;
		}

		@JsonCreator(mode = JsonCreator.Mode.DELEGATING)
		public static FeeType fromText(String text) {
			for (FeeType r : FeeType.values()) {
				if (r.getValue().equals(text)) {
					return r;
				}
			}
			throw new RuntimeBadRequestException();
		}

		@Override
		public String toString() {
			return this.value;
		}

		public static Stream<FeeType> stream() {
			return Stream.of(FeeType.values());
		}

	}

	public enum RemittanceType {

		VIETIN_INTERNAL("0"),
		NAPAS("1"),
		CITAD_TTSP("2"),
		ID("3");

		private final String value;

		RemittanceType(String value) {
			this.value = value;
		}

		public String getValue() {
			return value;
		}

		@JsonCreator(mode = JsonCreator.Mode.DELEGATING)
		public static RemittanceType fromText(String text) {
			for (RemittanceType r : RemittanceType.values()) {
				if (r.getValue().equals(text)) {
					return r;
				}
			}
			throw new RuntimeBadRequestException();
		}

		@Override
		public String toString() {
			return this.value;
		}

		public static Stream<RemittanceType> stream() {
			return Stream.of(RemittanceType.values());
		}

	}

	public enum VerifyByBank {

		YES("Y"),
		NO("N");

		private final String value;

		VerifyByBank(String value) {
			this.value = value;
		}

		public String getValue() {
			return value;
		}

		@JsonCreator(mode = JsonCreator.Mode.DELEGATING)
		public static VerifyByBank fromText(String text) {
			for (VerifyByBank r : VerifyByBank.values()) {
				if (r.getValue().equals(text)) {
					return r;
				}
			}
			throw new RuntimeBadRequestException();
		}

		@Override
		public String toString() {
			return this.value;
		}

		public static Stream<VerifyByBank> stream() {
			return Stream.of(VerifyByBank.values());
		}

	}

	public enum Channel {

		MOBILE("MOBILE"),
		WEB("WEB"),
		POS("POS"),
		DESKTOP("DESKTOP");

		private final String value;

		Channel(String value) {
			this.value = value;
		}

		public String getValue() {
			return value;
		}

		@JsonCreator(mode = JsonCreator.Mode.DELEGATING)
		public static Channel fromText(String text) {
			for (Channel r : Channel.values()) {
				if (r.getValue().equals(text)) {
					return r;
				}
			}
			throw new IllegalArgumentException();
		}

		@Override
		public String toString() {
			return this.value;
		}

		public static Stream<Channel> stream() {
			return Stream.of(Channel.values());
		}

	}

	public enum VirtualAcctMsgType {

		INQ_BILL("1100", "1110"),
		NOTIFY_TRANS("1200", "1210"),
		INQ_CUSTOMER("1300", "1310");

		private final String request;

		private final String response;

		VirtualAcctMsgType(String request, String response) {
			this.request = request;
			this.response = response;
		}

		public String getRequest() {
			return request;
		}

		public String getResponse() {
			return response;
		}

		@Override
		public String toString() {
			return this.request + this.response;
		}

		public static Stream<VirtualAcctMsgType> stream() {
			return Stream.of(VirtualAcctMsgType.values());
		}

	}

}
