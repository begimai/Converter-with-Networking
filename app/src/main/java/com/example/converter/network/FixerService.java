package com.example.converter.network;

import com.example.converter.model.CurrencyRates;
import com.octo.android.robospice.retrofit.RetrofitGsonSpiceService;

public class FixerService extends RetrofitGsonSpiceService {

    private final static String BASE_URL = "http://api.fixer.io";

    @Override
    public void onCreate() {
        super.onCreate();
        addRetrofitInterface(CurrencyRates.class);
    }

    @Override
    protected String getServerUrl() {
        return BASE_URL;
    }

}
