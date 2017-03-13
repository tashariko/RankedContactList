package com.tashariko.rankedcontactlist;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telecom.Call;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;


import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmResults;

public class MainActivity extends AppCompatActivity {

    private static final int PERMISSION_CALL_CODE = 1302;
    private static String SHARED = "share_pref";

    Realm realmDb;
    private ListView callList;
    private ProgressBar progressBar;

    ArrayList<CallModel> callModelsArrayList = new ArrayList<>();
    private CallTypeDetailAdapter adapter;
    private SharedPreferences sharedPref;
    private int currentFirstVisibleItem = 0;
    private boolean isDown = false;
    private int currentVisibleItemCount = 0;
    private int currentScrollState, pageNo = 0;
    private boolean isLoading = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        callList = (ListView) findViewById(R.id.callList);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        Realm.init(getApplicationContext());
        realmDb = Realm.getDefaultInstance();

        resumed();

        callList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                callModelsArrayList.get(position).rank++;

                updateDb();

                adapter.notifyDataSetChanged();

            }
        });

        callList.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                currentScrollState = scrollState;
                isScrollCompleted();
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if ((firstVisibleItem + visibleItemCount) >= totalItemCount) {
                    isDown = true;
                } else {
                    isDown = false;
                }

                currentFirstVisibleItem = firstVisibleItem;
                currentVisibleItemCount = visibleItemCount;
            }

            private void isScrollCompleted() {
                if (currentVisibleItemCount > 0 && currentScrollState == SCROLL_STATE_IDLE && isDown) {
                    if (!isLoading) {
                        isLoading = true;

                        getFromRealm();
                        pageNo++;

                        currentFirstVisibleItem = 0;
                        currentScrollState = 0;
                        currentVisibleItemCount = 0;
                        isLoading = false;
                    }
                }
            }
        });
    }

    private void updateDb() {

        Collections.sort(callModelsArrayList, new Comparator<CallModel>() {
            @Override
            public int compare(CallModel model2, CallModel model1) {
                return model1.rank.compareTo(model2.rank);
            }
        });

    }

    protected void resumed() {

        if (sharedPref.getBoolean(SHARED, true)) {
            if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
                Log.d("DebugInfo: Permission", "Location Permission Required");
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_CONTACTS,}, PERMISSION_CALL_CODE);
                return;
            } else {
                getData();
            }
        } else {
            RealmResults<CallModel> realmResults = realmDb.where(CallModel.class).findAll();
            progressBar.setVisibility(View.VISIBLE);
            if (realmResults.size() > 0) {
                adapter = new CallTypeDetailAdapter(getApplicationContext(), R.layout.list_item_detail, callModelsArrayList);
                callList.setAdapter(adapter);
                getFromRealm();
            } else {
                getData();
            }
        }

    }

    private void getData() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                new RetrieveCallForHome().getCall(getApplicationContext(), new RetrieveCallForHome.CallDetailCallback() {
                    @Override
                    public void list(ArrayList<CallModel> model) {
                        callModelsArrayList = model;

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                                adapter = new CallTypeDetailAdapter(getApplicationContext(), R.layout.list_item_detail, callModelsArrayList);
                                callList.setAdapter(adapter);

                                saveToRealm();
                                sharedPref.edit().putBoolean(SHARED, false).apply();
                                callModelsArrayList.clear();
                                getFromRealm();
                            }
                        });

                    }
                });
            }
        }).start();
    }


    private void getFromRealm() {

        RealmResults<CallModel> realmResults = realmDb.where(CallModel.class).findAll();
        if (realmResults.size() > 0) {
            realmResults = realmResults.sort("rank");
            if (!realmDb.isInTransaction()) {
                realmDb.beginTransaction();
            }

            for (int i = 0; i < realmResults.size(); i++) {
                if (i < 20) {
                    callModelsArrayList.add(realmResults.get(i + pageNo * 20));
                } else {
                    break;
                }
            }
        }

        progressBar.setVisibility(View.GONE);
        adapter.notifyDataSetChanged();
    }

    private void saveToRealm() {

        RealmResults<CallModel> realmResults = realmDb.where(CallModel.class).findAll();
        if (realmResults.size() != 0) {
            if (!realmDb.isInTransaction()) {
                realmDb.beginTransaction();
            }
            realmResults.deleteAllFromRealm();
            realmDb.commitTransaction();
        }

        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                for (final CallModel model : callModelsArrayList) {
                    realmDb.executeTransaction(new Realm.Transaction() {
                        @Override
                        public void execute(Realm realm) {
                            CallModel callModel = realmDb.createObject(CallModel.class);
                            callModel.load();
                            if (callModel.isLoaded()) {
                                callModel.setName(model.getName());
                                callModel.setNumber(model.getNumber());
                                callModel.setRank(model.getRank());
                            } else {
                                Toast.makeText(MainActivity.this, "Is not managed", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

                }

            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_CALL_CODE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getData();
                    Log.d("Request Permission", "GRANTED");
                } else {
                    Toast.makeText(this, "Without permission the app will not work.", Toast.LENGTH_SHORT).show();
                    Log.d("Request Permission", "DENIED");
                }
            }
        }
    }
}