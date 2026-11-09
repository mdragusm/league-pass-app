package com.mdragusm.leaguepass;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.ZoneId;
import java.time.ZonedDateTime;

/**
 * Every ~15 min: after 3 PM Monterrey time, check menu.json once; if it is today's menu and carries a
 * "push" text, show it as a notification (once per day). No Firebase, no server, no libraries.
 */
public class PollJobService extends JobService {
    private static final int JOB_ID = 1;
    private static final String CHANNEL = "menu";
    private static final String MENU_URL = "https://mdragusm.github.io/league-pass-app/menu.json";

    static void schedule(Context c) {
        JobScheduler js = (JobScheduler) c.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        if (js.getPendingJob(JOB_ID) != null) return;
        js.schedule(new JobInfo.Builder(JOB_ID, new ComponentName(c, PollJobService.class))
                .setPeriodic(15 * 60 * 1000L)
                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                .setPersisted(true)
                .build());
    }

    @Override
    public boolean onStartJob(final JobParameters params) {
        new Thread(() -> {
            try {
                check();
            } catch (Exception ignored) {
                // Try again at the next period.
            }
            jobFinished(params, false);
        }).start();
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        return true;
    }

    private void check() throws Exception {
        ZonedDateTime now = ZonedDateTime.now(ZoneId.of("America/Mexico_City"));
        String today = now.toLocalDate().toString();
        SharedPreferences sp = getSharedPreferences("lp", MODE_PRIVATE);
        if (today.equals(sp.getString("notified", ""))) return;
        if (now.getHour() < 15) return;

        HttpURLConnection con = (HttpURLConnection) new URL(MENU_URL + "?t=" + System.currentTimeMillis()).openConnection();
        con.setConnectTimeout(15000);
        con.setReadTimeout(15000);
        con.setUseCaches(false);
        con.setRequestProperty("Cache-Control", "no-cache");
        if (con.getResponseCode() != 200) return;
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        try (InputStream in = con.getInputStream()) {
            byte[] b = new byte[4096];
            int n;
            while ((n = in.read(b)) > 0) buf.write(b, 0, n);
        }
        JSONObject menu = new JSONObject(buf.toString("UTF-8"));
        if (!today.equals(menu.optString("date"))) return;
        String push = menu.optString("push", "").trim();
        if (push.isEmpty()) return;

        show(push);
        sp.edit().putString("notified", today).apply();
    }

    private void show(String text) {
        NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        nm.createNotificationChannel(new NotificationChannel(CHANNEL, "Tonight menu", NotificationManager.IMPORTANCE_DEFAULT));
        PendingIntent open = PendingIntent.getActivity(this, 0, new Intent(this, MainActivity.class),
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
        Notification n = new Notification.Builder(this, CHANNEL)
                .setSmallIcon(R.drawable.ic_stat)
                .setContentText(text)
                .setStyle(new Notification.BigTextStyle().bigText(text))
                .setContentIntent(open)
                .setAutoCancel(true)
                .build();
        nm.notify(1, n);
    }
}
