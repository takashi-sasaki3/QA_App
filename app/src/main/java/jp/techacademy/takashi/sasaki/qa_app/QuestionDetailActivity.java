package jp.techacademy.takashi.sasaki.qa_app;

import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.ToggleButton;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

public class QuestionDetailActivity extends AppCompatActivity {

    private ListView listView;

    private ToggleButton favoriteToggleButton;

    private Question question;

    private QuestionDetailListAdapter questionDetailListAdapter;

    private DatabaseReference databaseReference;

    private DatabaseReference answerDatabaseReference;

    private CompoundButton.OnCheckedChangeListener favoriteCheckedChangeListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
            Log.d("QA_App", ":: FavoriteCheckedChangeListener#onCheckedChanged :::::::::::");
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if ((compoundButton.getId() == R.id.favoriteToggleButton) && user != null) {
                String favoriteQuestionId = PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
                        .getString(Const.FAVORITE_QUESTIONS_KEY, "");

                List<String> favoriteQuestionIds = new ArrayList<>();
                if (!favoriteQuestionId.equals("")) {
                    favoriteQuestionIds = new ArrayList<>(Arrays.asList(favoriteQuestionId.split(",")));
                }

                List<String> results = new ArrayList<>();
                if (isChecked) {
                    favoriteQuestionIds.add(question.getQuestionUid());
                    results = new ArrayList<>(new LinkedHashSet<>(favoriteQuestionIds));
                } else {
                    for (String id : favoriteQuestionIds) {
                        if (!id.equals(question.getQuestionUid())) {
                            results.add(id);
                        }
                    }
                }

                favoriteQuestionId = "";
                for (int i = 0; i < results.size(); i++) {
                    favoriteQuestionId += results.get(i);
                    favoriteQuestionId += (i != results.size() - 1) ? "," : "";
                }

                DatabaseReference userReference = databaseReference.child(Const.FAVORITE_PATH).child(user.getUid());
                Map<String, String> data = new HashMap<>();
                data.put("questionId", favoriteQuestionId);
                userReference.setValue(data);
            }
        }
    };

    private ChildEventListener answersEventListener = new ChildEventListener() {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            HashMap map = (HashMap) dataSnapshot.getValue();
            String answerUid = dataSnapshot.getKey();
            for (Answer answer : question.getAnswers()) {
                if (answerUid.equals(answer.getAnswerUid())) {
                    return;
                }
            }
            Answer answer = new Answer((String) map.get("body")
                    , (String) map.get("name"), (String) map.get("uid"), answerUid);
            question.getAnswers().add(answer);
            questionDetailListAdapter.notifyDataSetChanged();
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
        Log.d("QA_App", ":: QuestionDetailActivity#onCreate ::::::::::::::::::::::::::");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_question_detail);

        Bundle extras = getIntent().getExtras();
        question = (Question) extras.get("question");

        setTitle(question.getTitle());

        listView = findViewById(R.id.listView);
        questionDetailListAdapter = new QuestionDetailListAdapter(this, question);
        listView.setAdapter(questionDetailListAdapter);
        questionDetailListAdapter.notifyDataSetChanged();

        favoriteToggleButton = findViewById(R.id.favoriteToggleButton);
        favoriteToggleButton.setOnCheckedChangeListener(favoriteCheckedChangeListener);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            favoriteToggleButton.setVisibility(View.GONE);
        } else {
            // TODO: 2018/12/16 お気に入り登録／未登録で文言を変化させる 
        }

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if (user == null) {
                    Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                    startActivity(intent);
                } else {
                    Intent intent = new Intent(getApplicationContext(), AnswerSendActivity.class);
                    intent.putExtra("question", question);
                    startActivity(intent);
                }
            }
        });

        databaseReference = FirebaseDatabase.getInstance().getReference();
        answerDatabaseReference = databaseReference.child(Const.CONTENTS_PATH)
                .child(String.valueOf(question.getGenre())).child(question.getQuestionUid()).child(Const.ANSWERS_PATH);
        answerDatabaseReference.addChildEventListener(answersEventListener);
    }
}
