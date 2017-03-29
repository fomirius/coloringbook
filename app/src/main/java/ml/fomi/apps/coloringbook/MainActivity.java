package ml.fomi.apps.coloringbook;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDialog;
import android.support.v7.widget.AppCompatCheckBox;
import android.support.v7.widget.Toolbar;
import android.text.util.Linkify;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements OnTouchListener {

    private PhilImageView centerImageView;
    private BrushImageView brushImageView;

    private ImageView imageViewLeft;

    private int currentPixelColor = 0;

    SharedPreferences sPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setLogo(R.mipmap.ic_launcher);
        getSupportActionBar().setDisplayUseLogoEnabled(true);

        TextView textViewLeftBlack = (TextView) findViewById(R.id.textView_black);
        ImageView imageViewGray = (ImageView) findViewById(R.id.imageView_gray);
        TextView textViewLeftWhite = (TextView) findViewById(R.id.textView_white);
        imageViewLeft = (ImageView) findViewById(R.id.imageView_left);


        final GradientDrawable drawableGray = new GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP,
                new int[]{0xFF000000, 0xFFFFFFFF});
        drawableGray.setShape(GradientDrawable.RECTANGLE);
        drawableGray.setGradientType(GradientDrawable.LINEAR_GRADIENT);
        imageViewGray.setImageDrawable(drawableGray);
        imageViewGray.setDrawingCacheEnabled(true);

        final GradientDrawable drawableLeft = new GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP,
                new int[]{0xFF000000, 0xFF00FF00, 0xFFFFFFFF});
        drawableLeft.setShape(GradientDrawable.RECTANGLE);
        drawableLeft.setGradientType(GradientDrawable.LINEAR_GRADIENT);
        imageViewLeft.setImageDrawable(drawableLeft);
        imageViewLeft.setDrawingCacheEnabled(true);

        ImageView imageViewRight = (ImageView) findViewById(R.id.imageView_right);
        final GradientDrawable drawableRight = new GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP,
                new int[]{0xFFFF0000, 0xFFFF7F00,
                        0xFFFFFF00, 0xFF00FF00, 0xFF00FFFF,
                        0xFF0000FF, 0xFFFF00FF});

        drawableRight.setShape(GradientDrawable.RECTANGLE);
        drawableRight.setGradientType(GradientDrawable.LINEAR_GRADIENT);
        imageViewRight.setImageDrawable(drawableRight);
        imageViewRight.setDrawingCacheEnabled(true);

        imageViewRight.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                touchView(v, event);

                imageViewLeft.setDrawingCacheEnabled(false);
                final GradientDrawable drawableLeft = new GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP,
                        new int[]{0xFF000000, currentPixelColor, 0xFFFFFFFF});
                drawableLeft.setShape(GradientDrawable.RECTANGLE);
                drawableLeft.setGradientType(GradientDrawable.LINEAR_GRADIENT);

                imageViewLeft.setDrawingCacheEnabled(true);

                imageViewLeft.setImageDrawable(drawableLeft);

                return true;
            }
        });

        imageViewLeft.setOnTouchListener(this);

        assert textViewLeftWhite != null;
        textViewLeftWhite.setOnTouchListener(this);

        assert textViewLeftBlack != null;
        textViewLeftBlack.setOnTouchListener(this);

        imageViewGray.setOnTouchListener(this);

        brushImageView = (BrushImageView) findViewById(R.id.imageView_brush);
        brushImageView.loadAsset("brush7.svg");

        centerImageView = (PhilImageView) findViewById(R.id.imageView_center);
        centerImageView.loadAsset("ul.svg");

        centerImageView.setOnImageCommandsListener(brushImageView);
        centerImageView.setOnImageCallbackListener(centerImageView);

        centerImageView.post(new Runnable() {
            @Override
            public void run() {
                helpOnStartWindow();
            }
        });
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        touchView(v, event);
        return true;
    }

    private void touchView(View v, MotionEvent event) {

        final int fieldWidth = 15;

        int y = (int) event.getY();
        int yImg = v.getMeasuredHeight();

        int x = (int) event.getX();
        int xImg = v.getMeasuredWidth();

        if ((y >= -fieldWidth) && (y < (yImg + fieldWidth)) && (x >= -fieldWidth) && (x < (xImg + fieldWidth))) {

            if (y >= 0 && y < yImg && x >= 0 && x < xImg) {

                if (v instanceof ImageView) {

                    currentPixelColor = v.getDrawingCache().getPixel(x, y);

                } else if (v instanceof TextView) {
                    //TextView section
                    if (((TextView) v).getText().toString().equals("B"))
                        currentPixelColor = Color.BLACK;

                    if (((TextView) v).getText().toString().equals("W"))
                        currentPixelColor = Color.WHITE;
                }

                brushImageView.pushColor(currentPixelColor);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        centerImageView.cleanup();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_share:
                Uri uri = centerImageView.doShare();
                if (uri != null) {
                    Intent myShareIntent = new Intent(Intent.ACTION_SEND);
                    myShareIntent.setType("image/png");
                    myShareIntent.putExtra(Intent.EXTRA_STREAM, uri);
                    startActivity(Intent.createChooser(myShareIntent, "Share to ..."));
                }
                return true;
            case R.id.action_undo:
                centerImageView.undoColor();
                return true;
            case R.id.action_clear_all:
                new AlertDialog.Builder(this)
                        .setTitle("Clearing all cells")
                        .setMessage("Do you really want to clear all cells? All cells will be white.")
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                brushImageView.clearAll();
                                centerImageView.clearAll();
                                Toast.makeText(MainActivity.this, "All cells cleared!", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .setNegativeButton(android.R.string.no, null).show();
                return true;
            case R.id.action_color_white_cells:

                new AlertDialog.Builder(this)
                        .setTitle("Coloring all cells.")
                        .setMessage("Do you really want to color all white cells? All white cells will be painted random color.")
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                brushImageView.colorAllWhite();
                                centerImageView.colorAllWhite();
                                Toast.makeText(MainActivity.this, "All white cells colored!", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .setNegativeButton(android.R.string.no, null).show();
                return true;
            case R.id.action_about:
                AboutWindow();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void AboutWindow() {

        final Dialog aboutWindow = new Dialog(this);

        LinearLayout linearLayout = (LinearLayout) getLayoutInflater().inflate(R.layout.about_dialog, null);

        aboutWindow.requestWindowFeature(Window.FEATURE_NO_TITLE);

        aboutWindow.setContentView(linearLayout);

        final TextView tx = (TextView) linearLayout.findViewById(R.id.about_textView);

        String[] about_ar = getResources().getStringArray(R.array.text_about);
        String about_string = "";
        for (String str : about_ar) {
            about_string += str;
        }
        about_string += BuildConfig.VERSION_NAME;

        tx.setAutoLinkMask(Linkify.EMAIL_ADDRESSES);
        tx.setText(about_string);

        linearLayout.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                aboutWindow.dismiss();
                return false;
            }
        });
        aboutWindow.show();
    }

    public void helpOnStartWindow() {

        final String isShowStr = "isShowPref";
        sPref = getPreferences(MODE_PRIVATE);
        boolean isShow = sPref.getBoolean(isShowStr, true);

        if (isShow) {

            final AppCompatDialog helpWindow = new AppCompatDialog(this);
            final LinearLayout linearLayout = (LinearLayout) getLayoutInflater().inflate(R.layout.help_on_start, null);
            helpWindow.requestWindowFeature(Window.FEATURE_NO_TITLE);
            helpWindow.setContentView(linearLayout);

            final AppCompatCheckBox checkBox = (AppCompatCheckBox) linearLayout.findViewById(R.id.help_view_checkBox);

            checkBox.setChecked(isShow);

            checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    SharedPreferences.Editor editor = sPref.edit();
                    editor.putBoolean(isShowStr, b);
                    editor.apply();
                }
            });

            linearLayout.setOnTouchListener(new OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    helpWindow.dismiss();
                    return false;
                }
            });

            helpWindow.show();
        }
    }
}
