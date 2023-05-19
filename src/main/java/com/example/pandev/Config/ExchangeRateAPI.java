package com.example.pandev.Config;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.json.JSONObject;

import java.io.IOException;
import java.math.BigDecimal;

public class ExchangeRateAPI {



    public BigDecimal convert(String currFrom, String currTo, BigDecimal quantity) throws IOException {
        String URL = "https://v6.exchangerate-api.com/v6/51fc155eb9aef706f82859c6/latest/" + currFrom.toUpperCase();
        OkHttpClient hc = new OkHttpClient();
        Request request = new Request.Builder()
                .url(URL)
                .get()
                .build();
        Response response = hc.newCall(request).execute();
        String r = response.body().string();
        JSONObject jsonObject = new JSONObject(r);
        JSONObject rateObject = jsonObject.getJSONObject("conversion_rates");
        BigDecimal rate = rateObject.getBigDecimal(currTo.toUpperCase());
        BigDecimal result = rate.multiply(quantity);
        return result;

    }

}
