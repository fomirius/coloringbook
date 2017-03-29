package ml.fomi.apps.coloringbook;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.PictureDrawable;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ImageView;

import com.pixplicity.sharp.OnSvgElementListener;
import com.pixplicity.sharp.Sharp;
import com.pixplicity.sharp.SharpDrawable;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Random;

import ml.fomi.apps.coloringbook.db.SectorsDAO;

/**
 * Created by Rius on 29.03.17.
 * VectorImageView class
 */
public abstract class VectorImageView extends AppCompatImageView implements OnSvgElementListener {

    private Context context;

    private PictureDrawable sharpDrawable;

    private VectorImageView vectorImageView;

    private OnImageCommandsListener onImageCommandsListener;
    private OnImageCallbackListener onImageCallbackListener;

    private Bitmap bitmapMap;

    private int actW;
    private int actH;

    private ArrayList<Boolean> sectorsFlags;

    private ArrayList<Integer> sectorsColors;
    private ArrayList<Path> sectorsPaths;

    private ArrayList<Integer> bckgSectorsColors;
    private ArrayList<Path> bckgSectorsPaths;

    private ArrayList<Float> brushSectors;

    private SectorsDAO sectorsDAO;

    private boolean isEmptyDB = false;

    public VectorImageView(Context context) {
        super(context);
        vectorImageView = this;
        vectorImageView.context = context;
    }

    public VectorImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        vectorImageView = this;
        vectorImageView.context = context;
    }

    public VectorImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        vectorImageView = this;
        vectorImageView.context = context;
    }

    public void loadAsset(String string) {
        sectorsFlags = new ArrayList<>();
        sectorsPaths = new ArrayList<>();
        bckgSectorsPaths = new ArrayList<>();
        bckgSectorsColors = new ArrayList<>();
        brushSectors = new ArrayList<>();

        Sharp mSharp = Sharp.loadAsset(context.getAssets(), string);
        mSharp.setOnElementListener(vectorImageView);

        mSharp.getDrawable(vectorImageView, new Sharp.DrawableCallback() {
            @Override
            public void onDrawableReady(SharpDrawable sd) {
                sharpDrawable = sd;
                vectorImageView.setImageDrawable(sharpDrawable);

                if (onImageCallbackListener != null)
                    onImageCallbackListener.imageCallback();

                createMap();
                updatePicture();
            }
        });

    }

    private int sectorId = 0;

    @Override
    public void onSvgStart(@NonNull Canvas canvas, @Nullable RectF bounds) {
        sectorId = 0;
        sectorsColors = sectorsDAO.getSectors();
        if (sectorsColors.isEmpty())
            isEmptyDB = true;
        bckgSectorsPaths.clear();
        bckgSectorsColors.clear();
        sectorsPaths.clear();
        brushSectors.clear();
    }

    @Override
    public void onSvgEnd(@NonNull Canvas canvas, @Nullable RectF bounds) {
        if (isEmptyDB) {
            AddSectorsTask task = new AddSectorsTask((Activity) context);
            task.execute((Void) null);
            isEmptyDB = false;
        }
    }

    @Override
    public <T> T onSvgElement(@Nullable String id, @NonNull T element, @Nullable RectF
            elementBounds, @NonNull Canvas canvas, @Nullable RectF canvasBounds, @Nullable Paint paint) {

        if (paint != null && (element instanceof Path)) {
            int color;
            if (id == null) {
                color = paint.getColor();
                sectorsFlags.add(false);
                bckgSectorsPaths.add((Path) element);
                bckgSectorsColors.add(color);
            } else {
                sectorsFlags.add(true);
                sectorsPaths.add((Path) element);

                if (onImageCommandsListener == null) {
                    float elB = elementBounds != null ? elementBounds.left : -1;
                    float canB = canvasBounds != null ? canvasBounds.width() : -1;
                    brushSectors.add(elB / canB);
                }

                if (isEmptyDB) {
                    Random random = new Random();
                    color = Color.argb(255, random.nextInt(256),
                            random.nextInt(256), random.nextInt(256));
                    sectorsColors.add(color);
                } else {
                    color = sectorsColors.get(sectorId++);
                }
            }
            paint.setColor(color);
        }
        //return element;
        return null;
    }

    @Override
    public <T> void onSvgElementDrawn(@Nullable String id, @NonNull T element, @NonNull Canvas
            canvas, @Nullable Paint paint) {
    }

    int getSector(float x, float y) {
        int lX = Math.round(x * actW);
        int lY = Math.round(y * actH);
        int curSector;
        if (lX >= 0 && lY < bitmapMap.getHeight() && lX < bitmapMap.getWidth() && lY >= 0) {
            curSector = ((bitmapMap.getPixel(lX, lY) << 16) >>> 16) - 1;
            return curSector;
        }
        curSector = 0xFFFFFFFF;
        return curSector;
    }

    int getSector(final ImageView imageView, float x, float y) {

        float paddingEventX = x / imageView.getWidth();

        int sectorId = -1;
        for (float fl : brushSectors) {
            if (paddingEventX < fl) break;
            sectorId++;
        }

        return sectorId;
    }

    void setSectorColor(int i, int c) {
        if (sectorsColors != null && c != sectorsColors.get(i)) {
            sectorsColors.set(i, c);
            sectorsDAO.update(i, c);
        }
    }

    int getColorFromSector(int i) {
        if (i == 0xFFFFFFFF) return sectorsColors.get(0);
        return sectorsColors.get(i);
    }

    int getSizeSectors() {
        return sectorsColors.size();
    }

    void setOnImageCommandsListener(OnImageCommandsListener onImageCommandsListener) {
        vectorImageView.onImageCommandsListener = onImageCommandsListener;
    }

    OnImageCommandsListener getOnImageCommandsListener() {
        return vectorImageView.onImageCommandsListener;
    }

    interface OnImageCommandsListener {
        int getCurrentColor();
    }

    public void setOnImageCallbackListener(OnImageCallbackListener onImageCallbackListener) {
        this.onImageCallbackListener = onImageCallbackListener;
    }

    interface OnImageCallbackListener {
        void imageCallback();
    }

    private void createMap() {

        actW = sharpDrawable.getPicture().getWidth();

        if (onImageCallbackListener != null) {

            actH = sharpDrawable.getPicture().getHeight();

            Paint paint = new Paint();
            paint.setAntiAlias(false);

            Canvas canvas = sharpDrawable.getPicture().beginRecording(actW, actH);

            for (int i = 0; i < sectorsPaths.size(); i++) {
                paint.setColor(i + 1);
                paint.setAlpha(0xFF);
                canvas.drawPath(sectorsPaths.get(i), paint);
            }

            sharpDrawable.getPicture().endRecording();

            bitmapMap = Bitmap.createBitmap(actW, actH, Bitmap.Config.ARGB_8888);
            bitmapMap.eraseColor(0x00000000);

            Canvas bitmapCanvas = new Canvas(bitmapMap);
            sharpDrawable.draw(bitmapCanvas);
        }
    }

    public abstract void initThis();

    private class AddSectorsTask extends AsyncTask<Void, Void, Long> {

        private final WeakReference<Activity> activityWeakRef;

        public AddSectorsTask(Activity context) {
            this.activityWeakRef = new WeakReference<>(context);
        }

        @Override
        protected Long doInBackground(Void... voids) {
            return sectorsDAO.init();
        }

        @Override
        protected void onPostExecute(Long aLong) {
            if (activityWeakRef.get() != null
                    && !activityWeakRef.get().isFinishing()) {
                if (aLong == -1)
                    Log.e("MLogs", "Handsof: Error to save sectorsColors.");
                else Log.d("MLogs", "Handsof: Sectors saved.");
            }
        }
    }

    public Bitmap getShareBitmap(Drawable drawable) {
        int w = getResources().getDimensionPixelSize(R.dimen.share_image_width_px);
        int iw = drawable.getIntrinsicWidth();
        int ih = drawable.getIntrinsicHeight();
        float ar = (float) iw / w;
        int ah = (int) (ih / ar);
        int aw = (int) (iw / ar);

        Bitmap btm = Bitmap.createBitmap(aw, ah, Bitmap.Config.ARGB_8888);
        btm.eraseColor(0xFFFFFFFF);
        Canvas canvas = new Canvas(btm);
        int p = getResources().getDimensionPixelSize(R.dimen.share_image_padding_px);
        drawable.setBounds(p, p, aw - p, ah - p);
        drawable.draw(canvas);
        return btm;
    }

    public void setSectorsDAO(SectorsDAO sectorsDAO) {
        this.sectorsDAO = sectorsDAO;
    }

    public void clearAll() {
        for (int i = 0; i < sectorsColors.size(); i++) {
            sectorsColors.set(i, 0xFFFFFFFF);
            sectorsDAO.update(i, 0xFFFFFFFF);
        }
        updatePicture();
    }

    public void colorAllWhite() {
        Random random = new Random();
        for (int i = 0; i < sectorsColors.size(); i++)
            if (sectorsColors.get(i) == Color.WHITE) {
                int c = Color.argb(255, random.nextInt(256),
                        random.nextInt(256), random.nextInt(256));
                sectorsColors.set(i, c);
                sectorsDAO.update(i, c);
            }
        updatePicture();
    }

    public void updatePicture() {

        Paint paint = new Paint();
        paint.setAntiAlias(true);

        Canvas canvas = sharpDrawable.getPicture().beginRecording(
                sharpDrawable.getPicture().getWidth(),
                sharpDrawable.getPicture().getHeight()
        );

        int j = 0, k = 0;
        for (int i = 0; i < sectorsFlags.size(); i++)
            if (sectorsFlags.get(i)) {
                paint.setColor(sectorsColors.get(j));
                canvas.drawPath(sectorsPaths.get(j++), paint);
            } else {
                paint.setColor(bckgSectorsColors.get(k));
                canvas.drawPath(bckgSectorsPaths.get(k++), paint);
            }
        sharpDrawable.getPicture().endRecording();
        vectorImageView.invalidate();
    }
}