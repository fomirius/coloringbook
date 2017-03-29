package ml.fomi.apps.coloringbook.db;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

/**
 * Created by Rius on 29.03.17.
 *
 */
public class dbsDAO {

    protected SQLiteDatabase database;
    private ml.fomi.apps.coloringbook.db.DataBaseHelper dbHelper;
    private Context mContext;

    public dbsDAO(Context context) {
        this.mContext = context;
        dbHelper = ml.fomi.apps.coloringbook.db.DataBaseHelper.getHelper(mContext);
        open();

    }

    public void open() throws SQLException {
        if(dbHelper == null)
            dbHelper = ml.fomi.apps.coloringbook.db.DataBaseHelper.getHelper(mContext);
        database = dbHelper.getWritableDatabase();
    }
}
