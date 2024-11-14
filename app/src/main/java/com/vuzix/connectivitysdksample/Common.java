package com.vuzix.connectivitysdksample;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class Common {
    Context context;
    SharedPreferences sharedPreferences;
    Common(Context context){
         this.context=context;
        sharedPreferences = context.getSharedPreferences("settings", Context.MODE_PRIVATE);
    }

    void Makerequest(String url,String bodydata,String requesttype,Callback callback){
        OkHttpClient client = new OkHttpClient.Builder()
                .readTimeout(30, TimeUnit.SECONDS)
                .build();
        Request.Builder requestBuilder=null;
        RequestBody body = null;

        if (bodydata != null) {
            MediaType mediaType = MediaType.parse("application/json");
            body = RequestBody.create(mediaType,bodydata);
        }
            if(Objects.equals(requesttype, "GET")) {
                 requestBuilder = new Request.Builder()
                        .url(url)
                        .addHeader("Content-Type", "application/json")
                        .addHeader("Accept", "application/json");
                        //.addHeader("x-api-key", apiKey)
                       // .method(requesttype, body);
            }else{
                requestBuilder = new Request.Builder()
                        .url(url)
                        .addHeader("Content-Type", "application/json")
                        .addHeader("Accept", "application/json")
                        //.addHeader("x-api-key", apiKey)
                        .method(requesttype, body);
            }
        Request request = requestBuilder.build();

        client.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.i("TAG","request failed :" +e.toString());
                callback.onDataFetched("request failed "+e);
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                String responcemessage =  response.body().string();
                callback.onDataFetched(responcemessage);
               // Log.i("TAG","Responce: "+ responcemessage);

            }
        });

    }
     public void processImage(String key){
         UUID requestuuid = UUID.randomUUID();
         UUID useruuid = UUID.randomUUID();
        String requestid=requestuuid.toString();
        String userid=useruuid.toString();
         Log.d("TAG", "requestid "+requestid);
         Log.d("TAG", "userid "+userid);
        // MainActivity.textView.setText("requestid: "+requestid+"userid "+userid);

         String url = "https://zq7spyy1f3.execute-api.us-east-1.amazonaws.com/dev/sneakers";
         String data  = "{\n" +
                 "                \"event_type\": \"process_image\",\n" +
                 "                \"s3_url\": \""+key+"\",\n" +
                 "                \"models\": [\"V12\"],\n" +
                 "                \"user_id\": \""+userid+"\",\n" +
                 "                \"is_workflow\": false,\n" +
                 "                \"request_id\": \""+requestid+"\"\n" +
                 " }";
         Callback    callback = new Callback() {
             @Override
             public void onDataFetched(String data) {
                 Log.d("TAG", "recccccccceved processImage"+data);
                 int dur = (int)getShared("duration",7000);
                 Handler handler = new Handler(Looper.getMainLooper());
                 handler.postDelayed(new Runnable() {
                     @Override
                     public void run() {
                         fetchInfo(requestid,userid);
                     }
                 }, dur);



             }
         };
         Makerequest(url,data,"POST",callback);
     }
     public void fetchInfo(String request_id,String user_id){
        String url = "https://zq7spyy1f3.execute-api.us-east-1.amazonaws.com/dev/sneakers?request_id="+request_id+"&user_id="+user_id;
         Log.d("TAG", " fetchurl "+url);
        Callback    callback = new Callback() {
             @Override
             public void onDataFetched(String data) {
                 Log.d("TAG", "recccccccceved fetchInfo"+data);
                 JSONObject jsonObject = StringToJson(data);
                 try {
                     JSONObject  body = jsonObject.getJSONObject("body");
                     String searchresult = body.getString("search_result");
                     JSONObject ob = StringToJson(searchresult);
                     String sku = ob.getString("sku");
                     String title = ob.getString("title");
                     updateUi("title: "+title+"\n sku "+sku);
                     sendNotification(sku,title);
                     hideProgress();
                     Log.d("TAG", "sku "+sku);
                     Log.d("title", "title "+title);
                 }catch (Exception e){
                     Log.e("TAG", "fetchInfo errrrrrr "+e.toString());
                     hideProgress();
                     updateUi("fetchInfo Error Try Again "+e);
                 }
             }
         };
         Makerequest(url,"","GET",callback);
      }
      public void uploadImage(Bitmap bitmap){
          UUID imageuuid = UUID.randomUUID();
          showProgress();
          String imageid=imageuuid.toString();
          Callback    callback = new Callback() {
              @Override
              public void onDataFetched(String data) {
                  Log.d("TAG", "recccccccceved "+data);
                  JSONObject jsonObject = StringToJson(data);
                  try {
                      String body = jsonObject.getString("body");
                      JSONObject ob = StringToJson(body);
                      String key = ob.getString("key");
                     // MainActivity.textView.setText(key);
                      processImage(key);
                      Log.d("TAG", "keyyyyyy "+key);
                  }catch (Exception e){
                      Log.d("TAG", "keyyyyyy "+"errrrrrr");
                     hideProgress();
                      updateUi("uploadImage Error Try Again"+e);
                  }
              }
          };
          String url = "https://zq7spyy1f3.execute-api.us-east-1.amazonaws.com/dev/images";
          String image = toBase64(bitmap);
         // Log.i("TAG","image:" +image);
          //if()
          String data = "{" +
                  "  \"image_id\": \""+imageid+".PNG\"," +
                  "  \"image\": \""+image+"\","+
                  "  \"file_type\": \"PNG\"" +
                  "}";
          Makerequest(url,data,"POST",callback);
      }



      public String toBase64(Bitmap bitmap){
      //  String base64Image = Base64.getUrlEncoder().withoutPadding().encodeToString(imageBytes);
        String image="";//logoimage.logo2;
        //Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(),R.drawable.sek);
        if(bitmap!=null){
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG,90,byteArrayOutputStream);
            byte [] bytes = byteArrayOutputStream.toByteArray();
            image= Base64.encodeToString(bytes,Base64.NO_WRAP);//Base64.encodeToString(bytes,Base64.NO_PADDING/*,Base64.CRLF*/);
        }
        return image;
       }

      public String readJsonFromAsset(Context context, String fileName) {
        String json = null;
        try {
            InputStream is = context.getAssets().open(fileName);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
          return json;

          }
    public JSONArray StringToJsonArray(String string){
        JSONArray json = null;

        try {
            json = new JSONArray(string);

            //textview.setText(data);
        } catch (JSONException e) {
            e.printStackTrace();
            Log.i("TAG","json convert error:"+e.toString());

            //Toast.makeText(context,"json convert error:"+e.toString(), Toast.LENGTH_LONG).show();
            // Onerror();
        }
        return json;
    }
    public JSONObject StringToJson(String string){
        JSONObject json = null;

        try {
            json = new JSONObject(string);


            //textview.setText(data);
        } catch (JSONException e) {
            e.printStackTrace();
            Log.i("TAG","json convert error:"+e.toString());

          //  Toast.makeText(context,"json convert error:"+e.toString(), Toast.LENGTH_LONG).show();
            // Onerror();
        }
        return json;
    }

public void showProgress(){

       // MainActivity.progressBar.setVisibility(View.VISIBLE);
}
    public void hideProgress(){
        Handler handler = new Handler(Looper.getMainLooper());

// In your background thread or Handler:
        handler.post(new Runnable() {
            @Override
            public void run() {
                // This code will run on the main thread
              //  MainActivity.progressBar.setVisibility(View.GONE);
            }
        });


    }
    public void updateUi(String data){

        Handler handler = new Handler(Looper.getMainLooper());

// In your background thread or Handler:
        handler.post(new Runnable() {
            @Override
            public void run() {
                // This code will run on the main thread
               // MainActivity.textView.setText(data);
            }
        });
    }
    public void sendNotification(String sku,String title){

        Handler handler = new Handler(Looper.getMainLooper());

// In your background thread or Handler:
        handler.post(new Runnable() {
            @Override
            public void run() {
                // This code will run on the main thread
                NotificationHelper n = new NotificationHelper(context);
                n.sendNotification(sku,title);
            }
        });
    }
    Object getShared(String key,Object defaultvalue) {
        // Method body
        if (defaultvalue instanceof String) {
            String str = (String) defaultvalue;
            return sharedPreferences.getString(key,str);
        }else if (defaultvalue instanceof Boolean) {
            boolean str = (boolean) defaultvalue;
            return sharedPreferences.getBoolean(key,str);
        }if (defaultvalue instanceof Integer) {
            int str = (int) defaultvalue;
            return sharedPreferences.getInt(key,str);
        }if (defaultvalue instanceof Long) {
            long str = (long) defaultvalue;
            return sharedPreferences.getLong(key,str);
        }if (defaultvalue instanceof Float) {
            float str = (float) defaultvalue;
            return sharedPreferences.getFloat(key,str);
        }if (defaultvalue instanceof Double) {
            double str = (double) defaultvalue;
            return sharedPreferences.getFloat(key, (float) str);
        }

        return null;
    }
    public void addtoShared(String key,Object value) {
        // Method body
        SharedPreferences.Editor editor=sharedPreferences.edit();
        if (value instanceof String) {
            String str = (String) value;
            editor.putString(key,str);
        }else if (value instanceof Boolean) {
            boolean str = (boolean) value;
            editor.putBoolean(key,str);
        }if (value instanceof Integer) {
            int str = (int) value;
            editor.putInt(key,str);
        }if (value instanceof Float) {
            float str = (float) value;
            editor.putFloat(key,str);
        }if (value instanceof Double) {
            double str = (double) value;
            editor.putFloat(key, (float) str);
        }if (value instanceof Long) {
            long str = (long) value;
            editor.putLong(key,str);
        }
        editor.commit();

    }
}
