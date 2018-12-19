package jp.techacademy.takashi.sasaki.qa_app;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

public class QuestionsListAdapter extends BaseAdapter {

    private LayoutInflater layoutInflater;

    private ArrayList<Question> questions;

    public QuestionsListAdapter(Context context) {
        layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public void setQuestions(ArrayList<Question> questions) {
        this.questions = questions;
    }

    @Override
    public int getCount() {
        return questions.size();
    }

    @Override
    public Object getItem(int i) {
        return questions.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.list_questions, parent, false);
        }

        TextView titleTextView = convertView.findViewById(R.id.titleTextView);
        titleTextView.setText(questions.get(position).getTitle());

        TextView nameTextView = convertView.findViewById(R.id.nameTextView);
        nameTextView.setText(questions.get(position).getName());

        TextView responseTextView = convertView.findViewById(R.id.responseTextView);
        int responseNum = questions.get(position).getAnswers().size();
        responseTextView.setText(String.valueOf(responseNum));

        byte[] bytes = questions.get(position).getImageBytes();
        if (bytes.length != 0) {
            Bitmap image = BitmapFactory.decodeByteArray(bytes, 0, bytes.length)
                    .copy(Bitmap.Config.ARGB_8888, true);
            ImageView imageView = convertView.findViewById(R.id.imageView);
            imageView.setImageBitmap(image);
        }

        return convertView;
    }
}
