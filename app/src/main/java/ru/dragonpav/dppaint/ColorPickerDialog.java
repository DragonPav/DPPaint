package ru.dragonpav.dppaint;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class ColorPickerDialog {
    private Context ctx;
    private int color;
    private String title;
    public ColorPickerDialog(Context context, String title, int initialColor) {
        ctx = context;
        this.title = title;
        color = initialColor;
    }
    public void createAndShow(OnDialogClosed callback) {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(ctx);
        builder.setTitle(title);
        LayoutInflater inflater = LayoutInflater.from(ctx);
        View v = inflater.inflate(R.layout.color_picker_dialog, null);
        SeekBar red = v.findViewById(R.id.redSeekBar);
        SeekBar green = v.findViewById(R.id.greenSeekBar);
        SeekBar blue = v.findViewById(R.id.blueSeekBar);
        final TextView redD = v.findViewById(R.id.redDisplay);
        final TextView greenD = v.findViewById(R.id.greenDisplay);
        final TextView blueD = v.findViewById(R.id.blueDisplay);
        final View preview = v.findViewById(R.id.colorPreview);
        red.setProgress(Color.red(color));
        green.setProgress(Color.green(color));
        blue.setProgress(Color.blue(color));
        preview.setBackgroundColor(color);
        redD.setText(String.valueOf(Color.red(color)));
        greenD.setText(String.valueOf(Color.green(color)));
        blueD.setText(String.valueOf(Color.blue(color)));
        red.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    redD.setText(String.valueOf(progress));
                    color = Color.rgb(progress, Color.green(color), Color.blue(color));
                    preview.setBackgroundColor(color);
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
                    color = Color.rgb(Color.red(color), progress, Color.blue(color));
                    preview.setBackgroundColor(color);
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
                if (fromUser) {
                    blueD.setText(String.valueOf(progress));
                    color = Color.rgb(Color.red(color), Color.green(color), progress);
                    preview.setBackgroundColor(color);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
        builder.setView(v);
        builder.setPositiveButton("Ок", (dialogInterface, which) -> {
            callback.onDialogClosed(color);
        });
        builder.create().show();
    }
    public interface OnDialogClosed {
        void onDialogClosed(int resultColor);
    }
}
