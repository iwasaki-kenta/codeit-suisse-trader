package codeit.suisse;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.http.client.fluent.Request;
import org.json.JSONObject;

public class RateStreamer extends Thread {
	public interface RateCallback {
		void onSlopeGradeCheck(Currency lowest, Currency highest);
	}

	private RateCallback callback;

	public RateStreamer(RateCallback callback) {
		this.callback = callback;
	}

	private double calculateSlope(Currency rateInfo, JSONObject newRateInfo) {
		return calculateRateChange(rateInfo, newRateInfo)
				/ calculateTimeChange(rateInfo);
	}

	private double calculateRateChange(Currency rateInfo, JSONObject newRateInfo) {
		return rateInfo.getRate() - newRateInfo.getDouble("fxRate");
	}

	private long calculateTimeChange(Currency rateInfo) {
		return (System.currentTimeMillis() - rateInfo.getLastRateUpdate()) / 1000;
	}

	private <K, V extends Comparable<? super V>> Map<K, V> sortByValue(
			Map<K, V> map) {
		List<Map.Entry<K, V>> list = new LinkedList<>(map.entrySet());
		Collections.sort(list, new Comparator<Map.Entry<K, V>>() {
			@Override
			public int compare(Map.Entry<K, V> o1, Map.Entry<K, V> o2) {
				return (o1.getValue()).compareTo(o2.getValue());
			}
		});

		Map<K, V> result = new LinkedHashMap<>();
		for (Map.Entry<K, V> entry : list) {
			result.put(entry.getKey(), entry.getValue());
		}
		return result;
	}

	@Override
	public void run() {
		while (true) {
			try {
				List<Currency> slopes = new ArrayList<Currency>();
				for (Currency currency : Currency.getCurrencies()) {
					JSONObject rateInfo = new JSONObject(Request
							.Get(Main.API_URL + "fx/" + currency.getName())
							.execute().returnContent().asString())
							.getJSONObject("fxValue");

					if (currency.getRate() != rateInfo.getDouble("fxRate")) {
						System.out
								.format("FX Rate for currency %s updated %s second(s) ago. New FX Rate: %f | Change in Rate: %f | Slope: %f |\n",
										rateInfo.getString("currencyPair"),
										calculateTimeChange(currency),
										rateInfo.getDouble("fxRate"),
										calculateRateChange(currency, rateInfo),
										calculateSlope(currency, rateInfo));
						currency.setSlope(calculateSlope(currency, rateInfo));
						currency.setRate(rateInfo.getDouble("fxRate"));
						currency.setLastRateUpdate(System.currentTimeMillis());
					}
				}
				Collections.sort(slopes, new Comparator<Currency>() {

					@Override
					public int compare(Currency arg0, Currency arg1) {
						return ((Double) arg0.getSlope())
								.compareTo((Double) arg1.getSlope());
					}

				});

				callback.onSlopeGradeCheck(slopes.get(0),
						slopes.get(slopes.size() - 1));

				TimeUnit.MILLISECONDS.sleep(500);
			} catch (Exception ex) {

			}
		}
	}
}
