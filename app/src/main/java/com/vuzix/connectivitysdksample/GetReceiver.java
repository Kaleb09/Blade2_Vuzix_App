package com.vuzix.connectivitysdksample;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.vuzix.connectivity.sdk.Connectivity;

public class GetReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Connectivity.get(context).verify(intent, "com.vuzix.connectivitysdksample")) {
            if (isOrderedBroadcast()) {
                setResultData(Build.MODEL);
            }
        }
    }
}
