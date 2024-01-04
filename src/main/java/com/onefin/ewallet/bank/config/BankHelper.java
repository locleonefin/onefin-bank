package com.onefin.ewallet.bank.config;

import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

import com.onefin.ewallet.common.base.service.BaseHelper;

@Service
public class BankHelper extends BaseHelper {

	@Bean
	public ModelMapper modelMapper() {
		return new ModelMapper();
	}

}
