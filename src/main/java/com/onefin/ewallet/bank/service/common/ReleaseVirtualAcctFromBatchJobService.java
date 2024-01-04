package com.onefin.ewallet.bank.service.common;

import com.onefin.ewallet.common.base.constants.BankConstants;
import com.onefin.ewallet.bank.common.VietinConstants;
import com.onefin.ewallet.bank.dto.bvb.BVBVirtualAcctInfoDetailResponse;
import com.onefin.ewallet.bank.repository.jpa.VirtualAcctRepo;
import com.onefin.ewallet.bank.repository.jpa.VirtualAcctTransHistoryRepo;
import com.onefin.ewallet.bank.service.bvb.BVBNotifyHandle;
import com.onefin.ewallet.bank.service.bvb.BVBVirtualAcct;
import com.onefin.ewallet.bank.service.vietin.VietinVirtualAcct;
import com.onefin.ewallet.common.base.constants.OneFinConstants;
import com.onefin.ewallet.common.domain.bank.vietin.VietinVirtualAcctTable;
import com.onefin.ewallet.common.domain.bank.vietin.VietinVirtualAcctTransHistory;
import com.onefin.ewallet.common.utility.date.DateTimeHelper;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ReleaseVirtualAcctFromBatchJobService {

	private static final Logger LOGGER = LoggerFactory.getLogger(ReleaseVirtualAcctFromBatchJobService.class);

	@Autowired
	private VirtualAcctRepo virtualAcctRepo;

	@Autowired
	private DateTimeHelper dateTimeHelper;

	@Autowired
	private VirtualAcctTransHistoryRepo virtualAcctTransHistoryRepo;

	@Autowired
	private ConfigLoader configLoader;

	@Autowired
	private BVBVirtualAcct bvbVirtualAcct;

	@Lazy
	@Autowired
	private VietinVirtualAcct vietinVirtualAcct;

	@Autowired
	private BVBNotifyHandle bvbNotifyHandle;


	public void releaseVirtualAcctFromBatchJob() {
		List<VietinVirtualAcctTable> items = virtualAcctRepo
				.findAllByInUseAndBufferExpired(true, dateTimeHelper.currentDate(OneFinConstants.HO_CHI_MINH_TIME_ZONE), configLoader.getVirtualAccPoolExpireBuffer());
		LOGGER.info("Number of virtual account need to release: {}", items.size());
		items.forEach(e -> {
			DateTime currDate = dateTimeHelper.currentDateTime(OneFinConstants.HO_CHI_MINH_TIME_ZONE);
			// Virtual acct borrowed, but after expired time there is no notify from bank => must release
			List<VietinVirtualAcctTransHistory> history = virtualAcctTransHistoryRepo
					.findByVirtualAcctVarIdAndTranStatus(
							e.getVirtualAcctId(),
							OneFinConstants.TRANS_PENDING,
							e.getBankCode());
			if (history.size() == 1) {
				LOGGER.info("Release account details: {}", e);
				try {
					// Disable virtual account
					if (e.getBankCode().equals(OneFinConstants.BankListQrService.CTG.getBankCode())) {
						try {
							vietinVirtualAcct.updateVirtualAccount(e.getVirtualAcctVar(),
									VietinConstants.VirtualAcctOnOff.OFF.getValue());
						} catch (Exception ex) {
							LOGGER.error("Process update virtual account fail", ex);
						}
					} else if (e.getBankCode().equals(OneFinConstants.BankListQrService.VCCB.getBankCode())) {
						try {
							BVBVirtualAcctInfoDetailResponse updateResponse
									= bvbVirtualAcct.checkVirtualAccountDetailAndUpdate(
									e.getVirtualAcctId());
							if (updateResponse.getData().getAccStat()
									.equals(BankConstants.BVBVirtualAcctStatus.OPEN.getValue())) {
								bvbVirtualAcct.closeVirtualAcct(e.getVirtualAcctId());
							}
						} catch (Exception ex) {
							LOGGER.error("Process update virtual account fail", ex);
						}
					}

					// TODO
					// Check if this virtual account hold any transaction from VietinBank. If hold update to SUCCESS and send Callback

					// Updated trans to Time out
					virtualAcctTransHistoryRepo.updateVietinVirtualAcctByStatus(
							OneFinConstants.TRANS_TIMEOUT, currDate.toDate(),
							null, e.getVirtualAcctId(),
							OneFinConstants.TRANS_PENDING,
							e.getBankCode(),
							history.get(0).getTransUniqueKey()
					);

					// Release virtual acct
					virtualAcctRepo.updateVirtualAcctInUse(e.getVirtualAcctId(),
							false, currDate.toDate(), currDate.toDate(),
							e.getBankCode());

					// CALL BACK TO UI
					if (e.getBankCode().equals(OneFinConstants.BankListQrService.CTG.getBankCode())) {
						vietinVirtualAcct.callbackWithPoolVirtualAcct(
								history.get(0).getBackendUrl(),
								history.get(0).getId().toString() + "_" + OneFinConstants.BankListQrService.CTG.getBankCode(),
								OneFinConstants.LazyListVirtualAccountTransStatus.TRANS_TIMEOUT_STATUS.getName(),
								history.get(0).getId().toString(),
								""
						);
					} else if (e.getBankCode().equals(OneFinConstants.BankListQrService.VCCB.getBankCode())) {
						bvbNotifyHandle.callbackWithPoolVirtualAcct(
								history.get(0).getBackendUrl(),
								history.get(0).getId().toString() + "_" + OneFinConstants.BankListQrService.VCCB.getBankCode(),
								OneFinConstants.LazyListVirtualAccountTransStatus.TRANS_TIMEOUT_STATUS.getName(),
								history.get(0).getId().toString(),
								""
						);
					}
				} catch (Exception ex) {
					LOGGER.error("Error release virtual account: ", ex);
				}
			} else {
				LOGGER.error("Something went wrong with release virtual account, Please check: {}, trans history {}", e, history.size());
			}
		});
	}

}
