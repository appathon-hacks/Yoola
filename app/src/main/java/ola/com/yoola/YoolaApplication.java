package ola.com.yoola;

import android.app.Application;

import uk.co.chrisjenx.calligraphy.CalligraphyConfig;

/**
 * Created by Maharshi on 07-02-2015.
 */

public class YoolaApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                        .setDefaultFontPath("fonts/Roboto-ThinItalic.ttf")
                        .setFontAttrId(R.attr.fontPath)
                        .build()
        );
    }
}