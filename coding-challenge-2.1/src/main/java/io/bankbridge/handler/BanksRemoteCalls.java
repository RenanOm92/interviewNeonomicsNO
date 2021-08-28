package io.bankbridge.handler;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.bankbridge.model.BankModel;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.stream.Collectors;
import spark.Request;
import spark.Response;

public class BanksRemoteCalls {

	public static final ObjectMapper mapper = new ObjectMapper().setSerializationInclusion(Include.NON_NULL);
	private static Map<String, String> config;

	public static void init() throws Exception {
		config = new ObjectMapper()
				.readValue(Thread.currentThread().getContextClassLoader().getResource("banks-v2.json"), Map.class);
	}

	public static String handle(Request request, Response response) {
		List<BankModel> result = config.values().stream().map(BanksRemoteCalls::requestBankInfo).collect(Collectors.toList());

		result = Operations.filterCountryCode(result, request.headers(Operations.HEADER_FILTER_COUNTRY_CODE_FIELD_NAME));
		result = Operations.filterAuth(result, request.headers(Operations.HEADER_FILTER_AUTH_FIELD_NAME));
		result = Operations.paginate(result, request.headers(Operations.HEADER_PAGE_SIZE_FIELD_NAME));

		try {
			return mapper.writeValueAsString(result);
		} catch (JsonProcessingException e) {
			throw new RuntimeException("Error while printing results with Jackson");
		}
	}

	private static BankModel requestBankInfo(String uri) {
		try {
			HttpRequest httpRequest = HttpRequest.newBuilder(new URI(uri))
				.timeout(Duration.ofMinutes(1))
				.GET().build();
			HttpResponse<String> httpResponse = HttpClient.newBuilder().build()
				.send(httpRequest, HttpResponse.BodyHandlers.ofString());
			String jsonResponse = httpResponse.body();
			return mapper.readValue(jsonResponse, BankModel.class);
		} catch ( URISyntaxException | IOException  | InterruptedException e) {
			throw new RuntimeException("Error when accessing remote URI: "+ uri);
		}
	}

}
