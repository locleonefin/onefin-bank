package com.onefin.ewallet.bank.controller;

import com.onefin.ewallet.bank.dto.bankCollection.GetVirtualAcctRequest;
import com.onefin.ewallet.bank.dto.bankCollection.GetVirtualAcctResponse;
import com.onefin.ewallet.bank.dto.bankCollection.UpdateVirtualAcctRequest;
import com.onefin.ewallet.bank.dto.bankCollection.UpdateVirtualAcctResponse;
import com.onefin.ewallet.bank.dto.bvb.*;
import com.onefin.ewallet.bank.dto.vietin.VirtualAcctGetRequest;
import com.onefin.ewallet.bank.repository.jpa.VietinNotifyTransTableRepo;
import com.onefin.ewallet.bank.repository.jpa.VirtualAcctRepo;
import com.onefin.ewallet.bank.repository.jpa.VirtualAcctTransHistoryRepo;
import com.onefin.ewallet.bank.service.bvb.BVBEncryptUtil;
import com.onefin.ewallet.bank.service.bvb.BVBRequestUtil;
import com.onefin.ewallet.bank.service.bvb.BVBVirtualAcct;
import com.onefin.ewallet.bank.service.common.ConfigLoader;
import com.onefin.ewallet.common.base.constants.BankConstants;
import com.onefin.ewallet.common.base.constants.OneFinConstants;
import com.onefin.ewallet.common.base.errorhandler.RuntimeBadRequestException;
import com.onefin.ewallet.common.base.errorhandler.RuntimeInternalServerException;
import com.onefin.ewallet.common.domain.bank.vietin.VietinVirtualAcctTable;
import com.onefin.ewallet.common.domain.bank.vietin.VietinVirtualAcctTransHistory;
import com.onefin.ewallet.common.utility.date.DateTimeHelper;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/bank/collection")
public class BankCollectionController {
	private static final org.apache.logging.log4j.Logger LOGGER = LogManager.getLogger(BankCollectionController.class);

	@Autowired
	private ConfigLoader configLoader;

	@Autowired
	private DateTimeHelper dateTimeHelper;

	@Autowired
	private BVBRequestUtil bvbRequestUtil;

	@Autowired
	private BVBEncryptUtil bvbEncryptUtil;

	@Autowired
	private VirtualAcctTransHistoryRepo virtualAcctTransHistoryRepo;

	@Autowired
	private ModelMapper modelMapper;

	@Autowired
	private Environment env;

	@Autowired
	private VietinNotifyTransTableRepo vietinNotifyTransTableRepo;

	@Autowired
	private BVBVirtualAcct bvbVirtualAcct;

	@Autowired
	private VirtualAcctRepo virtualAcctRepo;

	@Autowired
	private VietinVirtualAcctController vietinVirtualAcctController;


	@PostMapping("/VA/update")
	public ResponseEntity<?> bankTransfer(
			@RequestHeader(value = "walletId", required = false) String walletId,
			@Valid @RequestBody UpdateVirtualAcctRequest requestBody) throws Exception {
		Date currentDate = dateTimeHelper.currentDate(OneFinConstants.HO_CHI_MINH_TIME_ZONE);
		if (requestBody.getPartnerCode().equals(OneFinConstants.BankListQrService.VCCB.getPartnerCode())) {

			UpdateVirtualAcctResponse updateVirtualAcctResponse;
			// Check current status
			BVBVirtualAcctInfoDetailResponse updateResponse = Optional.ofNullable(bvbVirtualAcct.checkVirtualAccountDetailCollection(requestBody.getVirtualAcctId())
			).orElseThrow(
					() -> {
						String errorString = String.format("Error when checking VA with acc id %s", requestBody.getVirtualAcctId());
						LOGGER.error(errorString);
						return new RuntimeInternalServerException(errorString);
					}
			);

			if (requestBody.getStatus().equals(BankConstants.CollectionVirtualAcctStatus.OPEN.getValue())) {

				// check if virtual acc is open
				if (updateResponse.getData().getAccStat().equals(BankConstants.BVBVirtualAcctStatus.CLOSE.getValue())) {
					// if virtual acct is close, reopen and update
					BVBVirtualAcctReopenResponse bvbVirtualAcctReopenResponse = Optional.ofNullable(bvbVirtualAcct.reopenVirtualAcctCollectionAndUpdate(
							requestBody.getVirtualAcctId(),
							requestBody.getAmount(),
							requestBody.getMerchantCode(),
							requestBody.getVirtualAcctName())
					).orElseThrow(() -> {
						LOGGER.error("Virtual Acct {} closed but reopen Request Failed!!!", requestBody.getVirtualAcctId());
						return new RuntimeInternalServerException(String.format("Virtual Acct %s closed but reopen Request Failed !!!!!", requestBody.getVirtualAcctId()));
					});
					updateVirtualAcctResponse = modelMapper.map(bvbVirtualAcctReopenResponse.getData(), UpdateVirtualAcctResponse.class);
					updateVirtualAcctResponse.setUpdateDate(currentDate);
					return new ResponseEntity<>(updateVirtualAcctResponse, HttpStatus.OK);
				} else {
					// if virtual acct already open, only update
					BVBVirtualAcctUpdateResponse bvbVirtualAcctUpdateResponse = Optional.ofNullable(bvbVirtualAcct.updateVirtualAcctCollectionAndUpdate(
							requestBody.getVirtualAcctId(),
							requestBody.getAmount(),
							requestBody.getMerchantCode(),
							requestBody.getVirtualAcctName())
					).orElseThrow(() -> {
						LOGGER.error("Virtual Acct {} update info failed!!!", requestBody.getVirtualAcctId());
						return new RuntimeInternalServerException(String.format("Virtual Acct %s update info failed!!!", requestBody.getVirtualAcctId()));
					});
					updateVirtualAcctResponse = modelMapper.map(bvbVirtualAcctUpdateResponse.getData(), UpdateVirtualAcctResponse.class);
					updateVirtualAcctResponse.setUpdateDate(currentDate);
					return new ResponseEntity<>(updateVirtualAcctResponse, HttpStatus.OK);
				}
			} else if (requestBody.getStatus().equals(BankConstants.CollectionVirtualAcctStatus.CLOSE.getValue())) {
				// check if virtual acc is open
				if (updateResponse.getData().getAccStat().equals(BankConstants.BVBVirtualAcctStatus.OPEN.getValue())) {
					// if virtual acct already open, only update
					BVBVirtualAcctCloseResponse bvbVirtualAcctUpdateResponse = Optional.ofNullable(bvbVirtualAcct.closeVirtualAcctCollectionAndUpdate(
							requestBody.getVirtualAcctId())
					).orElseThrow(() -> {
						LOGGER.error("Virtual Acct {} update info failed!!!", requestBody.getVirtualAcctId());
						return new RuntimeInternalServerException(String.format("Virtual Acct %s update info failed!!!", requestBody.getVirtualAcctId()));
					});
					updateVirtualAcctResponse = modelMapper.map(bvbVirtualAcctUpdateResponse.getData(), UpdateVirtualAcctResponse.class);
					updateVirtualAcctResponse.setUpdateDate(currentDate);
					return new ResponseEntity<>(updateVirtualAcctResponse, HttpStatus.OK);
				}
			} else {
				LOGGER.error("Unknown status {} !", requestBody.getStatus());
				throw new RuntimeInternalServerException(String.format("Unknown status %s !", requestBody.getStatus()));
			}

			updateVirtualAcctResponse = modelMapper.map(updateResponse.getData(), UpdateVirtualAcctResponse.class);
			updateVirtualAcctResponse.setUpdateDate(currentDate);
			return new ResponseEntity<>(updateVirtualAcctResponse, HttpStatus.OK);

		} else {
			throw new RuntimeBadRequestException(String.format("Partner code %s is not supported", requestBody.getPartnerCode()));
		}
	}


	@PostMapping("/VA/get")
	public ResponseEntity<?> utilityGetVirtualAcct(
			@RequestHeader(value = "walletId", required = false) String walletId,
			@Valid @RequestBody() GetVirtualAcctRequest requestBody) throws Exception {

		Date currentDate = dateTimeHelper.currentDate(OneFinConstants.HO_CHI_MINH_TIME_ZONE);

		virtualAcctRepo.findFirstByVirtualAcctIdAndInUse(
						requestBody.getVirtualAcctId(),
						true,
						requestBody.getBankCode())
				.map(e -> {
					virtualAcctRepo.updateVietinVirtualAcctIdInUse(
							e.getVirtualAcctId(),
							false, currentDate, currentDate, requestBody.getBankCode()
					);
					return e;
				});

		// Check history
		List<VietinVirtualAcctTransHistory> historyDataPending = virtualAcctTransHistoryRepo
				.findByVirtualAcctIdAndTranStatusAndAmount(
						requestBody.getVirtualAcctId(),
						OneFinConstants.TRANS_PENDING,
						requestBody.getAmount(),
						requestBody.getBankCode());
		if (historyDataPending.size() > 1) {
			LOGGER.warn("more than one history trans with virtual acc id {} will be update, admin should check again", requestBody.getVirtualAcctId());
		}
		historyDataPending.forEach(
				e -> {
					LOGGER.log(Level.getLevel("INFOWT"), "history trans info: {}", e);
				}
		);
		// Update history
		virtualAcctTransHistoryRepo.updateVietinVirtualAcctByStatus(
				OneFinConstants.TRANS_ERROR,
				currentDate,
				null,
				requestBody.getVirtualAcctId(),
				OneFinConstants.TRANS_PENDING,
				requestBody.getBankCode(),
				requestBody.getAmount()
		);


		VirtualAcctGetRequest getVirtualAcctResponse =
				modelMapper.map(requestBody, VirtualAcctGetRequest.class);

		return vietinVirtualAcctController.getVirtualAcctInPool(getVirtualAcctResponse);
	}

}
