package jp.techacademy.takashi.sasaki.qa_app;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private Toolbar toolbar;

    private NavigationView navigationView;

    private int genre = 0;

    private DatabaseReference databaseReference;

    private DatabaseReference questionDatabaseReference;

    private ListView listView;

    private ArrayList<Question> questions;

    private QuestionsListAdapter questionsListAdapter;

    private ChildEventListener questionsEventListener = new ChildEventListener() {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            Log.d("QA_App", ":: QuestionsEventListener#onChildAdded ::::::::::::::::::::::");

            HashMap question = (HashMap) dataSnapshot.getValue();
            String title = (String) question.get("title");
            String body = (String) question.get("body");
            String name = (String) question.get("name");
            String uid = (String) question.get("uid");
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

            questions.add(new Question(title, body, name, uid, dataSnapshot.getKey(), genre, bytes, answers));
            questionsListAdapter.notifyDataSetChanged();
        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            Log.d("QA_App", ":: MainActivity#onChildChanged ::::::::::::::::::::::::::::::");
            HashMap map = (HashMap) dataSnapshot.getValue();
            for (Question question : questions) {
                if (dataSnapshot.getKey().equals(question.getQuestionUid())) {
                    question.getAnswers().clear();
                    HashMap answerMap = (HashMap) map.get("answers");
                    if (answerMap != null) {
                        for (Object key : answerMap.keySet()) {
                            HashMap temp = (HashMap) answerMap.get(key);
                            String answerBody = (String) temp.get("body");
                            String answerName = (String) temp.get("name");
                            String answerUid = (String) temp.get("uid");
                            question.getAnswers().add(new Answer(answerBody, answerName, answerUid, (String) key));
                        }
                    }
                    questionsListAdapter.notifyDataSetChanged();
                }
            }
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
        Log.d("QA_App", ":: MainActivity#onCreate ::::::::::::::::::::::::::::::::::::");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (genre == 0) {
                    Snackbar.make(view, "ジャンルを選択して下さい", Snackbar.LENGTH_LONG).show();
                    return;
                }
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if (user == null) {
                    Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                    startActivity(intent);
                } else {
                    Intent intent = new Intent(getApplicationContext(), QuestionSendActivity.class);
                    intent.putExtra("genre", genre);
                    startActivity(intent);
                }
            }
        });

        DrawerLayout drawer = findViewById(R.id.drawerLayout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.app_name, R.string.app_name);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        navigationView = findViewById(R.id.navigationView);
        navigationView.setNavigationItemSelectedListener(this);

        databaseReference = FirebaseDatabase.getInstance().getReference();

        listView = findViewById(R.id.listView);
        questionsListAdapter = new QuestionsListAdapter(this);
        questions = new ArrayList<Question>();
        questionsListAdapter.notifyDataSetChanged();

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getApplicationContext(), QuestionDetailActivity.class);
                intent.putExtra("question", questions.get(position));
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onResume() {
        Log.d("QA_App", ":: MainActivity#onResume ::::::::::::::::::::::::::::::::::::");
        Log.d("QA_App", "genre:" + genre);
        super.onResume();

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            navigationView.getMenu().findItem(R.id.navFavorite).setVisible(false);
        } else {
            navigationView.getMenu().findItem(R.id.navFavorite).setVisible(true);
        }

        if (genre == 0) {
            onNavigationItemSelected(navigationView.getMenu().getItem(1));
        } else {
            onNavigationItemSelected(navigationView.getMenu().getItem(genre));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.actionSettings) {
            Intent intent = new Intent(getApplicationContext(), SettingActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        Log.d("QA_App", ":: MainActivity#onNavigationItemSelected ::::::::::::::::::::");
        DrawerLayout drawer = findViewById(R.id.drawerLayout);
        drawer.closeDrawer(GravityCompat.START);

        questions.clear();
        questionsListAdapter.setQuestions(questions);
        listView.setAdapter(questionsListAdapter);
        if (questionDatabaseReference != null) {
            questionDatabaseReference.removeEventListener(questionsEventListener);
        }

        int id = item.getItemId();
        if (id == R.id.navFavorite) {
            Intent intent = new Intent(getApplicationContext(), FavoriteActivity.class);
            startActivity(intent);
        } else {
            if (id == R.id.navHobby) {
                toolbar.setTitle("趣味");
                genre = 1;
            } else if (id == R.id.navLife) {
                toolbar.setTitle("生活");
                genre = 2;
            } else if (id == R.id.navHealth) {
                toolbar.setTitle("健康");
                genre = 3;
            } else if (id == R.id.navComputer) {
                toolbar.setTitle("コンピューター");
                genre = 4;
            }
            questionDatabaseReference = databaseReference.child(Const.CONTENTS_PATH).child(String.valueOf(genre));
            questionDatabaseReference.addChildEventListener(questionsEventListener);
        }
        return true;
    }
}
