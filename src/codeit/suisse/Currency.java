package codeit.suisse;

import java.util.HashMap;
import java.util.Map;

import org.apache.http.client.fluent.Request;
import org.json.JSONObject;

public class Currency {
	public static final String[] CURRENCY_NAMES = new String[] { "CSC", "USD",
			"EUR", "SGD", "AUD" };

	private static final Map<String, Currency> currencies = new HashMap<String, Currency>() {
		{
			for (int x = 0; x < CURRENCY_NAMES.length; x++) {
				for (int y = CURRENCY_NAMES.length - 1; y >= 0; y--) {
					if (x != y) {
						String pairName = CURRENCY_NAMES[x] + CURRENCY_NAMES[y];

						try {
							JSONObject rateInfo = new JSONObject(Request
									.Get(Main.API_URL + "fx/" + pairName)
									.execute().returnContent().asString())
									.getJSONObject("fxValue");

							Currency currency = new Currency();
							currency.setRate(rateInfo.getDouble("fxRate"));
							currency.setLastRateUpdate(rateInfo.getLong("valueTime"));

							put(pairName, currency);
						} catch (Exception ex) {
							continue;
						}
					}
				}
			}
		}
	};

	private double rate, slope;
	private long lastRateUpdate;

	public static Currency getCurrency(String name) {
		return currencies.get(name);
	}

	public static Map<String, Currency> getCurrencies() {
		return currencies;
	}

	public double getSlope() {
		return slope;
	}

	public void setSlope(double slope) {
		this.slope = slope;
	}

	public double getRate() {
		return rate;
	}

	public void setRate(double rate) {
		this.rate = rate;
	}

	public long getLastRateUpdate() {
		return lastRateUpdate;
	}

	public void setLastRateUpdate(long lastUpdate) {
		this.lastRateUpdate = lastUpdate;
	}

}
