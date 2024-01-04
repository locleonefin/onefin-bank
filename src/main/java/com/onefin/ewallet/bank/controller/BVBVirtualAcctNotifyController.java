package com.onefin.ewallet.bank.controller;


import com.onefin.ewallet.common.base.constants.BankConstants;
import com.onefin.ewallet.bank.dto.bvb.BVBCallbackAPIBatchTrans;
import com.onefin.ewallet.bank.dto.bvb.BVBCallbackAPIBatchTransData;
import com.onefin.ewallet.bank.dto.bvb.BVBCallbackAPITrans;
import com.onefin.ewallet.bank.dto.vietin.ConnResponse;
import com.onefin.ewallet.bank.repository.jpa.VietinNotifyTransTableRepo;
import com.onefin.ewallet.bank.service.bvb.BVBNotifyHandle;
import com.onefin.ewallet.bank.service.bvb.BVBRequestUtil;
import com.onefin.ewallet.bank.service.bvb.BVBVirtualAcct;
import com.onefin.ewallet.common.base.anotation.MeasureExcutionTime;
import com.onefin.ewallet.common.base.constants.OneFinConstants;
import com.onefin.ewallet.common.domain.bank.vietin.VietinNotifyTransTable;
import com.onefin.ewallet.common.domain.bank.vietin.VietinVirtualAcctTransHistory;
import com.onefin.ewallet.common.utility.json.JSONHelper;
import com.onefin.ewallet.common.utility.string.StringHelper;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/bvb/virtual-acct")
public class BVBVirtualAcctNotifyController {

	private static final org.apache.logging.log4j.Logger LOGGER = LogManager.getLogger(BVBVirtualAcctNotifyController.class);

	@Autowired
	private BVBNotifyHandle bvbNotifyHandle;

	@Autowired
	private BVBRequestUtil bvbRequestUtil;

	@Autowired
	private VietinNotifyTransTableRepo vietinNotifyTransTableRepo;

	@Autowired
	private JSONHelper jsonHelper;

	@Autowired
	private BVBVirtualAcct bvbVirtualAcct;

	@Autowired
	private StringHelper stringHelper;

	@PostMapping("/notify-trans")
	@MeasureExcutionTime
	public ResponseEntity<?> notifyTrans(
			@Valid @RequestBody(required = true)
			String requestBody,
			@RequestHeader Map<String, String> headers) throws Exception {
		try {
			LOGGER.log(Level.getLevel("INFOWT"), "CallBack Api Request: {}", requestBody);
			// Convert object
			BVBCallbackAPITrans callBackRequest =
					Optional.ofNullable((BVBCallbackAPITrans)
									jsonHelper.convertString2Map(requestBody, BVBCallbackAPITrans.class))
							.orElse((BVBCallbackAPITrans)
									jsonHelper.convertString2MapIgnoreUnknown(requestBody, BVBCallbackAPITrans.class));

			// backup
			bvbRequestUtil.backUpRequestResponse(
					BankConstants.BVB_BACKUP_CALLBACK_PREFIX,
					RandomStringUtils.random(12, true, true),
					callBackRequest,
					null,
					headers);

			boolean isValid = bvbRequestUtil.validateBvbSignatureString(requestBody, headers);

			// Validate signature
			if (!isValid) {
				ConnResponse response = new ConnResponse();
				response.setConnectorCode(BankConstants.BVB_REQUEST_STATUS_WRONG_SIGNATURE);
				response.setMessage("Validate BVB signature failed!");
				LOGGER.error("Validate callback BVB signature failed!");
				return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);

			}

			// save notify record to database
			VietinNotifyTransTable vietinNotifyTransTable = new VietinNotifyTransTable();
			vietinNotifyTransTable.setAmount(callBackRequest.getData().getLcyAmount());
			vietinNotifyTransTable.setChannelId(callBackRequest.getData().getChannel());
			vietinNotifyTransTable.setCurrencyCode(callBackRequest.getData().getCcy());
			vietinNotifyTransTable.setCustCode(callBackRequest.getData().getClientUserID());
			vietinNotifyTransTable.setRecvAcctId(callBackRequest.getData().getAccNo());
			vietinNotifyTransTable.setRecvVirtualAcctId(callBackRequest.getData().getRelatedAccount());
			vietinNotifyTransTable.setRecvVirtualAcctName(callBackRequest.getData().getRelatedAccountName());
			vietinNotifyTransTable.setRemark(callBackRequest.getData().getNarrative());
			vietinNotifyTransTable.setSendAcctId(callBackRequest.getData().getFromAccNo());
			vietinNotifyTransTable.setSendAcctName(callBackRequest.getData().getFromAccName());
			vietinNotifyTransTable.setSendBankId(callBackRequest.getData().getFromBankCode());
			vietinNotifyTransTable.setTransId(callBackRequest.getData().getExternalRefNo());
			vietinNotifyTransTable.setTransType(callBackRequest.getData().getDrcr());
			vietinNotifyTransTable.setFromBankName(callBackRequest.getData().getFromBankName());
			vietinNotifyTransTable.setAcEntrySrNo(callBackRequest.getData().getAcEntrySrNo());
			vietinNotifyTransTable.setSourceTrans(callBackRequest.getData().getSource());
			vietinNotifyTransTable.setTrnRefNo(callBackRequest.getData().getTrnRefNo());
			vietinNotifyTransTable.setBankTime(callBackRequest.getBankTime());
			vietinNotifyTransTable.setEventName(callBackRequest.getEventName());
			vietinNotifyTransTable.setTraceId(callBackRequest.getTraceId());
			vietinNotifyTransTable.setTxnInitDt(
					bvbRequestUtil.readDateString(callBackRequest.getData().getTxnInitDt()));
			vietinNotifyTransTable.setValueDt(
					bvbRequestUtil.readDateString(callBackRequest.getData().getValueDt()));
			vietinNotifyTransTable.setTransTime(callBackRequest.getData().getTxnInitDt());
			vietinNotifyTransTable.setMsgType(BankConstants.BVBTransMsgType.INDIVIDUAL.getValue());
			vietinNotifyTransTable.setBankCode(OneFinConstants.BankListQrService.VCCB.getBankCode());
			vietinNotifyTransTable.setNapasTraceId(callBackRequest.getData().getNapasTraceId());
			vietinNotifyTransTable.setBankTransId(callBackRequest.getData().getExternalRefNo());
			VietinNotifyTransTable vietinNotifyTransStored
					= vietinNotifyTransTableRepo.save(vietinNotifyTransTable);

			// routing
			if (vietinNotifyTransStored.getTransType()
					.equals(BankConstants.BVBTransType.CREDIT.getValue())
					&& !stringHelper.checkNullEmptyBlank(vietinNotifyTransStored.getRecvVirtualAcctId())) {

				LOGGER.info("Transaction type Credit, release virtual acct");
				// Release virtual account in pool
				VietinVirtualAcctTransHistory historyData
						= bvbVirtualAcct.releaseVirtualAcctFromNotify(vietinNotifyTransStored);
				if (historyData != null) {
					bvbNotifyHandle.callbackWithPoolVirtualAcct(historyData.getBackendUrl(),
							vietinNotifyTransStored.getTransId()
									+ "_" + vietinNotifyTransStored.getId().toString()
									+ "_" + OneFinConstants.BankListQrService.VCCB.getBankCode(),
							OneFinConstants.LazyListVirtualAccountTransStatus.TRANS_SUCCESS_STATUS.getName(),
							vietinNotifyTransStored.getTransId(),
							vietinNotifyTransStored.getSendAcctId() + "_" + vietinNotifyTransStored.getSendAcctName());
				}
			} else {
				LOGGER.info("Transaction type not Credit");
			}

			ConnResponse response = new ConnResponse();
			bvbRequestUtil.transformErrorCode(
					response,
					BankConstants.BVB_REQUEST_STATUS_SUCCESS,
					OneFinConstants.LANGUAGE.VIETNAMESE.getValue()
			);
			return new ResponseEntity<>(response, HttpStatus.OK);
		} catch (Exception ex) {
			LOGGER.error("Process notify request failed", ex);
			ConnResponse response = new ConnResponse();
			bvbRequestUtil.transformErrorCode(
					response,
					BankConstants.BVB_REQUEST_STATUS_FAILED,
					OneFinConstants.LANGUAGE.VIETNAMESE.getValue()
			);
			return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
		}
	}

	@PostMapping("/notify-trans-batch")
	@MeasureExcutionTime
	public ResponseEntity<?> notifyTransBatch(
			@Valid @RequestBody(required = true)
			String requestBody,
			@RequestHeader Map<String, String> headers) throws Exception {
		try {
			LOGGER.log(Level.getLevel("INFOWT"), "CallBack batch Api Request: {}", requestBody);

			// Convert object
			BVBCallbackAPIBatchTrans callBackRequest =
					Optional.ofNullable((BVBCallbackAPIBatchTrans)
									jsonHelper.convertString2Map(requestBody, BVBCallbackAPIBatchTrans.class))
							.orElse((BVBCallbackAPIBatchTrans)
									jsonHelper.convertString2MapIgnoreUnknown(requestBody, BVBCallbackAPIBatchTrans.class));

			bvbRequestUtil.backUpRequestResponse(
					BankConstants.BVB_BACKUP_CALLBACK_BATCH_PREFIX,
					RandomStringUtils.random(12, true, true),
					callBackRequest,
					null,
					headers);

			boolean isValid = bvbRequestUtil.validateBvbSignatureString(requestBody, headers);

//			 Validate signature
			if (!isValid) {
				ConnResponse response = new ConnResponse();
				response.setConnectorCode(BankConstants.BVB_REQUEST_STATUS_WRONG_SIGNATURE);
				response.setMessage("Validate BVB signature failed!");
				LOGGER.error("Validate batch callback BVB signature failed!");
				return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
			}

			// save notify record to database
			List<BVBCallbackAPIBatchTransData> batchTransDataList = callBackRequest.getContent();
			LOGGER.info("Batch size: " + batchTransDataList.size());
			if (batchTransDataList.size() > 0) {
				for (BVBCallbackAPIBatchTransData callBackBatchTranData : batchTransDataList) {
					VietinNotifyTransTable vietinNotifyTransTable = new VietinNotifyTransTable();
					vietinNotifyTransTable.setAmount(callBackBatchTranData.getAmount().toString());
					vietinNotifyTransTable.setChannelId(callBackBatchTranData.getChannel());
					vietinNotifyTransTable.setCurrencyCode(callBackBatchTranData.getCcy());
					vietinNotifyTransTable.setCustCode(callBackBatchTranData.getClientUserID());
					vietinNotifyTransTable.setRecvVirtualAcctId(callBackBatchTranData.getRelatedAccount());
					vietinNotifyTransTable.setRecvVirtualAcctName(callBackBatchTranData.getRelatedAccName());
					vietinNotifyTransTable.setRemark(callBackBatchTranData.getNarrative());
					vietinNotifyTransTable.setSendAcctId(callBackBatchTranData.getFromAccNo());
					vietinNotifyTransTable.setSendAcctName(callBackBatchTranData.getFromAccName());
					vietinNotifyTransTable.setSendBankId(callBackBatchTranData.getFromBankCode());
					vietinNotifyTransTable.setTransId(callBackBatchTranData.getExternalRefNo());
					vietinNotifyTransTable.setTransType(callBackBatchTranData.getDrcr());
					vietinNotifyTransTable.setFromBankName(callBackBatchTranData.getFromBankName());
					vietinNotifyTransTable.setTraceId(callBackBatchTranData.getTraceId());
					vietinNotifyTransTable.setSourceTrans(callBackBatchTranData.getSource());
					vietinNotifyTransTable.setTrnRefNo(callBackBatchTranData.getTrnRefNo());
					vietinNotifyTransTable.setTxnInitDt(
							bvbRequestUtil.readBatchDateString(callBackBatchTranData.getTxnInitDt()));
					vietinNotifyTransTable.setValueDt(
							bvbRequestUtil.readBatchDateString(callBackBatchTranData.getValueDt()));
					vietinNotifyTransTable.setTransactionDate(
							bvbRequestUtil.readTransactionDateString(callBackBatchTranData.getTransactionDate()));
					vietinNotifyTransTable.setTransTime(callBackBatchTranData.getTxnInitDt());
					vietinNotifyTransTable.setPartnerCode(callBackBatchTranData.getPartnerCode());
					vietinNotifyTransTable.setPartnerId(callBackBatchTranData.getPartnerId());
					vietinNotifyTransTable.setCreditAccount(callBackBatchTranData.getCreditAccount());
					vietinNotifyTransTable.setCrAcc(callBackBatchTranData.getPartnerCode());
					vietinNotifyTransTable.setMsgType(BankConstants.BVBTransMsgType.BATCH.getValue());
					vietinNotifyTransTable.setBankCode(OneFinConstants.BankListQrService.VCCB.getBankCode());
					vietinNotifyTransTable.setBankTransId(callBackBatchTranData.getExternalRefNo());
					vietinNotifyTransTable.setNapasTraceId(callBackBatchTranData.getNapasTraceId());
					VietinNotifyTransTable vietinNotifyTransStored
							= vietinNotifyTransTableRepo.save(vietinNotifyTransTable);

					// routing
					if (vietinNotifyTransTable.getTransType()
							.equals(BankConstants.BVBTransType.CREDIT.getValue())
							&& !stringHelper.checkNullEmptyBlank(
							vietinNotifyTransTable.getRecvVirtualAcctId())) {
						LOGGER.info("Transaction type Credit, release virtual acct");
						// Release virtual account in pool
						VietinVirtualAcctTransHistory historyData
								= bvbVirtualAcct.releaseVirtualAcctFromNotify(vietinNotifyTransStored);
						if (historyData != null) {
							bvbNotifyHandle.callbackWithPoolVirtualAcct(historyData.getBackendUrl(),
									vietinNotifyTransStored.getTransId()
											+ "_" + vietinNotifyTransStored.getId().toString()
											+ "_" + OneFinConstants.BankListQrService.VCCB.getBankCode(),
									OneFinConstants.LazyListVirtualAccountTransStatus.TRANS_SUCCESS_STATUS.getName(),
									vietinNotifyTransStored.getTransId(),
									vietinNotifyTransStored.getSendAcctId() + "_" + vietinNotifyTransStored.getSendAcctName()
							);
						}
					} else {
						LOGGER.info("Transaction type not Credit");
					}
				}
			} else {
				LOGGER.info("Request empty!");
			}
			ConnResponse response = new ConnResponse();
			bvbRequestUtil.transformErrorCode(
					response,
					BankConstants.BVB_REQUEST_STATUS_SUCCESS,
					OneFinConstants.LANGUAGE.VIETNAMESE.getValue()
			);

			return new ResponseEntity<>(response, HttpStatus.OK);
		} catch (Exception e) {

			LOGGER.error("Process notify batch request failed", e);
			ConnResponse response = new ConnResponse();
			bvbRequestUtil.transformErrorCode(
					response,
					BankConstants.BVB_REQUEST_STATUS_FAILED,
					OneFinConstants.LANGUAGE.VIETNAMESE.getValue()
			);
			return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
		}

	}

}
