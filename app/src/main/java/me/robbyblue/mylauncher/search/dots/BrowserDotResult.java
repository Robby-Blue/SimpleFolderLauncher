package me.robbyblue.mylauncher.search.dots;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;

import me.robbyblue.mylauncher.files.icons.DotIconData;
import me.robbyblue.mylauncher.search.SearchActivity;

public class BrowserDotResult extends DotSearchResult {


    public BrowserDotResult() {
        super("Browser", "b", new DotIconData(Color.parseColor("#f87702")));
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

    @Override
    protected String getDiplayName(SearchActivity activity) {
        String query = getQueryWithoutDot(activity);
        if (query.length() == 0) return this.getName();

        if (query.equals("localhost") ||
                (query.contains(".") && !query.contains(" ")) ||
                query.startsWith("http")) {

            query = removePrefix(query, "https");
            query = removePrefix(query, "http");
            query = removePrefix(query, ":");
            query = removePrefix(query, "/");
            query = removePrefix(query, "/");

            return "open " + query;
        } else {
            return "search '" + query + "'";
        }
    }

    private String removePrefix(String input, String prefix){
        if (input.startsWith(prefix)) {
            return input.substring(prefix.length());
        }
        return input;
    }

}
