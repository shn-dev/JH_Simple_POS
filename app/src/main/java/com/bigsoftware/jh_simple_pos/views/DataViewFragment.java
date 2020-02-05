package com.bigsoftware.jh_simple_pos.views;


import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
import android.text.SpannableString;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.bigsoftware.jh_simple_pos.R;
import com.bigsoftware.jh_simple_pos.data.CSVWriter;
import com.bigsoftware.jh_simple_pos.data.DataExporter;
import com.bigsoftware.jh_simple_pos.data.FileSharing;
import com.bigsoftware.jh_simple_pos.data.PurchaseDBHelper;
import com.bigsoftware.jh_simple_pos.data.StoreDBHelper;

import java.io.File;
import java.io.PushbackReader;
import java.lang.reflect.Array;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link DataViewFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class DataViewFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_STORENAME = "param1";
    private static final String ARG_PARAM2 = "param2";

    private static final String TAG = "DATA VIEW FRAGMENT";

    private static final int READ_WRITE_PERMITS = 1000;

    // TODO: Rename and change types of parameters
    private String mStoreName;
    private String mParam2;
    private PurchaseDBHelper pHelper;


    public DataViewFragment() {
        // Required empty public constructor
    }

    protected void bindPurchaseHelper(PurchaseDBHelper helper){
        pHelper = helper;
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param storeName Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment DataViewFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static DataViewFragment newInstance(String storeName, String param2) {
        DataViewFragment fragment = new DataViewFragment();
        Bundle args = new Bundle();
        args.putString(ARG_STORENAME, storeName);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mStoreName = getArguments().getString(ARG_STORENAME);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

        //CHECK FOR READ AND WRITE TO EXTERNAL STORAGE PERMISSIONS
        if(checkPermissions()){
            Log.d(TAG, "onCreate: read + write permissions are granted...");
        }

    }

    private boolean checkPermissions() {

        String[] permissions = {
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE
        };

        int result;
        List<String> listPermissionsNeeded = new ArrayList<>();
        for (String p: permissions) {
            result = ContextCompat.checkSelfPermission(getActivity(),p);
            if (result != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(p);
            }
        }
        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(getActivity(),
                    listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]),READ_WRITE_PERMITS);
            return false;
        }
        return true;
    }

    protected void bindAdapter(final RecyclerView rv){

        if(!((MainActivity)getActivity()).getCurrentStore().equals(getString(R.string.defaultstore))){

            pHelper.obtainStoreData(mStoreName, new PurchaseDBHelper.ICompletion() {
                @Override
                public void success(ArrayList<PurchaseDBHelper.PurchaseDBEntry> entries) {
                    rv.setAdapter(new DataAdapter(entries));
                }

                @Override
                public void failure() {
                    //Set empty adapter
                    rv.setAdapter(new DataAdapter(new ArrayList<PurchaseDBHelper.PurchaseDBEntry>()));
                }
            });
        }
        else{
            rv.setAdapter(new DataAdapter(new ArrayList<PurchaseDBHelper.PurchaseDBEntry>()));
            Toast.makeText(getContext(), "No data available", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_data_view, container, false);

        final RecyclerView rv = v.findViewById(R.id.datarecyclerview);
        rv.setLayoutManager(new LinearLayoutManager(getContext()));

        bindAdapter(rv);

        final Button exportBtn = v.findViewById(R.id.exportDataBtn);
        exportBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openExportDialogue();
            }
        });


        final Button shareBtn = v.findViewById(R.id.shareDataBtn);
        shareBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FileSharing.showFilePrompt(getContext());
            }
        });

        return v;
    }

    private void openExportDialogue(){
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(), android.R.layout.select_dialog_singlechoice);

        builder.setTitle("Select from export options");
        adapter.add("Export set store");
        adapter.add("Export all stores");

        builder.setAdapter(adapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                final ArrayList<PurchaseDBHelper.PurchaseDBEntry> entryArr = new ArrayList<>();

                PurchaseDBHelper.ICompletion comp = new PurchaseDBHelper.ICompletion() {
                    @Override
                    public void success(ArrayList<PurchaseDBHelper.PurchaseDBEntry> entries) {
                        entryArr.addAll(entries);
                    }

                    @Override
                    public void failure() {
                        provokeExportErrorToast();
                    }
                };

                switch(i){
                    case 0:
                        //Export set store
                        pHelper.obtainStoreData(mStoreName, comp);
                        break;
                    case 1:
                        //Export all stores
                        pHelper.obtainAllStoreData(comp);
                        break;
                        default:
                            provokeExportErrorToast();
                }

                openExportConfirmationDialogue(entryArr, i);

            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        builder.show();
    }

    private void provokeExportErrorToast(){
        Toast.makeText(getContext(), "Export problem occurred. Contact dev.", Toast.LENGTH_LONG).show();
    }


    private void openExportConfirmationDialogue(ArrayList<PurchaseDBHelper.PurchaseDBEntry> entries, final int opt){

        String titlePrefix = "Confirm export for ";
        String titleSuffix = opt == 0 ? mStoreName : "all stores";

        long currTime = System.currentTimeMillis();
        final String filename = titleSuffix.replace(" ", "_") + String.valueOf(currTime) + ".csv";

        AlertDialog.Builder exportConfirmDialogue = new AlertDialog.Builder(getContext());
        exportConfirmDialogue.setTitle(titlePrefix + titleSuffix);
        exportConfirmDialogue.setMessage("Data will be exported to Google Drive under name \"" + filename +"\"");
        exportConfirmDialogue.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                DataExporter.ExportDatabaseCSVTask.setActivity(((MainActivity)getActivity()));
                DataExporter.ExportDatabaseCSVTask.setCurrFileName(filename);
                if(opt==0){
                    DataExporter.ExportDatabaseCSVTask.setStoreName(mStoreName);
                }
                new DataExporter.ExportDatabaseCSVTask().execute();

            }
        }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        }).show();


    }

    class DataHolder extends RecyclerView.ViewHolder{

        private TextView itemName;
        private TextView dateTime;
        private TextView storeName;

        public DataHolder(View itemView) {
            super(itemView);
            itemName = itemView.findViewById(R.id.itemname);
            dateTime = itemView.findViewById(R.id.dateinput);
            storeName = itemView.findViewById(R.id.storeName);
        }

        public void setItemName(String name){
            itemName.setText(name);
        }

        public void setDateTime(String dt){
            //TODO: Customize how date time should be displayed
            dateTime.setText(dt);
        }

        public void setStoreName(String sn){
            storeName.setText(sn);
        }

        public void setItemName(SpannableString name){
            itemName.setText(name);
        }

        public void setDateTime(SpannableString dt){
            //TODO: Customize how date time should be displayed
            dateTime.setText(dt);
        }

        public void setStoreName(SpannableString sn){
            storeName.setText(sn);
        }
    }

    class DataAdapter extends RecyclerView.Adapter<DataHolder>{
        private ArrayList<PurchaseDBHelper.PurchaseDBEntry> entries;
        DataAdapter(ArrayList<PurchaseDBHelper.PurchaseDBEntry> entries){
            this.entries = entries;
        }

        @Override
        public DataHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycleritem, parent, false);

            return new DataHolder(v);

        }

        @Override
        public void onBindViewHolder(DataHolder holder, int position) {
            if(position!=0) {
                PurchaseDBHelper.PurchaseDBEntry entry = entries.get(position-1);

                //display date time as readable format instaed of milliseconds

                Date d = new Date(entry.getDateTime());
                SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");


                holder.setItemName(entry.getItemName());
                holder.setDateTime(formatter.format(d));
                holder.setStoreName(entry.getStoreName());
            }
            else{

                SpannableString str1 = new SpannableString("Item Name");
                str1.setSpan(new android.text.style.StyleSpan(android.graphics.Typeface.BOLD), 0, str1.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
                holder.setItemName(str1);

                SpannableString str2 = new SpannableString("Datetime (UTC)");
                str2.setSpan(new android.text.style.StyleSpan(android.graphics.Typeface.BOLD), 0, str2.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
                holder.setDateTime(str2);

                SpannableString str3 = new SpannableString("Location");
                str3.setSpan(new android.text.style.StyleSpan(android.graphics.Typeface.BOLD), 0, str3.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
                holder.setStoreName(str3);

            }
        }

        @Override
        public int getItemCount() {
            return entries.size()+1;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case READ_WRITE_PERMITS:
                Log.d(TAG, "onRequestPermissionsResult: granted read/write permissions");
                break;
        }
    }
}
