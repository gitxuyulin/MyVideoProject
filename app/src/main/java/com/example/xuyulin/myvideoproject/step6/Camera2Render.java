package com.example.xuyulin.myvideoproject.step6;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureFailure;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.os.Build;
import android.os.Environment;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.Surface;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * 作者： xuyulin on 2018/10/22.
 * 邮箱： xuyulin@yixia.com
 * 描述：
 */
@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class Camera2Render implements GLSurfaceView.Renderer {

    private Context mContext;
    private GLSurfaceView mSurfaceView;
    private ImageView miv;
    private Button mbtn;
    private CameraDevice mCameraDevice;
    private Size previewSize;
    public static final int RESULT_CODE_CAMERA = 1;//判断是否有拍照权限的标识码
    private String mCameraId = "0";//摄像头id（通常0代表后置摄像头，1代表前置摄像头）
    private CaptureRequest.Builder mCaptureRequestBuilder, captureRequestBuilder;
    private CaptureRequest mCaptureRequest;
    private CameraCaptureSession mPreviewSession;
    private int mOutputTextureId = 0;
    private int width = 540, height = 960;
    private ImageReader imageReader;
    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();

    static {
        ORIENTATIONS.append(Surface.ROTATION_0, 90);
        ORIENTATIONS.append(Surface.ROTATION_90, 0);
        ORIENTATIONS.append(Surface.ROTATION_180, 270);
        ORIENTATIONS.append(Surface.ROTATION_270, 180);
    }

    public Camera2Render(Context context, GLSurfaceView surfaceView, ImageView iv, Button btn) {
        this.mContext = context;
        this.mSurfaceView = surfaceView;
        this.miv = iv;
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                takePicture();
            }
        });
//        openCamera();
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {

    }

    @Override
    public void onDrawFrame(GL10 gl) {

    }

    public void onPause() {
        if (mCameraDevice != null) {
            stopCamera();
        }
    }

    public void onResume() {
        startCamera();
    }

    public void onDestory() {
        if (mCameraDevice != null) {
            stopCamera();
        }
    }

    /**
     * 打开摄像头
     */
    public void openCamera() {
        CameraManager manager = (CameraManager) mContext.getSystemService(Context.CAMERA_SERVICE);
        //设置摄像头特性
        setCameraCharacteristics(manager);
        try {
            if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                //提示用户开户权限
                String[] perms = {"android.permission.CAMERA"};
                ActivityCompat.requestPermissions((Camera2Activity) mContext, perms, RESULT_CODE_CAMERA);

            } else {
                manager.openCamera(mCameraId, stateCallback, null);
            }

        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    /**
     * 设置摄像头的参数
     */
    private void setCameraCharacteristics(CameraManager manager) {
        try {
            // 获取指定摄像头的特性
            CameraCharacteristics characteristics
                    = manager.getCameraCharacteristics(mCameraId);
            // 获取摄像头支持的配置属性
            StreamConfigurationMap map = characteristics.get(
                    CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            // 获取摄像头支持的最大尺寸
            Size largest = Collections.max(
                    Arrays.asList(map.getOutputSizes(ImageFormat.JPEG)), new MainActivity.CompareSizesByArea());
            // 创建一个ImageReader对象，用于获取摄像头的图像数据
            imageReader = ImageReader.newInstance(largest.getWidth(), largest.getHeight(),
                    ImageFormat.JPEG, 2);
            //设置获取图片的监听
            imageReader.setOnImageAvailableListener(imageAvailableListener, null);
            // 获取最佳的预览尺寸
            previewSize = chooseOptimalSize(map.getOutputSizes(
                    SurfaceTexture.class), width, height, largest);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
        }
    }

    private static Size chooseOptimalSize(Size[] choices
            , int width, int height, Size aspectRatio) {
        // 收集摄像头支持的大过预览Surface的分辨率
        List<Size> bigEnough = new ArrayList<>();
        int w = aspectRatio.getWidth();
        int h = aspectRatio.getHeight();
        for (Size option : choices) {
            if (option.getHeight() == option.getWidth() * h / w &&
                    option.getWidth() >= width && option.getHeight() >= height) {
                bigEnough.add(option);
            }
        }
        // 如果找到多个预览尺寸，获取其中面积最小的
        if (bigEnough.size() > 0) {
            return Collections.min(bigEnough, new MainActivity.CompareSizesByArea());
        } else {
            //没有合适的预览尺寸
            return choices[0];
        }
    }

    // 为Size定义一个比较器Comparator
    static class CompareSizesByArea implements Comparator<Size> {
        @Override
        public int compare(Size lhs, Size rhs) {
            // 强转为long保证不会发生溢出
            return Long.signum((long) lhs.getWidth() * lhs.getHeight() -
                    (long) rhs.getWidth() * rhs.getHeight());
        }
    }

    /**
     * 摄像头状态的监听
     */
    private CameraDevice.StateCallback stateCallback = new CameraDevice.StateCallback() {
        // 摄像头被打开时触发该方法
        @Override
        public void onOpened(CameraDevice cameraDevice) {
            mCameraDevice = cameraDevice;
            // 开始预览
            takePreview();
        }

        // 摄像头断开连接时触发该方法
        @Override
        public void onDisconnected(CameraDevice cameraDevice) {
            mCameraDevice.close();
            mCameraDevice = null;

        }

        // 打开摄像头出现错误时触发该方法
        @Override
        public void onError(CameraDevice cameraDevice, int error) {
            cameraDevice.close();
        }
    };

    /**
     * 开始预览
     */
    private void takePreview() {
        int[] textures = new int[1];
        GLES20.glGenTextures(1, textures, 0);
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textures[0]);
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
        mOutputTextureId = textures[0];
        SurfaceTexture mSurfaceTexture = new SurfaceTexture(mOutputTextureId);
        //设置TextureView的缓冲区大小
        mSurfaceTexture.setDefaultBufferSize(previewSize.getWidth(), previewSize.getHeight());
        //获取Surface显示预览数据
        Surface mSurface = new Surface(mSurfaceTexture);
        try {
            //创建预览请求
            mCaptureRequestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            // 设置自动对焦模式
            mCaptureRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
            //设置Surface作为预览数据的显示界面
            mCaptureRequestBuilder.addTarget(mSurface);
            //创建相机捕获会话，第一个参数是捕获数据的输出Surface列表，第二个参数是CameraCaptureSession的状态回调接口，当它创建好后会回调onConfigured方法，第三个参数用来确定Callback在哪个线程执行，为null的话就在当前线程执行
            mCameraDevice.createCaptureSession(Arrays.asList(mSurface, imageReader.getSurface()), new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(CameraCaptureSession session) {
                    try {
                        //开始预览
                        mCaptureRequest = mCaptureRequestBuilder.build();
                        mPreviewSession = session;
                        //设置反复捕获数据的请求，这样预览界面就会一直有数据显示
                        mPreviewSession.setRepeatingRequest(mCaptureRequest, null, null);
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }

                }

                @Override
                public void onConfigureFailed(CameraCaptureSession session) {

                }
            }, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }

    }

    /**
     * 拍照
     */
    private void takePicture() {
        try {
            if (mCameraDevice == null) {
                return;
            }
            // 创建拍照请求
            captureRequestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
            // 设置自动对焦模式
            captureRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
            // 将imageReader的surface设为目标
            captureRequestBuilder.addTarget(imageReader.getSurface());
            // 获取设备方向
            int rotation = ((Camera2Activity) mContext).getWindowManager().getDefaultDisplay().getRotation();
            // 根据设备方向计算设置照片的方向
            captureRequestBuilder.set(CaptureRequest.JPEG_ORIENTATION
                    , ORIENTATIONS.get(rotation));
            // 停止连续取景
            mPreviewSession.stopRepeating();
            //拍照
            CaptureRequest captureRequest = captureRequestBuilder.build();
            //设置拍照监听
            mPreviewSession.capture(captureRequest, captureCallback, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    /**
     * 监听拍照结果
     */
    private CameraCaptureSession.CaptureCallback captureCallback = new CameraCaptureSession.CaptureCallback() {
        // 拍照成功
        @Override
        public void onCaptureCompleted(CameraCaptureSession session, CaptureRequest request, TotalCaptureResult result) {
            // 重设自动对焦模式
            captureRequestBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER, CameraMetadata.CONTROL_AF_TRIGGER_CANCEL);
            // 设置自动曝光模式
            captureRequestBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH);
            try {
                //重新进行预览
                mPreviewSession.setRepeatingRequest(mCaptureRequest, null, null);
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }

        }

        @Override
        public void onCaptureFailed(CameraCaptureSession session, CaptureRequest request, CaptureFailure failure) {
            super.onCaptureFailed(session, request, failure);
        }
    };

    /**
     * 监听拍照的图片
     */
    private ImageReader.OnImageAvailableListener imageAvailableListener = new ImageReader.OnImageAvailableListener() {
        // 当照片数据可用时激发该方法
        @Override
        public void onImageAvailable(ImageReader reader) {

            //先验证手机是否有sdcard
            String status = Environment.getExternalStorageState();
            if (!status.equals(Environment.MEDIA_MOUNTED)) {
                Toast.makeText(mContext.getApplicationContext(), "你的sd卡不可用。", Toast.LENGTH_SHORT).show();
                return;
            }
            // 获取捕获的照片数据
            Image image = reader.acquireNextImage();
            ByteBuffer buffer = image.getPlanes()[0].getBuffer();
            byte[] data = new byte[buffer.remaining()];
            buffer.get(data);

            //手机拍照都是存到这个路径
            String filePath = Environment.getExternalStorageDirectory().getPath() + "/DCIM/Camera/";
            String picturePath = System.currentTimeMillis() + ".jpg";
            File file = new File(filePath, picturePath);
            try {
                //存到本地相册
                FileOutputStream fileOutputStream = new FileOutputStream(file);
                fileOutputStream.write(data);
                fileOutputStream.close();

                //显示图片
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inSampleSize = 2;
                Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length, options);
                miv.setImageBitmap(bitmap);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                image.close();
            }
        }


    };

    /**
     * 启动拍照
     */
    private void startCamera() {
        if (mCameraDevice == null) {
            openCamera();
        }
    }

    /**
     * 停止拍照释放资源
     */
    private void stopCamera() {
        if (mCameraDevice != null) {
            mCameraDevice.close();
            mCameraDevice = null;
        }

    }

}
