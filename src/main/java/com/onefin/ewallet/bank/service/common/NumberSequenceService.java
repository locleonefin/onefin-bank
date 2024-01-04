package com.onefin.ewallet.bank.service.common;

import com.onefin.ewallet.bank.repository.jpa.NumberSequenceRepository;
import com.onefin.ewallet.common.base.constants.OneFinConstants;
import com.onefin.ewallet.common.base.repository.sequenceTrans.INumberSequenceRepository;
import com.onefin.ewallet.common.base.service.BaseNumberSequenceService;
import com.onefin.ewallet.common.domain.base.sequenceTrans.NumberSequenceTrans;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class NumberSequenceService extends BaseNumberSequenceService {

	private static final Logger LOGGER = LoggerFactory.getLogger(NumberSequenceService.class);

	@Autowired
	private NumberSequenceRepository seqRepository;

	@Autowired
	@Qualifier("numberSequenceRepository")
	public void setBaseNumberSequenceRepository(INumberSequenceRepository numberSequenceRepository) {
		this.setINumberSequenceRepository(numberSequenceRepository);
	}

	public String nextVietcomLinkBankAccountTransId() throws Exception {
		return String.format("%020d", this.getNumber(this.prefixVcbLinkBankAccountName, 1));
	}

	public String nextVietcomLinkBankCardTransId() throws Exception {
		return String.format("%032d", this.getNumber(this.prefixVcbLinkBankCardName, 1));
	}

	public String nextVietcomLinkBankCardTokenCvvTransId() throws Exception {
		return String.format("%09d", this.getNumber(this.prefixVcbLinkBankCardTokenCvvName, 1));
	}

	public String nextVTBBankTransferTransId() throws Exception {
		return String.format("%030d", this.getNumber(this.prefixVtbBankTransferName, 1));
	}

	public String nextVTBBankTransferChildTransId() throws Exception {
		return String.format("%025d", this.getNumber(this.prefixVtbBankTransferChildName, 1));
	}

	public String nextVTBLinkBankTransId() throws Exception {
		return String.format("%09d", this.getNumber(this.prefixVtbLinkBankName, 1));
	}

	public String nextVTBVirtualAcct() throws Exception {
		return this.prefixVtbVirtualAcctName + String.format("%09d", this.getNumber(this.prefixVtbVirtualAcctName, 1));
	}

	public String nextVTBSchoolMerchantVirtualAcctNumber() throws Exception {
		return this.prefixVtbPoolVirtualAcctSchoolMerchantNumberName + String.format("%06d", this.getNumber(this.prefixVtbPoolVirtualAcctSchoolMerchantNumberName, 1));
	}

	public String nextVTBCommonMerchantVirtualAcctNumber() throws Exception {
		return this.prefixVtbPoolVirtualAcctCommonMerchantNumberName + String.format("%06d", this.getNumber(this.prefixVtbPoolVirtualAcctCommonMerchantNumberName, 1));
	}

	public String nextBVBSchoolMerchantVirtualAcctNumber() throws Exception {
		return prefixBvbPoolVirtualAcctSchoolMerchantNumber
				+ String.format("%05d",
				this.getNumberV2(prefixBvbPoolVirtualAcctSchoolMerchantNumberName, 1, bvbVirtualBlockSize));
	}

	public String nextBVBCommonMerchantVirtualAcctNumber() throws Exception {
		return prefixBvbPoolVirtualAcctCommonMerchantNumber
				+ String.format("%05d",
				this.getNumberV2(prefixBvbPoolVirtualAcctCommonMerchantNumberName, 1, bvbVirtualBlockSize));
	}

	public String nextBVBBankTransferTransId() throws Exception {
		return String.format("%030d", this.getNumberV2(prefixBvbIBFTTransferNumberName, 1, bvbIBFTBlockSize));
	}

	public String nextBVBBBankTransferChildTransId() throws Exception {
		return String.format("%06d", this.getNumberV2(prefixBvbIBFTTransferChildNumberName, 1, bvbIBFTBlockSize));
	}

	public void createVietcomLinkBankCardSequenceIfNotExist() throws Exception {
		List<NumberSequenceTrans> sequenceList = this.findByName(this.prefixVcbLinkBankCardName);
		if (sequenceList == null || sequenceList.isEmpty()) {
			NumberSequenceTrans sequence = new NumberSequenceTrans();
			sequence.setCurrentNumber(this.vcbLinkBankCardSequenceStart);
			sequence.setName(this.prefixVcbLinkBankCardName);
			sequence.setDfltBlockSize(this.blockSize);
			Date createdDate = dateHelper.currentDate(OneFinConstants.HO_CHI_MINH_TIME_ZONE);
			sequence.setLastModified(createdDate);
			seqRepository.save(sequence);
		}
	}

	public void createVietcomCardLinkBankTokenCvvSequenceIfNotExist() throws Exception {
		List<NumberSequenceTrans> sequenceList = this.findByName(this.prefixVcbLinkBankCardTokenCvvName);
		if (sequenceList == null || sequenceList.isEmpty()) {
			NumberSequenceTrans sequence = new NumberSequenceTrans();
			sequence.setCurrentNumber(this.vcbLinkBankCardTokenCvvSequenceStart);
			sequence.setName(this.prefixVcbLinkBankCardTokenCvvName);
			sequence.setDfltBlockSize(this.blockSize);
			Date createdDate = dateHelper.currentDate(OneFinConstants.HO_CHI_MINH_TIME_ZONE);
			sequence.setLastModified(createdDate);
			seqRepository.save(sequence);
		}
	}

	public void createVietcomLinkBankAccountSequenceIfNotExist() throws Exception {
		List<NumberSequenceTrans> sequenceList = this.findByName(this.prefixVcbLinkBankAccountName);
		if (sequenceList == null || sequenceList.isEmpty()) {
			NumberSequenceTrans sequence = new NumberSequenceTrans();
			sequence.setCurrentNumber(this.vcbLinkBankAccountSequenceStart);
			sequence.setName(this.prefixVcbLinkBankAccountName);
			sequence.setDfltBlockSize(this.blockSize);
			Date createdDate = dateHelper.currentDate(OneFinConstants.HO_CHI_MINH_TIME_ZONE);
			sequence.setLastModified(createdDate);
			seqRepository.save(sequence);
		}
	}

	public void createVietinBankTransferSequenceIfNotExist() throws Exception {
		List<NumberSequenceTrans> sequenceList = this.findByName(this.prefixVtbBankTransferName);
		if (sequenceList == null || sequenceList.isEmpty()) {
			NumberSequenceTrans sequence = new NumberSequenceTrans();
			sequence.setCurrentNumber(this.vtbBankTransferSequenceStart);
			sequence.setName(this.prefixVtbBankTransferName);
			sequence.setDfltBlockSize(this.blockSize);
			Date createdDate = dateHelper.currentDate(OneFinConstants.HO_CHI_MINH_TIME_ZONE);
			sequence.setLastModified(createdDate);
			seqRepository.save(sequence);
		}
	}

	public void createVietinBankTransferChildSequenceIfNotExist() throws Exception {
		List<NumberSequenceTrans> sequenceList = this.findByName(this.prefixVtbBankTransferChildName);
		if (sequenceList == null || sequenceList.isEmpty()) {
			NumberSequenceTrans sequence = new NumberSequenceTrans();
			sequence.setCurrentNumber(this.vtbBankTransferChildSequenceStart);
			sequence.setName(this.prefixVtbBankTransferChildName);
			sequence.setDfltBlockSize(this.blockSize);
			Date createdDate = dateHelper.currentDate(OneFinConstants.HO_CHI_MINH_TIME_ZONE);
			sequence.setLastModified(createdDate);
			seqRepository.save(sequence);
		}
	}

	public void createVietinLinkBankSequenceIfNotExist() throws Exception {
		List<NumberSequenceTrans> sequenceList = this.findByName(this.prefixVtbLinkBankName);
		if (sequenceList == null || sequenceList.isEmpty()) {
			NumberSequenceTrans sequence = new NumberSequenceTrans();
			sequence.setCurrentNumber(this.vtbLinkBankSequenceStart);
			sequence.setName(this.prefixVtbLinkBankName);
			sequence.setDfltBlockSize(this.blockSize);
			Date createdDate = dateHelper.currentDate(OneFinConstants.HO_CHI_MINH_TIME_ZONE);
			sequence.setLastModified(createdDate);
			seqRepository.save(sequence);
		}
	}

	public void createVtbVirtualAcctIfNotExist() throws Exception {
		List<NumberSequenceTrans> citiSequenceList = this.findByName(this.prefixVtbVirtualAcctName);
		if (citiSequenceList == null || citiSequenceList.isEmpty()) {
			NumberSequenceTrans citiSequence = new NumberSequenceTrans();
			citiSequence.setCurrentNumber(this.vtbVirtualAcctSequenceStart);
			citiSequence.setName(this.prefixVtbVirtualAcctName);
			citiSequence.setDfltBlockSize(this.blockSize);
			Date createdDate = dateHelper.currentDate(OneFinConstants.HO_CHI_MINH_TIME_ZONE);
			citiSequence.setLastModified(createdDate);
			seqRepository.save(citiSequence);
		}
	}

	public void createVtbVirtualAcctSchoolMerchantNumberIfNotExist() throws Exception {
		List<NumberSequenceTrans> sequenceList = this.findByName(this.prefixVtbPoolVirtualAcctSchoolMerchantNumberName);
		if (sequenceList == null || sequenceList.isEmpty()) {
			NumberSequenceTrans sequence = new NumberSequenceTrans();
			sequence.setCurrentNumber(this.vtbVirtualAcctNumberSequenceStart);
			sequence.setName(this.prefixVtbPoolVirtualAcctSchoolMerchantNumberName);
			sequence.setDfltBlockSize(this.blockSize);
			Date createdDate = dateHelper.currentDate(OneFinConstants.HO_CHI_MINH_TIME_ZONE);
			sequence.setLastModified(createdDate);
			seqRepository.save(sequence);
		}
	}

	public void createVtbVirtualAcctCommonMerchantNumberIfNotExist() throws Exception {
		List<NumberSequenceTrans> sequenceList = this.findByName(this.prefixVtbPoolVirtualAcctCommonMerchantNumberName);
		if (sequenceList == null || sequenceList.isEmpty()) {
			NumberSequenceTrans sequence = new NumberSequenceTrans();
			sequence.setCurrentNumber(this.vtbVirtualAcctNumberSequenceStart);
			sequence.setName(this.prefixVtbPoolVirtualAcctCommonMerchantNumberName);
			sequence.setDfltBlockSize(this.blockSize);
			Date createdDate = dateHelper.currentDate(OneFinConstants.HO_CHI_MINH_TIME_ZONE);
			sequence.setLastModified(createdDate);
			seqRepository.save(sequence);
		}
	}

	public void createBvbVirtualAcctSchoolMerchantNumberIfNotExist() throws Exception {
		String nameField = prefixBvbPoolVirtualAcctSchoolMerchantNumberName;
		List<NumberSequenceTrans> sequenceList =
				this.findByName(nameField);
		if (sequenceList == null || sequenceList.isEmpty()) {
			NumberSequenceTrans sequence = new NumberSequenceTrans();
			sequence.setCurrentNumber(bvbVirtualAcctNumberSequenceStart);
			sequence.setName(nameField);
			sequence.setDfltBlockSize(this.bvbVirtualBlockSize);
			Date createdDate = dateHelper.currentDate(OneFinConstants.HO_CHI_MINH_TIME_ZONE);
			sequence.setLastModified(createdDate);
			seqRepository.save(sequence);
		}
	}

	public void createBvbVirtualAcctCommonMerchantNumberIfNotExist() throws Exception {
		String nameField = prefixBvbPoolVirtualAcctCommonMerchantNumberName;
		List<NumberSequenceTrans> sequenceList =
				this.findByName(nameField);
		if (sequenceList == null || sequenceList.isEmpty()) {
			NumberSequenceTrans sequence = new NumberSequenceTrans();
			sequence.setCurrentNumber(this.bvbVirtualAcctNumberSequenceStart);
			sequence.setName(nameField);
			sequence.setDfltBlockSize(this.bvbVirtualBlockSize);
			Date createdDate = dateHelper.currentDate(OneFinConstants.HO_CHI_MINH_TIME_ZONE);
			sequence.setLastModified(createdDate);
			seqRepository.save(sequence);
		}
	}

	public void createBvbIBFTTransferNumberIfNotExist() throws Exception {
		String nameField = prefixBvbIBFTTransferNumberName;
		List<NumberSequenceTrans> sequenceList =
				this.findByName(nameField);
		if (sequenceList == null || sequenceList.isEmpty()) {
			NumberSequenceTrans sequence = new NumberSequenceTrans();
			sequence.setCurrentNumber(this.bvbIBFTTransferNumberSequenceStart);
			sequence.setName(nameField);
			sequence.setDfltBlockSize(this.bvbIBFTBlockSize);
			Date createdDate = dateHelper.currentDate(OneFinConstants.HO_CHI_MINH_TIME_ZONE);
			sequence.setLastModified(createdDate);
			seqRepository.save(sequence);
		}
	}

	public void createBvbIBFTTransferChildNumberIfNotExist() throws Exception {
		String nameField = prefixBvbIBFTTransferChildNumberName;
		List<NumberSequenceTrans> sequenceList =
				this.findByName(nameField);
		if (sequenceList == null || sequenceList.isEmpty()) {
			NumberSequenceTrans sequence = new NumberSequenceTrans();
			sequence.setCurrentNumber(this.bvbIBFTTransferChildNumberSequenceStart);
			sequence.setName(nameField);
			sequence.setDfltBlockSize(this.bvbIBFTBlockSize);
			Date createdDate = dateHelper.currentDate(OneFinConstants.HO_CHI_MINH_TIME_ZONE);
			sequence.setLastModified(createdDate);
			seqRepository.save(sequence);
		}
	}
    
}