package ml.fomi.apps.coloringbook.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import java.util.ArrayList;

/**
 * Created by Rius on 29.03.17.
 */
public class SectorsDAO extends dbsDAO {

    private static final String WHERE_MAP_SECTORS_EQUALS = DataBaseHelper.KEY_MAP
            + " =?";

    private static final String WHERE_ID_ROW_EQUALS = DataBaseHelper.ID_ROW
            + " =?";

    private final DataBaseHelper.TABLES tableName = DataBaseHelper.TABLES.SECTORS_TABLE;
    private DataBaseHelper.SECTORS sectorType;

    ArrayList<Integer> sectorsID;
    ArrayList<Integer> sectors;

    public SectorsDAO(Context context, DataBaseHelper.SECTORS sectorType) {
        super(context);
        this.sectorType = sectorType;
        sectorsID = new ArrayList<>();
    }

    public long init() {
        ContentValues values = new ContentValues();
        long res = -1;
        for (int color : sectors) {
            values.put(DataBaseHelper.KEY_MAP, sectorType.toString());
            values.put(DataBaseHelper.COLOR_COLUMN, color);
            res = database.insert(tableName.toString(), null, values);
            if (res == -1) break;
        }

        sectorsID.clear();
        Cursor cursor = database.query(tableName.toString(),
                new String[]{
                        DataBaseHelper.ID_ROW
                },
                WHERE_MAP_SECTORS_EQUALS,
                new String[]{
                        sectorType.toString()
                },
                null,
                null, null);

        while (cursor.moveToNext()) {
            sectorsID.add(cursor.getInt(0));
        }

        return res;
    }

    public long update(int sectorID, int color) {
        ContentValues values = new ContentValues();
        sectors.set(sectorID, color);
        values.put(DataBaseHelper.COLOR_COLUMN, color);

        sectors = getSectors();

        return (long) database.update(tableName.toString(), values,
                WHERE_ID_ROW_EQUALS,
                new String[]{
                        String.valueOf(sectorsID.get(sectorID))
                });
    }

    public int delete(int sector) {
        return database.delete(tableName.toString(),
                WHERE_ID_ROW_EQUALS,
                new String[]{
                        String.valueOf(sectorsID.get(sector))
                });
    }

    public int clear() {
        return database.delete(tableName.toString(), null, null);
    }

    public ArrayList<Integer> getSectors() {
        if (sectors == null) {
            sectors = new ArrayList<>();
            sectorsID.clear();

            Cursor cursor = database.query(tableName.toString(),
                    new String[]{
                            DataBaseHelper.ID_ROW,
                            DataBaseHelper.COLOR_COLUMN
                    },
                    WHERE_MAP_SECTORS_EQUALS,
                    new String[]{
                            sectorType.toString()
                    },
                    null,
                    null, null);

            while (cursor.moveToNext()) {
                sectorsID.add(cursor.getInt(0));
                sectors.add(cursor.getInt(1));
            }
        }
        return sectors;
    }
}
