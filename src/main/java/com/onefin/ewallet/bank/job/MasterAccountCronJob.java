package com.onefin.ewallet.bank.job;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.Map;

import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.quartz.QuartzJobBean;

import com.onefin.ewallet.bank.controller.VietinLinkBankController;
import com.onefin.ewallet.bank.dto.vietin.Balance;
import com.onefin.ewallet.bank.dto.vietin.ProviderInquiry;
import com.onefin.ewallet.bank.service.common.NumberSequenceService;
import com.onefin.ewallet.common.base.service.RestTemplateHelper;

@DisallowConcurrentExecution
public class MasterAccountCronJob extends QuartzJobBean {

	private static final Logger LOGGER = LoggerFactory.getLogger(MasterAccountCronJob.class);

	private boolean masterAccountWarning = false;

	@Autowired
	private VietinLinkBankController vietinLinkBankController;

	@Autowired
	private Environment env;

	@Autowired
	private NumberSequenceService numberSequenceService;

	@Autowired
	private RestTemplateHelper restTemplateHelper;

	@Override
	protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
		LOGGER.info("Schedule query Vietin master account");
		ProviderInquiry request = new ProviderInquiry();
		try {
			request.setRequestId(numberSequenceService.nextVTBLinkBankTransId());
			request.setChannel("MOBILE");
			Map<String, Object> response = vietinLinkBankController.providerInquiry(request);
			Balance amount = (Balance) response.get("balances");
			BigDecimal masterBalance = new BigDecimal(amount.getAmount());
			BigDecimal floorBalance = new BigDecimal(env.getProperty("vietin.floorBalance"));
			if (masterBalance.compareTo(floorBalance) == -1 && masterAccountWarning == false) {
				LOGGER.error("Warning!!!!!!!!!. VietinBank master account is about to run out of money {}", new DecimalFormat("#,###.00").format(masterBalance));
				masterAccountWarning = true;
			}
			if (masterBalance.compareTo(floorBalance) != -1) {
				masterAccountWarning = false;
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
