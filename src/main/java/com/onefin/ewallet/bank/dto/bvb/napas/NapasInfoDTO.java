package com.onefin.ewallet.bank.dto.bvb.napas;

import com.onefin.ewallet.common.base.constants.BankConstants;
import com.onefin.ewallet.common.utility.file.ExcelHelper;
import lombok.Data;
import org.apache.poi.ss.usermodel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

@Data
public class NapasInfoDTO {

	private static final Logger LOGGER = LoggerFactory.getLogger(NapasInfoDTO.class);

	private Integer stt;

	private String bank;

	private String shortName;

	private String bankId;

	private String benId;

	private Boolean transModelAcct;

	private Boolean transModelCard;

	private Boolean recvModelAcct;

	private Boolean recvModelCard;

	private String recvBin;

	private String cardName;

	private Integer cardLength;

	private String citadCode;

	private Boolean isCustomCitad;

	public NapasInfoDTO() {
	}

	public static NapasInfoDTO getInstanceFromRow(FormulaEvaluator workbook, Sheet sheet, Row row) {
		try {

			NapasInfoDTO napasInfoDTO = new NapasInfoDTO();
			Cell cell = row.getCell(BankConstants.BVBExcelReadingForNapasBinEnum.STT.getColumn());
			napasInfoDTO.setStt(intParse(ExcelHelper.readCell(workbook, sheet, cell)));
			cell = row.getCell(BankConstants.BVBExcelReadingForNapasBinEnum.BANK.getColumn());
			napasInfoDTO.setBank(ExcelHelper.readCell(workbook, sheet, cell));
			cell = row.getCell(BankConstants.BVBExcelReadingForNapasBinEnum.SHORT_NAME.getColumn());
			napasInfoDTO.setShortName(ExcelHelper.readCell(workbook, sheet, cell));
			cell = row.getCell((BankConstants.BVBExcelReadingForNapasBinEnum.BANK_ID.getColumn()));
			napasInfoDTO.setBankId(parseString(intParse2(ExcelHelper.readCell(workbook, sheet, cell))));
			cell = row.getCell(BankConstants.BVBExcelReadingForNapasBinEnum.BEN_ID.getColumn());
			napasInfoDTO.setBenId(parseString(intParse2(ExcelHelper.readCell(workbook, sheet, cell))));
			cell = row.getCell(BankConstants.BVBExcelReadingForNapasBinEnum.TRANS_MODEL_CARD.getColumn());
			napasInfoDTO.setTransModelCard(booleanParse(ExcelHelper.readCell(workbook, sheet, cell)));
			cell = row.getCell(BankConstants.BVBExcelReadingForNapasBinEnum.TRANS_MODEL_ACC.getColumn());
			napasInfoDTO.setTransModelAcct(booleanParse(ExcelHelper.readCell(workbook, sheet, cell)));
			cell = row.getCell(BankConstants.BVBExcelReadingForNapasBinEnum.RECV_MODEL_CARD.getColumn());
			napasInfoDTO.setRecvModelCard(booleanParse(ExcelHelper.readCell(workbook, sheet, cell)));
			cell = row.getCell(BankConstants.BVBExcelReadingForNapasBinEnum.RECV_MODEL_ACC.getColumn());
			napasInfoDTO.setRecvModelAcct(booleanParse(ExcelHelper.readCell(workbook, sheet, cell)));
			cell = row.getCell(BankConstants.BVBExcelReadingForNapasBinEnum.BIN.getColumn());
			napasInfoDTO.setRecvBin(parseString(intParse2(ExcelHelper.readCell(workbook, sheet, cell))));
			cell = row.getCell(BankConstants.BVBExcelReadingForNapasBinEnum.CARD_NAME.getColumn());
			napasInfoDTO.setCardName(ExcelHelper.readCell(workbook, sheet, cell));
			cell = row.getCell(BankConstants.BVBExcelReadingForNapasBinEnum.CARD_LENGTH.getColumn());
			napasInfoDTO.setCardLength(intParse2(ExcelHelper.readCell(workbook, sheet, cell)));
			cell = row.getCell(BankConstants.BVBExcelReadingForNapasBinEnum.CITAD_COD.getColumn());
			napasInfoDTO.setCitadCode(ExcelHelper.readCell(workbook, sheet, cell));
			cell = row.getCell(BankConstants.BVBExcelReadingForNapasBinEnum.IS_CUSTOM.getColumn());
			napasInfoDTO.setIsCustomCitad(booleanParse(ExcelHelper.readCell(workbook, sheet, cell)));
			return napasInfoDTO;

		} catch (Exception e) {
			LOGGER.error("Error Occurred: ", e);
			return null;
		}

	}

	public static List<NapasInfoDTO> getInstanceFromSheet(Workbook workbook, Sheet sheet) {
		try {
			FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();
			List<NapasInfoDTO> napasInfoDTOS = new ArrayList<>();
			for (Row row : sheet) {
				NapasInfoDTO napasInfoDTO = getInstanceFromRow(evaluator, sheet, row);
				if (napasInfoDTO != null) {
					napasInfoDTOS.add(napasInfoDTO);
				}
			}

			return napasInfoDTOS;

		} catch (Exception e) {
			LOGGER.error("Error Occurred: ", e);
			return new ArrayList<>();
		}

	}

	private static Boolean booleanParse(String input) {
		return input != null && !input.isEmpty();
	}

	private static Integer intParse(String input) {
		LOGGER.info("value: {}", input);
		double d = Double.parseDouble(input);
		return (int) d;
	}

	private static Integer intParse2(String input) {
		if (input != null && !input.isEmpty()) {
			LOGGER.info("value: {}", input);
			double d = Double.parseDouble(input);
			return (int) d;
		}
		return null;
	}

	private static <T> String parseString(T e) {
		if (e != null) {
			return String.valueOf(e);
		}
		return "";
	}
}
