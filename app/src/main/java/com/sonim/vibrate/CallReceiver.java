package com.sonim.vibrate;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import static android.security.KeyStore.getApplicationContext;

public class CallReceiver extends BroadcastReceiver {
    private static int lastState;
    private final String TAG = "CallReceiver";
    AudioManager audioManager;
    private static boolean isIncoming;
    private int previous_setting;
    private int ON_CALL = 0;
    private int CALL_DICONNECTED = 1;
    SharedPreferences sharedPreferences;

    @Override
    public void onReceive(Context context, Intent intent) {
        audioManager = (AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
        sharedPreferences = getApplicationContext().getSharedPreferences("MyPreference",Context.MODE_PRIVATE);
        lastState = sharedPreferences.getInt("lastState",TelephonyManager.CALL_STATE_IDLE);
        if (intent.getAction().equals("android.intent.action.NEW_OUTGOING_CALL")) {
            Log.d(TAG,"action is NEW_OUTGOING_CALL");
        }
        else {
            String stateStr = intent.getExtras().getString(TelephonyManager.EXTRA_STATE);
            int state = 0;
            if (stateStr != null) {
                if (stateStr.equals(TelephonyManager.EXTRA_STATE_IDLE)) {
                    state = TelephonyManager.CALL_STATE_IDLE;
                } else if (stateStr.equals(TelephonyManager.EXTRA_STATE_OFFHOOK)) {
                    state = TelephonyManager.CALL_STATE_OFFHOOK;
                } else if (stateStr.equals(TelephonyManager.EXTRA_STATE_RINGING)) {
                    state = TelephonyManager.CALL_STATE_RINGING;
                }

                Log.d(TAG,"action is NEW_OUTGOING_CALL elSe statestr : "+stateStr);
                onCallStateChanged(state);
            }
        }
    }

    public void onCallStateChanged(int state) {
        if(lastState == state){
            Log.d(TAG,"No change lastState : "+lastState);
            return;
        }
        switch (state) {
            case TelephonyManager.CALL_STATE_RINGING:
                isIncoming = true;
                Log.d(TAG,"IncomingCallReceived state : CALL_STATE_RINGING");
                break;
            case TelephonyManager.CALL_STATE_OFFHOOK:
                if(lastState != TelephonyManager.CALL_STATE_RINGING){
                    isIncoming = false;
                    Log.d(TAG,"OutgoingCallStarted state : CALL_STATE_OFFHOOK");
                    setAudioManager(ON_CALL);
                }
                else
                {
                    isIncoming = true;
                    setAudioManager(ON_CALL);
                    Log.d(TAG,"IncomingCallAnswered state : CALL_STATE_OFFHOOK");
                }

                break;
            case TelephonyManager.CALL_STATE_IDLE:
                if(lastState == TelephonyManager.CALL_STATE_RINGING){
                    Log.d(TAG,"MissedCall state : CALL_STATE_IDLE");
                }
                else if(isIncoming){
                    Log.d(TAG,"IncomingCallEnded state : CALL_STATE_IDLE");
                    setAudioManager(CALL_DICONNECTED);
                }
                else{
                    Log.d(TAG,"OutgoingCallEnded state : CALL_STATE_IDLE");
                    setAudioManager(CALL_DICONNECTED);
                }
                break;
        }
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("lastState",state);
        editor.apply();
    }
    private void setAudioManager(int state)
    {
        if (state == ON_CALL){
            previous_setting = audioManager.getRingerMode();
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putInt("previousSetting",previous_setting);
            editor.apply();
            Log.d(TAG,"previous_setting :"+previous_setting);
            audioManager.setRingerMode(AudioManager.RINGER_MODE_SILENT);
        }
        else {
            previous_setting = sharedPreferences.getInt("previousSetting",2);
            Log.d(TAG,"previous_setting from else:"+previous_setting);
            audioManager.setRingerMode(previous_setting);
        }


    }

}
