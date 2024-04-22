package me.robbyblue.mylauncher.search.dots;

import android.graphics.Color;

import me.robbyblue.mylauncher.files.icons.DotIconData;
import me.robbyblue.mylauncher.search.SearchActivity;
import me.robbyblue.mylauncher.search.SearchResult;

public abstract class DotSearchResult extends SearchResult {

    protected String prefix;

    public DotSearchResult(String name, String prefix) {
        super(name, new DotIconData(Color.parseColor("#EEEEEE")));
        this.prefix = prefix;
    }

    public String getQueryWithoutDot(SearchActivity activity) {
        return getQueryWithoutDot(activity.getSearchQuery());
    }

    public String getQueryWithoutDot(String query) {
        StringBuilder queryWithoutDot = new StringBuilder();
        for (String word : query.split(" ")) {
            if (word.equals(prefix + ".") || word.equals("." + prefix)) {
                continue;
            }
            queryWithoutDot.append(word).append(" ");
        }
        queryWithoutDot = new StringBuilder(queryWithoutDot.toString().trim());
        return queryWithoutDot.toString();
    }

    public boolean isDotInQuery(SearchActivity activity) {
        return isDotInQuery(activity.getSearchQuery());
    }

    public boolean isDotInQuery(String query) {
        for (String word : query.split(" ")) {
            if (word.equals(prefix + ".") || word.equals("." + prefix)) {
                return true;
            }
        }
        return false;
    }

    public String getPrefix() {
        return prefix;
    }

    @Override
    protected int getTextColor() {
        return Color.parseColor("#EEEEEE");
    }

}
