package com.onefin.ewallet.bank.aop;

import com.onefin.ewallet.bank.common.VcbConstants;
import com.onefin.ewallet.bank.common.VietinConstants;
import com.onefin.ewallet.bank.dto.vcb.ConnResponse;
import com.onefin.ewallet.bank.repository.jpa.LinkBankTransRepo;
import com.onefin.ewallet.bank.service.common.ConfigLoader;
import com.onefin.ewallet.bank.service.vcb.LinkBankMessageUtil;
import com.onefin.ewallet.bank.service.vietin.VietinMessageUtil;
import com.onefin.ewallet.common.base.constants.OneFinConstants;
import com.onefin.ewallet.common.base.constants.OneFinConstants.LinkType;
import com.onefin.ewallet.common.base.errorhandler.RuntimeConflictException;
import com.onefin.ewallet.common.base.errorhandler.RuntimeInternalServerException;
import com.onefin.ewallet.common.base.service.BackupService;
import com.onefin.ewallet.common.domain.bank.common.LinkBankTransaction;
import com.onefin.ewallet.common.utility.json.JSONHelper;
import com.onefin.ewallet.common.utility.string.StringHelper;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

@Aspect
@Component
public class ControllerAspect {

	@Autowired
	private VietinMessageUtil vtbImsgUtil;

	@Autowired
	private LinkBankMessageUtil vcbImsgUtil;

	@Autowired
	private ConfigLoader configLoader;

	@Autowired
	private Environment env;

	@Autowired
	private LinkBankTransRepo linkBankTransRepo;

	@Autowired
	private JSONHelper jsonHelper;

	@Autowired
	private StringHelper stringHelper;

	@Autowired
	private BackupService backupService;

	/**************************** VietinBank ******************************/

	@Around(value = "execution(* com.onefin.ewallet.bank.controller.VietinLinkBankController.*(..))")
	public Object checkAvailableServiceVietin(ProceedingJoinPoint joinPoint) throws Throwable {
		Object[] args = joinPoint.getArgs();
		com.onefin.ewallet.bank.dto.vietin.ConnResponse responseEntity = null;
		try {
			if (args[0].equals(LinkType.ACCOUNT) && Boolean.parseBoolean(env.getProperty("vietin.linkbank.activeAccount")) == false) {
				responseEntity = vtbImsgUtil.buildVietinConnectorResponse(VietinConstants.CONN_SERVICE_NOT_AVAILABLE, null, args[0].toString());
				return new ResponseEntity<>(responseEntity, HttpStatus.OK);
			}
			if (args[0].equals(LinkType.CARD) && Boolean.parseBoolean(env.getProperty("vietin.linkbank.activeCard")) == false) {
				responseEntity = vtbImsgUtil.buildVietinConnectorResponse(VietinConstants.CONN_SERVICE_NOT_AVAILABLE, null, args[0].toString());
				return new ResponseEntity<>(responseEntity, HttpStatus.OK);
			}
		} catch (Exception e) {
			throw new RuntimeInternalServerException();
		}
		return joinPoint.proceed();
	}

	/**************************** VietinBank ******************************/

	/**************************** VietcomBank ******************************/

	@Around(value = "execution(* com.onefin.ewallet.bank.controller.VcbLinkBankAccountController.*(..))")
	public Object checkAvailableServiceVietcomAccount(ProceedingJoinPoint joinPoint) throws Throwable {
		Object[] args = joinPoint.getArgs();
		ConnResponse responseEntity = null;
		try {
			if (configLoader.isVcbActiveAccount() == false) {
				responseEntity = vcbImsgUtil.buildEcoreConnectorResponse(VcbConstants.CONN_SERVICE_UNDER_MAINTENANCE, null);
				return new ResponseEntity<>(responseEntity, HttpStatus.OK);
			}
		} catch (Exception e) {
			throw new RuntimeInternalServerException();
		}
		return joinPoint.proceed();
	}

	@Around("execution(* com.onefin.ewallet.bank.controller.VcbLinkBankAccountController.*(..)) && args(.., body, request)")
	public Object checkAccountDuplicateSSRequestId(ProceedingJoinPoint joinPoint, Object body, HttpServletRequest request) throws Throwable {
		try {
			Map<String, Object> bodyJsonObject = (Map<String, Object>) jsonHelper.convertObject2Map(body, Map.class);
			if (request.getRequestURI().contains(VcbConstants.REQUEST_CHECK_TRX_STATUS) == false && bodyJsonObject != null && !stringHelper.checkNullEmptyBlank(bodyJsonObject.get("requestId"))) {
				backupService.backup(configLoader.getBackupVcbLinkBank(), String.valueOf(bodyJsonObject.get("requestId")), bodyJsonObject, VcbConstants.BACKUP_REQUEST);
				List<LinkBankTransaction> result = linkBankTransRepo.findByBankAndSsRequestIdAndLinkType(OneFinConstants.PARTNER_VCB, String.valueOf(bodyJsonObject.get("requestId")), LinkType.ACCOUNT.toString());
				if (result.size() == 0) {
					return joinPoint.proceed();
				}
				throw new RuntimeConflictException(String.format("RequestId %s already exists", bodyJsonObject.get("requestId")));
			} else {
				if (bodyJsonObject != null) {
					backupService.backup(configLoader.getBackupVcbLinkBank(), null, bodyJsonObject, VcbConstants.BACKUP_REQUEST);
				}
				return joinPoint.proceed();
			}
		} catch (Exception e) {
			throw e;
		}
	}

	@Around("execution(* com.onefin.ewallet.bank.controller.VcbLinkBankCardController.*(..)) && args(.., body, request)")
	public Object checkCardDuplicateSSRequestId(ProceedingJoinPoint joinPoint, Object body, HttpServletRequest request) throws Throwable {
		try {
			Map<String, Object> bodyJsonObject = (Map<String, Object>) jsonHelper.convertObject2Map(body, Map.class);
			if (bodyJsonObject != null && !stringHelper.checkNullEmptyBlank(bodyJsonObject.get("requestId"))) {
				backupService.backup(configLoader.getBackupVcbLinkBank(), String.valueOf(bodyJsonObject.get("requestId")), bodyJsonObject, VcbConstants.BACKUP_REQUEST);
				List<LinkBankTransaction> result = linkBankTransRepo.findByBankAndSsRequestIdAndLinkType(OneFinConstants.PARTNER_VCB, String.valueOf(bodyJsonObject.get("requestId")), LinkType.CARD.toString());
				if (result.size() == 0) {
					return joinPoint.proceed();
				}
				throw new RuntimeConflictException(String.format("RequestId %s already exists", bodyJsonObject.get("requestId")));
			} else {
				if (bodyJsonObject != null) {
					backupService.backup(configLoader.getBackupVcbLinkBank(), null, bodyJsonObject, VcbConstants.BACKUP_REQUEST);
				}
				return joinPoint.proceed();
			}
		} catch (Exception e) {
			throw e;
		}
	}

	/**************************** VietcomBank ******************************/

}
