package codeit.suisse;

import java.util.Random;
import java.util.concurrent.TimeUnit;

public class Forecast implements Runnable {
	public interface ForecastCallback {
		void onForecastStart(Forecast forecast);

		void onForecastUpdate(Forecast forecast, boolean up);

		void onForecastEnd(Forecast forecast);
	}

	private ForecastCallback callback;

	private double lastSlope;

	private int impact;
	private double initialRate;
	private long valueTime, windowTime;
	private String currency;

	public Forecast(ForecastCallback callback, String currency,
			double initialRate, int impact, long valueTime, long windowTime) {
		this.callback = callback;

		this.currency = currency;
		this.initialRate = initialRate;
		this.impact = impact;

		this.valueTime = valueTime;
		this.windowTime = windowTime;
	}

	public double getCurrentPredictedRate() {
		return initialRate + (initialRate * (impact / 100));
	}

	public double getInitialRate() {
		return initialRate;
	}

	public void setInitialRate(double initialRate) {
		this.initialRate = initialRate;
	}

	public Currency getCurrency() {
		return Currency.getCurrency(currency);
	}

	public int getImpact() {
		return impact;
	}

	public void setImpact(int impact) {
		this.impact = impact;
	}

	public long getValueTime() {
		return valueTime;
	}

	public void setValueTime(long valueTime) {
		this.valueTime = valueTime;
	}

	public long getWindowTime() {
		return windowTime;
	}

	public void setWindowTime(long windowTime) {
		this.windowTime = windowTime;
	}

	@Override
	public void run() {
		try {
			callback.onForecastStart(this);
			long lastTick = System.currentTimeMillis();
			while (true) {
				if (System.currentTimeMillis() - valueTime >= windowTime) {
					break;
				}

				if (System.currentTimeMillis() - lastTick >= new Random()
						.nextInt(3000) + 2000) {
					lastSlope = (lastSlope + Currency.getCurrency(currency)
							.getSlope()) / 2;
					callback.onForecastUpdate(this, lastSlope > 0);
					lastTick = System.currentTimeMillis();
				}

				TimeUnit.MILLISECONDS.sleep(1000);
			}
			callback.onForecastEnd(this);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		Thread.yield();
	}
}
