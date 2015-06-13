package com.curiousily.ipoli.io.event;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 6/13/15.
 */
public class NewQueryEvent {
    private final String query;

    public NewQueryEvent(String query) {
        this.query = query;
    }

    public String getQuery() {
        return query;
    }
}
