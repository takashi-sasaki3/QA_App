package jp.techacademy.takashi.sasaki.qa_app;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class SettingActivity extends AppCompatActivity {

    private EditText nameEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("QA_App", ":: SettingActivity#onCreate :::::::::::::::::::::::::::::::::");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        setTitle("設定");

        nameEditText = findViewById(R.id.nameEditText);
        nameEditText.setText(PreferenceManager.getDefaultSharedPreferences(this).getString(Const.NAME_KEY, ""));

        Button changeButton = findViewById(R.id.changeButton);
        changeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                InputMethodManager im = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                im.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);

                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if (user == null) {
                    Snackbar.make(view, "ログインしていません", Snackbar.LENGTH_LONG).show();
                    return;
                }

                String name = nameEditText.getText().toString();
                DatabaseReference userReference = FirebaseDatabase.getInstance().getReference().child(Const.USERS_PATH).child(user.getUid());
                Map<String, String> data = new HashMap<>();
                data.put("name", name);
                userReference.setValue(data);

                SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit();
                editor.putString(Const.NAME_KEY, name);
                editor.commit();

                Snackbar.make(view, "表示名を変更しました", Snackbar.LENGTH_LONG).show();
            }
        });

        Button logoutButton = findViewById(R.id.logoutButton);
        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseAuth.getInstance().signOut();
                nameEditText.setText("");

                SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit();
                editor.putString(Const.NAME_KEY, "");
                editor.commit();

                Snackbar.make(view, "ログアウトしました", Snackbar.LENGTH_LONG).show();
            }
        });
    }
}
