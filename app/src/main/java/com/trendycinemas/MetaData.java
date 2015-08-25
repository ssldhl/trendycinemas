package com.trendycinemas;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;

/*
* Created by sushilthe on 8/25/15
*/
public final class MetaData {
    public String getMetaDataFromManifest(Context context, String metadataName) throws PackageManager.NameNotFoundException {
        ApplicationInfo appInfo = context.getPackageManager().getApplicationInfo(
                context.getApplicationContext().getPackageName(), PackageManager.GET_META_DATA);

        Bundle bundle = appInfo.metaData;
        return bundle.getString(metadataName);
    }
}
