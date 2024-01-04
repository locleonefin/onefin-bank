package com.onefin.ewallet.bank.service.common;

import com.onefin.ewallet.bank.common.VcbConstants;
import com.onefin.ewallet.bank.config.BankHelper;
import com.onefin.ewallet.bank.repository.jpa.BaseErrorCodeRepo;
import com.onefin.ewallet.bank.repository.jpa.LinkBankTransRepo;
import com.onefin.ewallet.bank.repository.jpa.PartnerErrorCodeRepo;
import com.onefin.ewallet.common.base.model.RestProxy;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Data
@Configuration
@Service
public class ConfigLoader implements InitializingBean {

	private static final Logger LOGGER = LoggerFactory.getLogger(ConfigLoader.class);

	@Autowired
	private NumberSequenceService numberSequenceService;

	@Autowired
	private Environment env;

	@Autowired
	private PartnerErrorCodeRepo partnerErrorCodeRepo;

	@Autowired
	private BaseErrorCodeRepo baseErrorCodeRepo;

	@Autowired
	private BankHelper bankHelper;

	@Autowired
	private LinkBankTransRepo linkBankTransRepo;


	/*********** Vietin config ******************/

	@Value("${vietin.linkbank.merchantIdAccount}")
	private String vietinMerchantIdAccount;

	@Value("${vietin.linkbank.merchantIdCard}")
	private String vietinMerchantIdCard;

	@Value("${vietin.linkbank.providerId}")
	private String vietinProviderId;

	@Value("${vietin.linkbank.version}")
	private String vietinVersion;

	@Value("${vietin.linkbank.url.tokenIssue}")
	private String tokenIssue;

	@Value("${vietin.linkbank.url.verifyPin}")
	private String verifyPin;

	@Value("${vietin.linkbank.url.registerOnlinePay}")
	private String registerOnlinePay;

	@Value("${vietin.linkbank.url.tokenRevoke}")
	private String tokenRevoke;

	@Value("${vietin.linkbank.url.tokenReissue}")
	private String tokenReissue;

	@Value("${vietin.linkbank.url.paymentByToken}")
	private String paymentByToken;

	@Value("${vietin.linkbank.url.paymentByOTP}")
	private String paymentByOTP;

	@Value("${vietin.linkbank.url.widthdraw}")
	private String widthdraw;

	@Value("${vietin.linkbank.url.transactionInquiry}")
	private String transactionInquiry;

	@Value("${vietin.linkbank.url.providerInquiry}")
	private String providerInquiry;

	@Value("${vietin.linkbank.url.tokenIssuePayment}")
	private String tokenIssuePayment;

	@Value("${vietin.linkbank.url.refund}")
	private String refund;

	@Value("${vietin.linkbank.onefinPrivateKey}")
	private String onefinPrivateKey;

	@Value("${vietin.linkbank.vtbPublicKey}")
	private String vtbPublicKey;

	@Value("${vietin.bankTransfer.onefinPrivateKey}")
	private String onefinBankTransferPrivateKey;

	@Value("${vietin.bankTransfer.vtbPublicKey}")
	private String vtbBankTransferPublicKey;

	@Value("${vietin.linkbank.alwaysTopupOTP}")
	private boolean alwaysTopupOTP;

	@Value("${backup.api.uriVietinLinkBank}")
	private String backupVietinLinkBank;

	@Value("${backup.api.uriVcbLinkBank}")
	private String backupVcbLinkBank;

	@Value("${vietin.bankTransfer.url.paymentUrl}")
	private String vietinBankTransferUrl;

	@Value("${vietin.bankTransfer.url.inquiryUrl}")
	private String vietinBankTransferInquiryUrl;

	@Value("${vietin.bankTransfer.url.accInquiryUrl}")
	private String vietinBankTransferAccInquiryUrl;

	@Value("${vietin.bankTransfer.url.providerInquiryUrl}")
	private String vietinBankTransferProviderInquiryUrl;

	@Value("${vietin.bankTransfer.merchantId}")
	private String vietinBankTransferMerchantId;

	@Value("${vietin.bankTransfer.forceVietinChannel}")
	private boolean vietinBankTransferForceVietinChannel;

	@Value("${vietin.bankTransfer.providerId}")
	private String vietinPaymentBankTransferProviderId;

	@Value("${vietin.bankTransfer.apiVersion}")
	private String vietinPaymentBankTransferApiVersion;

	@Value("${vietin.bankTransfer.channel}")
	private String vietinPaymentBankTransferChannel;

	@Value("${vietin.bankTransfer.clientIP}")
	private String vietinPaymentBankTransferClientIP;

	@Value("${vietin.bankTransfer.onefinAcctId}")
	private List<String> vietinPaymentBankTransferOnefinAcctId;

	@Value("${vietin.bankTransfer.onefinBankId}")
	private List<String> vietinPaymentBankTransferOnefinBankId;

	@Value("${vietin.virtualAcct.url.createVirtualAcct}")
	private String vietinVirtualAcctCreateVirtualAcctUrl;

	@Value("${vietin.virtualAcct.url.updateVirtualAcct}")
	private String vietinVirtualAcctUpdateVirtualAcctUrl;

	@Value("${vietin.virtualAcct.merchantId}")
	private String vietinVirtualAcctMerchantId;

	@Value("${vietin.virtualAcct.providerId}")
	private String vietinVirtualAcctProviderId;

	@Value("${vietin.virtualAcct.apiVersion}")
	private String vietinVirtualAcctApiVersion;

	@Value("${vietin.virtualAcct.channel}")
	private String vietinVirtualAcctChannel;

	@Value("${vietin.virtualAcct.virtualAcctCode}")
	private String vietinVirtualAcctVirtualAcctCode;

	@Value("${vietin.virtualAcct.clientIP}")
	private String vietinVirtualAcctClientIP;

	@Value("${vietin.virtualAcct.ibmClientId}")
	private String vietinVirtualAcctIbmClientId;

	@Value("${vietin.virtualAcct.xIbmClientSecret}")
	private String vietinVirtualAcctXIbmClientSecret;

	@Value("${vietin.virtualAcct.onefinAcctId}")
	private String vietinVirtualAcctOnefinAcctId;

	@Value("${vietin.virtualAcct.notifyOnefinPrivateKey}")
	private String vtbVirtualAcctNotifyOnefinPrivateKey;

	@Value("${vietin.virtualAcct.virtualAcctOnefinPrivateKey}")
	private String vietinVirtualAcctVirtualAcctOnefinPrivateKey;

	@Value("${vietin.virtualAcct.notifyVtbPublicKey}")
	private String vtbVirtualAcctNotifyVtbPublicKey;

	@Value("${vietin.virtualAcct.billInqVtbPublicKey}")
	private String vtbVirtualAcctBillInqVtbPublicKey;

	@Value("${backup.api.uriVietinVirtualAcct}")
	private String backupApiUriVietinVirtualAcct;

	@Value("${vietin.virtualAcct.pool.expireIn}")
	private int virtualAccPoolExpire;

	@Value("${vietin.virtualAcct.pool.expireBuffer}")
	private int virtualAccPoolExpireBuffer;

	/*********** Vietcom config ******************/

	@Value("${vcb.linkbank.account.secure.passPhrase}")
	private String vcbPassPhrase;

	@Value("${vcb.linkbank.account.secure.saltValue}")
	private String vcbSaltValue;

	@Value("${vcb.linkbank.account.secure.hashAlgorithm}")
	private String vcbHashAlgorithm;

	@Value("${vcb.linkbank.account.secure.passwordIterations}")
	private int vcbPasswordIterations;

	@Value("${vcb.linkbank.account.secure.keySize}")
	private int vcbKeySize;

	@Value("${vcb.linkbank.account.secure.initVector}")
	private String vcbInitVector;

	@Value("${vcb.linkbank.account.ofPrivateKey.modulus}")
	private String ofVcbPublicKeyM;

	@Value("${vcb.linkbank.account.ofPrivateKey.d}")
	private String ofVcbPrivateKeyD;

	@Value("${vcb.linkbank.account.vcbPublicKey.modulus}")
	private String vcbPublicKeyM;

	@Value("${vcb.linkbank.account.vcbPublicKey.exponent}")
	private String vcbKeyExponent;

	@Value("${vcb.linkbank.account.partnerId}")
	private String vcbPartnerId;

	@Value("${vcb.linkbank.account.vcb2OfBaseUrl}")
	private String vcb2OfBaseUrl;

	@Value("${vcb.linkbank.account.url}")
	private String vcbUrl;

	@Value("${vcb.linkbank.account.defaultMasterAccount}")
	private String vcbDefaultMasterAccount;

	@Value("${vcb.linkbank.account.activeAccount}")
	private boolean isVcbActiveAccount;

	@Value("${vcb.linkbank.card.baseUrl}")
	private String vcbCardBaseUrl;

	@Value("${vcb.linkbank.card.uri.lookupUser}")
	private String vcbCardLookupUser;

	@Value("${vcb.linkbank.card.uri.searchUser}")
	private String vcbCardSearchUser;

	@Value("${vcb.linkbank.card.uri.createUser}")
	private String vcbCardCreateUser;

	@Value("${vcb.linkbank.card.uri.createInstruments}")
	private String vcbCardCreateInstruments;

	@Value("${vcb.linkbank.card.uri.authorizeInstruments}")
	private String vcbCardAuthorizeInstruments;

	@Value("${vcb.linkbank.card.uri.deleteInstrument}")
	private String vcbCardDeleteInstrument;

	@Value("${vcb.linkbank.card.uri.deleteUser}")
	private String vcbCardDeleteUser;

	@Value("${vcb.linkbank.card.uri.createPayment}")
	private String vcbCardCreatePayment;

	@Value("${vcb.linkbank.card.uri.authorizePayment}")
	private String vcbCardAuthorizePayment;

	@Value("${vcb.linkbank.card.uri.lookupPayment}")
	private String vcbCardLookupPayment;

	@Value("${vcb.linkbank.card.uri.searchPayment}")
	private String vcbCardSearchPayment;

	@Value("${vcb.linkbank.card.uri.withdraw}")
	private String vcbCardWithdraw;

	@Value("${vcb.linkbank.card.accessKeyId}")
	private String vcbCardAccessKeyId;

	@Value("${vcb.linkbank.card.secretAccessKey}")
	private String vcbCardSecretAccessKey;

	@Value("${vcb.linkbank.card.mpin}")
	private String vcbCardMpin;

	@Value("${vcb.linkbank.card.userGroupId}")
	private String vcbCardUserGroupId;

	@Value("${vcb.linkbank.card.email}")
	private String vcbCardEmail;

	@Value("${vcb.linkbank.card.terminalId}")
	private String vcbCardTerminalId;

	@Value("${vcb.linkbank.card.merchantId}")
	private String vcbCardMerchantId;

	@Value("${vcb.linkbank.card.requestTimeout}")
	private String vcbCardRequestTimeout;

	@Value("${vcb.linkbank.card.region}")
	private String vcbCardRegion;

	@Value("${vcb.linkbank.card.service}")
	private String vcbCardService;

	/*********** BVB config ******************/


	@Value("${bvb.virtualAcct.url.testVirtualAcct}")
	private String bvbVirtualAcctTestUnit;

	@Value("${bvb.virtualAcct.partnerCode}")
	private String bvbVirtualAcctPartnerCode;

	@Value("${bvb.virtualAcct.defaultAccType}")
	private String bvbVirtualAcctDefaultAccType;

	@Value("${bvb.virtualAcct.url.validateCallback}")
	private String bvbValidateCallBackUrl;

	@Value("${bvb.virtualAcct.url.createVirtualAcct}")
	private String bvbVirtualAcctCreateVirtualAcctUrl;

	@Value("${bvb.virtualAcct.url.updateVirtualInfoAcct}")
	private String bvbVirtualAcctUpdateVirtualAcctUrl;

	@Value("${bvb.virtualAcct.url.closeVirtualAcct}")
	private String bvbVirtualAcctCloseVirtualAcctUrl;

	@Value("${bvb.virtualAcct.url.checkDetailVirtualAcct}")
	private String bvbVirtualAcctCheckDetailVirtualAcctUrl;

	@Value("${bvb.virtualAcct.url.reopenVirtualAcct}")
	private String bvbVirtualAcctReopenVirtualAcctUrl;

	@Value("${bvb.virtualAcct.url.findVirtualAcctList}")
	private String bvbFindVirtualAcctListUrl;

	@Value("${bvb.virtualAcct.url.searchTransByAccount}")
	private String bvbSearchTransByAccountUrl;

	@Value("${backup.api.uriBvbVirtualAcct}")
	private String backupApiUriBvbVirtualAcct;

	// ****************** BVB IBFT Service ***************************** //

	@Value("${bvb.IBFT.url.queryStatus}")
	private String bvbIBFTQueryStatusUrl;

	@Value("${bvb.IBFT.url.inquiry}")
	private String bvbIBFTInquiryUrl;

	@Value("${bvb.IBFT.url.fundTransfer}")
	private String bvbIBFTFundTransferUrl;

	@Value("${bvb.IBFT.url.inquiryEscrowAccount}")
	private String bvbIBFTInquiryEscrowAccountUrl;

	@Value("${bvb.IBFT.url.uploadReconciliation}")
	private String bvbIBFTUploadReconciliationUrl;

	@Value("${bvb.IBFT.onefinBankId}")
	private List<String> bvbBankTransferOnefinBankId;

	@Value("${bvb.IBFT.onefinAcctId}")
	private List<String> bvbBankTransferOnefinAcctId;

	// *********** transit config ******************
	@Value("${transit.url.bankTransfer}")
	private String transitUrlBankTransfer;

	/*********** Proxy config ******************/
	@Value("${proxy.active}")
	private boolean proxActive;

	@Value("${proxy.host}")
	private String proxHost;

	@Value("${proxy.port}")
	private int proxPort;

	@Value("${proxy.activeAuth}")
	private boolean proxActiveAuth;

	@Value("${proxy.userName}")
	private String proxUserName;

	@Value("${proxy.password}")
	private String proxPassword;

	/*********** Proxy config ******************/

	private RestProxy proxyConfig = new RestProxy();

	private List<String> vcbActionMap = new ArrayList<>();


	@Override
	public void afterPropertiesSet() throws Exception {
		setProxy();
		initListVcbActionMap();
		numberSequenceService.createVietcomLinkBankAccountSequenceIfNotExist();
		numberSequenceService.createVietcomLinkBankCardSequenceIfNotExist();
		numberSequenceService.createVietinBankTransferSequenceIfNotExist();
		numberSequenceService.createVietinLinkBankSequenceIfNotExist();
		numberSequenceService.createVietinBankTransferChildSequenceIfNotExist();
		numberSequenceService.createVietcomCardLinkBankTokenCvvSequenceIfNotExist();
		numberSequenceService.createVtbVirtualAcctIfNotExist();
		numberSequenceService.createVtbVirtualAcctSchoolMerchantNumberIfNotExist();
		numberSequenceService.createVtbVirtualAcctCommonMerchantNumberIfNotExist();
		numberSequenceService.createBvbVirtualAcctSchoolMerchantNumberIfNotExist();
		numberSequenceService.createBvbVirtualAcctCommonMerchantNumberIfNotExist();
		numberSequenceService.createBvbIBFTTransferNumberIfNotExist();
		numberSequenceService.createBvbIBFTTransferChildNumberIfNotExist();

	}

	private void setProxy() {
		proxyConfig.setActive(proxActive);
		proxyConfig.setHost(proxHost);
		proxyConfig.setPort(proxPort);
		proxyConfig.setAuth(proxActiveAuth);
		proxyConfig.setUserName(proxUserName);
		proxyConfig.setPassword(proxPassword);
	}

	private void initListVcbActionMap() {
		vcbActionMap.add(VcbConstants.VCBEwalletApiOperation.CHECK_ACTIVE.getVcbAction());
		vcbActionMap.add(VcbConstants.VCBEwalletApiOperation.ACTIVE.getVcbAction());
		vcbActionMap.add(VcbConstants.VCBEwalletApiOperation.DEACTIVE.getVcbAction());
		vcbActionMap.add(VcbConstants.VCBEwalletApiOperation.GET_INFO.getVcbAction());
		vcbActionMap.add(VcbConstants.VCBEwalletApiOperation.TOPUP.getVcbAction());
		vcbActionMap.add(VcbConstants.VCBEwalletApiOperation.CASHIN.getVcbAction());
		vcbActionMap.add(VcbConstants.VCBEwalletApiOperation.CASHIN_OTP.getVcbAction());
		vcbActionMap.add(VcbConstants.VCBEwalletApiOperation.CASHOUT.getVcbAction());
		vcbActionMap.add(VcbConstants.VCBEwalletApiOperation.CHECK_ACTIVE_STATUS.getVcbAction());
		vcbActionMap.add(VcbConstants.VCBEwalletApiOperation.CHECK_TRANS_STATUS.getVcbAction());
		vcbActionMap.add(VcbConstants.VCBEwalletApiOperation.ACTIVE_CUSTOMER.getVcbAction());
		vcbActionMap.add(VcbConstants.VCBEwalletApiOperation.ACTIVE_CUSTOMER_OTP.getVcbAction());
		vcbActionMap.add(VcbConstants.VCBEwalletApiOperation.DEACTIVE_CUSTOMER.getVcbAction());
		vcbActionMap.add(VcbConstants.VCBEwalletApiOperation.GET_PARTNER_ACC_BALANCE.getVcbAction());
		vcbActionMap.add(VcbConstants.VCBEwalletApiOperation.CHECK_CUSTOMER_PROFILE.getVcbAction());
	}

}
