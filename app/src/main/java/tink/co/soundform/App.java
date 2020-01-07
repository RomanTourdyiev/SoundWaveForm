package tink.co.soundform;

import android.app.Application;
import android.content.Context;

import tink.co.soundform.ui.activity.MainActivity;

/**
 * Created by Tourdyiev Roman on 2020-01-04.
 */
public class App extends Application {

    private static Application instance;
    private static MainActivity activity;

    public static Application getInstance() {
        return instance;
    }

    public static Context getContext() {
        return instance.getApplicationContext();
    }

    public static void setActivity(MainActivity mainActivity) {
        activity = mainActivity;
    }

    public static MainActivity getActivity() {
        return activity;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
    }

}
