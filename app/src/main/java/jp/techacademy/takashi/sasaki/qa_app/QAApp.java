package jp.techacademy.takashi.sasaki.qa_app;

import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class QAApp extends Application {

    @Override
    public void onCreate() {
        Log.d("QA_App", ":: QAApp#onCreate :::::::::::::::::::::::::::::::::::::::::::");
        super.onCreate();

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            Log.d("QA_App", "user id:" + user.getUid());
            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
            databaseReference.child(Const.FAVORITE_PATH).child(user.getUid())
                    .addChildEventListener(new ChildEventListener() {
                        @Override
                        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                            Log.d("QA_App", ":: UserFavoriteEventListener#onChildAdded :::::::::::::::::::");
                            Log.d("QA_App", "favoriteQuestionId:" + dataSnapshot.getValue().toString());

                            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit();
                            editor.putString(Const.FAVORITE_QUESTIONS_KEY, dataSnapshot.getValue().toString());
                            editor.commit();
                        }

                        @Override
                        public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                            Log.d("QA_App", ":: UserFavoriteEventListener#onChildChanged :::::::::::::::::");
                            Log.d("QA_App", "favoriteQuestionId:" + dataSnapshot.getValue().toString());

                            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit();
                            editor.putString(Const.FAVORITE_QUESTIONS_KEY, dataSnapshot.getValue().toString());
                            editor.commit();
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
                    });
        }
    }
}
