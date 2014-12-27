package org.metis;

import java.util.List;
import java.util.ArrayList;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.MediaType;
import org.springframework.web.client.*;

/**
 * Used for overriding the default timeout values in the RestTemplate.
 * 
 * @author jfernandez
 * 
 */
public class BaseRestTemplate extends RestTemplate {

	static private int connectTimeout = 2000;
	static private int readTimeout = 3000;

	// see if the defaults are being overridden via a -D
	static {
		String var = System.getProperty("wds.connectTimeout");
		if (var != null) {
			try {
				connectTimeout = Integer.parseInt(var);
			} catch (Exception e) {
			}
		}
		var = System.getProperty("wds.readTimeout");
		if (var != null) {
			try {
				readTimeout = Integer.parseInt(var);
			} catch (Exception e) {
			}
		}
	}

	public BaseRestTemplate() {
		if (getRequestFactory() instanceof SimpleClientHttpRequestFactory) {
			((SimpleClientHttpRequestFactory) getRequestFactory())
					.setConnectTimeout(connectTimeout);
			((SimpleClientHttpRequestFactory) getRequestFactory())
					.setReadTimeout(readTimeout);
		} else if (getRequestFactory() instanceof HttpComponentsClientHttpRequestFactory) {
			((HttpComponentsClientHttpRequestFactory) getRequestFactory())
					.setReadTimeout(readTimeout);
			((HttpComponentsClientHttpRequestFactory) getRequestFactory())
					.setConnectTimeout(connectTimeout);
		}
	}
	
	public static void main(String[] args){
		BaseRestTemplate t = new BaseRestTemplate();
		List<HttpMessageConverter<?>> converters = t.getMessageConverters();
		for (HttpMessageConverter<?> converter : converters) {
			if (converter instanceof MappingJackson2HttpMessageConverter) {
				System.out.println("Jackson converter found");
				System.out.println("Jackson converter String rep = " + converter.toString());
				System.out.println("Jackson object mapper = " + 
				((MappingJackson2HttpMessageConverter) converter).getObjectMapper().toString());
				for(MediaType m: converter.getSupportedMediaTypes()){
					//System.out.println("media type = " + m.getType());
					System.out.println("media type = " + m.toString());
				}
			}
		}
	}

}
