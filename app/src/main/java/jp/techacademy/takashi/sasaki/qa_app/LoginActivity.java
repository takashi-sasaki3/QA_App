package jp.techacademy.takashi.sasaki.qa_app;

import android.app.ProgressDialog;
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

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {

    EditText emailEditText;

    EditText passwordEditText;

    EditText nameEditText;

    ProgressDialog progressDialog;

    FirebaseAuth auth;

    OnCompleteListener<AuthResult> createAccountListener;

    OnCompleteListener<AuthResult> loginListener;

    DatabaseReference databaseReference;

    boolean isCreateAccount = false;

    private ChildEventListener favoriteEventListener = new ChildEventListener() {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            Log.d("QA_App", ":: FavoriteEventListener#onChildAdded :::::::::::::::::::::::");
            Log.d("QA_App", "favoriteQuestionId:" + dataSnapshot.getValue().toString());

            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit();
            editor.putString(Const.FAVORITE_QUESTIONS_KEY, dataSnapshot.getValue().toString());
            editor.commit();
        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {
        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {
        }

        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String s) {
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("QA_App", ":: LoginActivity#onCreate :::::::::::::::::::::::::::::::::::");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        databaseReference = FirebaseDatabase.getInstance().getReference();
        auth = FirebaseAuth.getInstance();

        createAccountListener = new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(Task<AuthResult> task) {
                Log.d("QA_App", ":: CreateAccountListener#onComplete :::::::::::::::::::::::::");
                if (task.isSuccessful()) {
                    String email = emailEditText.getText().toString();
                    String password = passwordEditText.getText().toString();
                    login(email, password);
                } else {
                    View view = findViewById(android.R.id.content);
                    Snackbar.make(view, "アカウント作成に失敗しました", Snackbar.LENGTH_LONG).show();
                    progressDialog.dismiss();
                }
            }
        };

        loginListener = new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(Task<AuthResult> task) {
                Log.d("QA_App", ":: LoginListener#onComplete :::::::::::::::::::::::::::::::::");
                if (task.isSuccessful()) {
                    FirebaseUser user = auth.getCurrentUser();
                    DatabaseReference userReference = databaseReference.child(Const.USERS_PATH).child(user.getUid());
                    if (isCreateAccount) {
                        String name = nameEditText.getText().toString();
                        Map<String, String> data = new HashMap<String, String>();
                        data.put("name", name);
                        userReference.setValue(data);
                        saveName(name);
                    } else {
                        userReference.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                Map data = (Map) dataSnapshot.getValue();
                                saveName((String) data.get("name"));
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                            }
                        });
                    }
                    Log.d("QA_App", "user id:" + user.getUid());
                    databaseReference.child(Const.FAVORITE_PATH).child(user.getUid()).addChildEventListener(favoriteEventListener);
                } else {
                    View view = findViewById(android.R.id.content);
                    Snackbar.make(view, "ログインに失敗しました", Snackbar.LENGTH_LONG).show();
                }
                progressDialog.dismiss();
                finish();
            }
        };

        setTitle("ログイン");

        emailEditText = findViewById(R.id.emailText);
        emailEditText.setText("t.sasaki.fujiyacamera@gmail.com");
        passwordEditText = findViewById(R.id.passwordText);
        passwordEditText.setText("password");
        nameEditText = findViewById(R.id.nameText);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("処理中…");

        Button createButton = findViewById(R.id.createButton);
        createButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("QA_App", ":: createButton#onClick :::::::::::::::::::::::::::::::::::::");
                InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);

                String email = emailEditText.getText().toString();
                String password = passwordEditText.getText().toString();
                String name = nameEditText.getText().toString();

                if (email.length() != 0 && password.length() >= 6 && name.length() != 0) {
                    isCreateAccount = true;
                    createAccount(email, password);
                } else {
                    Snackbar.make(view, "正しく入力して下さい", Snackbar.LENGTH_LONG).show();
                }
            }
        });

        Button loginButton = findViewById(R.id.loginButton);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("QA_App", ":: loginButton#onClick ::::::::::::::::::::::::::::::::::::::");
                InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);

                String email = emailEditText.getText().toString();
                String password = passwordEditText.getText().toString();

                if (email.length() != 0 && password.length() >= 6) {
                    isCreateAccount = false;
                    login(email, password);
                } else {
                    Snackbar.make(view, "正しく入力して下さい", Snackbar.LENGTH_LONG).show();
                }
            }
        });
    }

    private void createAccount(String email, String password) {
        progressDialog.show();
        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(createAccountListener);
    }

    private void login(String email, String password) {
        progressDialog.show();
        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(loginListener);
    }

    private void saveName(String name) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(Const.NAME_KEY, name);
        editor.commit();
    }
}
