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

public class QuestionDetailListAdapter extends BaseAdapter {

    private final static int TYPE_QUESTION = 0;

    private final static int TYPE_ANSWER = 1;

    private LayoutInflater layoutInflater;

    private Question question;

    public QuestionDetailListAdapter(Context context, Question question) {
        layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.question = question;
    }

    @Override
    public int getCount() {
        return 1 + question.getAnswers().size();
    }

    @Override
    public int getItemViewType(int position) {
        return (position == 0) ? TYPE_QUESTION : TYPE_ANSWER;
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public Object getItem(int i) {
        return question;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (getItemViewType(position) == TYPE_QUESTION) {
            if (convertView == null) {
                convertView = layoutInflater.inflate(R.layout.list_question_detail, parent, false);
            }
            TextView bodyTextView = convertView.findViewById(R.id.bodyTextView);
            bodyTextView.setText(question.getBody());
            TextView nameTextView = convertView.findViewById(R.id.nameTextView);
            nameTextView.setText(question.getName());

            byte[] bytes = question.getImageBytes();
            if (bytes.length != 0) {
                Bitmap image = BitmapFactory.decodeByteArray(bytes, 0, bytes.length)
                        .copy(Bitmap.Config.ARGB_8888, true);
                ImageView imageView = convertView.findViewById(R.id.imageView);
                imageView.setImageBitmap(image);
            }
        } else {
            if (convertView == null) {
                convertView = layoutInflater.inflate(R.layout.list_answer, parent, false);
            }
            Answer answer = question.getAnswers().get(position - 1);
            TextView bodyTextView = convertView.findViewById(R.id.bodyTextView);
            bodyTextView.setText(answer.getBody());
            TextView nameTextView = convertView.findViewById(R.id.nameTextView);
            nameTextView.setText(answer.getName());
        }
        return convertView;
    }
}
