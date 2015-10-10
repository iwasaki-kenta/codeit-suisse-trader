import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.http.client.fluent.Request;
import org.json.JSONArray;
import org.json.JSONObject;

public class Main {
	private Map<String, Currency> currentRates = new HashMap<String, Currency>();

	private boolean localMode = true;
	private String apiUrl = localMode ? "http://192.168.2.5:2015/"
			: "http://128.199.74.105:2015/";

	public static void main(String[] args) {
		new Main().init();
	}

	private void init() {
		Thread trader = new Trader();
		trader.start();
	}

	private class Trader extends Thread {

		@Override
		public void run() {
			while (true) {
				try {
					streamFXNews();
					TimeUnit.MILLISECONDS.sleep(500);
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		}

		private void streamFXNews() throws IOException {
			JSONArray news = new JSONObject(Request.Get(apiUrl + "news/fx")
					.execute().returnContent().asString()).getJSONArray("news");

			boolean found = false;
			for (int i = 0; i < news.length(); i++) {
				JSONObject newsObj = news.getJSONObject(i);

				JSONObject rateInfo = new JSONObject(
						Request.Get(
								apiUrl + "fx/"
										+ newsObj.getString("currencyPair"))
								.execute().returnContent().asString())
						.getJSONObject("fxValue");

				if (!currentRates.containsKey(rateInfo
						.getString("currencyPair"))) {
					Currency currency = new Currency();
					currency.setRate(rateInfo.getDouble("fxRate"));
					currency.setLastUpdate(System.currentTimeMillis());

					currentRates.put(rateInfo.getString("currencyPair"),
							currency);

					System.out
							.format("New currency found: %s - FX Rate: %f | Impact: %d | Window Time: %d\n",
									rateInfo.getString("currencyPair"),
									rateInfo.getDouble("fxRate"),
									newsObj.getInt("impact"),
									newsObj.getInt("windowMinutes"));
				} else if (currentRates.get(rateInfo.getString("currencyPair"))
						.getRate() != rateInfo.getDouble("fxRate")) {
					Currency currency = currentRates.get(rateInfo
							.getString("currencyPair"));

					System.out
							.format("Updated currency %s from %s seconds ago. New FX Rate: %f | Impact: %d | Window Time: %d | Change in Rate: %f | Slope: %f |\n",
									rateInfo.getString("currencyPair"),
									calculateTimeChange(currency),
									rateInfo.getDouble("fxRate"),
									newsObj.getInt("impact"),
									newsObj.getInt("windowMinutes"),
									calculateRateChange(currency, rateInfo),
									calculateSlope(currency, rateInfo));

					currency.setRate(rateInfo.getDouble("fxRate"));
					currency.setLastUpdate(System.currentTimeMillis());
					currentRates.put(rateInfo.getString("currencyPair"),
							currency);
				}
			}
		}

		private double calculateSlope(Currency rateInfo, JSONObject newRateInfo) {
			return calculateRateChange(rateInfo, newRateInfo)
					/ calculateTimeChange(rateInfo);
		}

		private double calculateRateChange(Currency rateInfo,
				JSONObject newRateInfo) {
			return rateInfo.getRate() - newRateInfo.getDouble("fxRate");
		}

		private long calculateTimeChange(Currency rateInfo) {
			return (System.currentTimeMillis() - rateInfo.getLastUpdate()) / 1000;
		}
	}
}
