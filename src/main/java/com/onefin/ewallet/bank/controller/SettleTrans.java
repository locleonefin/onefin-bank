package com.onefin.ewallet.bank.controller;

import com.onefin.ewallet.bank.repository.jpa.ChildBankTransferRecordsRepo;
import com.onefin.ewallet.bank.repository.jpa.LinkBankTransRepo;
import com.onefin.ewallet.bank.service.vietin.VietinBankTransferDto;
import com.onefin.ewallet.bank.service.vietin.VietinLinkBankDto;
import com.onefin.ewallet.common.base.constants.OneFinConstants;
import com.onefin.ewallet.common.domain.bank.common.LinkBankTransaction;
import com.onefin.ewallet.common.domain.bank.transfer.BankTransferChildRecords;
import com.onefin.ewallet.common.domain.bank.transfer.BankTransferTransaction;

import com.onefin.ewallet.common.utility.json.JSONHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/bank/settle")
public class SettleTrans {

	private static final Logger LOGGER = LoggerFactory.getLogger(SettleTrans.class);

	@Autowired
	private VietinLinkBankDto vietinLinkBankDto;


	@Autowired
	private LinkBankTransRepo<LinkBankTransaction> linkBankTransRepo;

	@Autowired
	private JSONHelper jsonHelper;

	@Autowired
	private ChildBankTransferRecordsRepo childBankTransferRecordsRepo;

	@Autowired
	private VietinBankTransferDto vietinBankTransferDto;

	@PutMapping("/partner/{partner}/domain/{domain}/transId/{transId}")
	public ResponseEntity<?> updateTrxToSettleStatus(@RequestParam(name = "transStatus") String transStatus, @PathVariable(value = "partner") String partner, @PathVariable(value = "domain") String domain, @PathVariable(value = "transId") String transId) throws Exception {
		switch (partner) {
			case OneFinConstants.PARTNER_VIETINBANK:
				switch (domain) {
					case OneFinConstants.VTB_DISBURSEMENT:
						BankTransferChildRecords childTrans = childBankTransferRecordsRepo.findByTransId(transId);
						BankTransferTransaction parentTrans = vietinBankTransferDto.findByRequestId(childTrans.getBankTransferTransaction().getRequestId());
						parentTrans.setTranStatus(transStatus);
						LOGGER.info("Disbursement - Update trans {} to {}", parentTrans, transStatus);
						vietinBankTransferDto.updateBankTransferTrans(parentTrans);
						return new ResponseEntity<>(HttpStatus.OK);
					case OneFinConstants.LINK_BANK:
						LinkBankTransaction trans = linkBankTransRepo.findByRequestId(transId);
						LOGGER.info("Link Bank - Update trans {} to {}", trans, transStatus);
						trans.setTranStatus(transStatus);
						vietinLinkBankDto.update(trans);
						return new ResponseEntity<>(HttpStatus.OK);
				}
		}
		return new ResponseEntity<>(HttpStatus.OK);
	}

}
