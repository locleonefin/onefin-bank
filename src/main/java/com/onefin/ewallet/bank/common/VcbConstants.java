package com.onefin.ewallet.bank.common;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.onefin.ewallet.common.base.constants.OneFinConstants;

import java.util.stream.Stream;

public class VcbConstants extends OneFinConstants {

	/******************** Vietcombank api operation name **********************/
	public enum VCBEwalletApiOperation {

		// VCB send request to OneFin
		CHECK_ACTIVE("check_active", "/vcb/checkAccount", TOKEN_ISSUER, 1100, 1101, 1102, 1103, -1),
		ACTIVE("active", "/vcb/linkAccount", TOKEN_ISSUER, 1200, 1201, 1102, 1103, -1),
		DEACTIVE("deactive", "/vcb/unlinkAccount", TOKEN_REVOKE, 1300, 1301, -1, 1103, -1),
		GET_INFO("get_info", "/vcb/getInfo", TOPUP_TOKEN, 1100, 1101, -1, -1, -1),
		TOPUP("topup", "/vcb/topup", TOPUP_TOKEN, 1400, 1401, -1, 1103, 1402),
		// OneFin send request to VCB
		CASHIN("cash_in", "", TOPUP_TOKEN, -1, -1, -1, -1, -1),
		CASHIN_OTP("cash_in_otp", "", TOPUP_TOKEN_OTP, -1, -1, -1, -1, -1),
		CASHOUT("cash_out", "", WITHDRAW, -1, -1, -1, -1, -1),
		CHECK_ACTIVE_STATUS("check_active_status", "", CHECK_LINKED, -1, -1, -1, -1, -1),
		CHECK_TRANS_STATUS("check_trans_status", "", CHECK_TRX_STATUS, -1, -1, -1, -1, -1),
		ACTIVE_CUSTOMER("active_customer", "", TOKEN_ISSUER, -1, -1, -1, -1, -1),
		ACTIVE_CUSTOMER_OTP("active_customer_otp", "", TOKEN_ISSUER, -1, -1, -1, -1, -1),
		DEACTIVE_CUSTOMER("deactive_customer", "", TOKEN_REVOKE, -1, -1, -1, -1, -1),
		GET_PARTNER_ACC_BALANCE("get_partner_acc_balance", "", GET_MASTER_ACCOUNT_DETAILS, -1, -1, -1, -1, -1),
		CHECK_CUSTOMER_PROFILE("check_customer_profile", "", GET_USER_DETAILS, -1, -1, -1, -1, -1);

		private final String vcbAction;
		private final String vcb2OfUri;
		private final String coreWalletAction;
		private final int successCode;
		private final int failCode;
		private final int existedCode;
		private final int invalidNotfoundCode;
		private final int timeoutCode;

		VCBEwalletApiOperation(String vcbAction, String vcb2OfUri, String coreWalletAction, int successCode, int failCode,
		                       int existedCode, int invalidNotfoundCode, int timeoutCode) {
			this.vcbAction = vcbAction;
			this.vcb2OfUri = vcb2OfUri;
			this.coreWalletAction = coreWalletAction;
			this.successCode = successCode;
			this.failCode = failCode;
			this.existedCode = existedCode;
			this.invalidNotfoundCode = invalidNotfoundCode;
			this.timeoutCode = timeoutCode;
		}

		public String getVcbAction() {
			return vcbAction;
		}

		public String getVcb2OfUri() {
			return vcb2OfUri;
		}

		public String getCoreWalletAction() {
			return coreWalletAction;
		}

		public int getSuccessCode() {
			return successCode;
		}

		public int getFailCode() {
			return failCode;
		}

		public int getExistedCode() {
			return existedCode;
		}

		public int getInvalidNotfoundCode() {
			return invalidNotfoundCode;
		}

		public int getTimeoutCode() {
			return timeoutCode;
		}

		public static Stream<VCBEwalletApiOperation> stream() {
			return Stream.of(VCBEwalletApiOperation.values());
		}

	}

	/******************** Vietcombank api operation name **********************/

	// Vcb parameters
	public static final String VCB_RESP_CODE = "code";
	public static final String VCB_RESP_DESC = "desc";
	public static final String VCB_RESP_DATA = "data";
	public static final String VCB_RESP_MESSAGE = "message";
	public static final String VCB_RESP_TRANS_ID = "transID";

	public static final String VCB_PARTNER_ID = "PartnerId";
	public static final String VCB_PARTNER_ID_PG_BM = "PartnerId";
	public static final String VCB_PARTNER_PASSWORD = "partnerPassword";
	public static final String VCB_REQUEST_ID = "RequestId";
	public static final String VCB_MESSAGE_TYPE = "MessageType";
	public static final String VCB_DATA = "Data";
	public static final String VCB_SIGNATURE = "Signature";

	public static final String VCB_CHECK_SUCCESS = "CHECK_SUCCESS";
	public static final String VCB_CHECK_FAIL = "CHECK_FAIL";
	public static final String VCB_ACTIVE_SUCCESS = "ACTIVE_SUCCESS";
	public static final String VCB_ACTIVE_FAIL = "ACTIVE_FAIL";
	public static final String VCB_DEACTIVE_SUCCESS = "DEACTIVE_SUCCESS";
	public static final String VCB_DEACTIVE_FAIL = "DEACTIVE_FAIL";
	public static final String VCB_TOPUP_SUCCESS = "TOPUP_SUCCESS";
	public static final String VCB_TOPUP_FAIL = "TOPUP_FAIL";
	public static final String VCB_TOPUP_TIMEOUT = "TOPUP_TIMEOUT";
	public static final String VCB_ALREADY_EXISTED = "ALREADY_EXISTED";
	public static final String VCB_NOT_FOUND_OR_INVALID = "NOT_FOUND_OR_INVALID";
	public static final String VCB_SYSTEM_ERROR = "SYSTEM_ERROR";

	// Vietin error code
	public static final int VCB_SUCCESS_CODE = 0;
	public static final int VCB_PAY_BY_OTP_CODE = 10;
	public static final int VCB_TIMEOUT_CODE = 100;

	public static final String VCB_SUCCESS_DETAIL_CODE = "A";
	public static final String VCB_PENDING_DETAIL_CODE = "P";
	public static final String VCB_FAIL_DETAIL_CODE = "F";
	public static final String VCB_TIMEOUT_DETAIL_CODE = "T";

	public static final String LINK_BANK_ACCOUNT = "LINK_BANK_ACCOUNT";
	public static final String LINK_BANK_CARD = "LINK_BANK_CARD";

	public static final String REQUEST_CHECK_TRX_STATUS = "/checkTransStatus";

	public static final String X_OP_DATE_CARD_HEADER = "X-OP-Date";
	public static final String X_OP_EXPIRES_CARD_HEADER = "X-OP-Expires";
	public static final String X_OP_AUTHORIZATION_CARD_HEADER = "X-OP-Authorization";

	public static final String APPROVED_STATUS_CARD = "approved";
	public static final String AUTHORIZATION_REQUIRED_STATUS_CARD = "authorization_required";
	public static final String USER_EXISTED = "USER_EXISTED";
	public static final String TOKEN_EXPIRED = "TOKEN_EXPIRED";

	public enum TransQueryType {

		TOPUP("01"),
		TOKEN_ISSUE("02"),
		WITHDRAW("04");

		private final String value;

		TransQueryType(String value) {
			this.value = value;
		}

		public String getValue() {
			return value;
		}

		@JsonCreator(mode = JsonCreator.Mode.DELEGATING)
		public static VcbConstants.TransQueryType fromText(String text) {
			for (VcbConstants.TransQueryType r : VcbConstants.TransQueryType.values()) {
				if (r.getValue().equals(text)) {
					return r;
				}
			}
			return null;
		}

		@Override
		public String toString() {
			return this.value;
		}

		public static Stream<VcbConstants.TransQueryType> stream() {
			return Stream.of(VcbConstants.TransQueryType.values());
		}

	}

}
