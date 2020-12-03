package com.app.sqs.model;

import java.util.Date;

public class Message {

    private String content;
    private Date sentDate;

    public static Message of(String content, Date sentDate) {
        Message msg = new Message();
        msg.setContent(content);
        msg.setSentDate(sentDate);
        return msg;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Date getSentDate() {
        return sentDate;
    }

    public void setSentDate(Date sentDate) {
        this.sentDate = sentDate;
    }

    @Override
    public String toString() {
        return "Message{" +
                "content='" + content + '\'' +
                ", sentDate=" + sentDate +
                '}';
    }
}
