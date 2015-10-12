package codeit.suisse;

import java.io.IOException;

import org.apache.http.client.fluent.Request;
import org.apache.http.entity.ContentType;
import org.json.JSONObject;

public class Main {

	public static final boolean LOCAL_MODE = false;
	public static final String API_URL = LOCAL_MODE ? "http://192.168.2.5:2015/"
			: "http://128.199.74.105:2015/";
	public static final String TEAM_ID = "56525d9f-3c29-4a16-839a-cd03972d7cf5";

	public static final int TRADE_AMOUNT = 500;

	public static void main(String[] args) throws IOException,
			InterruptedException {
		new Main().init();
	}

	private void init() {
		TriangularArbitrage arbitrageSolver = new TriangularArbitrage();
		arbitrageSolver.start();
	}

	public static void trade(String currencyPair, double quantity) {
		System.out
				.format("Traded $%f of %s dollars for $%f of %s dollars.\n",
						quantity, currencyPair.substring(0, 3),
						(quantity * (1 + Currency.getCurrency(currencyPair)
								.getRate())), currencyPair.substring(3));
		JSONObject quoteParams = new JSONObject();
		quoteParams.put("teamId", Main.TEAM_ID);
		quoteParams.put("currencyPair", currencyPair);
		quoteParams.put("quantity", quantity);

		try {

			String quoteId = new JSONObject(Request
					.Post(Main.API_URL + "fx/quote")
					.bodyString(quoteParams.toString(),
							ContentType.APPLICATION_JSON).execute()
					.returnContent().asString()).getJSONObject("quoteResponse")
					.getString("quoteId");

			JSONObject executeParams = new JSONObject();
			executeParams.put("teamId", Main.TEAM_ID);
			executeParams.put("quoteId", quoteId);

			Request.Post(Main.API_URL + "fx/quote/execute")
					.bodyString(executeParams.toString(),
							ContentType.APPLICATION_JSON).execute();
		} catch (Exception ex) {
			System.out.println(quoteParams.toString());
			ex.printStackTrace();
		}
	}

}
