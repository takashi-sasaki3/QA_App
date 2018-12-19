package jp.techacademy.takashi.sasaki.qa_app;

import java.io.Serializable;

public class Answer implements Serializable {

    private String body;

    private String name;

    private String uid;

    private String answerUid;

    public Answer(String body, String name, String uid, String answerUid) {
        this.body = body;
        this.name = name;
        this.uid = uid;
        this.answerUid = answerUid;
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

    public String getAnswerUid() {
        return answerUid;
    }
}
