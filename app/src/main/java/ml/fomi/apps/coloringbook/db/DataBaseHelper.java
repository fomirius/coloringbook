package ml.fomi.apps.coloringbook.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Rius on 29.03.17.
 *
 */
public class DataBaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "regions-db";
    private static final int DATABASE_VERSION = 1;

    //public static final String SECTORS_TABLE = "regions";
    //public static final String BRUSH_TABLE = "brush";

    public static final String ID_ROW = "id";
    public static final String KEY_MAP = "map";
    public static final String COLOR_COLUMN = "color";

    public enum TABLES {
        SECTORS_TABLE("sectors");

        private final String str;

        TABLES(String str) {
            this.str = str;
        }

        @org.jetbrains.annotations.Contract(pure = true)
        @Override
        public String toString() {
            return str;
        }
    }

    public enum SECTORS {
        SECTORS_BRUSH(0),
        SECTORS_PHIL(1);

        private final int i;

        SECTORS(int i) {
            this.i = i;
        }

        @Override
        public String toString() {
            return "" + i;
        }
    }

    public static final String CREATE_SECTORS_TABLE = "CREATE TABLE "
            + TABLES.SECTORS_TABLE + "(" + ID_ROW + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + KEY_MAP + " INTEGER, "
            + COLOR_COLUMN + " TEXT"
            + ")";

    private static DataBaseHelper instance;

    public static synchronized DataBaseHelper getHelper(Context context) {
        if (instance == null)
            instance = new DataBaseHelper(context);
        return instance;
    }

    private DataBaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
        if (!db.isReadOnly()) {
            // Enable foreign key constraints
            db.execSQL("PRAGMA foreign_keys=ON;");
        }
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(CREATE_SECTORS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
}
