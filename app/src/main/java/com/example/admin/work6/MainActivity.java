package com.example.admin.work6;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.hardware.Camera;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;

public class MainActivity extends Activity implements View.OnClickListener,SurfaceHolder.Callback {

    private SurfaceView mSurfaceView;  //相机视频浏览
    private ImageView mImageView;   //相片
    private SurfaceHolder mSurfaceHolder;
    private ImageView shutter;       //快照按钮
    private Camera mCamera = null;     //相机
    private boolean mPreviewRunning;   //运行相机浏览
    private static final int MENU_START = 1;
    private static final int MENU_SENSOR = 2;
    private Bitmap bitmap;     //相片 Bitmap

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        //设置布局文件
        setContentView(R.layout.activity_main);
        mSurfaceView = (SurfaceView) findViewById(R.id.camera);
        mImageView = (ImageView) findViewById(R.id.imag1);
        shutter = (ImageView) findViewById(R.id.shutter);
        //设置快照按钮事件
        shutter.setOnClickListener(this);
        mImageView.setVisibility(View.GONE);
        mSurfaceHolder = mSurfaceView.getHolder();
        //设置 SurfaceHolder 回调事件
        mSurfaceHolder.addCallback(this);
        mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }
    //快照按钮拍照事件
    public void onClick(View v){
        //判断是否可以进行拍照
        if(mPreviewRunning){
            shutter.setEnabled(false);
            //设置自动对焦
            mCamera.autoFocus(new Camera.AutoFocusCallback() {
                @Override
                public void onAutoFocus(boolean success, Camera camera) {
                    //聚焦后进行拍照
                    mCamera.takePicture(mShutterCallback,null,mPictureCallback);
                }
            });

        }


    }
    //相机图片拍照回调函数
    Camera.PictureCallback mPictureCallback = new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            //判定相片数据是否不为空
            if (data !=null){
                saveAndShow(data);

            }
        }
    };
    //快照回调函数
    Camera.ShutterCallback mShutterCallback = new Camera.ShutterCallback() {
        @Override
        public void onShutter() {
            System.out.println("快照回调函数...........");
        }
    };

    //SurfaceView 改变时调用
    public void surfaceChanged(SurfaceHolder holder,int format,int width,int height){
        try {

            //判断是否运行相机，运行就停止掉

            if(mPreviewRunning){
                 mCamera.stopPreview();

            }
        //启动相机

            mCamera.setPreviewDisplay(holder);
            mCamera.startPreview();
            mPreviewRunning = true;

        } catch (Exception e) {
            e.printStackTrace();
        }

    }
    // Surfaceview 创建时调用
    public void surfaceCreated(SurfaceHolder holder){
        setCameraParams();

    }
    //设置 Camera 参数
    private void setCameraParams() {
        if (mCamera != null)
        {
            return;
        }
        //创建相机，打开相机
        mCamera = Camera.open();
        //设置相机参数
        Camera.Parameters params = mCamera.getParameters();
        //拍照自动对焦
        params.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
        //设置预览帧速率
        params.setPreviewFrameRate(3);
        //设置预览格式
        params.setPictureFormat(PixelFormat.YCbCr_422_SP);
        //设置图片质量百分比
        params.set("jpeg-quality",85);
        //获取相机支持图片分辨率
        List<Camera.Size> list = params.getSupportedPictureSizes();
        Camera.Size size = list.get(0);
        int w = size.width;
        int h = size.height;
        //设置图片大小
        params.setPictureSize(w,h);
        //设置自动闪光灯
        params.setFlashMode(Camera.Parameters.FLASH_MODE_AUTO);
    }
    //Surfaceview消亡时调用
    public void surfaceDestroyed(SurfaceHolder holder){
        if (mCamera != null)
        {
            //停止相机预览
            mCamera.stopPreview();
            mPreviewRunning = false;
            //回收相机
            mCamera.release();
            mCamera = null;

        }

    }
    //创建菜单
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0,MENU_START,0,"重拍");
        menu.add(0,MENU_SENSOR,0,"打开相册");
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == MENU_START) {
            //重启相机拍照
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

            return true;
        } else if (item.getItemId() == MENU_SENSOR) {
            Intent intent = new Intent(this,AlbumActivity.class);
            startActivity(intent);
        }

    return super.onOptionsItemSelected(item);
    }
    //保存和显示图片
    public void saveAndShow(byte[] data){
        try
        {
            //图片 id
            String imageId = System.currentTimeMillis()+"";
            //相片保存路径
            String pathName = android.os.Environment.getExternalStorageDirectory().getPath()
                    +"/com.demo.pr4";
            //创建文件
            File file = new File(pathName);
            if (!file.exists()){
                file.mkdir();
            }
            pathName+="/"+imageId+".jpeg";
            file = new File(pathName);
            if (!file.exists()){
                file.createNewFile();  //文件不存在则新建
            }
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(data);
            fos.close();
            AlbumActivity album = new AlbumActivity();
            //读取相片Bitmap
            bitmap = album.loadImage(pathName);
            // bitmap = BitmapFactory.decodeFile(pathName,options);
            //设置到控件上显示
            mImageView.setImageBitmap(bitmap);
            mImageView.setVisibility(View.VISIBLE);
            mSurfaceView.setVisibility(View.GONE);
            //停止相机浏览
            if (mPreviewRunning)
            {
                mCamera.stopPreview();
                mPreviewRunning = false;
            }
            shutter.setEnabled(true);

        }catch (Exception e){
            e.printStackTrace();
        }

    }
}
