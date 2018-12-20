package jp.techacademy.takashi.sasaki.qa_app;

import android.content.Intent;
import android.os.Bundle;
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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class QuestionDetailActivity extends AppCompatActivity {

    private ListView listView;

    private ToggleButton favoriteToggleButton;

    private Question question;

    private QuestionDetailListAdapter questionDetailListAdapter;

    private DatabaseReference reference;

    private DatabaseReference answersReference;

    private DatabaseReference favoritesReference;

    private ChildEventListener favoritesEventListener = new DefaultChildEventListener() {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            Log.d("QA_App", ":: FavoritesEventListener#onChildAdded ::::::::::::::::::::::");
            favoriteToggleButton.setChecked(true);
        }
    };

    private CompoundButton.OnCheckedChangeListener favoriteCheckedChangeListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
            Log.d("QA_App", ":: FavoriteCheckedChangeListener#onCheckedChanged :::::::::::");
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if ((compoundButton.getId() == R.id.favoriteToggleButton) && user != null) {
                DatabaseReference favoriteReference = reference.child(Const.FAVORITE_PATH).child(user.getUid()).child(question.getQuestionUid());
                favoriteReference.removeEventListener(favoritesEventListener);
                if (isChecked) {
                    Map<String, String> data = new HashMap<>();
                    data.put("genre", String.valueOf(question.getGenre()));
                    favoriteReference.setValue(data);
                } else {
                    favoriteReference.removeValue();
                }
            }
        }
    };

    private ChildEventListener answersEventListener = new DefaultChildEventListener() {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            HashMap map = (HashMap) dataSnapshot.getValue();
            String answerUid = dataSnapshot.getKey();
            for (Answer answer : question.getAnswers()) {
                if (answerUid.equals(answer.getAnswerUid())) {
                    return;
                }
            }
            Answer answer = new Answer((String) map.get("body"), (String) map.get("name"), (String) map.get("uid"), answerUid);
            question.getAnswers().add(answer);
            questionDetailListAdapter.notifyDataSetChanged();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("QA_App", ":: QuestionDetailActivity#onCreate ::::::::::::::::::::::::::");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_question_detail);

        question = (Question) getIntent().getExtras().get("question");
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

        reference = FirebaseDatabase.getInstance().getReference();
        answersReference = reference.child(Const.CONTENTS_PATH).child(String.valueOf(question.getGenre())).child(question.getQuestionUid()).child(Const.ANSWERS_PATH);
        answersReference.addChildEventListener(answersEventListener);

        favoritesReference = reference.child(Const.FAVORITE_QUESTIONS_KEY).child(user.getUid()).child(question.getQuestionUid());
        favoritesReference.addChildEventListener(favoritesEventListener);
    }

    @Override
    protected void onResume() {
        Log.d("QA_App", ":: QuestionDetailActivity#onResume ::::::::::::::::::::::::::");
        super.onResume();
    }
}
