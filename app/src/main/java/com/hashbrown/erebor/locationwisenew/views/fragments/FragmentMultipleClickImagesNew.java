package com.hashbrown.erebor.locationwisenew.views.fragments;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
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
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.hashbrown.erebor.locationwisenew.R;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;


@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class FragmentMultipleClickImagesNew extends Fragment {
    public static FragmentMultipleClickImagesNew newInstance() {

        Bundle args = new Bundle();

        FragmentMultipleClickImagesNew fragment = new FragmentMultipleClickImagesNew();
        fragment.setArguments(args);
        return fragment;
    }

    private static final String TAG = "AndroidCameraApi";
    @BindView(R.id.changecamera)
    ImageView changecamera;
    @BindView(R.id.count)
    ImageView count;
    @BindView(R.id.count_number)
    TextView count_number;
    @BindView(R.id.done)
    ImageView done;
    @BindView(R.id.cancel)
    ImageView cancel;
    @BindView(R.id.btn_takepicture)
    ImageView takePictureButton;
    @BindView(R.id.texture)
    TextureView textureView;
    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();
    private static final SparseIntArray ORIENTATIONS_FRONT = new SparseIntArray();

    static {
        ORIENTATIONS.append(Surface.ROTATION_0, 90);
        ORIENTATIONS.append(Surface.ROTATION_90, 0);
        ORIENTATIONS.append(Surface.ROTATION_180, 270);
        ORIENTATIONS.append(Surface.ROTATION_270, 180);
    }

    static {
        ORIENTATIONS_FRONT.append(Surface.ROTATION_0, 270);
        ORIENTATIONS_FRONT.append(Surface.ROTATION_90, 0);
        ORIENTATIONS_FRONT.append(Surface.ROTATION_180, 180);
        ORIENTATIONS_FRONT.append(Surface.ROTATION_270, 270);
    }

    public static final String CAMERA_FRONT = "1";
    public static final String CAMERA_BACK = "0";
    private String cameraId = CAMERA_BACK;


    protected CameraDevice cameraDevice;
    protected CameraCaptureSession cameraCaptureSessions;
    protected CaptureRequest captureRequest;
    protected CaptureRequest.Builder captureRequestBuilder;
    private Size imageDimension;
    private ImageReader imageReader;
    private File file;
    private static final int REQUEST_CAMERA_PERMISSION = 200;
    private boolean mFlashSupported;
    private Handler mBackgroundHandler;
    private HandlerThread mBackgroundThread;
    ArrayList<String> filenames = new ArrayList<>();
    int i = 0;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragmnet_multiple_click_images_new, container, false);
        ButterKnife.bind(this, v);
        assert textureView != null;
        textureView.setSurfaceTextureListener(textureListener);
        // count.setText(String.valueOf(i));
        assert takePictureButton != null;
        manager = (CameraManager) getActivity().getSystemService(Context.CAMERA_SERVICE);
        try {
            cameraId = manager.getCameraIdList()[0];
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        return v;
    }

    TextureView.SurfaceTextureListener textureListener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
            //open your camera here
            openCamera();
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
            // Transform you image captured size according to the surface width and height
        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            surface.release();
            return true;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surface) {
        }
    };

    @OnClick(R.id.btn_takepicture)
    void OnTakePicture() {
        takePicture();
    }

    @OnClick(R.id.cancel)
    void oncancel() {
        FragmentHomeScreen fragment = FragmentHomeScreen.newInstance();
        FragmentTransaction fts = getActivity().getSupportFragmentManager().beginTransaction();
        fts.replace(R.id.home_coordinate, fragment);
        fts.commit();
        deleteBackupForloc_mid();

    }

    @OnClick(R.id.done)
    void onDone() {
        closeCamera();
        for (int i = 0; i < filenames.size(); i++) {
            System.out.println(filenames.get(i));
        }

        FragmentMultipleSelectedImages fragment = FragmentMultipleSelectedImages.newInstance(filenames);
        FragmentTransaction fts = getActivity().getSupportFragmentManager().beginTransaction();
        fts.add(R.id.home_coordinate, fragment);
        // fts.addToBackStack(fragment.getClass().getSimpleName());
        fts.commit();
        // navigateReplacingCurrent(FragmentMultipleClickImagesNew.newInstance(),FragmentMultipleSelectedImages.newInstance());
    }

    public void navigateReplacingCurrent(Fragment currentFragment, Fragment fragmentToNavigate) {
        FragmentTransaction fts = getActivity().getSupportFragmentManager().beginTransaction();
        getActivity().getSupportFragmentManager().popBackStack();
        fts.replace(R.id.home_coordinate, fragmentToNavigate);
        fts.addToBackStack(fragmentToNavigate.getClass().getSimpleName());
        fts.remove(currentFragment).commit();
    }

    @OnClick(R.id.changecamera)
    void onchange() {
        switchCamera();
    }


    private final CameraDevice.StateCallback stateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(CameraDevice camera) {
            //This is called when the camera is open
            Log.e(TAG, "onOpened");
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
    final CameraCaptureSession.CaptureCallback captureCallbackListener = new CameraCaptureSession.CaptureCallback() {
        @Override
        public void onCaptureCompleted(CameraCaptureSession session, CaptureRequest request, TotalCaptureResult result) {
            super.onCaptureCompleted(session, request, result);

            //  Toast.makeText(MainActivity.this, "Saved:" , Toast.LENGTH_SHORT).show();
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    count.setVisibility(View.VISIBLE);
                    done.setVisibility(View.VISIBLE);
                    i = i + 1;
                    count_number.setText(String.valueOf(i));
                    Glide.with(getActivity()).load(Uri.fromFile(new File(filename))).dontAnimate().into(count);


                }
            });
            createCameraPreview();
        }
    };

    protected void startBackgroundThread() {
        mBackgroundThread = new HandlerThread("Camera Background");
        mBackgroundThread.start();
        mBackgroundHandler = new Handler(mBackgroundThread.getLooper());
    }

    protected void stopBackgroundThread() {

        try {
            if (mBackgroundThread != null) {
                mBackgroundThread.quitSafely();
                mBackgroundThread.join();
                mBackgroundThread = null;
                mBackgroundHandler = null;
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    protected void takePicture() {
        if (null == cameraDevice) {
            Log.e(TAG, "cameraDevice is null");
            return;
        }
        CameraManager manager = (CameraManager) getActivity().getSystemService(Context.CAMERA_SERVICE);
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
//            int rotation = getActivity().getWindowManager().getDefaultDisplay().getRotation();
//
//            captureBuilder.set(CaptureRequest.JPEG_ORIENTATION, ORIENTATIONS.get(rotation));
//


            if (cameraId.equals(CAMERA_FRONT)) {
                int rotation1 = characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);
                captureBuilder.set(CaptureRequest.JPEG_ORIENTATION, rotation1);
            } else if (cameraId.equals(CAMERA_BACK)) {
                int rotation = getActivity().getWindowManager().getDefaultDisplay().getRotation();
                captureBuilder.set(CaptureRequest.JPEG_ORIENTATION, ORIENTATIONS.get(rotation));
            }

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
                    createFolder(bytes);
                }
            };
            reader.setOnImageAvailableListener(readerListener, mBackgroundHandler);
            final CameraCaptureSession.CaptureCallback captureListener = new CameraCaptureSession.CaptureCallback() {
                @Override
                public void onCaptureCompleted(CameraCaptureSession session, CaptureRequest request, TotalCaptureResult result) {
                    super.onCaptureCompleted(session, request, result);
                    createCameraPreview();
                    //Toast.makeText(MainActivity.this, "Saved:", Toast.LENGTH_SHORT).show();
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            count.setVisibility(View.VISIBLE);
                            done.setVisibility(View.VISIBLE);
                            i = i + 1;
                            count_number.setText(String.valueOf(i));
                            Glide.with(getActivity()).load(Uri.fromFile(new File(filename))).dontAnimate().into(count);
                        }
                    });


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

    protected void createCameraPreview() {
        try {
            SurfaceTexture texture = textureView.getSurfaceTexture();
            assert texture != null;
            texture.setDefaultBufferSize(imageDimension.getWidth(), imageDimension.getHeight());
            Surface surface = new Surface(texture);
            captureRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            captureRequestBuilder.addTarget(surface);
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
                    Toast.makeText(getActivity(), "Configuration change", Toast.LENGTH_SHORT).show();
                }
            }, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    CameraManager manager;

    private void openCamera() {
        manager = (CameraManager) getActivity().getSystemService(Context.CAMERA_SERVICE);
        Log.e(TAG, "is camera open");
        try {
            //   cameraId = manager.getCameraIdList()[0];
            CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);
            StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);


            assert map != null;
            imageDimension = map.getOutputSizes(SurfaceTexture.class)[0];
            // Add permission for camera and let user grant the permission
            if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CAMERA_PERMISSION);
                return;
            }
            manager.openCamera(cameraId, stateCallback, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        Log.e(TAG, "openCamera X");
    }

    protected void updatePreview() {
        if (null == cameraDevice) {
            Log.e(TAG, "updatePreview error, return");
        }
        captureRequestBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
        try {
            cameraCaptureSessions.setRepeatingRequest(captureRequestBuilder.build(), null, mBackgroundHandler);
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
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                // close the app
                Toast.makeText(getActivity(), "Sorry!!!, you can't use this app without granting permission", Toast.LENGTH_LONG).show();
                getActivity().finish();
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        filenames.clear();
        Log.e(TAG, "onResume");
        startBackgroundThread();
        if (textureView.isAvailable()) {
            openCamera();
        } else {
            textureView.setSurfaceTextureListener(textureListener);
        }
    }

    @Override
    public void onPause() {
        Log.e(TAG, "onPause");
        closeCamera();
        stopBackgroundThread();
        super.onPause();
    }

    public void createFolder(byte[] bytes) throws IOException {
        //create a folder
        File path_to_folder = new File("/sdcard/tmploc");

        if (path_to_folder.exists()) {
            // Toast.makeText(this, "Alreday Exists", Toast.LENGTH_SHORT).show();
            manageFile(bytes);
        } else {
            boolean dir_success = path_to_folder.mkdirs();
            if (dir_success == true) {

                manageFile(bytes);

            } else {
                //Toast.makeText(this, "Folder Not Created", Toast.LENGTH_SHORT).show();
            }
        }
    }

    String filename;

    public void manageFile(byte[] bytes) throws IOException {


        //name with milli seconds
        long seconds = System.currentTimeMillis();
        filename = "sdcard/tmploc/" + seconds + ".png";
        filenames.add(filename);
        File path_to_file = new File(filename);
        if (path_to_file.exists()) {
            boolean delete = path_to_file.delete();
            if (delete == true) {
                writeFile(bytes);
                // Toast.makeText(this, "Deleted Successfully", Toast.LENGTH_SHORT).show();

            } else {

                // Toast.makeText(this, "Deletion Failed", Toast.LENGTH_SHORT).show();
            }

        } else {
            writeFile(bytes);

        }
    }

    void writeFile(byte[] bytes) throws IOException {


        OutputStream output = null;
        try {
            output = new FileOutputStream(filename);
            output.write(bytes);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            if (null != output) {
                output.close();
            }
        }

    }

    public void switchCamera() {

        try {
            if (cameraId.equals(manager.getCameraIdList()[0])) {
                cameraId = manager.getCameraIdList()[1];
                closeCamera();
                openCamera();


            } else if (cameraId.equals(manager.getCameraIdList()[1])) {
                cameraId = manager.getCameraIdList()[0];
                closeCamera();
                openCamera();

            }
        } catch (Exception e) {
            Toast.makeText(getActivity(), "There are Issues With Your Camera Hardware", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopBackgroundThread();
        closeCamera();

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        deleteBackupForloc_mid();
    }

    private void deleteBackupForloc_mid()
    {
        try {
            for (int i = 0; i < filenames.size(); i++) {
                File fichero = new File(filenames.get(i));
                File carpeta = fichero.getParentFile();
                for (File file : carpeta.listFiles()) {
                    file.delete();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();

        }
    }


}
