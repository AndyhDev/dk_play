package com.dk.play.util;

import android.annotation.SuppressLint;
import android.app.SearchManager;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.provider.BaseColumns;
import com.dk.play.database.SQLiteDataSource;
import com.dk.play.database.SQLiteHelper;
 
public class SearchProvider extends ContentProvider {
	@SuppressWarnings("unused")
	private static final String TAG = "PlaylistSearchProvider";
	private static final String AUTHORITY = "com.dk.play.util.PlaylistSearchProvider";
	
	private SQLiteDataSource datasource;
	private final UriMatcher uriMatcher;
	
	private static final int SEARCH_SUGGEST = 0;
    private static final int SHORTCUT_REFRESH = 1;
    
    public static final String COLUMN_ID = "COULMN_ID";
    
	public String[] searchColumns = {
			BaseColumns._ID,
			SearchManager.SUGGEST_COLUMN_TEXT_1,
			SearchManager.SUGGEST_COLUMN_TEXT_2,
			SearchManager.SUGGEST_COLUMN_ICON_1,
			SearchManager.SUGGEST_COLUMN_INTENT_DATA_ID,
			SearchManager.SUGGEST_COLUMN_INTENT_DATA};
	
	public String[] test = { "1",
			"cool",
			"test",
			"file:///storage/emulated/0/covers/1399456012186.jpg"};
	
	public String LIKE_COLUMNS = SQLiteHelper.COLUMN_TITLE + " LIKE ? OR "
			+ SQLiteHelper.COLUMN_ARTIST + " LIKE ? OR "
			+ SQLiteHelper.COLUMN_ALBUM + " LIKE ? OR "
			+ SQLiteHelper.COLUMN_GENRE + " LIKE ?";
	
    public SearchProvider() {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(AUTHORITY, SearchManager.SUGGEST_URI_PATH_QUERY, SEARCH_SUGGEST);
        uriMatcher.addURI(AUTHORITY, SearchManager.SUGGEST_URI_PATH_QUERY + "/*", SEARCH_SUGGEST);
        uriMatcher.addURI(AUTHORITY, SearchManager.SUGGEST_URI_PATH_SHORTCUT, SHORTCUT_REFRESH);
        uriMatcher.addURI(AUTHORITY, SearchManager.SUGGEST_URI_PATH_SHORTCUT + "/*", SHORTCUT_REFRESH);
    }
 
    @Override
    public boolean onCreate() {
    	datasource = new SQLiteDataSource(getContext());
        return true;
    }
 
    
	@SuppressLint("DefaultLocale")
	@Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        //Cursor cursor = queryBuilder.query(mEmployeeDatabase.getReadableDatabase(), projection, selection, selectionArgs, null, null, sortOrder);
    	String query = uri.getLastPathSegment().toLowerCase();;

    	if(query != null){
    		MatrixCursor cursor2 = new MatrixCursor(searchColumns);
    		
    		datasource.open();
    		SQLiteDatabase database = datasource.getDatabase();
    		String[] search = new String[]{"%"+query+"%", "%"+query+"%", "%"+query+"%", "%"+query+"%"};
        	Cursor cursor = database.query(SQLiteHelper.TABLE_SONGS, datasource.allSongsColumns, LIKE_COLUMNS, search, null, null, null);
        	
        	cursor.moveToFirst();
        	String title;
        	String artist;
        	String cover;
        	String id;
    		while (!cursor.isAfterLast()) {
    			id = String.valueOf(cursor.getLong(0));
    			title = cursor.getString(1);
    			artist = cursor.getString(2);
    			cover = cursor.getString(5);
    			
    			String[] row = {id,
    					title,
    					artist,
    					"file://" + Paths.getCoverPath(cover),
    					COLUMN_ID,
    					id};
    			
    			cursor2.addRow(row);
    			cursor.moveToNext();
    		}
    		cursor.close();
    		datasource.close();
    		
            return cursor2;
    	}else{
    		MatrixCursor cursor2 = new MatrixCursor(searchColumns);
            return cursor2;
    	}
    }

	@Override
    public String getType(Uri uri) {
        /*switch (mUriMatcher.match(uri)) {
        case EMPLOYEES:
            return CONTENT_TYPE;
        case EMPLOYEE_ID:
            return CONTENT_ITEM_TYPE;
        case SEARCH_SUGGEST:
            return null;
        default:
            throw new IllegalArgumentException("Unknown URI " + uri);
        }*/
    	return "";
    }
 
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        return null;
    }
 
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        return 0;
    }
 
    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return 0;
    }
 
}