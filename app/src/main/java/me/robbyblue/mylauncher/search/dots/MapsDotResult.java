package me.robbyblue.mylauncher.search.dots;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;

import me.robbyblue.mylauncher.search.SearchActivity;

public class MapsDotResult extends DotSearchResult {


    public MapsDotResult() {
        super("Maps", "m");
    }

    @Override
    protected int getTextColor() {
        return Color.parseColor("#EEEEEE");
    }

    @Override
    protected void open(SearchActivity activity) {
        String query = getQueryWithoutDot(activity);

        query = "geo:0,0?q=" + query;

        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(query));
        activity.startActivity(intent);
    }

}
