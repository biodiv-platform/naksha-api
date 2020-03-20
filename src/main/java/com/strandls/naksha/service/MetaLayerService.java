package com.strandls.naksha.service;

import java.io.IOException;
import java.util.Map;

import javax.naming.directory.InvalidAttributesException;
import javax.servlet.http.HttpServletRequest;

import org.json.simple.parser.ParseException;

import com.sun.jersey.multipart.FormDataMultiPart;

public interface MetaLayerService {

	public Map<String, String> upload(HttpServletRequest request, FormDataMultiPart multiPart) throws IOException, ParseException, InvalidAttributesException, InterruptedException;
	
}
