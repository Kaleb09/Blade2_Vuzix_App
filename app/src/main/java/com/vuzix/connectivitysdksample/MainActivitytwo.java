package com.vuzix.connectivitysdksample;



import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.vuzix.connectivity.sdk.Connectivity;
import com.vuzix.connectivity.sdk.Device;

import java.io.ByteArrayOutputStream;

public class MainActivitytwo extends AppCompatActivity {
    int WRITE_EXTERNAL_STORAGE =1;
    private static final String ACTION_SEND = "com.vuzix.connectivitysdksample.SEND";
    private static final String ACTION_GET = "com.vuzix.connectivitysdksample.GET";
    private static final String EXTRA_TEXT = "text";
    private static final String Image = "Image";
    //phone
    private EditText mEditText;
    ImageView imageView;
    Button sendimage;
    public static ProgressBar progressBar;
    Button changedur;
    Button selectgallery,camera;

   // ImageView imageView;
    EditText duredit;
    Common common ;
    int selectimagecode=3463456;
    Callback callback;
    String url = "https://zq7spyy1f3.execute-api.us-east-1.amazonaws.com/dev/images";
   public static TextView textView;
    @Override
    protected void  onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!Connectivity.get(this).isAvailable()) {
            Toast.makeText(this, R.string.not_available, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        setContentView(R.layout.activity_main);
        duredit = findViewById(R.id.duration);
        changedur = findViewById(R.id.button);
        camera = findViewById(R.id.camera);

        selectgallery = findViewById(R.id.button2);
        imageView = findViewById(R.id.imageView);
         textView = findViewById(R.id.textView);
        progressBar = findViewById(R.id.progressBar);
        progressBar.setVisibility(View.GONE);
        common = new Common(this);

        /* if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
          //  Toast.makeText(this,"accept notification permission ",Toast.LENGTH_SHORT).show();

            requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
          }*/
        callback = new Callback() {
            @Override
            public void onDataFetched(String data) {
                Log.d("TAG", "recccccccceved "+data);
                textView.setText(data);
            }
        };
        changedur.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!duredit.getText().toString().isEmpty()) {
                    int dur = Integer.parseInt(duredit.getText().toString());
                     common.addtoShared("duration",dur);
                    Toast.makeText(MainActivitytwo.this,"duration changed",Toast.LENGTH_SHORT).show();

                }else{
              Toast.makeText(MainActivitytwo.this,"Enter Duration",Toast.LENGTH_SHORT).show();

                }

            }
        });

      //if this comment is opend then he app will crash in android 14
        // to fix this issue we have to build the app to api 30
       // Connectivity c = Connectivity.get(this);
      //  Intent myRemoteBroadcast = new Intent("com.example.myapp.MY_ACTION");
           // myRemoteBroadcast.setp
       /* if (c.isAvailable()) {
            // Proceed with sending/receiving data
        }*/
        selectgallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               /* Intent intent = new Intent(MainActivity.this, selectgallery.class);
                startActivity(intent);*/
                imageChooser(selectimagecode);

                Log.d("TAG", "imageChooser  ");
                System.out.print("imageChooser  ");


            }
        });
        camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivitytwo.this, CameraApp.class);
                startActivity(intent);


            }
        });
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
                sendIntent.putExtra(Image, byteArray);
                // sendBroadcast(sendIntent);
                Connectivity.get(MainActivitytwo.this).sendBroadcast(sendIntent);
                mEditText.setText(null);
            }
        });

    }




   /* private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override public void onReceive(Context context, Intent intent) {
            // do something with intent
                byte[] byteArray =intent.getByteArrayExtra("message");
            Bitmap bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);

            imageView.setImageBitmap(bitmap);
            saveImageToPhotoRoll(bitmap);
            Toast.makeText(context,"recccccccceved ",Toast.LENGTH_SHORT).show();
            Log.d("TAG", "recccccccceved");

        }
    };*/
    public void permission(){

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M&&Build.VERSION.SDK_INT<33) {
            int permission = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE);
            if (permission != PackageManager.PERMISSION_GRANTED) {

                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, WRITE_EXTERNAL_STORAGE);
            }else {
                //senddata();
            }
        }else if (Build.VERSION.SDK_INT >= 33) {


        }else{


        }


    }
   /* public void saveImageToPhotoRoll(Bitmap bitmapString imagePath)  {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis());
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/png");
       // values.put(MediaStore.Images.Media.DATA, imagePath);
        values.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DCIM);

       // values.put(MediaStore.Images.Media.DATA, outputStream.toString());
        final ContentResolver resolver = getContentResolver();

       Uri uri= resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
       try{
           final OutputStream stream = resolver.openOutputStream(uri);
           bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
       }catch (Exception e){
           Toast.makeText(this,"error  ",Toast.LENGTH_SHORT).show();

       }

        if (uri != null) {
            Toast.makeText(this,"image aded successs ",Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this,"image not aded ",Toast.LENGTH_SHORT).show();
        }
    }*/

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode ==WRITE_EXTERNAL_STORAGE&&grantResults.length > 0 &&grantResults[0]== PackageManager.PERMISSION_GRANTED) {
            //senddata();

        }else if (requestCode ==WRITE_EXTERNAL_STORAGE&&grantResults.length > 0 &&grantResults[0]==PackageManager.PERMISSION_DENIED) {
            Toast.makeText(getApplicationContext(), "Please Accept the permision", Toast.LENGTH_LONG).show();

        }

    }
      public void imageChooser(int requestcode) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, requestcode);


      }

      public void onActivityResult(int requestCode, int resultCode, Intent data) {
           super.onActivityResult(requestCode, resultCode, data);
          Log.d("TAG", "onActivityResult  ");

             if (resultCode == RESULT_OK) {
                 Uri selectedImageUri = data.getData();
                 String imageuri=selectedImageUri.toString();
                 try {
                   //  Bitmap bitmap = BitmapFactory.decodeResource(getResources(),R.drawable.shose);
                     Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), Uri.parse(imageuri));
                     common.uploadImage(bitmap);
                     Log.d("TAG", "imageuri "+imageuri);

                 } catch (Exception e/*IOException e*/) {
                     Log.d("TAG", "error galery ");

                     // throw new RuntimeException(e);
                 }

             }else{
                 Log.d("TAG", "error result ");

             }

          }

       // Register the permissions callback, which handles the user's response to the
       // system permissions dialog. Save the return value, an instance of
       // ActivityResultLauncher, as an instance variable.
         private ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    // Permission is granted. Continue the action or workflow in your
                    // app.
                } else {
                    Toast.makeText(getApplicationContext(), "Please Accept For Notification", Toast.LENGTH_LONG).show();

                }
            });



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
        //sendBroadcast(sendIntent);
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
                byte[] byteArray =intent.getByteArrayExtra(Image);
                if(byteArray!=null) {
                    Bitmap bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
                    imageView.setImageBitmap(bitmap);
                }

            }
            }
    };




       }