package me.robbyblue.mylauncher.search.dots;

import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;

import me.robbyblue.mylauncher.files.icons.DotIconData;
import me.robbyblue.mylauncher.search.SearchActivity;

public class YoutubeDotResult extends DotSearchResult {


    public YoutubeDotResult() {
        super("YouTube", "y", new DotIconData(Color.parseColor("#c12025")));
    }

    @Override
    protected void open(SearchActivity activity) {
        String query = getQueryWithoutDot(activity);

        Intent intent;

        PackageManager packageManager = activity.getPackageManager();
        try {
            packageManager.getPackageInfo("org.schabi.newpipe", PackageManager.GET_ACTIVITIES);
            // newpipe installed
            intent = new Intent();
            intent.setComponent(new ComponentName("org.schabi.newpipe", "org.schabi.newpipe.MainActivity"));

            intent.putExtra("key_open_search", true);
            intent.putExtra("key_search_string", query);
        } catch (PackageManager.NameNotFoundException e) {
            // not installed, open link (in default yt app)
            String link = "https://www.youtube.com/results?search_query=" + query;
            intent = new Intent(Intent.ACTION_VIEW, Uri.parse(link));
        }

        activity.startActivity(intent);
    }

    @Override
    protected String getDiplayName(SearchActivity activity) {
        String query = getQueryWithoutDot(activity);
        if (query.length() == 0) return this.getName();
        return "'" + query + "' on yt";
    }

}
