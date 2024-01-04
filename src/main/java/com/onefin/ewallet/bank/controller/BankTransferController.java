package com.onefin.ewallet.bank.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.onefin.ewallet.bank.common.VietinConstants;
import com.onefin.ewallet.bank.config.BankHelper;
import com.onefin.ewallet.bank.dto.cms.bankTransfer.*;
import com.onefin.ewallet.bank.dto.vietin.*;
import com.onefin.ewallet.bank.repository.jpa.BankListDetailsRepository;
import com.onefin.ewallet.bank.repository.jpa.BankListRepository;
import com.onefin.ewallet.bank.repository.jpa.BankTransferRepo;
import com.onefin.ewallet.bank.service.bvb.BVBTransferBusinessService;
import com.onefin.ewallet.bank.service.common.ConfigLoader;
import com.onefin.ewallet.bank.service.vietin.VietinBankTransferDto;
import com.onefin.ewallet.bank.service.vietin.VietinMessageUtil;
import com.onefin.ewallet.bank.service.vietin.VietinRequestUtil;
import com.onefin.ewallet.common.base.constants.OneFinConstants;
import com.onefin.ewallet.common.base.controller.AbstractBaseController;
import com.onefin.ewallet.common.base.errorhandler.RuntimeBadRequestException;
import com.onefin.ewallet.common.base.errorhandler.RuntimeNotFoundException;
import com.onefin.ewallet.common.domain.bank.common.BankList;
import com.onefin.ewallet.common.domain.bank.common.BankListDetails;
import com.onefin.ewallet.common.utility.date.DateTimeHelper;
import org.apache.commons.lang3.RandomStringUtils;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/bank/transfer")
public class BankTransferController extends AbstractBaseController {

	private static final Logger LOGGER = LoggerFactory.getLogger(BankTransferController.class);

	@Autowired
	public VietinBankTransferDto vietinBankTransferService;

	@Autowired
	private VietinRequestUtil vietinRequestUtil;

	@Autowired
	private BankTransferRepo bankTransferRepo;

	@Autowired
	private BankListDetailsRepository bankListDetailsRepository;

	@Autowired
	private ConfigLoader configLoader;

	@Autowired
	private VietinMessageUtil vietinMessageUtil;

	@Autowired
	private BankHelper bankHelper;

	@Autowired
	private VietinBankTransferDto vietinBankTransferDto;

	@Autowired
	private BankListRepository bankListRepository;

	@Autowired
	private DateTimeHelper dateTimeHelper;

	@Autowired
	private ModelMapper modelMapper;

	@Autowired
	private BVBTransferBusinessService bvbTransferBusinessService;

	@PostMapping
	public ResponseEntity<?> bankTransfer(
			@RequestHeader(value = "walletId", required = false) String walletId,
			@Valid @RequestBody CMSBankTransferRequest cmsRequestBody) throws Exception {

		BankTransferRequest requestBody = new BankTransferRequest();
		requestBody.setClientRequestId(cmsRequestBody.getTransactionId());
		requestBody.setRemittanceType(cmsRequestBody.getTransactionMethod());
		requestBody.setExecUserID(cmsRequestBody.getMemberId());
		requestBody.setLanguage(OneFinConstants.LANGUAGE.VIETNAMESE.getValue());
		requestBody.setBankCode(cmsRequestBody.getBankCode());
		if (cmsRequestBody.getBankCode().equals(OneFinConstants.BankListQrService.VCCB.getBankCode())) {
			requestBody.setFeeType(VietinConstants.FeeType.B2C.getValue());
		} else {
			requestBody.setFeeType(cmsRequestBody.getFeeType());
		}
		requestBody.setChannel(configLoader.getVietinPaymentBankTransferChannel());
		BankTransferRecord bankTransferRecord = new BankTransferRecord();
		bankTransferRecord.setPriority(1);
		bankTransferRecord.setRecvBankId(cmsRequestBody.getRecvBankId());
		bankTransferRecord.setRecvAcctId(cmsRequestBody.getRecvAcctId());
		bankTransferRecord.setRecvAcctName(cmsRequestBody.getRecvAcctName());
		bankTransferRecord.setAmount(BigDecimal.valueOf(cmsRequestBody.getAmount().longValue()));
		bankTransferRecord.setCurrencyCode(cmsRequestBody.getCurrency());
		bankTransferRecord.setPayRefNo(cmsRequestBody.getPayRefNo());
		bankTransferRecord.setPayRefInfor(cmsRequestBody.getPayRefInfor());
		bankTransferRecord.setRemark(cmsRequestBody.getTransDescription());
		bankTransferRecord.setSenderAccountOrder(cmsRequestBody.getSenderAccountOrder());
		bankTransferRecord.setIsCard(cmsRequestBody.getIsCard());
		requestBody.addRecord(bankTransferRecord);

		ResponseEntity<?> responseEntity;
		if (requestBody.getBankCode().equals(OneFinConstants.BankListQrService.VCCB.getBankCode())) {
			responseEntity = bvbTransferBusinessService.bankTransfer(walletId, requestBody);
		} else if (requestBody.getBankCode().equals(OneFinConstants.BankListQrService.CTG.getBankCode())) {
			responseEntity = vietinBankTransferService.bankTransfer(walletId, requestBody);
		} else {
			throw new RuntimeBadRequestException(String.format("Unknown provider %s", requestBody.getBankCode()));
		}

		ConnResponse responseEntityResponse
				= modelMapper.map(responseEntity.getBody(), ConnResponse.class);


		BankTransferResponse bankTransferResponse
				= modelMapper.map(responseEntityResponse.getResponse(), BankTransferResponse.class
		);

		CMSBankTransferResponse cmsBankTransferResponse = new CMSBankTransferResponse();

		cmsBankTransferResponse.setTransactionId(cmsRequestBody.getTransactionId());
		Status status = new Status();
		status.setMessage(bankTransferResponse.getStatus().getMessage());
		status.setCode(bankTransferResponse.getStatus().getCode());
		cmsBankTransferResponse.setStatus(status);
		cmsBankTransferResponse.setProcessedRecord(bankTransferResponse.getRecords().size());
		cmsBankTransferResponse.setBankName(bankTransferResponse.getBankName());
		cmsBankTransferResponse.setBankId(bankTransferResponse.getBankId());
		cmsBankTransferResponse.setBranchId(bankTransferResponse.getBranchId());
		cmsBankTransferResponse.setAccountName(bankTransferResponse.getAccountName());
		cmsBankTransferResponse.setCurrency(bankTransferResponse.getRecords().get(0).getCurrencyCode());
		cmsBankTransferResponse.setTransDescription(bankTransferResponse.getRecords().get(0).getRemark());
		cmsBankTransferResponse.setBankTransactionId(bankTransferResponse.getRecords().get(0).getBankTransactionId());
		return new ResponseEntity<>(cmsBankTransferResponse, HttpStatus.OK);

	}

	@PostMapping("/trans-inquiry")
	public ResponseEntity<?> bankTransferTransInquiry(
			@Valid @RequestBody CMSTransInquiryRequest cmsRequestBody) throws Exception {

		// Mapping to banktransfer dto
		BankTransferInquiryRequest requestBody = new BankTransferInquiryRequest();
		requestBody.setQueryRequestId(cmsRequestBody.getTransactionId());
		requestBody.setLanguage(OneFinConstants.LANGUAGE.VIETNAMESE.getValue());
		requestBody.setBankCode(cmsRequestBody.getBankCode());
		requestBody.setChannel(configLoader.getVietinPaymentBankTransferChannel());

		ResponseEntity<?> responseEntity;
		if (requestBody.getBankCode().equals(OneFinConstants.BankListQrService.VCCB.getBankCode())) {
			responseEntity = bvbTransferBusinessService.bankTransferTransInquiry(requestBody);
		} else if (requestBody.getBankCode().equals(OneFinConstants.BankListQrService.CTG.getBankCode())) {
			responseEntity = vietinBankTransferService.bankTransferTransInquiry(requestBody);
		} else {
			throw new RuntimeBadRequestException(String.format("Unknown provider %s", requestBody.getBankCode()));
		}

		ConnResponse responseEntityBody
				= modelMapper.map(responseEntity.getBody(), ConnResponse.class);

		// construct BVB Create virtual code response body
		BankTransferResponse bankTransferResponse
				= modelMapper.map(
				responseEntityBody.getResponse(),
				BankTransferResponse.class
		);

		CMSTransInquiryResponse cmsBankTransferResponse = new CMSTransInquiryResponse();

		cmsBankTransferResponse.setTransactionId(cmsRequestBody.getTransactionId());
		Status status = new Status();
		status.setMessage(bankTransferResponse.getStatus().getMessage());
		status.setCode(bankTransferResponse.getStatus().getCode());
		cmsBankTransferResponse.setStatus(status);
		cmsBankTransferResponse.setProcessedRecord(bankTransferResponse.getRecords().size());
		cmsBankTransferResponse.setBankName(bankTransferResponse.getBankName());
		cmsBankTransferResponse.setBankId(bankTransferResponse.getBankId());
		cmsBankTransferResponse.setBranchId(bankTransferResponse.getBranchId());
		cmsBankTransferResponse.setAccountName(bankTransferResponse.getAccountName());
		cmsBankTransferResponse.setCurrency(bankTransferResponse.getRecords().get(0).getCurrencyCode());
		cmsBankTransferResponse.setTransDescription(bankTransferResponse.getRecords().get(0).getRemark());
		cmsBankTransferResponse.setBankTransId(bankTransferResponse.getRecords().get(0).getBankTransactionId());
		return new ResponseEntity<>(cmsBankTransferResponse, HttpStatus.OK);

	}

	@PostMapping("/account-inquiry")
	public ResponseEntity<?> bankTransferAccountInquiry(
			@Valid @RequestBody CMSAccountInquiryRequest cmsRequestBody) throws Exception {

		// mapping
		BankTransferAccountInquiryRequest requestBody = new BankTransferAccountInquiryRequest();
		requestBody.setRequestId(RandomStringUtils.random(16, true, true));
		requestBody.setLanguage(OneFinConstants.LANGUAGE.VIETNAMESE.getValue());
		requestBody.setBankCode(cmsRequestBody.getBankCode());
		requestBody.setChannel(configLoader.getVietinPaymentBankTransferChannel());
		requestBody.setRemittanceType(cmsRequestBody.getTransactionMethod());
		requestBody.setBankId(cmsRequestBody.getBankId());
		requestBody.setAccountId(cmsRequestBody.getAccountId());
		requestBody.setIsCard(cmsRequestBody.getIsCard());

		ResponseEntity<?> responseEntity;
		if (requestBody.getBankCode().equals(OneFinConstants.BankListQrService.VCCB.getBankCode())) {
			responseEntity = bvbTransferBusinessService.bankTransferAccountInquiry(requestBody);
		} else if (requestBody.getBankCode().equals(OneFinConstants.BankListQrService.CTG.getBankCode())) {
			responseEntity = vietinBankTransferService.bankTransferAccountInquiry(requestBody);
		} else {
			throw new RuntimeBadRequestException(String.format("Unknown provider %s", requestBody.getBankCode()));
		}

		ConnResponse bankTransferResponse
				= modelMapper.map(responseEntity.getBody(), ConnResponse.class);

		// construct BVB Create virtual code response body
		BankTransferResponse bankTransferResponseBody
				= modelMapper.map(
				bankTransferResponse.getResponse(),
				BankTransferResponse.class
		);

		CMSAccountInquiryResponse cmsAccountInquiryResponse =
				new CMSAccountInquiryResponse();
		Status status = new Status();
		status.setMessage(bankTransferResponseBody.getStatus().getMessage());
		status.setCode(bankTransferResponseBody.getStatus().getCode());
		cmsAccountInquiryResponse.setStatus(status);
		cmsAccountInquiryResponse.setBankName(bankTransferResponseBody.getBankName());
		cmsAccountInquiryResponse.setBankId(bankTransferResponseBody.getBankId());
		cmsAccountInquiryResponse.setAccountName(bankTransferResponseBody.getAccountName());

		return new ResponseEntity<>(
				cmsAccountInquiryResponse,
				HttpStatus.OK);
	}

	@PostMapping("/provider-inquiry")
	public ResponseEntity<?> providerInquiry(@Valid @RequestBody BankTransferProviderInquiryRequest requestBody) throws Exception {
		return vietinBankTransferService.providerInquiry(requestBody);
	}

	@GetMapping("/bank-list/type/{type}")
	public ResponseEntity<?> getBankTransferList(@PathVariable() String type) {
		if (type.equals(VietinConstants.BANK_TRANSFER_TYPE.CITAD_TTSP.getValue())) {
			List<BankList> listBank = bankListRepository.findListBankByCitad();
			return new ResponseEntity<>(vietinMessageUtil.buildVietinBankTransferConnectorResponse(VietinConstants.CONN_SUCCESS, listBank.stream().map(p -> new Lookup(p.getName(), p.getBankCode())).collect(Collectors.toList())), HttpStatus.OK);
		}
		if (type.equals(VietinConstants.BANK_TRANSFER_TYPE.NAPAS_247.getValue())) {
			List<BankList> listBank = bankListRepository.findListBankByNapas247();
			return new ResponseEntity<>(vietinMessageUtil.buildVietinBankTransferConnectorResponse(VietinConstants.CONN_SUCCESS, listBank.stream().map(p -> new Lookup(p.getName(), p.getCode())).collect(Collectors.toList())), HttpStatus.OK);
		}
		return new ResponseEntity<>(vietinMessageUtil.buildVietinBankTransferConnectorResponse(VietinConstants.CONN_SUCCESS, null), HttpStatus.OK);
	}

	@GetMapping("/bank-list/bankCode/{bankCode}/province")
	public ResponseEntity<?> getProvinceByBank(@PathVariable() String bankCode) {
		List<BankList> listBank = bankListRepository.findByBankCode(bankCode);
		List<BankListDetails> listBankDetails = new ArrayList<>();
		listBank.stream().forEach(e -> {
			listBankDetails.addAll(bankListDetailsRepository.findAllBranchByCitadCode(e.getCode()));
		});
		List<BankListDetails> distinctElements = listBankDetails.stream()
				.filter(BankHelper.distinctByKey(p -> p.getProvince()))
				.collect(Collectors.toList());
		return new ResponseEntity<>(vietinMessageUtil.buildVietinBankTransferConnectorResponse(VietinConstants.CONN_SUCCESS, distinctElements.stream().map(p -> new Lookup(p.getProvince(), p.getProvince())).collect(Collectors.toList())), HttpStatus.OK);
	}

	@GetMapping("/bank-list/bankCode/{bankCode}/province/{province}")
	public ResponseEntity<?> getBankBranchByBankCodeAndProvice(@PathVariable() String bankCode, @PathVariable() String province) {
		List<BankList> listBanks = bankListRepository.findByBankCode(bankCode);
		List<String> citadList = listBanks.stream().map(listBank -> listBank.getCode()).collect(Collectors.toList());
		LOGGER.info("List citad: {}", citadList);
		List<BankListDetails> listBranchs = new ArrayList<>();
		if (province != null && !province.isEmpty()) {
			listBranchs.addAll(bankListDetailsRepository.findAllBranchByBankListCodeAndProvince(citadList, province));
		} else {
			for (String cited : citadList) {
				listBranchs.addAll(bankListDetailsRepository.findAllBranchByCitadCode(cited));
			}
		}

		List<BankListDetailsDto> listBranchsDto = modelMapper.map(listBranchs, new TypeToken<List<BankListDetailsDto>>() {
		}.getType());
		return new ResponseEntity<>(vietinMessageUtil.buildVietinBankTransferConnectorResponse(VietinConstants.CONN_SUCCESS, listBranchsDto), HttpStatus.OK);
	}

	@PostMapping("/bank-list/branch")
	public ResponseEntity<?> createBankBranch(@Valid @RequestBody BankTransferList requestBody) {
		Date currentDate = dateTimeHelper.currentDate(OneFinConstants.HO_CHI_MINH_TIME_ZONE);
		Optional<BankList> bank = Optional.ofNullable(bankListRepository.findBankListByCode(requestBody.getCitad()));
		return bank.map(e -> {
			BankListDetails bankDetails = new BankListDetails();
			bankDetails.setId(bankListDetailsRepository.findMaxInColumnId() + 1);
			bankDetails.setCreatedDate(currentDate);
			bankDetails.setUpdatedDate(currentDate);
			bankDetails.setProvince(requestBody.getProvince());
			bankDetails.setBranchName(requestBody.getBranchName());
			bankDetails.setBranchCode(requestBody.getBranchCode());
			bankDetails.setBankList(e);
			bankDetails = bankListDetailsRepository.save(bankDetails);
			if (e.getDetail() == null) {
				e.setDetail(new HashSet<>());
			}
			e.getDetail().add(bankDetails);
			e.setUpdatedDate(currentDate);
			return new ResponseEntity(bankListRepository.save(e), HttpStatus.OK);
		}).orElseThrow(() -> new RuntimeNotFoundException(String.format("Bank with citad %s not found", requestBody.getCitad())));
	}

	@PostMapping("/callback")
	public ResponseEntity<?> bankTransferCallBack(
			@Valid @RequestBody HashMap<String,String> cmsRequestBody) throws Exception {
		ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
		String json = ow.writeValueAsString(cmsRequestBody);
		LOGGER.info("callback received: {}", json);
		return new ResponseEntity<>(HttpStatus.OK);

	}

}
