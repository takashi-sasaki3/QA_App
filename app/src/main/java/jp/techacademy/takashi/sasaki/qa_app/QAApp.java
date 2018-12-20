package jp.techacademy.takashi.sasaki.qa_app;

import android.app.Application;
import android.util.Log;

public class QAApp extends Application {

    @Override
    public void onCreate() {
        Log.d("QA_App", ":: QAApp#onCreate :::::::::::::::::::::::::::::::::::::::::::");
        super.onCreate();
    }
}
