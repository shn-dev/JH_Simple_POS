package com.bigsoftware.jh_simple_pos.data;

import android.app.ProgressDialog;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import com.bigsoftware.jh_simple_pos.R;
import com.bigsoftware.jh_simple_pos.views.MainActivity;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.ref.WeakReference;

/**
 * Created by shanesepac on 4/17/19.
 */

public class DataExporter {

    public static class ExportDatabaseCSVTask extends AsyncTask<String, Void, Boolean> {

        private static final String TAG = "ExportTask";
        private static String filename;
        private static WeakReference<MainActivity> mActivity;
        private static String storeName;

        public static final int ALL_STORES = 2000;
        public static final int SELECT_STORE = 2001;

        public static void setStoreName(String name){
            storeName = name;
        }

        public static void setActivity(MainActivity activity){
            mActivity = new WeakReference<MainActivity>(activity);
        }

        public static void setCurrFileName(String fileName){
            filename = fileName;
        }

        private final ProgressDialog dialog = new ProgressDialog(mActivity.get());
        PurchaseDBHelper dbhelper;

        @Override
        protected void onPreExecute() {
            this.dialog.setMessage("Exporting database...");
            this.dialog.show();
            dbhelper = new PurchaseDBHelper(mActivity.get());
        }

        protected Boolean doInBackground(final String... args) {

            String exportDir = Environment.getExternalStorageDirectory().getAbsolutePath() + mActivity.get().getString(R.string.exportDir);
            File root = new File(exportDir);

            if (!root.exists()) { root.mkdirs(); }

            File file = new File(exportDir + filename);
            try {
                Log.d(TAG, "doInBackground: File path= " + file.getPath());
                file.createNewFile();
                CSVWriter csvWrite = new CSVWriter(new FileWriter(file));
                Cursor curCSV = storeName == null ? dbhelper.getAllStores() : dbhelper.querySingleStore(storeName);
                csvWrite.writeNext(curCSV.getColumnNames());
                while(curCSV.moveToNext()) {
                    String arrStr[]=null;
                    String[] mySecondStringArray = new String[curCSV.getColumnNames().length];
                    for(int i=0;i<curCSV.getColumnNames().length;i++)
                    {
                        mySecondStringArray[i] =curCSV.getString(i);
                    }
                    csvWrite.writeNext(mySecondStringArray);
                }
                csvWrite.close();
                curCSV.close();
                return true;
            } catch (IOException e) {
                Log.e(TAG, "doInBackground: ", e);
                return false;
            }
        }

        protected void onPostExecute(final Boolean success) {
            if (this.dialog.isShowing()) { this.dialog.dismiss(); }
            if (success) {
                Toast.makeText(mActivity.get(), "Export successful!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(mActivity.get(), "Export failed", Toast.LENGTH_SHORT).show();
            }
        }
    }

}
