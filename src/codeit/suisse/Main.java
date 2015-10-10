package codeit.suisse;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import codeit.suisse.Forecast.ForecastCallback;
import codeit.suisse.RateStreamer.RateCallback;

public class Main implements ForecastCallback, RateCallback {

	public static final boolean LOCAL_MODE = false;
	public static final String API_URL = LOCAL_MODE ? "http://192.168.2.5:2015/"
			: "http://128.199.74.105:2015/";

	public static final String[] CURRENCY_NAMES = new String[] { "CSC", "USD",
			"EUR", "SGD", "AUD" };
	
	private RateStreamer rateStreamer;
	private ForecastStreamer forecastStreamer;

	public static final int TRADE_AMOUNT = 1000;

	static {
		// for (int x = 0; x < CURRENCY_NAMES.length; x++) {
		// for (int y = CURRENCY_NAMES.length - 1; y >= 0; y--) {
		// if (x != y)
		// Currency.getCurrency(CURRENCY_NAMES[x] + CURRENCY_NAMES[y]);
		// }
		// }

		for (int x = 1; x < CURRENCY_NAMES.length; x++) {
			Currency.getCurrency(CURRENCY_NAMES[x] + CURRENCY_NAMES[0]);

		}
	}

	public static void main(String[] args) throws IOException,
			InterruptedException {
		new Main().init();

		// for (Currency c : Currency.getCurrencies()) {
		// System.out.println(c.getName());
		// }
	}

	private void init() {
		rateStreamer = new RateStreamer(this);
		rateStreamer.start();

		forecastStreamer = new ForecastStreamer(this);
		forecastStreamer.start();
	}

	@Override
	public void onForecastStart(Forecast forecast) {
		System.out
				.format("Forecast for %s has started. Window is %d minute(s) for %d%%. Started %d minute(s) ago. @ Start: %f | Predicted Rate @ End: %f\n",
						forecast.getCurrency().getName(), TimeUnit.MINUTES
								.convert(forecast.getWindowTime(),
										TimeUnit.MILLISECONDS), forecast
								.getImpact(), TimeUnit.MINUTES.convert(
								System.currentTimeMillis()
										- forecast.getValueTime(),
								TimeUnit.MILLISECONDS), forecast
								.getInitialRate(), forecast
								.getCurrentPredictedRate());
	}

	@Override
	public void onForecastEnd(Forecast forecast) {
		System.out
				.format("Forecast for %s has ended %d minutes(s) ago. Exchange rate affected by %d%%. @ Start: %f | Predicted Rate @ End: %f | Current Rate: %f\n",
						forecast.getCurrency().getName(), TimeUnit.MINUTES
								.convert(
										System.currentTimeMillis()
												- forecast.getValueTime(),
										TimeUnit.MILLISECONDS), forecast
								.getImpact(), forecast.getInitialRate(),
						forecast.getCurrentPredictedRate(), forecast
								.getCurrency().getRate());

		try {
			if (forecast.getCurrency().getRate()
					- forecast.getCurrentPredictedRate() < 0) {
				Runtime.getRuntime().exec(
						"node -e \"require('./trade')('"
								+ forecast.getCurrency().getInverseCurrency()
								+ "', " + TRADE_AMOUNT * 10 / 2 + ")\"");
			} else {
				Runtime.getRuntime().exec(
						"node -e \"require('./trade')('"
								+ forecast.getCurrency().getName() + "', "
								+ TRADE_AMOUNT * 10 + ")\"");
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	@Override
	public void onSlopeGradeCheck(Currency lowest, Currency highest) {
		System.out.println(lowest.getName() + " Lowest Slope: "
				+ lowest.getSlope());
		System.out.println(highest.getName() + " Highest Slope: "
				+ highest.getSlope());
	}

	@Override
	public void onForecastUpdate(Forecast forecast, boolean up) {
		try {
			if (up) {
				System.out
						.format("Traded %d in currency %s as a result of forecast distribution.\n",
								TRADE_AMOUNT, forecast.getCurrency().getName());
				Runtime.getRuntime().exec(
						"node -e \"require('./trade')('"
								+ forecast.getCurrency().getName() + "', "
								+ TRADE_AMOUNT + ")\"");
			} else if (forecast.getCurrency().getRate() < 0) {
				System.out
						.format("Traded %d in currency %s as a result of forecast distribution.\n",
								TRADE_AMOUNT, forecast.getCurrency()
										.getInverseCurrency());
				Runtime.getRuntime().exec(
						"node -e \"require('./trade')('"
								+ forecast.getCurrency().getInverseCurrency()
								+ "', " + TRADE_AMOUNT + ")\"");
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

}
