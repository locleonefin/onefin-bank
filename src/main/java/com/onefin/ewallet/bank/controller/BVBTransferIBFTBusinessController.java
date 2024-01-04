package com.onefin.ewallet.bank.controller;


import com.onefin.ewallet.bank.dto.vietin.*;
import com.onefin.ewallet.bank.repository.jpa.BankListRepository;
import com.onefin.ewallet.bank.repository.jpa.BankTransferRepo;
import com.onefin.ewallet.bank.repository.jpa.ChildBankTransferRecordsRepo;
import com.onefin.ewallet.bank.service.bvb.BVBEncryptUtil;
import com.onefin.ewallet.bank.service.bvb.BVBTransferBusinessService;
import com.onefin.ewallet.bank.service.bvb.BVBTransferRequestUtil;
import com.onefin.ewallet.bank.service.common.ConfigLoader;
import com.onefin.ewallet.bank.service.common.NumberSequenceService;
import com.onefin.ewallet.bank.service.vietin.VietinBankTransferDto;
import com.onefin.ewallet.bank.service.vietin.VietinRequestUtil;
import com.onefin.ewallet.common.utility.date.DateTimeHelper;
import org.apache.logging.log4j.LogManager;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.validation.Valid;

@Controller
@RequestMapping("/bvb/transfer")
public class BVBTransferIBFTBusinessController {

	//    private static final Logger LOGGER = LoggerFactory.getLogger(BVBTransferIBFTBusinessController.class);
	private static final org.apache.logging.log4j.Logger LOGGER = LogManager.getLogger(BVBTransferIBFTBusinessController.class);

	@Autowired
	private DateTimeHelper dateTimeHelper;

	@Autowired
	private BVBEncryptUtil bvbEncryptUtil;

	@Autowired
	private BVBTransferRequestUtil bvbTransferRequestUtil;

	@Autowired
	private Environment env;

	@Autowired
	private ConfigLoader configLoader;

	@Autowired
	private ModelMapper modelMapper;

	@Autowired
	private BankTransferRepo bankTransferRepo;

	@Autowired
	private BankListRepository bankListRepository;

	@Value("${bvb.IBFT.channel}")
	private String bvbTransferChannel;

	@Value("${bvb.IBFT.merchantId}")
	private String bvbTransferMerchantId;

	@Value("${bvb.IBFT.onefinClientCode}")
	private String onefinClientCode;

	@Autowired
	private VietinBankTransferDto vietinBankTransferDto;

	@Autowired
	private VietinRequestUtil vietinRequestUtil;

	@Autowired
	private NumberSequenceService numberSequenceService;

	@Autowired
	private ChildBankTransferRecordsRepo childBankTransferRecordsRepo;

	@Autowired
	private BVBTransferBusinessService bvbTransferBusinessService;

	@PostMapping()
	public ResponseEntity<?> bankTransfer(@RequestHeader(value = "walletId", required = false) String walletId,
										  @Valid @RequestBody BankTransferRequest requestBody) throws Exception {

		return bvbTransferBusinessService.bankTransfer(walletId, requestBody);
	}


	@PostMapping("/trans-inquiry")
	public ResponseEntity<?> bankTransferTransInquiry(@Valid @RequestBody BankTransferInquiryRequest requestBody) throws Exception {
		return bvbTransferBusinessService.bankTransferTransInquiry(requestBody);
	}


	@PostMapping("/account-inquiry")
	public ResponseEntity<?> bankTransferAccountInquiry(@Valid @RequestBody BankTransferAccountInquiryRequest requestBody) throws Exception {
		return bvbTransferBusinessService.bankTransferAccountInquiry(requestBody);
	}


}
