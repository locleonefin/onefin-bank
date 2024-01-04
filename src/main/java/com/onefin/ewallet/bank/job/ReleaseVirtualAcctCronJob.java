package com.onefin.ewallet.bank.job;

import com.onefin.ewallet.bank.service.common.ReleaseVirtualAcctFromBatchJobService;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.QuartzJobBean;

@DisallowConcurrentExecution
public class ReleaseVirtualAcctCronJob extends QuartzJobBean {

	private static final Logger LOGGER = LoggerFactory.getLogger(ReleaseVirtualAcctCronJob.class);

	@Autowired
	private ReleaseVirtualAcctFromBatchJobService releaseService;

	/**
	 * Run every 15 minutes
	 *
	 * @param context
	 */
	@Override
	protected void executeInternal(JobExecutionContext context) {
		releaseService.releaseVirtualAcctFromBatchJob();
	}
}
