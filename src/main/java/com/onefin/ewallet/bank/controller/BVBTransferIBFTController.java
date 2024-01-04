package com.onefin.ewallet.bank.controller;

import com.onefin.ewallet.common.base.constants.BankConstants;
import com.onefin.ewallet.bank.dto.bvb.bankTransfer.*;
import com.onefin.ewallet.bank.dto.bvb.napas.NapasInfoDTO;
import com.onefin.ewallet.bank.repository.jpa.BankListRepository;
import com.onefin.ewallet.bank.repository.jpa.BankTransferRepo;
import com.onefin.ewallet.bank.service.bvb.BVBEncryptUtil;
import com.onefin.ewallet.bank.service.bvb.BVBIBFTPersistance;
import com.onefin.ewallet.bank.service.bvb.BVBTransferRequestUtil;
import com.onefin.ewallet.bank.service.common.ConfigLoader;
import com.onefin.ewallet.common.base.constants.OneFinConstants;
import com.onefin.ewallet.common.base.service.RestTemplateHelper;
import com.onefin.ewallet.common.domain.bank.common.BankList;
import com.onefin.ewallet.common.domain.bank.transfer.BankTransferTransaction;
import com.onefin.ewallet.common.utility.date.DateTimeHelper;
import com.onefin.ewallet.common.utility.file.ExcelHelper;
import com.onefin.ewallet.common.utility.json.JSONHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.*;
import java.util.*;
import java.util.stream.Collectors;


@Controller
@RequestMapping("/bvb/ibft")
public class BVBTransferIBFTController {
	//	private static final Logger LOGGER = LoggerFactory.getLogger(BVBTransferIBFTController.class);
	private static final org.apache.logging.log4j.Logger LOGGER = LogManager.getLogger(BVBTransferIBFTController.class);

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
	private JSONHelper jsonHelper;

	@Autowired
	protected RestTemplateHelper restTemplateHelper;

	@Autowired
	private ExcelHelper excelHelper;

	@Autowired
	private BankListRepository bankListRepository;

	@Autowired
	private BankTransferRepo bankTransferRepo;

	@Value("${bvb.IBFT.onefinPrivateKey}")
	private String privateKeyPath;

	@Value("${bvb.IBFT.onefinClientCode}")
	private String clientCode;

	@Value("${bvb.IBFT.onefinMerchantCode}")
	private String requestIdPrefix;

	@Autowired
	private BVBIBFTPersistance bvbibftPersistance;

	@PostMapping("/queryStatus")
	public ResponseEntity<?> queryStatus(
			@Valid @RequestBody(required = true)
			BVBIBFTQueryStatusRequest requestBody
	) throws Exception {
		return bvbTransferRequestUtil.ibftRequest(requestBody,
				configLoader.getBvbIBFTQueryStatusUrl(),
				requestBody.getRequestId(),
				BankConstants.BVB_IBFT_BACKUP_QUERY_STATUS_PREFIX,
				BVBIBFTQueryStatusRequest.class,
				BVBIBFTQueryStatusResponse.class
		);
	}

	@PostMapping("/inquiry")
	public ResponseEntity<?> inquiry(
			@Valid @RequestBody(required = true)
			BVBIBFTInquiryRequest requestBody
	) throws Exception {

		return bvbTransferRequestUtil.ibftRequest(requestBody,
				configLoader.getBvbIBFTInquiryUrl(),
				requestBody.getRequestId(),
				BankConstants.BVB_IBFT_BACKUP_QUERY_STATUS_PREFIX,
				BVBIBFTInquiryRequest.class,
				BVBIBFTInquiryResponse.class
		);
	}

	@PostMapping("/fundTransfer")
	public ResponseEntity<?> fundTransfer(
			@Valid @RequestBody(required = true)
			BVBIBFTFundTransferRequest requestBody
	) throws Exception {
		LOGGER.info("requestBody: {}", (BVBIBFTCommonRequest<BVBIBFTFundTransferRequestData>) requestBody);
		return bvbTransferRequestUtil.ibftRequest(requestBody,
				configLoader.getBvbIBFTFundTransferUrl(),
				requestBody.getRequestId(),
				BankConstants.BVB_IBFT_BACKUP_QUERY_STATUS_PREFIX,
				BVBIBFTFundTransferRequest.class,
				BVBIBFTFundTransferResponse.class
		);
	}

	@PostMapping("/fundTransfer-test")
	public ResponseEntity<?> fundTransferTest(
			@Valid @RequestBody(required = true)
			BVBIBFTFundTransferRequest requestBody
	) throws Exception {

//		List<BankList> bankList = bankListRepository.findByVccbBankId(requestBody.getData().getBankCode());
		BankTransferTransaction bankTransferTransaction = bvbibftPersistance.initBankTransferRecord(requestBody);

		LOGGER.info("bankTransferTransaction: {}", bankTransferTransaction);
		ResponseEntity<?> responseEntity = bvbTransferRequestUtil.ibftRequest(requestBody,
				configLoader.getBvbIBFTFundTransferUrl(),
				requestBody.getRequestId(),
				BankConstants.BVB_IBFT_BACKUP_QUERY_STATUS_PREFIX,
				BVBIBFTFundTransferRequest.class,
				BVBIBFTFundTransferResponse.class
		);

		try {
			BVBIBFTFundTransferResponse responseBody
					= modelMapper.map(
					responseEntity.getBody(),
					BVBIBFTFundTransferResponse.class
			);

			bankTransferTransaction = bvbibftPersistance.updateBankTransferRecord(requestBody, responseBody);
			LOGGER.info("bankTransferTransaction updated: {}", bankTransferTransaction);

		} catch (Exception e) {
			LOGGER.error("Failed while updating transaction status", e);
		}
		return new ResponseEntity<>(responseEntity.getBody(), HttpStatus.OK);
	}

	@PostMapping("/inquiryEscrowAccount")
	public ResponseEntity<?> inquiryEscrowAccount(
			@Valid @RequestBody(required = true)
			BVBIBFTInquiryEscrowAccountRequest requestBody
	) throws Exception {

		return bvbTransferRequestUtil.ibftRequest(requestBody,
				configLoader.getBvbIBFTInquiryEscrowAccountUrl(),
				requestBody.getRequestId(),
				BankConstants.BVB_IBFT_BACKUP_QUERY_STATUS_PREFIX,
				BVBIBFTInquiryEscrowAccountRequest.class,
				BVBIBFTInquiryEscrowAccountResponse.class
		);
	}

	@PostMapping("/encrypt")
	public ResponseEntity<?> encrypt(
			@Valid @RequestBody(required = true)
			BVBIBFTCommonRequest<?> requestBody
	) throws Exception {

		Map<String, Object> map = new HashMap<>();
		String stringHex = bvbTransferRequestUtil.signatureConstruction(requestBody);
		map.put("signature", stringHex);
		map.put("data", jsonHelper.convertMap2JsonString(requestBody.getData()));
		map.put("object", requestBody);
		return new ResponseEntity<>(map, HttpStatus.OK);
	}

	@PostMapping("/update/bank-list")
	public ResponseEntity<?> bvbReconciliation(
			@RequestParam("file") MultipartFile file,
			RedirectAttributes redirectAttributes) throws Exception {

		if (file.isEmpty()) {
			redirectAttributes.addFlashAttribute("message", "Please select a file to upload");
		}

		Workbook workbook = new XSSFWorkbook(file.getInputStream());
		Sheet sheet = workbook.getSheetAt(0);
		List<CellRangeAddress> listMergeCell = sheet.getMergedRegions();
		LOGGER.info("sheet merge region: {}", sheet.getMergedRegions());
		LOGGER.info("sheet merge region: {}", sheet.getNumMergedRegions());

		List<NapasInfoDTO> napasInfoDTOList = NapasInfoDTO.getInstanceFromSheet(workbook, sheet);
		LOGGER.info("number of record: {}", napasInfoDTOList.size());
		for (int i = 0; i < napasInfoDTOList.size(); i++) {
			LOGGER.info("Record {}: {}", i, napasInfoDTOList.get(i));
		}
		Date currentDate = dateTimeHelper.currentDate(OneFinConstants.HO_CHI_MINH_TIME_ZONE);
		Set<String> setCitad = new HashSet<>(napasInfoDTOList.stream().map(NapasInfoDTO::getCitadCode).collect(Collectors.toList()));
		for (String citad : setCitad) {
			List<NapasInfoDTO> listRecord = napasInfoDTOList.stream().filter(
					e -> {
						return e.getCitadCode().equals(citad);
					}
			).collect(Collectors.toList());
			List<String> listBenId = new HashSet<>(listRecord.stream().map(
					NapasInfoDTO::getBenId
			).collect(Collectors.toList())).stream().filter(s -> !s.isEmpty()).collect(Collectors.toList());
			List<String> listBinId = new HashSet<>(listRecord.stream().map(
					NapasInfoDTO::getRecvBin
			).collect(Collectors.toList())).stream().filter(s -> !s.isEmpty()).collect(Collectors.toList());
			BankList senderBank = bankListRepository.findByCode(citad.trim());
			if (senderBank == null) {
				BankList bankList = new BankList();
				bankList.setCode(citad);
				bankList.setName(listRecord.get(0).getBank());
				bankList.setSsCode(listRecord.get(0).getShortName());
				bankList.setVccbBankId(listRecord.get(0).getBankId());
				bankList.setVccbBenId(listBenId);
				bankList.setVccbBinId(listBinId);
				bankList.setIsAccountRecv(listRecord.get(0).getRecvModelAcct());
				bankList.setIsCardRecv(listRecord.get(0).getRecvModelCard());
				bankList.setIsAccountTransfer(listRecord.get(0).getTransModelAcct());
				bankList.setIsCardTransfer(listRecord.get(0).getTransModelCard());
				bankList.setIsCustomCitad(listRecord.get(0).getIsCustomCitad());
				bankList.setModifiedBy("System");
				bankList.setUpdatedDate(currentDate);
				bankListRepository.save(bankList);
			} else {
				senderBank.setVccbBankId(listRecord.get(0).getBankId());
				senderBank.setVccbBenId(listBenId);
				senderBank.setVccbBinId(listBinId);
				senderBank.setIsAccountRecv(listRecord.get(0).getRecvModelAcct());
				senderBank.setIsCardRecv(listRecord.get(0).getRecvModelCard());
				senderBank.setIsAccountTransfer(listRecord.get(0).getTransModelAcct());
				senderBank.setIsCardTransfer(listRecord.get(0).getTransModelCard());
				senderBank.setIsCustomCitad(listRecord.get(0).getIsCustomCitad());
				senderBank.setUpdatedDate(currentDate);
				bankListRepository.save(senderBank);
			}
		}

		// Return the file content as ResponseEntity with appropriate headers and status
		return new ResponseEntity<>(HttpStatus.OK);
	}
}
