package me.robbyblue.mylauncher.search.dots;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;

import me.robbyblue.mylauncher.files.icons.DotIconData;
import me.robbyblue.mylauncher.search.SearchActivity;

public class MapsDotResult extends DotSearchResult {


    public MapsDotResult() {
        super("Maps", "m", new DotIconData(Color.parseColor("#097f42")));
    }

    @Override
    protected void open(SearchActivity activity) {
        String query = getQueryWithoutDot(activity);

        query = "geo:0,0?q=" + query;

        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(query));
        activity.startActivity(intent);
    }

    @Override
    protected String getDiplayName(SearchActivity activity) {
        String query = getQueryWithoutDot(activity);
        if (query.length() == 0) return this.getName();
        return "'" + query + "' on a map";
    }

}
