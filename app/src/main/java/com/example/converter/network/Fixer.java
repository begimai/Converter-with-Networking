package com.example.converter.network;

import com.example.converter.model.CurrencyRates;

import retrofit.http.GET;

public interface Fixer {
    @GET("/latest")
    CurrencyRates getCurrencyRates();
}
