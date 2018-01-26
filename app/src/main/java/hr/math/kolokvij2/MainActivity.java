package hr.math.kolokvij2;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.URLUtil;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RemoteViews;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

public class MainActivity extends AppCompatActivity {

    int notificationID = 1;
    private static long startTime = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        DBAdapter db = new DBAdapter(this);

        /*
        // insert - pokrenuto, dakle postoji u bazi
        db.open();
        long id = db.insertSlika("Izlazak sunca", "Claude Monet");
        db.insertSlika("Guernica", "Pablo Picasso");
        db.insertSlika("Krist u oluji na Galilejskom jezeru", "Rembrandt van Rijn");
        db.insertSlika("Umjetnik u svom studiju", "Rembrandt van Rijn");
        db.insertSlika("Zena koja place", "Pablo Picasso");
        db.insertPeriod("Impresionizam", "Claude Monet");
        db.close();
        */




        // ispis baze:
        db.open();
        Cursor c = db.getAllSlike();
        String popis = "Slike:\n";
        if (c.moveToFirst())
        {
            do {
                popis += c.getString(1) + " - " + c.getString(2) + "\n";
            } while (c.moveToNext());
        }
        TextView txt = (TextView)findViewById(R.id.list_show);
        txt.setText(popis);

        c = db.getAllRazdoblja();
        db.close();

    }

    public void AddToList(Cursor c){

    }

    public void onClickDownloadImage(View view){

        // Isprobani linkovi za download slika:
        // https://web.math.pmf.unizg.hr/~karaga/light.gif
        // https://www.popwebdesign.net/popart_blog/wp-content/uploads/2013/06/slike-za-desktop-05.jpg
        // https://www.popwebdesign.net/popart_blog/wp-content/uploads/2013/06/slike-za-desktop-40.jpg
        // http://www.makarskainfo.com/images/stories/brela/m/Croatia_Brela_1.jpg

        EditText editTxt = (EditText)findViewById(R.id.editTextImg);
        String adress = editTxt.getText().toString();

        Toast.makeText(getApplicationContext(), adress, Toast.LENGTH_SHORT).show();

        // check if vaild url was given
        if( URLUtil.isValidUrl(adress) ){
            new DownloadImageTask().execute(adress);
        }else{
            Toast.makeText(getApplicationContext(), "Given input is not a vaild url.", Toast.LENGTH_SHORT).show();
        }
    }



    //----------- SAVE IMAGE -------------------------------------------------------------------------
    private String saveToInternalStorage(Bitmap bitmapImage){
        ContextWrapper cw = new ContextWrapper(getApplicationContext());
        File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);
        // Create imageDir
        File mypath=new File(directory,"downloaded_image.jpg");

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(mypath);
            // Use the compress method on the BitMap object to write image to the OutputStream
            bitmapImage.compress(Bitmap.CompressFormat.PNG, 100, fos);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return directory.getAbsolutePath();
    }
    //-------------------------------------------------------------------------------------------------


    //------------ DOWNLOAD IMAGE----------------------------------------------------------------------
    private InputStream OpenHttpConnection(String urlString)
            throws IOException
    {
        InputStream in = null;
        int response = -1;

        URL url = new URL(urlString);
        URLConnection conn = url.openConnection();

        if (!(conn instanceof HttpURLConnection))
            throw new IOException("Not an HTTP connection");
        try{
            HttpURLConnection httpConn = (HttpURLConnection) conn;
            httpConn.setAllowUserInteraction(false);
            httpConn.setInstanceFollowRedirects(true);
            httpConn.setRequestMethod("GET");
            httpConn.connect();
            response = httpConn.getResponseCode();
            if (response == HttpURLConnection.HTTP_OK) {
                in = httpConn.getInputStream();
            }
        }
        catch (Exception ex)
        {
            Log.d("Networking", ex.getLocalizedMessage());
            throw new IOException("Error connecting");
        }
        return in;
    }


    private Bitmap DownloadImage(String URL)
    {
        Bitmap bitmap = null;
        InputStream in = null;
        try {
            startTime = System.currentTimeMillis();
            in = OpenHttpConnection(URL);
            bitmap = BitmapFactory.decodeStream(in);
            in.close();
        } catch (IOException e1) {
            Log.d("NetworkingActivity", e1.getLocalizedMessage());
        }
        return bitmap;
    }


    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        protected Bitmap doInBackground(String... urls) {
            return DownloadImage(urls[0]);
        }

        protected void onPostExecute(Bitmap result) {
            // Download completed, get download time
            long downloadTime = System.currentTimeMillis() - startTime;
            // path to saved image
            String path = saveToInternalStorage(result);
            EditText text = (EditText)findViewById(R.id.tmp);
            text.setText(path);
            // display notification
            displayNotification(path, downloadTime);

            ImageView img = (ImageView) findViewById(R.id.show_img);
            img.setImageBitmap(result);
        }
    }
    //-------------------------------------------------------------------------------------------------


    //------------ NOTIFICATION ----------------------------------------------------------------------
    protected void displayNotification(String path, long downloadTime)
    {
        //---PendingIntent to launch activity if the user selects
        // this notification---
        Intent i = new Intent(this, DownloadNotification.class);

        i.putExtra("notificationID", notificationID);
        i.putExtra("path", path);
        i.putExtra("downloadTime", downloadTime);

        PendingIntent pendingIntent =
                PendingIntent.getActivity(this, 0, i, PendingIntent.FLAG_UPDATE_CURRENT);

        long[] vibrate = new long[] { 100, 250, 100, 500};

//Notification Channel - novo od Android O

        String NOTIFICATION_CHANNEL_ID = "my_channel_01";
        CharSequence channelName = "hr.math.karga.MYNOTIF";
        int importance = NotificationManager.IMPORTANCE_LOW;
        NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName, importance);
        notificationChannel.enableLights(true);
        notificationChannel.setLightColor(Color.RED);
        notificationChannel.enableVibration(true);
        notificationChannel.setVibrationPattern(vibrate);

//za sve verzije
        NotificationManager nm = (NotificationManager)
                getSystemService(NOTIFICATION_SERVICE);

// za Notification Chanel

        nm.createNotificationChannel(notificationChannel);




//ovako je i u starim verzijama, jedino dodano .setChannelId (za stare verzije to brisemo)

        Notification notif = new Notification.Builder(this)
                .setTicker("Reminder: meeting starts in 5 minutes")
                .setContentTitle("Image download completed")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setWhen(System.currentTimeMillis())
                .setShowWhen(true)
                .setContentIntent(pendingIntent)
                .setVibrate(vibrate)
                .setChannelId(NOTIFICATION_CHANNEL_ID)
                .build();
        //najnovije, od API level 26.1.0., .setWhen ide po defautlu ovdje na currentTimeMillis

/*        final NotificationCompat.Builder notif = new NotificationCompat.Builder(this,NOTIFICATION_CHANNEL_ID)

                .setDefaults(Notification.DEFAULT_ALL)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setVibrate(vibrate)
                .setSound(null)
                .setChannelId(NOTIFICATION_CHANNEL_ID)
                .setContentTitle("Meeting with customer at 3pm...")
                .setContentText("this is the second row")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setTicker("Reminder: meeting starts in 5 minutes")
                .setContentIntent(pendingIntent)
                .setAutoCancel(false); */

// za sve verzije

        nm.notify(notificationID, notif);
    }
    //-------------------------------------------------------------------------------------------------

}
