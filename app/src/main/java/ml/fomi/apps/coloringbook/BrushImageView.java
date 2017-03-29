package ml.fomi.apps.coloringbook;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

import ml.fomi.apps.coloringbook.db.DataBaseHelper;
import ml.fomi.apps.coloringbook.db.SectorsDAO;

/**
 * Created by buz on 17.06.16.
 * Vector Brush class
 */
public class BrushImageView extends VectorImageView implements View.OnTouchListener, VectorImageView.OnImageCommandsListener {

    private int prevSector;

    private boolean onPush = true;

    private Context mContext;

    public BrushImageView(Context context) {
        super(context);
        this.mContext = context;
        initThis();
    }

    public BrushImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;
        initThis();
    }

    public BrushImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mContext = context;
        initThis();
    }

    @Override
    public void initThis() {
        setOnTouchListener(this);
    }

    @Override
    public void loadAsset(String string) {
        super.loadAsset(string);
        setSectorsDAO(new SectorsDAO(mContext, DataBaseHelper.SECTORS.SECTORS_BRUSH));
    }

    private void setFirstColorSector(int firstColorSector) {
        setSectorColor(0, firstColorSector);
    }

    private int getFirsSectorColor() {
        return getColorFromSector(0);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        int sector = getSector((ImageView) v, event.getX(), event.getY());

        int color = getColorFromSector(sector);

        if (sector != 0 && sector != 0xFFFFFFFF && sector != prevSector) {
            int c = getFirsSectorColor();
            prevSector = sector;
            setFirstColorSector(color);
            for (int i = sector; i >= 1; i--) {
                setSectorColor(i, getColorFromSector(i-1));
            }
            setSectorColor(1, c);
            updatePicture();
        }

        if (event.getAction() == MotionEvent.ACTION_UP) {
            prevSector = -1;
        }

        return true;
    }

    @Override
    public int getCurrentColor() {
        onPush = true;
        return getFirsSectorColor();
    }

    void pushColor(int c) {
        if (onPush) {
            for (int i = getSizeSectors() - 1; i > 0; i--) {
                setSectorColor(i, getColorFromSector(i - 1));
            }
            onPush = false;
        }
        setFirstColorSector(c);
        updatePicture();
    }
}
