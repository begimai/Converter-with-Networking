package com.example.converter.network;

import com.example.converter.model.CurrencyRates;
import com.octo.android.robospice.request.retrofit.RetrofitSpiceRequest;

public class FixerRequest extends RetrofitSpiceRequest<CurrencyRates, Fixer> {

    public FixerRequest() {
        super(CurrencyRates.class, Fixer.class);
    }

    @Override
    public CurrencyRates loadDataFromNetwork() throws Exception {
        return getService().getCurrencyRates();
    }

}
