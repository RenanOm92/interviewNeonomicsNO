package io.bankbridge.handler;

import io.bankbridge.model.BankModel;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class Operations {

	public static final String HEADER_PAGE_SIZE_FIELD_NAME = "xPage-Size";
	public static final String HEADER_FILTER_PRODUCTS_FIELD_NAME = "xFilter-Products";
	public static final String HEADER_FILTER_COUNTRY_CODE_FIELD_NAME = "xFilter-Country-Code";
	public static final String HEADER_FILTER_AUTH_FIELD_NAME = "xFilter-Auth";
	public static int DEFAULT_PAGE_SIZE = 5;

	public static List<BankModel> paginate(List<BankModel> listBanks, String sizeFromHeader) {
		int sizeListCurrent = listBanks.size();
		if (sizeFromHeader != null && sizeFromHeader.matches("\\d+")) {
			int sizeFromHeaderInt = Integer.parseInt(sizeFromHeader);

			if (sizeFromHeaderInt < sizeListCurrent) {
				return listBanks.subList(0, sizeFromHeaderInt);
			} else {
				return listBanks;
			}
		} else {
			return listBanks.subList(0, DEFAULT_PAGE_SIZE);
		}
	}

	public static List<BankModel> filterCountryCode(List<BankModel> listBanks, String countryCode) {
		if (countryCode != null) {
			return listBanks.stream().filter(bank -> bank.countryCode.equals(countryCode.toUpperCase())).collect(Collectors.toList());
		} else {
			return listBanks;
		}
	}

	public static List<BankModel> filterProducts(List<BankModel> listBanks, String products) {
		if (products != null) {
			List<String> productsList = Arrays.stream(products.split(",")).map(String::toLowerCase).collect(Collectors.toList());
			return listBanks.stream().filter(bank -> bank.products.containsAll(productsList)).collect(Collectors.toList());
		} else {
			return listBanks;
		}
	}

	public static List<BankModel> filterAuth(List<BankModel> listBanks, String auth) {
		if (auth != null) {
			return listBanks.stream().filter(bank -> bank.auth.equals(auth.toLowerCase())).collect(Collectors.toList());
		} else {
			return listBanks;
		}
	}

}
