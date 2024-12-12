package ru.dragonpav.dppaint;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.icu.text.DateFormat;
import android.icu.text.SimpleDateFormat;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.PixelCopy;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private SurfaceView sv;
    private SurfaceHolder sh;
    private Path preview;
    private int brushSize = 16;
    private int brushColor = Color.RED;
    private ArrayList<Drawing> drawings = new ArrayList<>();
    private Handler pc;
    private HandlerThread thread;
    private int redC, greenC, blueC;
    private Bitmap image;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sv = findViewById(R.id.surfaceView);
        sh = sv.getHolder();
        thread = new HandlerThread("PixelCopyThread");
        thread.start();
        pc = new Handler(thread.getLooper());
        sh.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(@NonNull SurfaceHolder holder) {
                draw();
            }

            @Override
            public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {}

            @Override
            public void surfaceDestroyed(@NonNull SurfaceHolder holder) {}
        });
        sv.setOnTouchListener((v, event) -> {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        preview = new Path();
                        preview.moveTo(event.getX(), event.getY());
                        return true;
                    case MotionEvent.ACTION_UP:
                        Drawing drawing = new Drawing();
                        drawing.color = brushColor;
                        drawing.size = brushSize;
                        drawing.path = preview;
                        drawings.add(drawing);
                        preview = null;
                        draw();
                        return true;
                    case MotionEvent.ACTION_MOVE:
                        preview.lineTo(event.getX(), event.getY());
                        draw();
                        return true;
                    default:
                        return false;
                }
        });
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.clear) {
            drawings.clear();
            draw();
        } else if (id == R.id.undo) {
            if (!drawings.isEmpty()) {
                drawings.remove(drawings.size() - 1);
                draw();
            }
        } else if (id == R.id.save) {
            requestPermissions(new String[] {Manifest.permission.READ_MEDIA_IMAGES, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
        } else if (id == R.id.changeSize) {
            MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
            builder.setTitle("Выберите размер кисти");
            LayoutInflater inflater = getLayoutInflater();
            View v = inflater.inflate(R.layout.change_size_dialog, null);
            final SeekBar sb = v.findViewById(R.id.sizeSeekBar);
            final TextView bs = v.findViewById(R.id.brushSizeDisplay);
            bs.setText(String.valueOf(brushSize));
            sb.setProgress(brushSize);
            sb.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    if (fromUser) {
                        bs.setText(String.valueOf(progress));
                    }
                }
                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {}
                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {}
            });
            builder.setView(v);
            builder.setPositiveButton("Ок", (dialog, which) -> {
                brushSize = sb.getProgress();
            });
            builder.create().show();
        } else if (id == R.id.changeColor) {
            MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
            builder.setTitle("Выберите цвет");
            LayoutInflater inflater = getLayoutInflater();
            View v = inflater.inflate(R.layout.color_picker_dialog, null);
            SeekBar red = v.findViewById(R.id.redSeekBar);
            SeekBar green = v.findViewById(R.id.greenSeekBar);
            SeekBar blue = v.findViewById(R.id.blueSeekBar);
            final TextView redD = v.findViewById(R.id.redDisplay);
            final TextView greenD = v.findViewById(R.id.greenDisplay);
            final TextView blueD = v.findViewById(R.id.blueDisplay);
            final View preview = v.findViewById(R.id.colorPreview);
            red.setProgress(redC);
            green.setProgress(greenC);
            blue.setProgress(blueC);
            preview.setBackgroundColor(Color.rgb(redC, greenC, blueC));
            redD.setText(String.valueOf(redC));
            greenD.setText(String.valueOf(greenC));
            blueD.setText(String.valueOf(blueC));
            red.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    if (fromUser) {
                        redD.setText(String.valueOf(progress));
                        redC = progress;
                        preview.setBackgroundColor(Color.rgb(redC, greenC, blueC));
                    }
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {}

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {}
            });
            green.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    if (fromUser) {
                        greenD.setText(String.valueOf(progress));
                        greenC = progress;
                        preview.setBackgroundColor(Color.rgb(redC, greenC, blueC));
                    }
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {}

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {}
            });
            blue.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    blueD.setText(String.valueOf(progress));
                    blueC = progress;
                    preview.setBackgroundColor(Color.rgb(redC, greenC, blueC));
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {}

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {}
            });
            builder.setView(v);
            builder.setPositiveButton("Ок", (dialogInterface, which) -> {
                brushColor = Color.rgb(redC, greenC, blueC);
            });
            builder.create().show();
        } else if (id == R.id.loadImage) {
            Intent pickImage = new Intent(Intent.ACTION_GET_CONTENT);
            pickImage.setType("image/*");
            Intent chooser = Intent.createChooser(pickImage, "Загрузите картинку");
            startActivityForResult(chooser, 1);
        }
        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 0) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
                DateFormat format = new SimpleDateFormat("ddMMyyyy_hhmmss", Locale.ROOT);
                File out = new File(dir, "drawing" + format + ".png");
                try {
                    final FileOutputStream fw = new FileOutputStream(out);
                    final Bitmap bmp = Bitmap.createBitmap(sh.getSurfaceFrame().width(), sh.getSurfaceFrame().height(), Bitmap.Config.RGB_565);
                    PixelCopy.request(sh.getSurface(), bmp, (copyResult) -> {
                        if (copyResult == PixelCopy.SUCCESS) {
                            bmp.compress(Bitmap.CompressFormat.PNG, 100, fw);
                            try {
                                fw.flush();
                                fw.close();
                            } catch (IOException e) {
                                e.fillInStackTrace();
                            }
                            Toast.makeText(this, "Файл сохранен", Toast.LENGTH_SHORT).show();
                        }
                    }, pc);
                } catch (IOException e) {
                    Toast.makeText(this, "Ошибка сохранения файла", Toast.LENGTH_SHORT).show();
                    e.fillInStackTrace();
                }
            } else {
                Toast.makeText(this, "Предоставьте разрешения в настройках", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                final String[] path = {MediaStore.Images.Media.DATA};
                Cursor cursor = getContentResolver().query(data.getData(), path, null, null);
                cursor.moveToFirst();
                int index = cursor.getColumnIndex(path[0]);
                if (index != -1) {
                    String imagePath = cursor.getString(index);
                    Bitmap origin = BitmapFactory.decodeFile(imagePath);
                    image = Bitmap.createScaledBitmap(origin, sh.getSurfaceFrame().width(), sh.getSurfaceFrame().height(), true);
                    draw();
                }
            }
        }
    }

    private void draw() {
        Canvas canvas = sh.lockCanvas(null);
        if (image != null) {
            canvas.drawBitmap(image, 0, 0, null);
        } else {
            canvas.drawColor(Color.WHITE);
        }
        Paint mp = new Paint();
        mp.setStrokeJoin(Paint.Join.ROUND);
        mp.setStrokeCap(Paint.Cap.ROUND);
        mp.setAntiAlias(true);
        mp.setStyle(Paint.Style.STROKE);
        if (preview != null) {
            mp.setColor(brushColor);
            mp.setStrokeWidth(brushSize);
            canvas.drawPath(preview, mp);
        }
        for (Drawing d : drawings) {
            mp.setColor(d.color);
            mp.setStrokeWidth(d.size);
            canvas.drawPath(d.path, mp);
        }
        sh.unlockCanvasAndPost(canvas);
    }
}