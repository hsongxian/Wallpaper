package con.song.wallpaper;

import android.hardware.Camera;
import android.service.wallpaper.WallpaperService;
import android.view.SurfaceHolder;

import java.io.IOException;

public class CameraLiveWallpaper extends WallpaperService {
    // 实现WallpaperService必须实现的抽象方法  
    public Engine onCreateEngine() {
        // 返回自定义的Engine  
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
        public void onDestroy() {
            super.onDestroy();
            // 删除回调
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