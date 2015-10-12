package codeit.suisse;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.http.client.fluent.Request;
import org.apache.http.entity.ContentType;
import org.json.JSONObject;

public class TriangularArbitrage extends Thread {
	private ExecutorService executor = Executors.newCachedThreadPool();

	private static final int TRADE_AMOUNT = 5000;

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

	@Override
	public void run() {
		while (true) {
			try {
				List<String> currencyNames = new ArrayList<String>();
				currencyNames.addAll(Currency.getCurrencies().keySet());

				for (String currencyName : currencyNames) {
					Currency currency = Currency.getCurrency(currencyName);

					JSONObject rateInfo = new JSONObject(Request
							.Get(Main.API_URL + "fx/" + currencyName).execute()
							.returnContent().asString())
							.getJSONObject("fxValue");

					if (currency.getRate() != rateInfo.getDouble("fxRate")) {
						currency.setSlope(calculateSlope(currency, rateInfo));
						currency.setRate(rateInfo.getDouble("fxRate"));
						currency.setLastRateUpdate(System.currentTimeMillis());
					}
				}

				for (int x = 0; x < currencyNames.size(); x++) {
					String firstPair = currencyNames.get(x);
					for (int y = 0; y < currencyNames.size(); y++) {
						String secondPair = currencyNames.get(y);
						if (!firstPair.equals(currencyNames.get(y))
								&& !secondPair.equals(firstPair.substring(3)
										+ firstPair.substring(0, 3))
								&& currencyNames.get(y).substring(0, 3)
										.equals(firstPair.substring(3))) {
							String thirdPair = secondPair.substring(3)
									+ firstPair.substring(0, 3);

							double finalExchangeValue = TRADE_AMOUNT
									* Currency.getCurrency(firstPair).getRate()
									* Currency.getCurrency(secondPair)
											.getRate()
									* Currency.getCurrency(thirdPair).getRate();
							if (finalExchangeValue - TRADE_AMOUNT > 0) {
								processArbitrage(new String[] { firstPair,
										secondPair, thirdPair },
										finalExchangeValue - TRADE_AMOUNT);
							}
						}
					}
				}

				TimeUnit.MILLISECONDS.sleep(500);
			} catch (Exception e) {
				e.printStackTrace();
			}

		}
	}

	private void processArbitrage(String[] currencyPairs, double profit) {
		try {
			executor.execute(new ArbitageProcess(currencyPairs, profit));
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private class ArbitageProcess implements Runnable {
		private String[] currencyPairs;
		private double profit;

		public ArbitageProcess(String[] currencyPairs, double profit) {
			this.currencyPairs = currencyPairs;
			this.profit = profit;
		}

		@Override
		public void run() {
			try {
				for (String currencyPair : currencyPairs) {
					Main.trade(currencyPair, TRADE_AMOUNT);
				}
				System.out.println(currencyPairs[0] + " " + currencyPairs[1]
						+ " " + currencyPairs[2]
						+ " was arbitraged for a profit of $" + profit + ".");
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}
}
