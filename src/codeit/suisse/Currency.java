package codeit.suisse;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class Currency {
	private static final Map<String, Currency> currencies = new HashMap<String, Currency>();

	private String name;

	private double slope;

	private double rate;
	private long lastRateUpdate;

	public static Currency getCurrency(String name) {
		if (!currencies.containsKey(name)) {
			return currencies.put(name, new Currency(name));
		}
		return currencies.get(name);
	}

	public static Collection<Currency> getCurrencies() {
		return currencies.values();
	}

	private Currency(String name) {
		this.name = name;
	}
	
	public String getInverseCurrency() {
		String inverse = name.substring(3) + name.substring(0, 3);
		return inverse;
	}

	public double getSlope() {
		return slope;
	}

	public void setSlope(double slope) {
		this.slope = slope;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
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
