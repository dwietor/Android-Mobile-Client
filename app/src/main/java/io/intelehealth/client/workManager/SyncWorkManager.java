package io.intelehealth.client.workManager;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;

import androidx.work.Worker;
import androidx.work.WorkerParameters;

import io.intelehealth.client.R;
import io.intelehealth.client.dao.PullDataDAO;
import io.intelehealth.client.utilities.Logger;

public class SyncWorkManager extends Worker {


    String TAG = SyncWorkManager.class.getSimpleName();

    public SyncWorkManager(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        Context applicationContext = getApplicationContext();
        Logger.logD(TAG, "result job");
        PullDataDAO pullDataDAO = new PullDataDAO();
        boolean pull = pullDataDAO.pullData(applicationContext);
        if (pull)
            sendNotification("Sync", "Synced Data");
        else
            sendNotification("Sync", "failed to Sync");

        boolean push = pullDataDAO.pushDataApi();

        if (push)
            sendNotification("Sync", "Synced Data");
        else
            sendNotification("Sync", "failed to Sync");

        return Result.success();
    }

    public void sendNotification(String title, String message) {
        NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);

        //If on Oreo then notification required a notification channel.
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("default", "Default", NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
        }

        NotificationCompat.Builder notification = new NotificationCompat.Builder(getApplicationContext(), "default")
                .setContentTitle(title)
                .setContentText(message)
                .setSmallIcon(R.mipmap.ic_launcher);

        notificationManager.notify(1, notification.build());
    }
}

