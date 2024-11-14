package com.vuzix.connectivitysdksample;
import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;

public class CameraApp extends AppCompatActivity {
    private static final String TAG = "AndroidCameraApi";
    private ImageView capture,preview;
    private TextureView textureView,textureViewtwo;
    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();

    static {
        ORIENTATIONS.append(Surface.ROTATION_0, 90);
        ORIENTATIONS.append(Surface.ROTATION_90, 0);
        ORIENTATIONS.append(Surface.ROTATION_180, 270);
        ORIENTATIONS.append(Surface.ROTATION_270, 180);
    }

    private String cameraId,cameraIdtwo;
    protected CameraDevice cameraDevice,cameraDevicetwo;
    protected CameraCaptureSession cameraCaptureSessions,cameraCaptureSessionstwo;
    protected CaptureRequest captureRequest;
    protected CaptureRequest.Builder captureRequestBuilder,captureRequestBuildertwo;
    private Size imageDimension,imageDimensiontwo;
    private ImageReader imageReader;
    private File file;
    private static final int REQUEST_CAMERA_PERMISSION = 200;
    private boolean mFlashSupported;
    private Handler mBackgroundHandler,mBackgroundHandlertwo;
    private HandlerThread mBackgroundThread,mBackgroundThreadtwo;
    ImageView cancel ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cameralayout);
        textureView = findViewById(R.id.textureView);
        textureViewtwo = findViewById(R.id.textureView2);
        cancel = findViewById(R.id.cancel);

        assert textureView != null;
        //textureView.setSurfaceTextureListener(textureListener);
        capture = findViewById(R.id.button_capture);
        preview = findViewById(R.id.captured);

        assert capture != null;
        OkHttpClient defaultClient = new OkHttpClient.Builder()
                .readTimeout(1, TimeUnit.SECONDS)
                .build();
          capture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                takePicture();

            }
          });
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
           /* Glide.with(capture)
                .load(R.drawable.cameraicon).placeholder(R.drawable.cameraicon)
                //.apply(new RequestOptions().override(210,180))
                //  .centerCrop().transform(new CenterCrop(),new RoundedCorners(50))
                .circleCrop()
                .into(capture);*/


          }

    TextureView.SurfaceTextureListener textureListener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
            //open your camera here
            if (textureView.isAvailable()/*&&textureViewtwo.isAvailable()*/) {
                openCamera();
               // openCameratwo();
            }
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
            // Transform you image captured size according to the surface width and height
        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            return false;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surface) {
        }
    };

    private final CameraDevice.StateCallback stateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(CameraDevice camera) {
            //This is called when the camera is open
            Log.d(TAG, "onOpened");
            cameraDevice = camera;
            createCameraPreview();
        }

        @Override
        public void onDisconnected(CameraDevice camera) {
            cameraDevice.close();
        }

        @Override
        public void onError(CameraDevice camera, int error) {
            cameraDevice.close();
            cameraDevice = null;
        }
    };

    private final CameraDevice.StateCallback stateCallbacktwo = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(CameraDevice camera) {
            //This is called when the camera is open
            Log.d(TAG, "onOpened");
            cameraDevicetwo = camera;
            createCameraPreviewTwo();
        }

        @Override
        public void onDisconnected(CameraDevice camera) {
            cameraDevice.close();
        }

        @Override
        public void onError(CameraDevice camera, int error) {
            cameraDevicetwo.close();
            cameraDevicetwo = null;
        }
    };


    final CameraCaptureSession.CaptureCallback captureCallbackListener = new CameraCaptureSession.CaptureCallback() {
        @Override
        public void onCaptureCompleted(CameraCaptureSession session, CaptureRequest request, TotalCaptureResult result) {
            super.onCaptureCompleted(session, request, result);
            Toast.makeText(CameraApp.this, "Saved:" + file, Toast.LENGTH_SHORT).show();
            createCameraPreview();
        }
    };

    protected void startBackgroundThread() {
        mBackgroundThread = new HandlerThread("Camera Background");
        mBackgroundThread.start();
        mBackgroundHandler = new Handler(mBackgroundThread.getLooper());
    }
    protected void startBackgroundThreadTwo() {
        mBackgroundThreadtwo = new HandlerThread("Camera Background");
        mBackgroundThreadtwo.start();
        mBackgroundHandlertwo = new Handler(mBackgroundThreadtwo.getLooper());
    }

    protected void stopBackgroundThread() {
        mBackgroundThread.quitSafely();
        try {
            mBackgroundThread.join();
            mBackgroundThread = null;
            mBackgroundHandler = null;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    protected void takePicture() {
        if (null == cameraDevice) {
            Log.d(TAG, "cameraDevice is null");
            return;
        }
        CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraDevice.getId());
            Size[] jpegSizes = null;
            if (characteristics != null) {
                jpegSizes = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP).getOutputSizes(ImageFormat.JPEG);
            }
            int width = 640;
            int height = 480;
            if (jpegSizes != null && 0 < jpegSizes.length) {
                width = jpegSizes[0].getWidth();
                height = jpegSizes[0].getHeight();
            }
            ImageReader reader = ImageReader.newInstance(width, height, ImageFormat.JPEG, 1);
            List<Surface> outputSurfaces = new ArrayList<Surface>(2);
            outputSurfaces.add(reader.getSurface());
            outputSurfaces.add(new Surface(textureView.getSurfaceTexture()));
            final CaptureRequest.Builder captureBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
            captureBuilder.addTarget(reader.getSurface());
            captureBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
            // Orientation
            int rotation = getWindowManager().getDefaultDisplay().getRotation();
            captureBuilder.set(CaptureRequest.JPEG_ORIENTATION, ORIENTATIONS.get(rotation));
            final File file = new File(Environment.getExternalStorageDirectory() + "/pic.jpg");
            ImageReader.OnImageAvailableListener readerListener = new ImageReader.OnImageAvailableListener() {
                @Override
                public void onImageAvailable(ImageReader reader) {
                    Image image = null;
                    try {
                        image = reader.acquireLatestImage();
                        ByteBuffer buffer = image.getPlanes()[0].getBuffer();
                        byte[] bytes = new byte[buffer.capacity()];
                        buffer.get(bytes);
                        save(bytes);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        if (image != null) {
                            image.close();
                        }
                    }
                }

                private void save(byte[] bytes) throws IOException {
                        senddata(bytes);
                    OutputStream output = null;
                    try {
                        output = new FileOutputStream(file);
                        output.write(bytes);
                    } finally {
                        if (null != output) {
                            output.close();
                        }
                    }
                }
            };
            reader.setOnImageAvailableListener(readerListener, mBackgroundHandler);
            final CameraCaptureSession.CaptureCallback captureListener = new CameraCaptureSession.CaptureCallback() {
                @Override
                public void onCaptureCompleted(CameraCaptureSession session, CaptureRequest request, TotalCaptureResult result) {
                    super.onCaptureCompleted(session, request, result);
                    Toast.makeText(CameraApp.this, "Saved:" + file, Toast.LENGTH_SHORT).show();
                    createCameraPreview();
                }
            };
            cameraDevice.createCaptureSession(outputSurfaces, new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(CameraCaptureSession session) {
                    try {
                        session.capture(captureBuilder.build(), captureListener, mBackgroundHandler);
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onConfigureFailed(CameraCaptureSession session) {
                }
            }, mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    protected void createCameraPreviewTwo() {
        try {
            SurfaceTexture texturetwo = textureViewtwo.getSurfaceTexture();
            assert texturetwo != null;
            texturetwo.setDefaultBufferSize(imageDimensiontwo.getWidth(), imageDimensiontwo.getHeight());
            Surface surfacetwo = new Surface(texturetwo);
            captureRequestBuildertwo = cameraDevicetwo.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            captureRequestBuildertwo.addTarget(surfacetwo);

            cameraDevicetwo.createCaptureSession(Arrays.asList(surfacetwo), new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
                    //The camera is already closed
                    if (null == cameraDevicetwo) {
                        return;
                    }
                    // When the session is ready, we start displaying the preview.
                    cameraCaptureSessionstwo = cameraCaptureSession;
                    updatePreviewtwo();
                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {
                    Toast.makeText(CameraApp.this, "Configuration change", Toast.LENGTH_SHORT).show();
                }
            }, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    protected void createCameraPreview() {
        try {
            SurfaceTexture texture = textureView.getSurfaceTexture();
            assert texture != null;
            texture.setDefaultBufferSize(imageDimension.getWidth(), imageDimension.getHeight());
            Surface surface = new Surface(texture);
            captureRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            captureRequestBuilder.addTarget(surface);

            //neeeeeeeeeeee
          /*  SurfaceTexture texturetwo = textureViewtwo.getSurfaceTexture();
            assert texture != null;
            texturetwo.setDefaultBufferSize(imageDimension.getWidth(), imageDimension.getHeight());
            Surface surfacetwo = new Surface(texturetwo);
            captureRequestBuilder.addTarget(surfacetwo);*/

            cameraDevice.createCaptureSession(Arrays.asList(surface), new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
                    //The camera is already closed
                    if (null == cameraDevice) {
                        return;
                    }
                    // When the session is ready, we start displaying the preview.
                    cameraCaptureSessions = cameraCaptureSession;
                    updatePreview();
                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {
                    Toast.makeText(CameraApp.this, "Configuration change", Toast.LENGTH_SHORT).show();
                }
            }, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void openCamera() {
        CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        Log.d(TAG, "is camera open");
        try {
            cameraId = manager.getCameraIdList()[0];
            CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);
            StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            assert map != null;
            imageDimension = map.getOutputSizes(SurfaceTexture.class)[0];
            // Add permission for camera and let user grant the permission
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(CameraApp.this, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CAMERA_PERMISSION);
                return;
            }
            manager.openCamera(cameraId, stateCallback, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        Log.d(TAG, "openCamera X");
    }

    private void openCameratwo() {
        CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        Log.d(TAG, "is camera open");
        try {
            cameraIdtwo = manager.getCameraIdList()[1];
            CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraIdtwo);
            StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            assert map != null;
            imageDimensiontwo = map.getOutputSizes(SurfaceTexture.class)[0];
            // Add permission for camera and let user grant the permission
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(CameraApp.this, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CAMERA_PERMISSION);
                return;
            }
            manager.openCamera(cameraIdtwo, stateCallbacktwo, mBackgroundHandlertwo);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        Log.d(TAG, "openCamera X");
    }


    protected void updatePreview() {
        if (null == cameraDevice) {
            Log.d(TAG, "updatePreview error, return");
        }
        captureRequestBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
        try {
            cameraCaptureSessions.setRepeatingRequest(captureRequestBuilder.build(), null, mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }
    protected void updatePreviewtwo() {
        if (null == cameraDevicetwo) {
            Log.d(TAG, "updatePreview error, return");
        }
        captureRequestBuildertwo.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
        try {
            cameraCaptureSessionstwo.setRepeatingRequest(captureRequestBuildertwo.build(), null, mBackgroundHandlertwo);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }
    private void closeCamera() {
        if (null != cameraDevice) {
            cameraDevice.close();
            cameraDevice = null;
        }
        if (null != imageReader) {
            imageReader.close();
            imageReader = null;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                // close the app
                Toast.makeText(CameraApp.this, "Sorry!!!, you can't use this app without granting permission", Toast.LENGTH_LONG).show();
                finish();
            }else{
              openCamera();
            }
        }
    }

    @Override
    protected void onResume() {

        super.onResume();

        Log.d(TAG, "onResume");
       /* startBackgroundThread();
        startBackgroundThreadTwo();*/
        if (textureView.isAvailable()&&textureViewtwo.isAvailable()) {
            openCamera();
        } else {
            textureView.setSurfaceTextureListener(textureListener);
           // textureViewtwo.setSurfaceTextureListener(textureListener);

        }
       /* if (textureViewtwo.isAvailable()) {
            openCameratwo();
        } else {
            textureViewtwo.setSurfaceTextureListener(textureListener);
        }*/
    }

    @Override
    protected void onPause() {
        Log.d(TAG, "onPause");
        closeCamera();

//        stopBackgroundThread();
        super.onPause();
    }
    public void senddata(byte[] bytes){
        int [] result= {-1,-1} ;
        int r=result.length;
        preview.setVisibility(View.VISIBLE);
        Bitmap bitmapp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
           preview.setImageBitmap(bitmapp);
       /* Intent intent = new Intent("com.example.myapp.MY_ACTION");
        intent.putExtra("message", bytes);
        sendBroadcast(intent);*/
       /* Drawable drawable = getResources().getDrawable(R.drawable.buna);
        Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        byte[] byteArray = stream.toByteArray();

        intent.putExtra("message", "byteArray");
        sendBroadcast(intent);*/
        Log.d("TAG", "senttttttttttttttttt");
    }

    @Override
    public void onBackPressed() {
        if(preview.getVisibility()==View.VISIBLE){
            preview.setVisibility(View.GONE);
        }else {
             super.onBackPressed();
        }
    }
}
