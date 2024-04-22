package me.robbyblue.mylauncher.search.dots;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;

import me.robbyblue.mylauncher.search.SearchActivity;

public class BrowserDotResult extends DotSearchResult {


    public BrowserDotResult() {
        super("Browser", "b");
    }

    @Override
    protected int getTextColor() {
        return Color.parseColor("#EEEEEE");
    }

    @Override
    protected void open(SearchActivity activity) {
        String query = getQueryWithoutDot(activity);

        if (query.equals("localhost") ||
                (query.contains(".") && !query.contains(" ")) ||
                query.startsWith("http")) {
            // query is link, use as is
            // maybe add https
            if (!query.startsWith("http")) {
                query = "https://" + query;
            }
        } else {
            // search for it on ddg
            query = "https://duckduckgo.com/?q=" + query;
        }

        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(query));
        activity.startActivity(intent);
    }

}
