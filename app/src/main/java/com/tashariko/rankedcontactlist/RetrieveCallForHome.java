package com.tashariko.rankedcontactlist;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.provider.CallLog;
import android.provider.ContactsContract;

import java.util.ArrayList;
import java.util.Collections;

import static android.provider.BaseColumns._ID;

/**
 * Created by tashariko on 20/7/16.
 */

public class RetrieveCallForHome {

    ArrayList<CallModel> callModelsArrayList=new ArrayList<>();

    public void getCall( Context context, CallDetailCallback callback) {

        ContentResolver cr = context.getContentResolver();
        Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI,
                null, null, null, null);

        if (cur.getCount() > 0) {
            while (cur.moveToNext()) {
                CallModel callModel=new CallModel();
                String id = cur.getString(
                        cur.getColumnIndex(ContactsContract.Contacts._ID));
                String name = cur.getString(cur.getColumnIndex(
                        ContactsContract.Contacts.DISPLAY_NAME));

                if (cur.getInt(cur.getColumnIndex(
                        ContactsContract.Contacts.HAS_PHONE_NUMBER)) > 0) {
                    Cursor pCur = cr.query(
                            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                            null,
                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID +" = ?",
                            new String[]{id}, null);
                    while (pCur.moveToNext()) {
                        String phoneNo = pCur.getString(pCur.getColumnIndex(
                                ContactsContract.CommonDataKinds.Phone.NUMBER));

                        callModel.number=phoneNo;
                        callModel.name=name;
                        callModelsArrayList.add(callModel);
                    }
                    pCur.close();
                }
            }
        }

        callback.list(callModelsArrayList);
    }

    public interface CallDetailCallback{
        void list(ArrayList<CallModel> model);
    }
}
