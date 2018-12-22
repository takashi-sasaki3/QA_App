package jp.techacademy.takashi.sasaki.qa_app;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class AnswerSendActivity extends AppCompatActivity implements View.OnClickListener, DatabaseReference.CompletionListener {

    private EditText answerEditText;

    private Question question;

    private ProgressDialog progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_answer_send);

        Bundle extras = getIntent().getExtras();
        question = (Question) extras.get("question");

        setTitle("回答作成");

        answerEditText = findViewById(R.id.answerEditText);
        progress = new ProgressDialog(this);
        progress.setMessage("投稿中…");

        Button sendButton = findViewById(R.id.sendButton);
        sendButton.setOnClickListener(this);
    }

    @Override
    public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
        progress.dismiss();
        if (databaseError == null) {
            finish();
        } else {
            Snackbar.make(findViewById(android.R.id.content), "投稿に失敗しました", Snackbar.LENGTH_LONG).show();
        }
    }

    @Override
    public void onClick(View view) {
        InputMethodManager im = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        im.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        DatabaseReference answerReference = reference.child(Const.CONTENTS_PATH)
                .child(String.valueOf(question.getGenre())).child(question.getQuestionUid()).child(Const.ANSWERS_PATH);

        Map<String, String> data = new HashMap<>();
        data.put("uid", FirebaseAuth.getInstance().getCurrentUser().getUid());

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String name = preferences.getString(Const.NAME_KEY, "");
        data.put("name", name);

        String answer = answerEditText.getText().toString();
        if (answer.length() == 0) {
            Snackbar.make(view, "回答を入力して下さい", Snackbar.LENGTH_LONG).show();
            return;
        }
        data.put("body", answer);

        progress.show();
        answerReference.push().setValue(data, this);
    }
}
