package com.onefin.ewallet.bank.service.bvb;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class ExtractVarNameSerializedJson extends JsonSerializer<String> {


	@Autowired
	private BVBVirtualAcct bvbVirtualAcct;

	@Override
	public void serialize(String value, JsonGenerator gen, SerializerProvider serializers) throws IOException {

		gen.writeString(bvbVirtualAcct.extractVarName(value));

	}
}
