package codeit.suisse;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.http.client.fluent.Request;
import org.json.JSONArray;
import org.json.JSONObject;

import codeit.suisse.Forecast.ForecastCallback;

public class ForecastStreamer extends Thread {
	private ExecutorService forecastExecutor = Executors.newCachedThreadPool();
	private Map<Long, Forecast> currentForecasts = new HashMap<Long, Forecast>();

	public ForecastCallback callback;

	public ForecastStreamer(ForecastCallback callback) {
		this.callback = callback;
	}

	@Override
	public void run() {
		while (true) {
			try {
				JSONArray newsArray = new JSONObject(Request
						.Get(Main.API_URL + "news/fx").execute()
						.returnContent().asString()).getJSONArray("news");

				for (int i = 0; i < newsArray.length(); i++) {
					JSONObject news = newsArray.getJSONObject(i);
					if (!currentForecasts
							.containsKey(news.getLong("valueTime"))) {
						Forecast forecast = new Forecast(
								callback,
								news.getString("currencyPair"),
								new JSONObject(
										Request.Get(
												Main.API_URL
														+ "fx/"
														+ news.getString("currencyPair"))
												.execute().returnContent()
												.asString()).getJSONObject(
										"fxValue").getDouble("fxRate"),
								news.getInt("impact"),
								news.getLong("valueTime"),
								TimeUnit.MILLISECONDS.convert(
										news.getInt("windowMinutes"),
										TimeUnit.MINUTES));
						forecastExecutor.execute(forecast);

						currentForecasts.put(forecast.getValueTime(), forecast);
					}
				}
				TimeUnit.MILLISECONDS.sleep(1000);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}

	// if (!currentRates.containsKey(rateInfo.getString("currencyPair"))) {
	// Currency currency = new Currency();
	// currency.setRate(rateInfo.getDouble("fxRate"));
	// currency.setValueTime(newsObj.getLong("valueTime"));
	// currency.setLastRateUpdate(System.currentTimeMillis());
	//
	// currentRates.put(rateInfo.getString("currencyPair"), currency);
	//
	// System.out
	// .format("New currency forecast found: %s - FX Rate: %f | Impact: %d | Window Time: %d\n",
	// rateInfo.getString("currencyPair"),
	// rateInfo.getDouble("fxRate"),
	// newsObj.getInt("impact"),
	// newsObj.getInt("windowMinutes"));
	// }
	//
	// Currency currency = currentRates.get(rateInfo
	// .getString("currencyPair"));
	//
	// if (currency.getRate() != rateInfo.getDouble("fxRate")) {
	// System.out
	// .format("FX Rate for currency %s updated %s second(s) ago. New FX Rate: %f | Change in Rate: %f | Slope: %f |\n",
	// rateInfo.getString("currencyPair"),
	// calculateTimeChange(currency),
	// rateInfo.getDouble("fxRate"),
	// calculateRateChange(currency, rateInfo),
	// calculateSlope(currency, rateInfo));
	//
	// currency.setRate(rateInfo.getDouble("fxRate"));
	// currency.setLastRateUpdate(System.currentTimeMillis());
	//
	// currentRates.put(rateInfo.getString("currencyPair"), currency);
	// }
}