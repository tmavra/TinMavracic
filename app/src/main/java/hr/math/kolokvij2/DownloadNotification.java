package hr.math.kolokvij2;

import android.app.NotificationManager;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

public class DownloadNotification extends AppCompatActivity {

    private String path_to_image;
    private long downloadTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download_notification);

        Intent i = getIntent();
        path_to_image = i.getExtras().getString("path");
        downloadTime = i.getExtras().getLong("downloadTime");

        //dohvat Notification Managera
        NotificationManager nm=(NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        //cancel metoda za gasenje notificationa na kraju
        nm.cancel(getIntent().getExtras().getInt("notificationID"));
    }


    private void loadImageFromStorage(String path)
    {

        try {
            File f=new File(path, "downloaded_image.jpg");
            Bitmap b = BitmapFactory.decodeStream(new FileInputStream(f));
            ImageView img=(ImageView)findViewById(R.id.show_img_notif);
            img.setImageBitmap(b);
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }

    }

    public void onClickShowImage(View v){

        // set details
        TextView text = (TextView)findViewById(R.id.details);
        text.setText("Download time: " + downloadTime + "milliseconds");

        loadImageFromStorage(path_to_image);
        Toast.makeText(getApplicationContext(), "found path: " + path_to_image, Toast.LENGTH_LONG).show();
    }


}
