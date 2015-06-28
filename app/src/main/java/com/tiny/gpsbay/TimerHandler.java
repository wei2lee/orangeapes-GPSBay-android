package com.tiny.gpsbay;

import android.os.Handler;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by leeyeechuan on 6/20/15.
 */
public class TimerHandler extends TimerTask {
    public Handler handler;
    public Runnable runnable;
    public Timer timer;

    public TimerHandler(Runnable runnable, int interval) {
        handler = new Handler();
        this.runnable = runnable;
        timer = new Timer();
        timer.schedule(this, 0,interval);
    }

    @Override
    public void run() {
        handler.post(runnable);
    }
}