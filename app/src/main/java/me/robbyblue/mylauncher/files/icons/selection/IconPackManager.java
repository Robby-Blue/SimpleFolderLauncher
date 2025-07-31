package me.robbyblue.mylauncher.files.icons.selection;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;

import androidx.core.content.res.ResourcesCompat;

import org.xmlpull.v1.XmlPullParser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import me.robbyblue.mylauncher.files.icons.IconPackIconData;

public class IconPackManager {

    private static IconPackManager instance;
    private PackageManager pm;

    private IconPackManager(PackageManager pm) {
        this.pm = pm;
    }

    public static IconPackManager getInstance() {
        return instance;
    }

    public static IconPackManager getInstance(PackageManager pm) {
        if (instance == null) {
            instance = new IconPackManager(pm);
        }
        return instance;
    }

    public ArrayList<IconOption> getIconOptions(String packageName) {
        Set<String> drawableNames = new LinkedHashSet<>();
        ArrayList<IconOption> iconOptions = new ArrayList<>();

        List<ResolveInfo> infos = getIconPackApps(pm);
        for (ResolveInfo info : infos) {
            String iconPackPackageName = info.activityInfo.packageName;
            String iconPackName = info.loadLabel(pm).toString();
            Map<String, String> icons = parseAppFilter(pm, iconPackPackageName);

            // added because of https://github.com/Robby-Blue/SimpleFolderLauncher/issues/51
            if(icons == null) {
                continue;
            }

            for (String component : icons.keySet()) {
                if (!component.split("/")[0].equals(packageName))
                    continue;

                String drawableName = icons.get(component);
                String drawableKey = iconPackPackageName+"/"+drawableName;

                if (drawableNames.contains(drawableKey))
                    continue;
                drawableNames.add(drawableKey);

                IconPackIconData iconData = new IconPackIconData(iconPackPackageName, drawableName);

                iconOptions.add(new IconOption(iconPackName + ": " + drawableName, iconData));
            }
        }

        return iconOptions;
    }

    public Map<String, String> parseAppFilter(PackageManager pm, String iconPackPackage) {
        Map<String, String> result = new HashMap<>();
        try {
            Resources res = pm.getResourcesForApplication(iconPackPackage);

            // androids recommends using R.id.<something>
            // but ofc the available icon packs cant be known at compile time
            @SuppressLint("DiscouragedApi")
            int id = res.getIdentifier("appfilter", "xml", iconPackPackage);
            if (id == 0) return result;

            XmlPullParser parser = res.getXml(id);
            int eventType = parser.getEventType();

            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG && "item".equals(parser.getName())) {
                    String component = parser.getAttributeValue(null, "component");
                    String drawable = parser.getAttributeValue(null, "drawable");

                    if (component != null && drawable != null) {
                        if (!component.contains("{")) {
                            parser.next();
                            continue;
                        }
                        // extract from ComponentInfo{package/component}
                        String name = component.substring(component.indexOf("{") + 1, component.indexOf("}")).toLowerCase();

                        result.put(name, drawable);
                    }
                }
                eventType = parser.next();
            }
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private List<ResolveInfo> getIconPackApps(PackageManager pm) {
        Intent intent = new Intent("com.novalauncher.THEME");
        return pm.queryIntentActivities(intent, PackageManager.GET_META_DATA);
    }

    public Drawable getIconPackIcon(String iconPackPackage, String drawableName) {
        try {
            System.out.println(iconPackPackage + " " + drawableName);
            Resources res = pm.getResourcesForApplication(iconPackPackage);
            @SuppressLint("DiscouragedApi")
            int id = res.getIdentifier(drawableName, "drawable", iconPackPackage);
            if (id != 0) {
                return ResourcesCompat.getDrawable(res, id, null);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}
