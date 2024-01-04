package com.onefin.ewallet.bank.service.woori;

import com.fasterxml.jackson.core.type.TypeReference;
import com.onefin.ewallet.bank.service.bvb.BVBRequestUtil;
import com.onefin.ewallet.common.utility.json.JSONHelper;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import java.io.StringReader;
import java.io.StringWriter;

@Service
public class WooriRequestUtil {

	private static final Logger LOGGER = LoggerFactory.getLogger(WooriRequestUtil.class);


	@Autowired
	private JSONHelper jsonHelper;

	@Autowired
	private ModelMapper modelMapper;

	public Object xmlToObject(String data, Class... classesToBeBound) throws JAXBException {
		JAXBContext context = JAXBContext.newInstance(classesToBeBound);
		StringReader stringReader = new StringReader(data);
		return (context.createUnmarshaller().unmarshal(stringReader));
	}

	public <T> T xmlToObject(String data, Class<T> type) throws JAXBException {
		JAXBContext context = JAXBContext.newInstance(type);
		StringReader stringReader = new StringReader(data);
		return type.cast(context.createUnmarshaller().unmarshal(stringReader));
	}

	public <T> String objectToXml(T data, Class... classesToBeBound) throws JAXBException {

		LOGGER.info("objectToXml get class: {}", data.getClass());
		JAXBContext context = JAXBContext.newInstance(data.getClass());
		Marshaller mar = context.createMarshaller();
		mar.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
//		mar.setProperty(Marshaller.JAXB_FRAGMENT,Boolean.TRUE);
//		mar.setProperty(Marshaller.JAXB_ENCODING,"UTF-8");

		StringWriter stringWriter = new StringWriter();
		mar.marshal(data, stringWriter);
		return stringWriter.toString();
	}


}
