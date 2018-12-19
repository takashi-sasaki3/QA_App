package jp.techacademy.takashi.sasaki.qa_app;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class QuestionSendActivity extends AppCompatActivity implements View.OnClickListener, DatabaseReference.CompletionListener {

    private static final int PERMISSIONS_REQUEST_CODE = 100;

    private static final int CHOOSER_REQUEST_CODE = 100;

    private ProgressDialog progress;

    private EditText titleEditText;

    private EditText bodyEditText;

    private ImageView imageView;

    private Button sendButton;

    private int genre;

    private Uri pictureUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("QA_App", ":: QuestionSendActivity#onCreate ::::::::::::::::::::::::::::");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_question_send);

        Bundle extras = getIntent().getExtras();
        genre = extras.getInt("genre");

        setTitle("質問作成");

        titleEditText = findViewById(R.id.titleEditText);
        bodyEditText = findViewById(R.id.bodyEditText);

        sendButton = findViewById(R.id.sendButton);
        sendButton.setOnClickListener(this);

        imageView = findViewById(R.id.imageView);
        imageView.setOnClickListener(this);

        progress = new ProgressDialog(this);
        progress.setMessage("投稿中…");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d("QA_App", ":: QuestionSendActivity#onActivityResult ::::::::::::::::::::");
        if (requestCode == CHOOSER_REQUEST_CODE) {
            if (resultCode != RESULT_OK) {
                if (pictureUri != null) {
                    getContentResolver().delete(pictureUri, null, null);
                    pictureUri = null;
                }
                return;
            }

            Uri uri = (data == null || data.getData() == null) ? pictureUri : data.getData();
            Bitmap image;
            try {
                InputStream is = getContentResolver().openInputStream(uri);
                image = BitmapFactory.decodeStream(is);
                is.close();
            } catch (Exception e) {
                return;
            }

            int imageWidth = image.getWidth();
            int imageHeight = image.getHeight();
            float scale = Math.min((float) 500 / imageWidth, (float) 500 / imageHeight);

            Matrix matrix = new Matrix();
            matrix.postScale(scale, scale);

            Bitmap resizedImage = Bitmap.createBitmap(image, 0, 0, imageWidth, imageHeight, matrix, true);
            imageView.setImageBitmap(resizedImage);
            pictureUri = null;
        }
    }

    @Override
    public void onClick(View view) {
        Log.d("QA_App", ":: QuestionSendActivity#onClick :::::::::::::::::::::::::::::");
        if (view == imageView) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    showChooser();
                } else {
                    requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSIONS_REQUEST_CODE);
                    return;
                }
            } else {
                showChooser();
            }
        } else if (view == sendButton) {
            InputMethodManager im = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            im.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);

            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
            DatabaseReference genreReference = databaseReference.child(Const.CONTENTS_PATH).child(String.valueOf(genre));

            Map<String, String> data = new HashMap<>();
            data.put("uid", FirebaseAuth.getInstance().getCurrentUser().getUid());

            String title = titleEditText.getText().toString();
            String body = bodyEditText.getText().toString();

            if (title.length() == 0) {
                Snackbar.make(view, "タイトルを入力して下さい", Snackbar.LENGTH_LONG).show();
                return;
            }
            if (body.length() == 0) {
                Snackbar.make(view, "質問を入力して下さい", Snackbar.LENGTH_LONG).show();
                return;
            }

            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
            String name = sp.getString(Const.NAME_KEY, "");
            data.put("title", title);
            data.put("body", body);
            data.put("name", name);

            BitmapDrawable drawable = (BitmapDrawable) imageView.getDrawable();
            if (drawable != null) {
                Bitmap bitmap = drawable.getBitmap();
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos);
                String bitmapString = Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT);
                data.put("image", bitmapString);
            }

            genreReference.push().setValue(data, this);
            progress.show();
        }
    }

    @Override
    public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
        Log.d("QA_App", ":: QuestionSendActivity#onComplete ::::::::::::::::::::::::::");
        progress.dismiss();
        if (databaseError == null) {
            finish();
        } else {
            Snackbar.make(findViewById(android.R.id.content), "投稿に失敗しました", Snackbar.LENGTH_LONG).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.d("QA_App", ":: QuestionSendActivity#onRequestPermissionsResult ::::::::::");
        switch (requestCode) {
            case PERMISSIONS_REQUEST_CODE: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    showChooser();
                }
                return;
            }
        }
    }

    private void showChooser() {
        Log.d("QA_App", ":: QuestionSendActivity#showChooser :::::::::::::::::::::::::");
        Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        galleryIntent.addCategory(Intent.CATEGORY_OPENABLE);

        String fileName = System.currentTimeMillis() + ".jpg";
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, fileName);
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
        pictureUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, pictureUri);

        Intent chooserIntent = Intent.createChooser(galleryIntent, "画像を取得");

        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[]{cameraIntent});

        startActivityForResult(chooserIntent, CHOOSER_REQUEST_CODE);
    }
}
