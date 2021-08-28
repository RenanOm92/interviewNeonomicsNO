
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.bankbridge.handler.Operations;
import io.bankbridge.model.BankModel;
import io.bankbridge.model.BankModelList;
import java.util.List;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class OperationsBanksTest {

	static BankModelList models;

	@BeforeAll
	static void before() throws Exception {
		models = new ObjectMapper().readValue(
			Thread.currentThread().getContextClassLoader().getResource("banks-v1.json"), BankModelList.class);
	}

	@Test()
	public void testFilterCountryCode() {
		assertEquals(20, models.banks.size());

		List<BankModel> gbBanks = Operations.filterCountryCode(models.banks, "GB");
		assertEquals(2, gbBanks.size());
		List<BankModel> chBanks = Operations.filterCountryCode(models.banks, "CH");
		assertEquals(2, chBanks.size());
		List<BankModel> noBanks = Operations.filterCountryCode(models.banks, "no");
		assertEquals(5, noBanks.size());
		List<BankModel> spBanks = Operations.filterCountryCode(models.banks, "Sp");
		assertEquals(3, spBanks.size());
		List<BankModel> seBanks = Operations.filterCountryCode(models.banks, "sE");
		assertEquals(3, seBanks.size());
		List<BankModel> deBanks = Operations.filterCountryCode(models.banks, "DE");
		assertEquals(3, deBanks.size());
		List<BankModel> ptBanks = Operations.filterCountryCode(models.banks, "pt");
		assertEquals(2, ptBanks.size());
	}

	@Test()
	public void testFilterAuth() {
		assertEquals(20, models.banks.size());

		List<BankModel> sslBanks = Operations.filterAuth(models.banks, "ssl-certificate");
		assertEquals(5, sslBanks.size());
		List<BankModel> oauthBanks = Operations.filterAuth(models.banks, "oauth");
		assertEquals(12, oauthBanks.size());
		List<BankModel> openIdBanks = Operations.filterAuth(models.banks, "open-id");
		assertEquals(3, openIdBanks.size());
	}

	@Test()
	public void testFilterProducts() {
		List<BankModel> allBanks = Operations.filterProducts(models.banks, null);
		assertEquals(20, allBanks.size());
		List<BankModel> paymentsBanks = Operations.filterProducts(models.banks, "payments");
		assertEquals(16, paymentsBanks.size());
		List<BankModel> accountsBanks = Operations.filterProducts(models.banks, "accounts");
		assertEquals(16, accountsBanks.size());
		List<BankModel> accountsAndPaymentsBanks = Operations.filterProducts(models.banks, "accounts,payments");
		assertEquals(12, accountsAndPaymentsBanks.size());
	}

	@Test()
	public void testPageSize() {
		List<BankModel> fiveBanks = Operations.paginate(models.banks, null);
		assertEquals(Operations.DEFAULT_PAGE_SIZE, fiveBanks.size());

		List<BankModel> eightBanks = Operations.paginate(models.banks, "8");
		assertEquals(8, eightBanks.size());

		List<BankModel> allBanks = Operations.paginate(models.banks, "100");
		assertEquals(20, allBanks.size());
	}

	@Test
	public void testMixedOperations() {
		List<BankModel> noSslAccounts = Operations.filterCountryCode(models.banks, "no");
		noSslAccounts = Operations.filterAuth(noSslAccounts, "ssl-certificate");
		noSslAccounts = Operations.filterProducts(noSslAccounts, "accounts");
		assertEquals(3, noSslAccounts.size());

		List<BankModel> checkSize = Operations.paginate(noSslAccounts, "5");
		assertEquals(3, checkSize.size());
		checkSize = Operations.paginate(noSslAccounts, "1");
		assertEquals(1, checkSize.size());
	}

}
