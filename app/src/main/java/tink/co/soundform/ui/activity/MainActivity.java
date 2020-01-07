package tink.co.soundform.ui.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;

import tink.co.soundform.App;
import tink.co.soundform.R;
import tink.co.soundform.callback.BackCallback;
import tink.co.soundform.ui.fragment.MusicListFragment;
import tink.co.soundform.util.Util;

import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static tink.co.soundform.Config.MUSIC_LIST;
import static tink.co.soundform.Config.RECORD;

public class MainActivity extends AppCompatActivity {

    private Util util;
    private BackCallback backCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        App.setActivity(this);
        util = Util.getInstance();
        initApp();
    }

    public void setBackCallback(BackCallback backCallback) {
        this.backCallback = backCallback;
    }

    @Override
    public void onBackPressed() {

        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            if (getSupportFragmentManager().getBackStackEntryAt(getSupportFragmentManager().getBackStackEntryCount() - 1).getName().equalsIgnoreCase(MUSIC_LIST)) {
                if (backCallback != null) {
                    backCallback.onBackPressed();
                }
            } else if (getSupportFragmentManager().getBackStackEntryAt(getSupportFragmentManager().getBackStackEntryCount() - 1).getName().equalsIgnoreCase(RECORD)) {
                if (backCallback != null) {
                    backCallback.onBackPressed();
                }
            } else {
                super.onBackPressed();
            }
        } else {
            finish();
        }
    }

    private void initApp() {
        if (ActivityCompat.checkSelfPermission(this, WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            setContentView(R.layout.activity_main);
            util.showFragment(new MusicListFragment(), MUSIC_LIST);
        } else {
            util.permissionsRequestDialog(WRITE_EXTERNAL_STORAGE);
        }

    }
}
