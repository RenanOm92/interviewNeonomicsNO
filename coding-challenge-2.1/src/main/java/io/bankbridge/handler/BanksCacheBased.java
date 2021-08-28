package io.bankbridge.handler;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import java.util.ArrayList;
import java.util.List;

import org.ehcache.Cache;
import org.ehcache.CacheManager;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.bankbridge.model.BankModel;
import io.bankbridge.model.BankModelList;
import spark.Request;
import spark.Response;

public class BanksCacheBased {

	public static final ObjectMapper mapper = new ObjectMapper().setSerializationInclusion(Include.NON_NULL);

	public static CacheManager cacheManager;

	public static void init() throws Exception {
		cacheManager = CacheManagerBuilder
				.newCacheManagerBuilder().withCache("banks", CacheConfigurationBuilder
						.newCacheConfigurationBuilder(String.class, BankModel.class, ResourcePoolsBuilder.heap(20)))
				.build();
		cacheManager.init();
		Cache cache = cacheManager.getCache("banks", String.class, BankModel.class);
		try {
			BankModelList models = new ObjectMapper().readValue(
					Thread.currentThread().getContextClassLoader().getResource("banks-v1.json"), BankModelList.class);
			for (BankModel model : models.banks) {
				model.auth = null;
				cache.put(model.bic, model);
			}
		} catch (Exception e) {
			throw e;
		}
	}

	public static String handle(Request request, Response response) {
		List<BankModel> banksAll = new ArrayList<>();
		cacheManager.getCache("banks", String.class, BankModel.class).forEach(entry -> {
			banksAll.add(entry.getValue());
		});

		List<BankModel> result = banksAll;
		result = Operations.filterCountryCode(result, request.headers(Operations.HEADER_FILTER_COUNTRY_CODE_FIELD_NAME));
		result = Operations.filterProducts(result, request.headers(Operations.HEADER_FILTER_PRODUCTS_FIELD_NAME));
		result = Operations.paginate(result, request.headers(Operations.HEADER_PAGE_SIZE_FIELD_NAME));

		try {
			return mapper.writeValueAsString(result);
		} catch (JsonProcessingException e) {
			throw new RuntimeException("Error while processing request");
		}

	}

}
