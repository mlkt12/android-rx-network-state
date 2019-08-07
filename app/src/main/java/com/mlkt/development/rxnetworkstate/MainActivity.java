package com.mlkt.development.rxnetworkstate;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.processors.PublishProcessor;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "NETWORK_STATE";

    private BroadcastReceiver broadcastReceiver;
    private Disposable disposable;
    private PublishProcessor<Boolean> publishProcessor;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onStart() {
        super.onStart();
        publishProcessor = PublishProcessor.create();

        disposable =
                publishProcessor
                        .startWith(getConnectivityStatus(this))
                        .distinctUntilChanged()
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                online -> {
                                    if (online) {
                                        Log.d(TAG,"You are online");
                                    } else {
                                        Log.d(TAG,"You are offline");
                                    }
                                });

        listenToNetworkConnectivity();
    }

    @Override
    protected void onStop() {
        super.onStop();
        disposable.dispose();
        unregisterReceiver(broadcastReceiver);
    }

    private void listenToNetworkConnectivity() {

        broadcastReceiver =
                new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        publishProcessor.onNext(getConnectivityStatus(context));
                    }
                };

        final IntentFilter intentFilter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(broadcastReceiver, intentFilter);
    }

    private boolean getConnectivityStatus(Context context) {
        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }


}

