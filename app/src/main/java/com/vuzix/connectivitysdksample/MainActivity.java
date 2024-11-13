package com.vuzix.connectivitysdksample;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.vuzix.connectivity.sdk.Connectivity;
import com.vuzix.connectivity.sdk.Device;

import java.io.ByteArrayOutputStream;

public class MainActivity extends Activity {

    private static final String ACTION_SEND = "com.vuzix.connectivitysdksample.SEND";
    private static final String ACTION_GET = "com.vuzix.connectivitysdksample.GET";

    private static final String EXTRA_TEXT = "text";

    private EditText mEditText;
     ImageView imageView;
     Button sendimage;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // check for Connectivity framework
        if (!Connectivity.get(this).isAvailable()) {
            Toast.makeText(this, R.string.not_available, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        setContentView(R.layout.main);
        mEditText = findViewById(R.id.text);
        sendimage = findViewById(R.id.sendimage);
        imageView = findViewById(R.id.imageView);
        sendimage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Drawable drawable = getResources().getDrawable(R.drawable.samplepic);
                Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();

                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                byte[] byteArray = stream.toByteArray();
                Intent sendIntent = new Intent(ACTION_SEND);
                sendIntent.setPackage("com.vuzix.connectivitysdksample");

                sendIntent.putExtra(EXTRA_TEXT, byteArray);
                Connectivity.get(MainActivity.this).sendBroadcast(sendIntent);
                mEditText.setText(null);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        registerReceiver(receiver, new IntentFilter(ACTION_SEND));
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(receiver);
    }

    public void sendClicked(View view) {
        Intent sendIntent = new Intent(ACTION_SEND);
        sendIntent.setPackage("com.vuzix.connectivitysdksample");
        sendIntent.putExtra(EXTRA_TEXT, mEditText.getText().toString());
        Connectivity.get(this).sendBroadcast(sendIntent);
        mEditText.setText(null);
    }

    public void getRemoteDeviceModelClicked(View view) {
        Connectivity connectivity = Connectivity.get(this);
        Device device = connectivity.getDevice();
        if (device != null) {
            Intent getIntent = new Intent(ACTION_GET);
            getIntent.setPackage("com.vuzix.connectivitysdksample");
            connectivity.sendOrderedBroadcast(device, getIntent, new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    String model = getResultData();
                    if (model != null) {
                        Toast.makeText(context, model, Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (Connectivity.get(context).verify(intent, "com.vuzix.connectivitysdksample")) {
                String text = intent.getStringExtra(EXTRA_TEXT);

                if (text != null) {
                    Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
                }
                byte[] byteArray =intent.getByteArrayExtra("message");
                  if(byteArray!=null) {
                      Bitmap bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
                      imageView.setImageBitmap(bitmap);
                  }

            }
        }
    };
}
