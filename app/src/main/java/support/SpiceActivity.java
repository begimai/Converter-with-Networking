package support;

import android.support.v7.app.AppCompatActivity;

import com.example.converter.network.FixerService;
import com.octo.android.robospice.SpiceManager;

public class SpiceActivity extends AppCompatActivity {

    private SpiceManager spiceManager = new SpiceManager(FixerService.class);

    @Override
    protected void onStart() {
        spiceManager.start(this);
        super.onStart();
    }

    @Override
    protected void onStop() {
        spiceManager.shouldStop();
        super.onStop();
    }

    protected SpiceManager getSpiceManager() {
        return spiceManager;
    }

}
