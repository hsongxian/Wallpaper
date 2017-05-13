# Wallpaper

### 前言
我在 Google Play 上看到一个挺有意思的app:

![](http://upload-images.jianshu.io/upload_images/2215276-570e2fa766d02641.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

>Url :      https://play.google.com/store/apps/details?id=com.m2c.studio.transparent.screen

看着有点酷的样子,试着实现以下.

### 最终实现效果

![效果](http://upload-images.jianshu.io/upload_images/2215276-b39566a29680bc52.gif?imageMogr2/auto-orient/strip)

### 实现步骤

*   AndroidManifest.xml

```
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
          package="com.song.wallpaper">
    <uses-permission android:name="android.permission.CAMERA"/>
    <uses-permission android:name="android.permission.SET_WALLPAPER"/>
    <application
        android:allowBackup="true"
        android:icon="@mipmap/ic_launcher"
        android:label="@string/app_name"
        android:supportsRtl="true"
        android:theme="@style/AppTheme">
        <activity android:name=".MainActivity">
            <intent-filter>
                <action android:name="android.intent.action.MAIN"/>
                <category android:name="android.intent.category.LAUNCHER"/>
            </intent-filter>
        </activity>
        <!-- 配置实时壁纸Service -->
        <service android:label="@string/app_name"
                 android:name=".CameraLiveWallpaper"
                 android:permission="android.permission.BIND_WALLPAPER">
            <!-- 为实时壁纸配置intent-filter -->
            <intent-filter>
                <action  android:name="android.service.wallpaper.WallpaperService" />
            </intent-filter>
            <!-- 为实时壁纸配置meta-data -->
            <meta-data android:name="android.service.wallpaper"
                       android:resource="@xml/livewallpaper" />
        </service>
    </application>

</manifest>
```

*    在res下面新建一个xml文件夹 然后新建一个livewallpaper.xml 内容如下： 

``` 
<?xml version="1.0" encoding="utf-8"?>
<!-- ic_launcher 预览是显示的图片-->
<wallpaper
  xmlns:android="http://schemas.android.com/apk/res/android"
  android:thumbnail="@mipmap/ic_launcher"/>
```

*   实现动态壁纸的LiveWallpaper.java:

```
package com.song.wallpaper;

import android.hardware.Camera;
import android.service.wallpaper.WallpaperService;
import android.view.MotionEvent;
import android.view.SurfaceHolder;

import java.io.IOException;

public class CameraLiveWallpaper extends WallpaperService {
    // 实现WallpaperService必须实现的抽象方法  
    public Engine onCreateEngine() {
        // 返回自定义的CameraEngine  
        return new CameraEngine();
    }

    class CameraEngine extends Engine implements Camera.PreviewCallback {
        private Camera camera;

        @Override
        public void onCreate(SurfaceHolder surfaceHolder) {
            super.onCreate(surfaceHolder);

            startPreview();
            // 设置处理触摸事件  
            setTouchEventsEnabled(true);

        }

        @Override
        public void onTouchEvent(MotionEvent event) {
            super.onTouchEvent(event);
            // 时间处理:点击拍照,长按拍照
        }

        @Override
        public void onDestroy() {
            super.onDestroy();
            stopPreview();
        }

        @Override
        public void onVisibilityChanged(boolean visible) {
            if (visible) {
                startPreview();
            } else {
                stopPreview();
            }
        }

        /**
         * 开始预览
         */
        public void startPreview() {
            camera = Camera.open();
            camera.setDisplayOrientation(90);

            try {
                camera.setPreviewDisplay(getSurfaceHolder());
            } catch (IOException e) {
                e.printStackTrace();
            }
            camera.startPreview();

        }

        /**
         * 停止预览
         */
        public void stopPreview() {
            if (camera != null) {
                try {
                    camera.stopPreview();
                    camera.setPreviewCallback(null);
                    // camera.lock();
                    camera.release();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                camera = null;
            }
        }

        @Override
        public void onPreviewFrame(byte[] bytes, Camera camera) {
            camera.addCallbackBuffer(bytes);
        }
    }
}  
```

* 启动动态壁纸的MainActivity.java:

```
package com.song.wallpaper;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;


public class MainActivity extends AppCompatActivity {

    private static final int PERMISSIONS_REQUEST_CAMERA = 454;
    private Context mContext;
    static final String PERMISSION_CAMERA = Manifest.permission.CAMERA;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContext = this;
        findViewById(R.id.text)
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        checkSelfPermission();
                    }
                });
    }

    /**
     * 检查权限
     */
    void checkSelfPermission() {
        if (ContextCompat.checkSelfPermission(mContext, PERMISSION_CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{PERMISSION_CAMERA},
                    PERMISSIONS_REQUEST_CAMERA);
        } else {
//            setTransparentWallpaper();
            startWallpaper();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case PERMISSIONS_REQUEST_CAMERA: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                    setTransparentWallpaper();
                    startWallpaper();

                } else {
                    Toast.makeText(mContext, getString(R.string._lease_open_permissions), Toast.LENGTH_SHORT).show();
                }
                return;
            }
        }
    }
    /**
     * 选择壁纸
     */
    void startWallpaper() {
        final Intent pickWallpaper = new Intent(Intent.ACTION_SET_WALLPAPER);
        Intent chooser = Intent.createChooser(pickWallpaper, getString(R.string.choose_wallpaper));
        startActivity(chooser);
    }

    /**
     * 不需要手动启动服务
     */
    void setTransparentWallpaper() {
        startService(new Intent(mContext, CameraLiveWallpaper.class));
    }
}

```

这样一个简单装逼神器出来了...

![偶尔装下逼](http://upload-images.jianshu.io/upload_images/2215276-b079576542df7d40.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)
> github: https://github.com/songixan/Wallpaper




### 其他资料
* 壁纸
> http://blog.csdn.net/t12x3456/article/details/7857741
https://www.diycode.cc/topics/334
http://www.qingpingshan.com/rjbc/az/232984.html
http://mzh3344258.blog.51cto.com/1823534/806560/
https://my.oschina.net/u/1770617/blog/339881

* 相机
> https://github.com/google/cameraview
https://github.com/afollestad/material-camera
