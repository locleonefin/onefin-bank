package com.onefin.ewallet.bank.controller;

import com.onefin.ewallet.bank.common.VietinConstants;
import com.onefin.ewallet.bank.dto.vietin.InquiryBillResponseDetail;
import com.onefin.ewallet.bank.dto.vietin.NotifyTransRecordsRequest;
import com.onefin.ewallet.bank.dto.vietin.NotifyTransRequest;
import com.onefin.ewallet.bank.dto.vietin.NotifyTransResponse;
import com.onefin.ewallet.bank.repository.jpa.VietinNotifyTransTableRepo;
import com.onefin.ewallet.bank.repository.jpa.VirtualAcctRepo;
import com.onefin.ewallet.bank.repository.jpa.VirtualAcctTransHistoryRepo;
import com.onefin.ewallet.bank.service.common.ConfigLoader;
import com.onefin.ewallet.bank.service.onefin.OneFinTransit;
import com.onefin.ewallet.bank.service.vietin.VietinMessageUtil;
import com.onefin.ewallet.bank.service.vietin.VietinVirtualAcct;
import com.onefin.ewallet.common.base.anotation.MeasureExcutionTime;
import com.onefin.ewallet.common.base.constants.OneFinConstants;
import com.onefin.ewallet.common.base.controller.AbstractBaseController;
import com.onefin.ewallet.common.base.service.BaseNumberSequenceService;
import com.onefin.ewallet.common.domain.bank.vietin.VietinNotifyTransTable;
import com.onefin.ewallet.common.domain.bank.vietin.VietinVirtualAcctTable;
import com.onefin.ewallet.common.domain.bank.vietin.VietinVirtualAcctTransHistory;
import com.onefin.ewallet.common.utility.date.DateTimeHelper;
import com.onefin.ewallet.common.utility.json.JSONHelper;
import com.onefin.ewallet.common.utility.string.StringHelper;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.function.Predicate;

@RestController
@RequestMapping("/api/v1")
public class VietinVirtualAcctNotifyController extends AbstractBaseController {
	@Autowired
	private VietinNotifyTransTableRepo vietinNotifyTransTableRepo;

	@Autowired
	private VirtualAcctRepo virtualAcctRepo;

	@Autowired
	public VietinVirtualAcct vietinVirtualAcct;

	@Autowired
	private VietinMessageUtil imsgUtil;

	@Autowired
	private DateTimeHelper dateTimeHelper;

	@Autowired
	private ConfigLoader configLoader;

	@Autowired
	@Qualifier("jsonHelper")
	private JSONHelper JsonHelper;

	@Autowired
	private OneFinTransit oneFinTransit;

	@Autowired
	private ModelMapper modelMapper;

	@Autowired
	private VirtualAcctTransHistoryRepo virtualAcctTransHistoryRepo;

	@Autowired
	private StringHelper stringHelper;

	private static final Logger LOGGER = LoggerFactory.getLogger(VietinVirtualAcctNotifyController.class);

	private static final org.apache.logging.log4j.Logger LOGGER1 = LogManager.getLogger(VietinVirtualAcctNotifyController.class);


	@PostMapping("/notify-trans-gen-sign")
	public ResponseEntity<?> notifyTransGenSign(@Valid @RequestBody(required = true) NotifyTransRequest requestBody, HttpServletRequest request) throws Exception {
		NotifyTransRequest responseEntity = vietinVirtualAcct.notifyTransGenSign(requestBody);
		return new ResponseEntity<>(responseEntity, HttpStatus.OK);
	}


	@MeasureExcutionTime
	@PostMapping("/notify-trans")
	public ResponseEntity<?> notifyTrans(@Valid @RequestBody() NotifyTransRequest requestBody,
										 HttpServletRequest request) throws Exception {
		try {
			LOGGER.info("notifyTrans msgId: {}, msgType: {}, providerId: {}, timestamp: {}, recordNum: {}", requestBody.getHeader().getMsgId(), requestBody.getHeader().getMsgType(), requestBody.getHeader().getProviderId(), requestBody.getHeader().getTimestamp(), requestBody.getHeader().getRecordNum());
			for (NotifyTransRecordsRequest notifyTransRecordsRequest :
					requestBody.getData().getRecords()) {
				LOGGER.info(notifyTransRecordsRequest.toStringWithoutBalance());
			}

			vietinVirtualAcct.backUpRequestResponse(requestBody.getHeader().getMsgId(), requestBody, null);

			NotifyTransResponse responseEntity = vietinVirtualAcct.validateAndBuildNotifyTransResponse(requestBody);

			if (responseEntity.getData().getErrors().getErrorCode().equals("00")) {
				for (NotifyTransRecordsRequest notifyTransRecordsRequest : requestBody.getData().getRecords()) {
					VietinNotifyTransTable vietinNotifyTrans = modelMapper.map(notifyTransRecordsRequest, VietinNotifyTransTable.class);
					vietinNotifyTrans.setMsgType(requestBody.getHeader().getMsgType());
					vietinNotifyTrans.setBankCode(OneFinConstants.BankListQrService.CTG.getBankCode());
					VietinNotifyTransTable vietinNotifyTransStored = vietinNotifyTransTableRepo.save(vietinNotifyTrans);
					// Routing
					if (vietinNotifyTrans.getTransType().equals(VietinConstants.VirtualAcctDebitCredit.CREDIT.getValue()) && !stringHelper.checkNullEmptyBlank(vietinNotifyTrans.getRecvVirtualAcctId())) {
						Predicate<String> accountPrefixPool = s -> vietinNotifyTrans.getRecvVirtualAcctId().substring(0, configLoader.getVietinVirtualAcctVirtualAcctCode().length() + BaseNumberSequenceService.prefixVtbPoolVirtualAcct.length()).toUpperCase().contains(s);
						// Check if virtual account in pool
						if (accountPrefixPool.test(configLoader.getVietinVirtualAcctVirtualAcctCode() + BaseNumberSequenceService.prefixVtbPoolVirtualAcct)) {
							// Release virtual account in pool
							VietinVirtualAcctTransHistory historyData = vietinVirtualAcct.releaseVirtualAcctFromNotify(vietinNotifyTransStored);
							// Callback and notify
							if (historyData != null) {
								vietinVirtualAcct.callbackWithPoolVirtualAcct(
										historyData.getBackendUrl(),
										vietinNotifyTransStored.getBankTransId() + "_" + vietinNotifyTransStored.getId().toString() + "_" + OneFinConstants.BankListQrService.CTG.getBankCode(),
										OneFinConstants.LazyListVirtualAccountTransStatus.TRANS_SUCCESS_STATUS.getName(),
										vietinNotifyTransStored.getBankTransId(),
										vietinNotifyTransStored.getSendAcctId() + "_" + vietinNotifyTransStored.getSendAcctName()
								);
							}
						} else {
							oneFinTransit.updateTransitBankTransfer(vietinNotifyTrans);
						}
					}
				}
			}

			vietinVirtualAcct.backUpRequestResponse(requestBody.getHeader().getMsgId(), null, responseEntity);
			LOGGER.info("RequestID {} - End notifyTrans", requestBody.getHeader().getMsgId());
			return new ResponseEntity<>(responseEntity, HttpStatus.OK);
		} catch (Exception e) {
			LOGGER.error("Fail to process notifyTrans", e);
			return new ResponseEntity<>(vietinVirtualAcct.NotifyTransBuildExceptionResponse(), HttpStatus.OK);
		}
	}

	@PostMapping("/inq-customer")
	public ResponseEntity<?> customerInquiry(@Valid @RequestBody() NotifyTransRequest requestBody, HttpServletRequest request) {
		try {
			LOGGER1.log(Level.getLevel("INFOWT"), "Full body: {}", requestBody);
			LOGGER.info("inquiryBillRequest msgId: {}, msgType: {}, providerId: {}, timestamp: {}, recordNum: {}", requestBody.getHeader().getMsgId(), requestBody.getHeader().getMsgType(), requestBody.getHeader().getProviderId(), requestBody.getHeader().getTimestamp(), requestBody.getHeader().getRecordNum());
			vietinVirtualAcct.backUpRequestResponse(requestBody.getHeader().getMsgId(), requestBody, null);
			NotifyTransResponse responseEntity = vietinVirtualAcct.validateAndBuildNotifyTransResponse(requestBody);
			if (responseEntity.getData().getErrors().getErrorCode().equals("00")) {
				VietinNotifyTransTable vietinNotifyTrans = modelMapper.map(requestBody.getData(), VietinNotifyTransTable.class);
				vietinNotifyTrans.setMsgType(requestBody.getHeader().getMsgType());
				vietinNotifyTransTableRepo.save(vietinNotifyTrans);
				// Routing
				Predicate<String> accountPrefixPool = s -> vietinNotifyTrans.getCustCode().substring(0, configLoader.getVietinVirtualAcctVirtualAcctCode().length() + BaseNumberSequenceService.prefixVtbPoolVirtualAcct.length()).toUpperCase().contains(s);
				// Check if virtual account in pool
				if (accountPrefixPool.test(configLoader.getVietinVirtualAcctVirtualAcctCode() + BaseNumberSequenceService.prefixVtbPoolVirtualAcct)) {
					VietinVirtualAcctTable useVirtualAcct = virtualAcctRepo.findByVirtualAcctIdAndInUseAndNotExpired(vietinNotifyTrans.getCustCode(), true, dateTimeHelper.currentDate(OneFinConstants.HO_CHI_MINH_TIME_ZONE), OneFinConstants.BankListQrService.CTG.getBankCode());
					if (useVirtualAcct == null) {
						responseEntity.getData().setDetails(new InquiryBillResponseDetail());
						responseEntity.getData().getDetails().setTransId(requestBody.getData().getTransId());
						responseEntity.getData().getDetails().setTransTime(requestBody.getData().getTransTime());
						responseEntity.getData().getDetails().setCustCode(requestBody.getData().getCustCode());
						responseEntity = vietinVirtualAcct.buildNotifyTransResponseWithErrorCode(responseEntity, "02", null);
					} else {
						VietinVirtualAcctTransHistory virtualAcctHistory = virtualAcctTransHistoryRepo.findByTransStatusAndVirtualAcctId(vietinNotifyTrans.getCustCode(), OneFinConstants.TRANS_PENDING, OneFinConstants.BankListQrService.CTG.getBankCode());
						virtualAcctTransHistoryRepo.updateCustomerInquiryVietinVirtualAcct(true, dateTimeHelper.currentDate(OneFinConstants.HO_CHI_MINH_TIME_ZONE), vietinNotifyTrans.getCustAcct(), OneFinConstants.TRANS_PENDING, OneFinConstants.BankListQrService.CTG.getBankCode());
						responseEntity.getData().setDetails(new InquiryBillResponseDetail());
						responseEntity.getData().getDetails().setTransId(requestBody.getData().getTransId());
						responseEntity.getData().getDetails().setTransTime(requestBody.getData().getTransTime());
						responseEntity.getData().getDetails().setCustCode(requestBody.getData().getCustCode());
						responseEntity.getData().getDetails().setBillId(virtualAcctHistory.getId().toString());
						responseEntity.getData().getDetails().setCustName(useVirtualAcct.getVirtualAcctName());
						responseEntity.getData().getDetails().setAmount(Integer.toString(virtualAcctHistory.getAmount().intValue()));
						responseEntity.getData().getDetails().setAmountMin(Integer.toString(virtualAcctHistory.getAmount().intValue()));
						responseEntity.getData().getDetails().setCurrencyCode(VietinConstants.CurrencyCode.VND.getValue());
						responseEntity = vietinVirtualAcct.buildNotifyTransResponseWithErrorCode(responseEntity, "00", null);
					}
				} else {
					// TODO Other business
					responseEntity.getData().setDetails(new InquiryBillResponseDetail());
//					responseEntity.getData().getDetails().setTransId(requestBody.getData().getTransId());
//					responseEntity.getData().getDetails().setTransTime(requestBody.getData().getTransTime());
//					responseEntity.getData().getDetails().setCustCode(requestBody.getData().getCustCode());
//					responseEntity.getData().getDetails().setBillId((requestBody.getData().getTransId()));
//					responseEntity.getData().getDetails().setCustName("Test");
//					responseEntity.getData().getDetails().setAmount(String.valueOf(10000));
					responseEntity = vietinVirtualAcct.buildNotifyTransResponseWithErrorCode(responseEntity, "02", null);
				}
			}
			vietinVirtualAcct.backUpRequestResponse(requestBody.getHeader().getMsgId(), null, responseEntity);
			LOGGER.info("RequestID {} - End notifyTrans", requestBody.getHeader().getMsgId());
			return new ResponseEntity<>(responseEntity, HttpStatus.OK);
		} catch (Exception e) {
			LOGGER.error("Fail to process notifyTrans", e);
			return new ResponseEntity<>(vietinVirtualAcct.NotifyTransBuildExceptionResponse(), HttpStatus.OK);
		}
	}

}
