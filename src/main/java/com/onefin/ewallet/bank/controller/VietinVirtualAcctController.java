package com.onefin.ewallet.bank.controller;

import com.onefin.ewallet.bank.common.OtherConstants;
import com.onefin.ewallet.bank.dto.vietin.*;
import com.onefin.ewallet.bank.repository.jpa.VirtualAcctRepo;
import com.onefin.ewallet.bank.repository.jpa.VirtualAcctStatusHistoryRepo;
import com.onefin.ewallet.bank.repository.jpa.VirtualAcctTransHistoryRepo;
import com.onefin.ewallet.bank.repository.redis.VirtualAcctTransWatching;
import com.onefin.ewallet.bank.repository.redis.VirtualAcctTransWatchingRepo;
import com.onefin.ewallet.bank.service.bvb.BVBVirtualAcct;
import com.onefin.ewallet.bank.service.common.ConfigLoader;
import com.onefin.ewallet.bank.service.common.NumberSequenceService;
import com.onefin.ewallet.bank.service.vietin.VietinRequestUtil;
import com.onefin.ewallet.bank.service.vietin.VietinVirtualAcct;
import com.onefin.ewallet.common.base.anotation.MeasureExcutionTime;
import com.onefin.ewallet.common.base.constants.OneFinConstants;
import com.onefin.ewallet.common.base.controller.AbstractBaseController;
import com.onefin.ewallet.common.utility.date.DateTimeHelper;
import com.onefin.ewallet.common.utility.string.StringHelper;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequestMapping("/vietin/virtualacct")
public class VietinVirtualAcctController extends AbstractBaseController {

	@Autowired
	private VirtualAcctStatusHistoryRepo virtualAcctStatusHistoryRepo;

	@Autowired
	private VirtualAcctRepo virtualAcctRepo;

	@Autowired
	public VietinVirtualAcct vietinVirtualAcct;

	@Autowired
	private VirtualAcctTransHistoryRepo vietVirtualAcctTransHistoryRepo;

	@Autowired
	private VietinRequestUtil IHTTPRequestUtil;

	@Autowired
	private ConfigLoader configLoader;

	@Autowired
	private DateTimeHelper dateTimeHelper;

	@Autowired
	private NumberSequenceService numberSequenceService;

	@Autowired
	private ModelMapper modelMapper;

	@Autowired
	private StringHelper stringHelper;

	private static final Logger LOGGER = LoggerFactory.getLogger(VietinVirtualAcctController.class);

	@Autowired
	private BVBVirtualAcct bvbVirtualAcct;

	@Autowired
	private VirtualAcctTransWatchingRepo virtualAcctTransWatchingRepo;


	@PostMapping("/create")
	public ResponseEntity<?> createVirtualAcct(@Valid @RequestBody(required = true) VirtualAcctCreateRequest requestBody) throws Exception {

		ConnResponse createVirtualResponse = vietinVirtualAcct.buildCreateResponseEntity(requestBody);
		return new ResponseEntity<>(createVirtualResponse, HttpStatus.OK);
	}

	@PostMapping("/updatestatus")
	public ResponseEntity<?> VirtualAcctUpdateStatus(@Valid @RequestBody VirtualAcctUpdateStatusRequest requestBody) throws Exception {
		return new ResponseEntity<>(vietinVirtualAcct.virtualAcctUpdateStatus(requestBody), HttpStatus.OK);
	}

	@PostMapping("/seed")
	public ResponseEntity<?> seedVirtualAcctPool(
			@Valid @RequestBody() VirtualAcctSeedRequest requestBody
	)
			throws Exception {
		if (requestBody.getBankCode().equals(OneFinConstants.BankListQrService.CTG.getBankCode())) {
			vietinVirtualAcct.buildSeedVirtualAcctPoolResponseEntity(requestBody);
		} else if (requestBody.getBankCode().equals(OneFinConstants.BankListQrService.VCCB.getBankCode())) {
			bvbVirtualAcct.buildSeedVirtualAcctPoolResponseEntity(requestBody);
		}
		return new ResponseEntity<>(HttpStatus.OK);
	}

	@PostMapping("/get")
	@MeasureExcutionTime
	public ResponseEntity<?> getVirtualAcctInPool(
			@Valid @RequestBody() VirtualAcctGetRequest requestBody
	)
			throws Exception {
		//TODO: get TTL by merchantCode
		virtualAcctTransWatchingRepo.save(new VirtualAcctTransWatching(requestBody.getTransUniqueKey(), requestBody.getMerchantCode(), requestBody.getBankCode(), false, OtherConstants.VIRTUAL_ACCOUNT_TRANS_WATCHING_DEFAULT_TTL));

		VietinVirtualAcctDto reponseItem = null;
		if (requestBody.getBankCode().equals(OneFinConstants.BankListQrService.CTG.getBankCode())) {
			reponseItem
					= vietinVirtualAcct.buildVirtualAcctInPool(requestBody);
		} else if (requestBody.getBankCode().equals(OneFinConstants.BankListQrService.VCCB.getBankCode())) {
			reponseItem = bvbVirtualAcct.buildVirtualAcctInPool(requestBody);
		}
		return new ResponseEntity<>(reponseItem, HttpStatus.OK);
	}

}
