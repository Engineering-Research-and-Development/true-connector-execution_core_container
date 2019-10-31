package it.eng.idsa.businesslogic.web.rest;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.http.Header;
import org.apache.http.HttpEntity;

public class MultipartFormEntityTest implements HttpEntity {

	@Override
	public boolean isRepeatable() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isChunked() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public long getContentLength() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Header getContentType() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Header getContentEncoding() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public InputStream getContent() throws IOException, UnsupportedOperationException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void writeTo(OutputStream outStream) throws IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean isStreaming() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void consumeContent() throws IOException {
		// TODO Auto-generated method stub
		
	}

}
