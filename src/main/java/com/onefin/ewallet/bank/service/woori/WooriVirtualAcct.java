package com.onefin.ewallet.bank.service.woori;

import com.onefin.ewallet.bank.dto.bvb.*;
import com.onefin.ewallet.bank.dto.vietin.ConnResponse;
import com.onefin.ewallet.bank.dto.vietin.VirtualAcctCreateRequest;
import com.onefin.ewallet.bank.dto.vietin.VirtualAcctSeedRequest;
import com.onefin.ewallet.bank.dto.vietin.VirtualAcctUpdateStatusRequest;
import com.onefin.ewallet.bank.dto.woori.DirectMessageRV006;
import com.onefin.ewallet.bank.dto.woori.DirectMessageRV007;
import com.onefin.ewallet.bank.repository.jpa.VirtualAcctRepo;
import com.onefin.ewallet.bank.repository.jpa.VirtualAcctStatusHistoryRepo;
import com.onefin.ewallet.bank.repository.jpa.VirtualAcctTransHistoryRepo;
import com.onefin.ewallet.bank.service.common.VirtualAcctAbstract;
import com.onefin.ewallet.common.base.constants.BankConstants;
import com.onefin.ewallet.common.base.constants.OneFinConstants;
import com.onefin.ewallet.common.base.constants.WooriConstants;
import com.onefin.ewallet.common.domain.bank.vietin.VietinVirtualAcctStatusHistory;
import com.onefin.ewallet.common.domain.bank.vietin.VietinVirtualAcctTable;
import com.onefin.ewallet.common.utility.date.DateTimeHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Optional;

@Service
public class WooriVirtualAcct extends VirtualAcctAbstract {

	private static final Logger LOGGER = LoggerFactory.getLogger(WooriVirtualAcct.class);

	@Autowired
	private VirtualAcctRepo virtualAcctRepo;

	@Autowired
	private DateTimeHelper dateTimeHelper;

	@Autowired
	private VirtualAcctTransHistoryRepo virtualAcctTransHistoryRepo;

	@Autowired
	private VirtualAcctStatusHistoryRepo virtualAcctStatusHistoryRepo;

	@Override
	public ConnResponse buildCreateResponseEntity(VirtualAcctCreateRequest requestBody) throws Exception {
		return null;
	}

	@Override
	public ConnResponse buildActUpdateResponseEntity(VirtualAcctUpdateStatusRequest requestBody) throws Exception {
		return null;
	}

	@Override
	public void buildSeedVirtualAcctPoolResponseEntity(VirtualAcctSeedRequest requestBody) throws Exception {

	}

	public void createApiUpdateTable(DirectMessageRV006 wooriResponse,
									 String poolName) throws Exception {

		if (wooriResponse.getCommon().getRspCd().equals(WooriConstants.PROCESS_COMPLETED)) {
			// insert to virtual acct table
			updateVirtualAccTableWithCreateApi(wooriResponse, poolName);
			// insert to virtual acct status history table
			updateVirtualAccStatusHistoryTableWithCreateApi(wooriResponse);
		} else {
			// todo: unknown
		}
	}

	private void updateVirtualAccTableWithCreateApi(DirectMessageRV006 wooriResponse, String poolName) {
		VietinVirtualAcctTable vietinVirtualAcctTable;
		Optional<VietinVirtualAcctTable> checkAccount = virtualAcctRepo.findFirstByVirtualAcctVarId(
				wooriResponse.getIndividual().getVirActNo(),
				OneFinConstants.BankListQrService.WOORI.getBankCode());
		// insert to Virtual acct Table
		vietinVirtualAcctTable = checkAccount.orElseGet(VietinVirtualAcctTable::new);
		vietinVirtualAcctTable.setInUse(false);
		vietinVirtualAcctTable.setPoolName(poolName);
		vietinVirtualAcctTable.setBankCode(OneFinConstants.BankListQrService.WOORI.getBankCode());
		vietinVirtualAcctTable.setQrURL("");
		vietinVirtualAcctTable.setLanguage(OneFinConstants.LANGUAGE.VIETNAMESE.getValue());
		vietinVirtualAcctTable.setVirtualAcctName(wooriResponse.getIndividual().getVirAcNm());
//		vietinVirtualAcctTable.setVirtualAcctVar(extractVarNumber(bvbVirtualAcctCreateResponse.getData().getAccNo()));
		vietinVirtualAcctTable.setCustomerName(wooriResponse.getIndividual().getRefNo());
		vietinVirtualAcctTable.setPartnerAccNo(wooriResponse.getIndividual().getOutActNo());
		vietinVirtualAcctTable.setAccType(wooriResponse.getIndividual().getRecCodCd());
		String dateTime = wooriResponse.getCommon().getTmsDt() + wooriResponse.getCommon().getTmsTm();
		Date getTime = dateTimeHelper.parseDate2(dateTime, OneFinConstants.DATE_FORMAT_yyyyMMDDHHmmss);
		vietinVirtualAcctTable.setBankTime(getTime.getTime());
		vietinVirtualAcctTable.setPartnerCode(wooriResponse.getIndividual().getCorpRecCompCode());
//		vietinVirtualAcctTable.setMakerId(bvbVirtualAcctCreateResponse.getData().getMakerId());
//		vietinVirtualAcctTable.setAccName(wooriResponse.getData().getAccName());
//		vietinVirtualAcctTable.setCheckerId(bvbVirtualAcctCreateResponse.getData().getCheckerId());
//		vietinVirtualAcctTable.setMsgId(bvbVirtualAcctCreateResponse.getMsgId());
//		vietinVirtualAcctTable.setCcy(bvbVirtualAcctCreateResponse.getData().getCcy());
		vietinVirtualAcctTable.setVirtualAcctStatus(wooriResponse.getIndividual().getRecCodCd());
		vietinVirtualAcctTable.setVirtualAcctId(wooriResponse.getIndividual().getVirActNo());
		vietinVirtualAcctTable.setEffectiveDate(dateTimeHelper.parseDate2String(getTime, OneFinConstants.DATE_FORMAT_TRANS));
		vietinVirtualAcctTable.setFixedAmount(BigDecimal.valueOf(Long.parseLong(wooriResponse.getIndividual().getTrnAm())));
		vietinVirtualAcctTable.setReOpenDate(null);
		vietinVirtualAcctTable.setCloseDate(null);
		virtualAcctRepo.save(vietinVirtualAcctTable);
	}

	// ******************************************** UPDATE TABLE FUNCTIONS ******************************************* //
	private void updateVirtualAccStatusHistoryTableWithCreateApi(DirectMessageRV006 wooriResponse) {
		VietinVirtualAcctStatusHistory vietinVirtualAcctStatusHistory = new VietinVirtualAcctStatusHistory();

		LOGGER.info("DirectMessageRV006: {}\n", wooriResponse);
		updateVirtualAccStatusHistoryTableCommonApi(vietinVirtualAcctStatusHistory, wooriResponse);
		vietinVirtualAcctStatusHistory.setClientUserId(wooriResponse.getIndividual().getRefNo());
//		vietinVirtualAcctStatusHistory.setVirtualAcctVar(extractVarNumber(responseBody.getData().getAccNo()));
		vietinVirtualAcctStatusHistory.setVirtualAcctName(wooriResponse.getIndividual().getVirAcNm());

		virtualAcctStatusHistoryRepo.save(vietinVirtualAcctStatusHistory);
	}

	public void updateApiUpdateTable(DirectMessageRV007 wooriResponse) {
		if (wooriResponse.getCommon().getRspCd().equals(WooriConstants.PROCESS_COMPLETED)) {
			Date currentDate = dateTimeHelper.currentDate(OneFinConstants.HO_CHI_MINH_TIME_ZONE);
//			virtualAcctRepo.updateBVBVirtualAcctUpdateApi(
//					responseBody.getData().getAccNo(),
//					requestBody.getData().getAccNameSuffix(),
//					responseBody.getData().getFixedAmount(),
//					responseBody.getData().getClientUserId(),
//					responseBody.getData().getAccName(),
//					currentDate, OneFinConstants.BankListQrService.WOORI.getBankCode());
		}
		updateVirtualAccStatusHistoryTableWithUpdateApi(wooriResponse);
	}

	private void updateVirtualAccStatusHistoryTableWithUpdateApi(DirectMessageRV007 wooriResponse) {

		VietinVirtualAcctStatusHistory vietinVirtualAcctStatusHistory = new VietinVirtualAcctStatusHistory();
		updateVirtualAccStatusHistoryTableCommonApi(vietinVirtualAcctStatusHistory, wooriResponse);
		String clientUserId = wooriResponse.getIndividual().getRefNo();
		clientUserId = (clientUserId != null) ? clientUserId : "";
		vietinVirtualAcctStatusHistory.setClientUserId(clientUserId);
//		vietinVirtualAcctStatusHistory.setVirtualAcctVar(extractVarNumber(responseBody.getData().getAccNo()));
//		vietinVirtualAcctStatusHistory.setVirtualAcctName(requestBody.getData().getAccNameSuffix());
		virtualAcctStatusHistoryRepo.save(vietinVirtualAcctStatusHistory);
	}

//	private <X extends BVBVirtualAcctCommonResponse, Y extends BVBVirtualAcctCommonRequest> void updateVirtualAccStatusHistoryTableCommonApi(VietinVirtualAcctStatusHistory vietinVirtualAcctStatusHistory, BVBVirtualAcctCommonPartnerResponse partnerResponse, X commonResponse, BVBVirtualAcctCommonPartnerRequest partnerRequest, Y commonRequest) {
//		vietinVirtualAcctStatusHistory.setRequestId(commonRequest.getRequestId());
//		vietinVirtualAcctStatusHistory.setVirtualAcctVar(extractVarNumber(partnerRequest.getAccNo()));
//		vietinVirtualAcctStatusHistory.setVirtualAcctName(partnerRequest.getAccNameSuffix());
//		if (commonResponse.getRCode().equals(BankConstants.BVB_REQUEST_STATUS_SUCCESS)) {
//			if (partnerResponse.getFixedAmount() != null) {
//				vietinVirtualAcctStatusHistory.setFixedAmount(partnerResponse.getFixedAmount());
//			}
//			vietinVirtualAcctStatusHistory.setNewStatus(partnerResponse.getAccStat());
//		} else {
//			try {
//				vietinVirtualAcctStatusHistory.setNewStatus(partnerResponse.getError());
//				vietinVirtualAcctStatusHistory.setDataErrorMessage(partnerResponse.getMessage());
//			} catch (Exception e) {
//				vietinVirtualAcctStatusHistory.setNewStatus(commonResponse.getRCode());
//				LOGGER.warn("String error when reading error field, try reading rCode");
//			}
//
//		}
//
//		// default in active status
//		vietinVirtualAcctStatusHistory.setStatusCode(commonResponse.getRCode());
//		vietinVirtualAcctStatusHistory.setBankCode(OneFinConstants.BankListQrService.VCCB.getBankCode());
//		vietinVirtualAcctStatusHistory.setMsgId(commonResponse.getMsgId());
//		vietinVirtualAcctStatusHistory.setBankTime(commonResponse.getBankTime());
//		vietinVirtualAcctStatusHistory.setRequestErrorMessage(commonResponse.getRMsg());
//
//	}

	private <X extends BVBVirtualAcctCommonResponse, Y extends BVBVirtualAcctCommonRequest>
	void updateVirtualAccStatusHistoryTableCommonApi(VietinVirtualAcctStatusHistory vietinVirtualAcctStatusHistory,
													 DirectMessageRV006 wooriResponse) {
		vietinVirtualAcctStatusHistory.setRequestId(wooriResponse.getCommon().getReqRsqDscd());
//		vietinVirtualAcctStatusHistory.setVirtualAcctVar(extractVarNumber(partnerRequest.getAccNo()));
		vietinVirtualAcctStatusHistory.setVirtualAcctName(wooriResponse.getIndividual().getVirAcNm());
//		if (wooriResponse.getRCode().equals(BankConstants.BVB_REQUEST_STATUS_SUCCESS)) {
//			if (partnerResponse.getFixedAmount() != null) {
//				vietinVirtualAcctStatusHistory.setFixedAmount(partnerResponse.getFixedAmount());
//			}
//			vietinVirtualAcctStatusHistory.setNewStatus(partnerResponse.getAccStat());
//		} else {
//			try {
//				vietinVirtualAcctStatusHistory.setNewStatus(partnerResponse.getError());
//				vietinVirtualAcctStatusHistory.setDataErrorMessage(partnerResponse.getMessage());
//			} catch (Exception e) {
//				vietinVirtualAcctStatusHistory.setNewStatus(commonResponse.getRCode());
//				LOGGER.warn("String error when reading error field, try reading rCode");
//			}
//
//		}

		// default in active status
//		vietinVirtualAcctStatusHistory.setStatusCode(commonResponse.getRCode());
		vietinVirtualAcctStatusHistory.setBankCode(OneFinConstants.BankListQrService.WOORI.getBankCode());
//		vietinVirtualAcctStatusHistory.setMsgId(commonResponse.getMsgId());
		String dateTime = wooriResponse.getCommon().getTmsDt() + wooriResponse.getCommon().getTmsTm();
		Date getTime = dateTimeHelper.parseDate2(dateTime, OneFinConstants.DATE_FORMAT_yyyyMMDDHHmmss);
		vietinVirtualAcctStatusHistory.setBankTime(getTime.getTime());
//		vietinVirtualAcctStatusHistory.setRequestErrorMessage(commonResponse.getRMsg());

	}

	private <X extends BVBVirtualAcctCommonResponse, Y extends BVBVirtualAcctCommonRequest>
	void updateVirtualAccStatusHistoryTableCommonApi(VietinVirtualAcctStatusHistory vietinVirtualAcctStatusHistory,
													 DirectMessageRV007 wooriResponse) {
		vietinVirtualAcctStatusHistory.setRequestId(wooriResponse.getCommon().getReqRsqDscd());
//		vietinVirtualAcctStatusHistory.setVirtualAcctVar(extractVarNumber(partnerRequest.getAccNo()));
		vietinVirtualAcctStatusHistory.setVirtualAcctName(wooriResponse.getIndividual().getVirAcNm());
//		if (wooriResponse.getRCode().equals(BankConstants.BVB_REQUEST_STATUS_SUCCESS)) {
//			if (partnerResponse.getFixedAmount() != null) {
//				vietinVirtualAcctStatusHistory.setFixedAmount(partnerResponse.getFixedAmount());
//			}
//			vietinVirtualAcctStatusHistory.setNewStatus(partnerResponse.getAccStat());
//		} else {
//			try {
//				vietinVirtualAcctStatusHistory.setNewStatus(partnerResponse.getError());
//				vietinVirtualAcctStatusHistory.setDataErrorMessage(partnerResponse.getMessage());
//			} catch (Exception e) {
//				vietinVirtualAcctStatusHistory.setNewStatus(commonResponse.getRCode());
//				LOGGER.warn("String error when reading error field, try reading rCode");
//			}
//
//		}

		// default in active status
//		vietinVirtualAcctStatusHistory.setStatusCode(commonResponse.getRCode());
		vietinVirtualAcctStatusHistory.setBankCode(OneFinConstants.BankListQrService.WOORI.getBankCode());
//		vietinVirtualAcctStatusHistory.setMsgId(commonResponse.getMsgId());
		String dateTime = wooriResponse.getCommon().getTmsDt() + wooriResponse.getCommon().getTmsTm();
		Date getTime = dateTimeHelper.parseDate2(dateTime, OneFinConstants.DATE_FORMAT_yyyyMMDDHHmmss);
		vietinVirtualAcctStatusHistory.setBankTime(getTime.getTime());
//		vietinVirtualAcctStatusHistory.setRequestErrorMessage(commonResponse.getRMsg());

	}
}
