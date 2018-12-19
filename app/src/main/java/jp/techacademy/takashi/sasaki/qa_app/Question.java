package jp.techacademy.takashi.sasaki.qa_app;

import java.io.Serializable;
import java.util.ArrayList;

public class Question implements Serializable {

    private String title;

    private String body;

    private String name;

    private String uid;

    private String questionUid;

    private int genre;

    private byte[] bitmapArray;

    private ArrayList<Answer> answers;

    public Question(String title, String body, String name, String uid, String questionUid, int genre, byte[] bitmapArray, ArrayList<Answer> answers) {
        this.title = title;
        this.body = body;
        this.name = name;
        this.uid = uid;
        this.questionUid = questionUid;
        this.genre = genre;
        this.bitmapArray = bitmapArray.clone();
        this.answers = answers;
    }

    public String getTitle() {
        return title;
    }

    public String getBody() {
        return body;
    }

    public String getName() {
        return name;
    }

    public String getUid() {
        return uid;
    }

    public String getQuestionUid() {
        return questionUid;
    }

    public int getGenre() {
        return genre;
    }

    public byte[] getImageBytes() {
        return bitmapArray;
    }

    public ArrayList<Answer> getAnswers() {
        return answers;
    }

}
