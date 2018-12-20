package jp.techacademy.takashi.sasaki.qa_app;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;

public class FavoriteActivity extends AppCompatActivity {

    private DatabaseReference reference;

    private DatabaseReference favoritesReference;

    private DatabaseReference questionsReference;

    private ListView listView;

    private ArrayList<Question> questions;

    private QuestionsListAdapter questionsListAdapter;

    private ChildEventListener favoritesEventListener = new DefaultChildEventListener() {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            Log.d("QA_App", ":: FavoritesEventListener#onChildAdded ::::::::::::::::::::::");

            HashMap data = (HashMap) dataSnapshot.getValue(); // {genre=2}
            questionsReference = reference.child(Const.CONTENTS_PATH).child((String) data.get("genre")).child(dataSnapshot.getKey()); // -LTqFS2ZXYNNlvN64qSO
            questionsReference.addListenerForSingleValueEvent(new DefaultValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Log.d("QA_App", ":: onDataChange :::::::::::::::::::::::::::::::::::::::::::::");
                    //Log.d("QA_App", "key:" + dataSnapshot.getKey()); // -LTqFS2ZXYNNlvN64qSO
                    //Log.d("QA_App", "value:" + dataSnapshot.getValue()); // {name=, answers={-LU9Bu6-dD5jHKPGC7n0={name=佐々木, uid=UB56iOhBkLYAtEbIZIznxuhHcTW2, body=生活の質問の回答}}, uid=UB56iOhBkLYAtEbIZIznxuhHcTW2, body=生活の質問, title=生活の質問, image=/9j/4AAQSkZJRgABAQAAAQABAAD/2wBDAAYEBQYFBAYGBQYHBwYIChAKCgkJChQODwwQFxQYGBcU...}

                    HashMap question = (HashMap) dataSnapshot.getValue();
                    String title = (String) question.get("title");
                    String body = (String) question.get("body");
                    String name = (String) question.get("name");
                    String uid = (String) question.get("uid");
                    String genre = (String) question.get("genre");
                    String imageString = (String) question.get("image");
                    byte[] bytes = (imageString != null) ? Base64.decode(imageString, Base64.DEFAULT) : new byte[0];

                    ArrayList<Answer> answers = new ArrayList<>();
                    HashMap answer = (HashMap) question.get("answers");
                    if (answer != null) {
                        for (Object key : answer.keySet()) {
                            HashMap temp = (HashMap) answer.get(key);
                            String answerBody = (String) temp.get("body");
                            String answerName = (String) temp.get("name");
                            String answerUid = (String) temp.get("uid");
                            answers.add(new Answer(answerBody, answerName, answerUid, (String) key));
                        }
                    }

                    questions.add(new Question(title, body, name, uid, dataSnapshot.getKey(), Integer.valueOf(genre), bytes, answers));
                    questionsListAdapter.notifyDataSetChanged();
                }
            });
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("QA_App", ":: FavoriteActivity#onCreate ::::::::::::::::::::::::::::::::");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorite);
        setTitle("お気に入り");

        questionsListAdapter = new QuestionsListAdapter(this);
        questions = new ArrayList<>();
        questionsListAdapter.setQuestions(questions);

        listView = findViewById(R.id.listView);
        listView.setAdapter(questionsListAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getApplicationContext(), QuestionDetailActivity.class);
                intent.putExtra("question", questions.get(position));
                startActivity(intent);
            }
        });

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        reference = FirebaseDatabase.getInstance().getReference();
        favoritesReference = reference.child(Const.FAVORITE_QUESTIONS_KEY).child(user.getUid());
        favoritesReference.addChildEventListener(favoritesEventListener);
    }
}
