package com.jackfangqi.canvasboard;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private ImageView canvasBoard;

    private Paint mPaint;
    private Bitmap mBitmap;
    private Canvas mCanvas;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);

        canvasBoard = (ImageView) findViewById(R.id.canvas_board);
        FloatingActionButton save = (FloatingActionButton) findViewById(R.id.fab_save);
        FloatingActionButton clear = (FloatingActionButton) findViewById(R.id.fab_clear);

        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setColor(Color.BLACK);
        mPaint.setStrokeWidth(5);

        initCanvas();

        save.setOnClickListener(this);
        clear.setOnClickListener(this);
        canvasBoard.setOnTouchListener(new View.OnTouchListener() {
            int startX;
            int startY;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (mBitmap == null || mBitmap.isRecycled()) {
                    initCanvas();
                }

                switch (event.getAction()) {
                    case MotionEvent.ACTION_UP:
                        break;

                    case MotionEvent.ACTION_DOWN:
                        startX = (int) event.getX();
                        startY = (int) event.getY();
                        break;

                    case MotionEvent.ACTION_MOVE:
                        int newX = (int) event.getX();
                        int newY = (int) event.getY();
                        mCanvas.drawLine(startX, startY, newX, newY, mPaint);
                        startX = (int) event.getX();
                        startY = (int) event.getY();
                        canvasBoard.setImageBitmap(mBitmap);
                        break;

                    default:
                        break;
                }

                return true;
            }
        });
    }

    private void initCanvas() {
        mBitmap = Bitmap.createBitmap(ScreenUtil.getScreenWidthPixels(this),
                ScreenUtil.getScreenHeightPixels(this), Bitmap.Config.ARGB_8888);
        mCanvas = new Canvas(mBitmap);
        mCanvas.drawColor(Color.GRAY);
        canvasBoard.setImageBitmap(mBitmap);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mBitmap != null && !mBitmap.isRecycled()) {
            mBitmap.recycle();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 666 && grantResults.length > 0) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                realSaveImage();
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fab_clear:
                if (mBitmap != null && !mBitmap.isRecycled())
                    mBitmap.recycle();
                initCanvas();
                break;

            case R.id.fab_save:
                saveImage();
                break;
        }
    }

    private void saveImage() {
        if (PackageManager.PERMISSION_GRANTED ==
                ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            realSaveImage();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    666);
        }
    }

    private void realSaveImage() {
        String filePath = Environment.getExternalStorageDirectory() + File.separator + "CanvasBoard" + File.separator;
        File dir = new File(filePath);
        if (!dir.exists()) {
            dir.mkdir();
        }
        File file = new File(dir, "canvasBoardImg" + System.currentTimeMillis() + ".jpg");
        OutputStream out = null;
        try {
            out = new FileOutputStream(file);
            mBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
            Toast.makeText(this, "saved", Toast.LENGTH_SHORT).show();

            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            intent.setData(Uri.fromFile(dir));
            sendBroadcast(intent);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
