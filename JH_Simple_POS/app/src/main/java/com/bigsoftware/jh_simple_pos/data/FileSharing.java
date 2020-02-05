package com.bigsoftware.jh_simple_pos.data;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.os.StrictMode;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.Toast;

import com.bigsoftware.jh_simple_pos.R;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.TreeMap;

/**
 * Created by shanesepac on 4/20/19.
 */

public class FileSharing {

    private static TreeMap<String, String> getFiles(String relDir){

        //K= filename, V = abs file path
        TreeMap<String, String> fileMap = new TreeMap<>();

        String absDirStr = Environment.getExternalStorageDirectory().toString() + relDir;

        File absDir = new File(absDirStr);
        File[] files = absDir.listFiles(); //will return null if no files are listed in the directory

        if(files != null && files.length > 0) {
            for (File f :
                    files) {
                fileMap.put(f.getName(), f.getAbsolutePath());
            }
        }

        return fileMap;
    }

    public static void showFilePrompt(final Context c){

        String relDir = c.getString(R.string.exportDir);
        final TreeMap<String, String> files = getFiles(relDir);

        if(files.isEmpty()){
            Toast.makeText(c, "There are no files to share!", Toast.LENGTH_LONG).show();
            return;
        }

        AlertDialog.Builder b = new AlertDialog.Builder(c);

        b.setTitle("Select file to share");

        final ArrayAdapter<String> adapter = new ArrayAdapter<String>(c, android.R.layout.select_dialog_singlechoice);

        for (String f:
                files.keySet()) {
            adapter.add(f);
        }

        b.setAdapter(adapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, final int i) {

                final String selectedFileName = adapter.getItem(i);
                final String selectedAbsPath = files.get(selectedFileName);

                showConfirmationPrompt(selectedFileName, selectedAbsPath, c);
            }
        });

        b.show();
    }

    private static void showConfirmationPrompt(String fname, final String fAbsPath, final Context c){

        AlertDialog.Builder b = new AlertDialog.Builder(c);

        b.setTitle("Confirm Sharing");
        b.setMessage("Are you sure you'd like to share " + fname + "?");

        b.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //Share file
                shareCSV(c, fAbsPath);
            }
        });

        b.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });

        b.show();
    }

    private static void shareCSV(Context c, String absFileName){

        //Required per
        //https://stackoverflow.com/questions/48117511/exposed-beyond-app-through-clipdata-item-geturi
        //Apparently, this is not the correct way to solve the problem (it removes strict security).
        //TODO: Change the two lines of code immediately below if any issues arrive with security
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());

        File sharingGifFile = new File(absFileName);
        Intent shareIntent = new Intent(android.content.Intent.ACTION_SEND);
        shareIntent.setType("application/csv");
        Uri uri = Uri.fromFile(sharingGifFile);
        shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
        c.startActivity(Intent.createChooser(shareIntent, "Share CSV"));
    }

}
