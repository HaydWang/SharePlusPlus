package com.droidrise.snaptext.model;

import io.realm.RealmObject;

/**
 * Created by Hai on 4/25/17.
 */
public class ClipItem extends RealmObject {
    private String clip;
    private String source;
    private long date;

    public void setClip(String text) {
        clip = text;
    }

    public String getClip() {
        return clip;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getSource() {
        return source;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public long getData() {
        return date;
    }
}
