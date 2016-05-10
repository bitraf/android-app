package bitraf.bitraf;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.wearable.activity.ConfirmationActivity;
import android.support.wearable.view.DelayedConfirmationView;
import android.support.wearable.view.WatchViewStub;
import android.view.View;
import android.widget.TextView;

public class MainActivity extends Activity implements
        DelayedConfirmationView.DelayedConfirmationListener{

    private DelayedConfirmationView mDelayedView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);
        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
            @Override
            public void onLayoutInflated(WatchViewStub stub) {
                mDelayedView = (DelayedConfirmationView) findViewById(R.id.delayed_confirm);
                mDelayedView.setListener(MainActivity.this);
                // Two seconds to cancel the action
                mDelayedView.setTotalTimeMs(3000);
                // Start the timer
                mDelayedView.start();
            }
        });



        /*int notificationId = 001;
        Intent intent= new Intent(this, SendUnlockMessageToPhoneService.class);
        intent.setAction("UNLOCK");
        PendingIntent pendingIntent = PendingIntent.getService(this, 0, intent, 0);w

        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_full_sad)
                        .setContentTitle("Bitraf")
                        .setContentText("unlock door")
                        .setContentIntent(pendingIntent);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(notificationId, notificationBuilder.build());*/
        //finish();
    }

    @Override
    public void onTimerFinished(View view) {
        // User didn't cancel, perform the action
        Intent intent= new Intent(this, SendUnlockMessageToPhoneService.class);
        intent.setAction("UNLOCK");
        startService(intent);


        Intent confirmIntent = new Intent(this, ConfirmationActivity.class);
        intent.putExtra(ConfirmationActivity.EXTRA_ANIMATION_TYPE, ConfirmationActivity.SUCCESS_ANIMATION);
        intent.putExtra(ConfirmationActivity.EXTRA_MESSAGE, "Access granted");
        startActivity(confirmIntent);
        finish();
    }

    @Override
    public void onTimerSelected(View view) {
        // User canceled, abort the action
        mDelayedView.reset();
        finish();
    }
}