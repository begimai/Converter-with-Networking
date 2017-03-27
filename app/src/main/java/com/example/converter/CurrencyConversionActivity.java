package com.example.converter;

import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.converter.model.CurrencyRates;
import com.example.converter.network.FixerRequest;
import com.octo.android.robospice.persistence.DurationInMillis;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

import support.SpiceActivity;

public class CurrencyConversionActivity extends SpiceActivity {
	private static final String PREFERENCE_FILE =
        "com.example.converter.values.currency";

	private static final int PAUSE_BEFORE_HIDING_THE_PROGRESS_BAR_IN_MS =
        1000;

	private static final String PREFERENCE_FIRST_VALUE_KEY =
        "firstValue";
	private static final String PREFERENCE_FIRST_UNIT_SELECTION_KEY =
        "firstSelectedPosition";
	private static final String PREFERENCE_SECOND_UNIT_SELECTION_KEY =
        "secondSelectedPosition";

    private static final String RESULT_DECIMAL_FORMAT =
        "#.####";

	private TextWatcher firstTextWatcher,
						secondTextWatcher;

	private EditText firstValueEditText,
					 secondValueEditText;

	private Spinner firstUnitSpinner,
					secondUnitSpinner;

	private ProgressDialog progressDialog;

	private SharedPreferences previousUserValues;
	private Map<String, Double> conversionRatios;

	private FixerRequest fixerRequest;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_currency_conversion);

		firstValueEditText =
			(EditText) findViewById(R.id.firstCurrencyValueEditText);
		secondValueEditText =
			(EditText) findViewById(R.id.secondCurrencyValueEditText);

		firstUnitSpinner =
			(Spinner) findViewById(R.id.firstCurrencyUnitSpinner);
		secondUnitSpinner =
			(Spinner) findViewById(R.id.secondCurrencyUnitSpinner);

		previousUserValues =
			getSharedPreferences(PREFERENCE_FILE, 0);
		fixerRequest =
			new FixerRequest();
        conversionRatios =
            new HashMap<String, Double>();

        progressDialog = new ProgressDialog(CurrencyConversionActivity.this);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setCancelable(false);

        setupEventHandlers();
        loadConversionData();
	}

	@Override
	protected void onStop() {
		super.onStop();
		saveUserInput();
	}

    @Override
    protected void onDestroy() {
        super.onDestroy();
        progressDialog.dismiss();
    }

    private void setupEventHandlers() {
		setupEditTextEventHandlers();
		setupSpinnersEventHandlers();
	}

	private void setupEditTextEventHandlers() {
		firstTextWatcher = new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) { }

			@Override
			public void afterTextChanged(Editable arg0) {
				calculateResult(
					firstValueEditText,  firstUnitSpinner,
					secondValueEditText, secondUnitSpinner,
					secondTextWatcher
				);
			}
		};
		secondTextWatcher = new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) { }

			@Override
			public void afterTextChanged(Editable s) {
				calculateResult(
					secondValueEditText, secondUnitSpinner,
					firstValueEditText,  firstUnitSpinner,
					firstTextWatcher
				);
			}
		};

		firstValueEditText.addTextChangedListener(firstTextWatcher);
		secondValueEditText.addTextChangedListener(secondTextWatcher);
	}

	private void setupSpinnersEventHandlers() {
		AdapterView.OnItemSelectedListener listener = new AdapterView.OnItemSelectedListener() {
			@Override
			public void onNothingSelected(AdapterView<?> parent) { }

			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int from, long id) {
				calculateResult(
					firstValueEditText, firstUnitSpinner,
					secondValueEditText, secondUnitSpinner,
					secondTextWatcher
				);
			}
		};

		firstUnitSpinner.setOnItemSelectedListener(listener);
		secondUnitSpinner.setOnItemSelectedListener(listener);
	}

	private void loadConversionData() {
		showProgressDialog();

		getSpiceManager().execute(
			fixerRequest,
			"fixer",
			DurationInMillis.ONE_MINUTE,
			new RequestListener<CurrencyRates>() {
				@Override
				public void onRequestFailure(SpiceException spiceException) {
					hideProgressDialog();

					reportError(getString(R.string.conversion_data_update_error_message));
				}

				@Override
				public void onRequestSuccess(CurrencyRates serviceData) {
                    prepareConversionRatios(serviceData);
                    populateSpinners();
                    loadUserInput();

                    hideProgressDialogAfterDelay(
                        PAUSE_BEFORE_HIDING_THE_PROGRESS_BAR_IN_MS
                    );
				}
		});
	}

	private void showProgressDialog() {
		progressDialog.setMessage(getString(R.string.please_wait_message));
		progressDialog.show();
	}

    private void hideProgressDialog() {
        hideProgressDialogAfterDelay(0);
    }

	private void hideProgressDialogAfterDelay(int delayInMilliseconds) {
		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				progressDialog.hide();
			}
		}, delayInMilliseconds);
	}

	private void prepareConversionRatios(@NonNull CurrencyRates serviceData) {
		Map<String, Double> rates =
			serviceData.rates;
		String baseCurrency =
			serviceData.base;

        rates.put(baseCurrency, 1.0);

        conversionRatios =
            rates;
	}

	private void populateSpinners() {
		ArrayAdapter<CharSequence> adapter =
			new ArrayAdapter<>(this, android.R.layout.simple_spinner_item);

		for (String currency : conversionRatios.keySet()) {
			adapter.add(currency);
		}

		adapter.setDropDownViewResource(
		    android.R.layout.simple_spinner_dropdown_item
        );

		firstUnitSpinner.setAdapter(adapter);
		secondUnitSpinner.setAdapter(adapter);
	}

	private void loadUserInput() {
		String firstValue =
			previousUserValues.getString(PREFERENCE_FIRST_VALUE_KEY, "");
		int firstSpinnerSelectedPosition =
			previousUserValues.getInt(PREFERENCE_FIRST_UNIT_SELECTION_KEY, 0);
		int secondSpinnerSelectedPosition =
			previousUserValues.getInt(PREFERENCE_SECOND_UNIT_SELECTION_KEY, 0);

		firstValueEditText.setText(firstValue);
		firstUnitSpinner.setSelection(firstSpinnerSelectedPosition);
		secondUnitSpinner.setSelection(secondSpinnerSelectedPosition);
	}

	private void saveUserInput() {
		SharedPreferences.Editor editor =
			previousUserValues.edit();

		String firstValue =
			firstValueEditText.getText().toString();
		int firstSpinnerSelectedPosition =
			firstUnitSpinner.getSelectedItemPosition();
		int secondSpinnerSelectedPosition =
			secondUnitSpinner.getSelectedItemPosition();

		editor.putString(
			PREFERENCE_FIRST_VALUE_KEY,
			firstValue
		);
		editor.putInt(
			PREFERENCE_FIRST_UNIT_SELECTION_KEY,
			firstSpinnerSelectedPosition
		);
		editor.putInt(
			PREFERENCE_SECOND_UNIT_SELECTION_KEY,
			secondSpinnerSelectedPosition
		);

		editor.apply();
	}

	private void calculateResult(
		             @NonNull EditText fromEditText,
					 @NonNull Spinner fromUnitSpinner,
					 @NonNull EditText toEditText,
					 @NonNull Spinner toUnitSpinner,
					 @NonNull TextWatcher toEditTextTextWatcher
	             ) {
		String fromSelectedCurrency =
			(String) fromUnitSpinner.getSelectedItem();
		String toSelectedCurrency =
			(String) toUnitSpinner.getSelectedItem();

		double value = 0.0;
		try {
			value =
				Double.parseDouble(fromEditText.getText().toString());
		} catch (NumberFormatException ignored) { }

		double fromValue = 1.0;
        try {
            fromValue = conversionRatios.get(fromSelectedCurrency);
        } catch(NullPointerException ignored) { }

		double toValue = 1.0;
        try {
            toValue = conversionRatios.get(toSelectedCurrency);
        } catch(NullPointerException ignored) { }

		double result =
			(value / fromValue) * toValue;

		toEditText.removeTextChangedListener(toEditTextTextWatcher);
		toEditText.setText(new DecimalFormat(RESULT_DECIMAL_FORMAT).format(result));
		toEditText.addTextChangedListener(toEditTextTextWatcher);
	}

	private void reportError(String message) {
		Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
	}
}
