package com.droidrise.snaptext;

import java.util.ArrayList;

/**
 * Created by F10210C on 21.03.2017.
 */
public class SnapData {
    public static final String ID = "snapdata_id";

    private long id;
    private String content;
    private String source;
    public String signature;
    public String time;

    public SnapData(final String content, final String source) {
        id = System.currentTimeMillis();
        this.content = content;
        this.source = source;
    }

    public long getId() {
        return id;
    }

    public String getTitle() {
        return source != null ? source : "null";
    }

    public String getContent() {
        return content;
    }
}

