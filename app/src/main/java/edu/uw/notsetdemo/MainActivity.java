package edu.uw.notsetdemo;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.SharedPreferencesCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v4.app.NotificationCompat;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

public class MainActivity extends AppCompatActivity {

    public static final String TAG = "**Main**";

    public static final String EXTRA_MESSAGE = "edu.uw.intentdemo.message";

    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int SMS_SENT_CODE = 2;
    private static final int NOTIFY_CODE = 3;
    private static final int TEST_NOTIFICATION_ID = 1;

    public static final String ACTION_SMS_STATUS = "edu.uw.intentdemo.ACTION_SMS_STATUS";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        View launchButton = findViewById(R.id.btnLaunch);
        launchButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Log.v(TAG, "Launch button pressed");

                //                         context,           target
                Intent intent = new Intent(MainActivity.this, SecondActivity.class);
                intent.putExtra(EXTRA_MESSAGE, "Greetings from sunny MainActivity!");
                startActivity(intent);
            }
        });

        // PREFERENCES STUFF UNREALTED TO NOTIFICATION STUFF BELOW
        // Pass in file for preferences to look at, level of privacy
        SharedPreferences prefs = getSharedPreferences("test_prefs", MODE_PRIVATE);
        // Similar to bundle
        // get with default value
        // this.notifications = prefs.getInt("notifications", 0);


        //////

        //dynamic BroadcastReceiver registry
//        IntentFilter batteryFilter = new IntentFilter();
//        batteryFilter.addAction(Intent.ACTION_BATTERY_LOW);
//        batteryFilter.addAction(Intent.ACTION_BATTERY_OKAY);
//        batteryFilter.addAction(Intent.ACTION_POWER_CONNECTED);
//        batteryFilter.addAction(Intent.ACTION_POWER_DISCONNECTED);
//        this.registerReceiver(new MyReceiver(), batteryFilter);

    }

    @Override
    protected void onStop() {
        super.onStop();
        SharedPreferences prefs = getSharedPreferences("test_prefs", MODE_PRIVATE);
        SharedPreferences.Editor ed = prefs.edit();
        // put with ed.putInt("notifications", 1);
        ed.commit();
    }

    public void callNumber(View v) {
        Log.v(TAG, "Call button pressed");

        Intent intent = new Intent(Intent.ACTION_DIAL);
        intent.setData(Uri.parse("tel:206-685-1622"));
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
        
    }

    public void takePicture(View v) {
        Log.v(TAG, "Camera button pressed");

        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap)extras.get("data");
            ImageView imageView = (ImageView)findViewById(R.id.imgThumbnail);
            imageView.setImageBitmap(imageBitmap);
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    public void sendMessage(View v) {
        Log.v(TAG, "Message button pressed");

        //demo
        SmsManager smsManager = SmsManager.getDefault();

        Intent intent = new Intent(ACTION_SMS_STATUS);
        // Wrapping intent in pendingIntent gives permissions and context for the intent.
        // Basically, penking intent can sent the intent as if he were me
        // Like sending n RSVP note that has your finger print on it
        PendingIntent pendingIntent = PendingIntent.getBroadcast(MainActivity.this, SMS_SENT_CODE, intent, 0);

        smsManager.sendTextMessage("5554", null, "This is a test message!", pendingIntent, null);

    }

    // Dealing with pulldown notifications
    public void notify(View v){
        Log.v(TAG, "Notify button pressed");

        // Will return shared XML preferences file
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

        // Three required setters
        // Three required setters
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.note_icon)
                        .setContentTitle("My notification")
                        .setContentText("Hello World!");
        Intent intent =  new Intent(this, SecondActivity.class); //Clicking this notificiation, should makeit go to second activity

        // Above i maing intent

        // articifical history
        TaskStackBuilder tsb = TaskStackBuilder.create(this);
        // in manifest is how we relate activitite sto eachother
        // in manifrest for activivty 2

        tsb.addParentStack(SecondActivity.class);
        tsb.addNextIntent(intent);
        // Need to give it a code. Giving ID to this epending intent so we can refer to that item later
        // and update it or somwthing
        PendingIntent intent2 = tsb.getPendingIntent(NOTIFY_CODE, PendingIntent.FLAG_UPDATE_CURRENT);

        // et the intent for the builder
        mBuilder.setContentIntent(intent2);
        mBuilder.setPriority(NotificationCompat.PRIORITY_HIGH);
        mBuilder.setVibrate(new long[] {100, 500, 100, 1000});
        mBuilder.setSound(Settings.System.DEFAULT_NOTIFICATION_URI);
        // now we send the notification
        NotificationManager noteMan = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        Notification notification = mBuilder.build();
        // Basically ID lets us refer, replace, and control the namespace
        noteMan.notify(TEST_NOTIFICATION_ID, notification);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case R.id.menu_item_notify:
                notify(null);
                return true;
            case R.id.menu_item_prefs:
                Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(intent);
                Log.v(TAG, "Settings button pressed");
                return true;
            case R.id.menu_item_click:
                Log.v(TAG, "Extra button pressed");
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

}
