package com.onefin.ewallet.bank.service.bvb;

import com.onefin.ewallet.common.base.constants.BankConstants;
import com.onefin.ewallet.bank.dto.bvb.bankTransfer.BVBIBFTFundTransferRequest;
import com.onefin.ewallet.bank.dto.bvb.bankTransfer.BVBIBFTFundTransferResponse;
import com.onefin.ewallet.bank.repository.jpa.BankListRepository;
import com.onefin.ewallet.bank.repository.jpa.BankTransferRepo;
import com.onefin.ewallet.bank.repository.jpa.ChildBankTransferRecordsRepo;
import com.onefin.ewallet.bank.repository.jpa.HolidayRepo;
import com.onefin.ewallet.bank.service.vietin.VietinBankTransferDto;
import com.onefin.ewallet.common.base.constants.OneFinConstants;
import com.onefin.ewallet.common.base.constants.OneFinEnum;
import com.onefin.ewallet.common.base.errorhandler.RuntimeInternalServerException;
import com.onefin.ewallet.common.domain.bank.common.BankList;
import com.onefin.ewallet.common.domain.bank.transfer.BankTransferChildRecords;
import com.onefin.ewallet.common.domain.bank.transfer.BankTransferTransaction;
import com.onefin.ewallet.common.utility.date.DateTimeHelper;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.logging.log4j.LogManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;

@Service
public class BVBIBFTPersistance {

	private static final org.apache.logging.log4j.Logger LOGGER = LogManager.getLogger(BVBIBFTPersistance.class);

	@Autowired
	private VietinBankTransferDto vietinBankTransferDto;

	@Autowired
	private DateTimeHelper dateTimeHelper;

	@Autowired
	private BankListRepository bankListRepository;

	@Autowired
	private BankTransferRepo bankTransferRepo;

	@Autowired
	private ChildBankTransferRecordsRepo childBankTransferRecordsRepo;

	@Autowired
	private HolidayRepo holidayRepo;

	@Autowired
	private BVBEncryptUtil bvbEncryptUtil;

	@Value("${bvb.IBFT.onefinPrivateKey}")
	private String privateKeyPath;

	@Value("${bvb.IBFT.onefinClientCode}")
	private String clientCode;

	@Value("${bvb.IBFT.onefinMerchantCode}")
	private String requestIdPrefix;

	public BankTransferTransaction initBankTransferRecord(BVBIBFTFundTransferRequest request) throws Exception {

		List<BankTransferTransaction> existTrans = bankTransferRepo.findByRequestIdVCCB(
				request.getRequestId(), OneFinConstants.BankListQrService.VCCB.getBankCode()
		);
		if (existTrans.size() > 0) {
			throw new RuntimeInternalServerException(String.format("Transaction ID %s already exist, please try another one ", request.getData().getBankCode()));
		}
		List<BankList> bankList = bankListRepository.findByVccbBankId(request.getData().getBankCode());

		if (bankList.isEmpty()) {
			throw new RuntimeInternalServerException(String.format("Not found bank with napas bank id: %s", request.getData().getBankCode()));
		}
		if (bankList.size() > 1) {
			throw new RuntimeInternalServerException(String.format("Found more than 1 bank with napas bank id: %s", request.getData().getBankCode()));
		}
		LOGGER.info("bankList: {}", bankList);
		BankList bankGet = bankList.get(0);

		BankTransferTransaction trans = new BankTransferTransaction();
		trans.setRequestId(request.getRequestId());
		trans.setApiOperation(OneFinConstants.BANK_TRANSFER);
		trans.setTranStatus(OneFinConstants.TRANS_PROCESSING);
//		trans.setMerchantId(data.getMerchantId());
//		trans.setProviderId(data.getProviderId());
		trans.setProcessedRecord(1);
//		trans.setRemittanceType(VietinConstants.RemittanceType.NAPAS.getValue());
		trans.setFeeType(request.getData().getFeeModel());
//		trans.setVerifyByBank("N");
		trans.setClientRequestId(RandomStringUtils.random(12, true, true));

		BankTransferChildRecords childRecords = new BankTransferChildRecords();
		childRecords.setAmount(BigDecimal.valueOf(Long.parseLong(request.getData().getAmount())));
		if (request.getData().getCardNo() != null) {
			childRecords.setRecvAcctCard(request.getData().getCardNo());
		} else if (request.getData().getAccountNo() != null) {
			childRecords.setRecvAcctId(request.getData().getAccountNo());
		}
		trans.setCreatedDate(request.getTime());
		trans.setUpdatedDate(request.getTime());
		trans.setProviderBankCode(OneFinConstants.BankListQrService.VCCB.getBankCode());
		childRecords.setTransId(request.getRequestId());
		childRecords.setRemark(request.getData().getDescription());
		childRecords.setRecvBank(bankGet);
		childRecords.setBankTransferTransaction(trans);
		childRecords.setVccbOnus(request.getData().getOnus());
		childRecords.setCreatedDate(request.getTime());
		childRecords.setUpdatedDate(request.getTime());
		childRecords.setCurrency(OneFinEnum.CurrencyCode.VND.getValue());
		trans.addChildRecord(childRecords);
		return (BankTransferTransaction) bankTransferRepo.saveAndFlush(trans);
	}

	public BankTransferTransaction updateBankTransferRecord(BVBIBFTFundTransferRequest request,
															BVBIBFTFundTransferResponse response) throws Exception {


		List<BankTransferTransaction> existTrans = bankTransferRepo.findByRequestIdVCCB(
				request.getRequestId(), OneFinConstants.BankListQrService.VCCB.getBankCode()
		);

		if (existTrans.isEmpty()) {
			throw new RuntimeInternalServerException(String.format("Not found transaction id: %s", request.getRequestId()));
		}

		if (existTrans.size() > 1) {
			throw new RuntimeInternalServerException(String.format("Found more than 1 transaction id: %s", request.getRequestId()));
		}
		BankTransferTransaction bankTransferTransaction
				= existTrans.get(0);
		Date currentDate = dateTimeHelper.currentDate(OneFinConstants.HO_CHI_MINH_TIME_ZONE);
		bankTransferTransaction.setBankStatusCode(response.getStatus());
		bankTransferTransaction.setUpdatedDate(currentDate);

		if (BankConstants.BVBIBFTErrorCode.findByErrorCode(response.getErrorCode())
				.getTransStatus().equals(BankConstants.BVBIBFTTransStatus.SUCCESS.getName())) {
			bankTransferTransaction.setTranStatus(OneFinConstants.TRANS_SUCCESS);
		} else if (BankConstants.BVBIBFTErrorCode.findByErrorCode(response.getErrorCode())
				.getTransStatus().equals(BankConstants.BVBIBFTTransStatus.PENDING.getName())) {
			bankTransferTransaction.setTranStatus(OneFinConstants.TRANS_PENDING);
		} else if (BankConstants.BVBIBFTErrorCode.findByErrorCode(response.getErrorCode())
				.getTransStatus().equals(BankConstants.BVBIBFTTransStatus.FAILED.getName())) {
			bankTransferTransaction.setTranStatus(BankConstants.BVBIBFTTransStatus.FAILED.getName());
		} else {
			bankTransferTransaction.setTranStatus(OneFinConstants.TRANS_ERROR);
		}
		bankTransferTransaction.getRecords().forEach(
				e -> {
					e.setBankStatusCode(response.getStatus());
					e.setMessage(response.getErrorMessage());
					e.setVccbErrorCode(response.getErrorCode());
					e.setBankTransactionId(response.getResponseId());
					e.setUpdatedDate(currentDate);
					try {
						e.setSettleDate(response.getDataParsed().getSettleDate());
					} catch (Exception ex) {
						LOGGER.error("Error reading settle date", ex);
					}
				}
		);
		return (BankTransferTransaction) bankTransferRepo.saveAndFlush(bankTransferTransaction);
	}


}
