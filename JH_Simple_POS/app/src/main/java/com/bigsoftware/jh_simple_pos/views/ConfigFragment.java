package com.bigsoftware.jh_simple_pos.views;


import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.bigsoftware.jh_simple_pos.R;
import com.bigsoftware.jh_simple_pos.data.StoreDBHelper;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ConfigFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ConfigFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final String TAG = "Config Frag";
    private StoreDBHelper helper;

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;


    @Override
    public void onDestroy() {
        helper.close();
        super.onDestroy();
    }

    public ConfigFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ConfigFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ConfigFragment newInstance(String param1, String param2) {
        ConfigFragment fragment = new ConfigFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
        helper = new StoreDBHelper(getContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View v = inflater.inflate(R.layout.fragment_config, container, false);

        final EditText storeName = ((EditText) v.findViewById(R.id.storeNameET));
        final EditText storeAddress = ((EditText) v.findViewById(R.id.storeAddressET));
        final EditText storeContact = ((EditText) v.findViewById(R.id.storeContactNameET));
        final EditText storePhone = ((EditText) v.findViewById(R.id.storePhoneET));
        final EditText storeNotes = ((EditText) v.findViewById(R.id.storeNotesET));

        //Load config info in config fragment if a store is set (so it's not blank)
        if(!((MainActivity)getActivity()).getCurrentStore().equals(getString(R.string.defaultstore))){
            fillForms(v, helper.getEntry(((MainActivity) getActivity()).getCurrentStore(), new Runnable() {
                @Override
                public void run() {
                    try {
                        throw new Exception("Could not load store data.");
                    } catch (Exception e) {
                        Log.e(TAG, "run: ", e);
                    }
                }
            }));
        }


        //TODO: Make submit button ALSO update if entry already exists.
        Button submit = v.findViewById(R.id.submitConfigBtn);
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(storeName.getText().toString().equals("")) {
                    Toast.makeText(getContext(), "Please enter store info!", Toast.LENGTH_LONG).show();
                    return;
                }

                helper.updateOrInsertEntry(storeName.getText().toString(),
                        storeAddress.getText().toString(),
                        storeContact.getText().toString(),
                        storePhone.getText().toString(),
                        storeNotes.getText().toString(),
                        new Runnable() { //on success
                            @Override
                            public void run() {
                                Toast.makeText(getContext(),
                                        "Added/updated store to database.",
                                        Toast.LENGTH_LONG).show();
                            }
                        }, new Runnable() {//on failure
                            @Override
                            public void run() {
                                Toast.makeText(getContext(),
                                        "Failed to add the store to the database. Contact developer.",
                                        Toast.LENGTH_LONG).show();
                            }
                        });
            }
        });

        Button viewStores = v.findViewById(R.id.viewStoresBtn);
        viewStores.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (!helper.hasStores()) {
                    Toast.makeText(getContext(), "There are no stores saved.", Toast.LENGTH_LONG).show();
                } else {
                    AlertDialog.Builder builderSingle = new AlertDialog.Builder(getContext());
                    builderSingle.setTitle("Available Stores: ");

                    final ArrayAdapter<String> storeAdapter = new ArrayAdapter<String>(getContext(), android.R.layout.select_dialog_singlechoice);

                    for (String s:
                         helper.getStoreNames()) {
                        storeAdapter.add(s);
                    }

                    builderSingle.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });

                    builderSingle.setAdapter(storeAdapter, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            final String storeChosen = storeAdapter.getItem(which);

                            AlertDialog.Builder builderSingleActions = new AlertDialog.Builder(getContext());
                            final ArrayAdapter<String> actions = new ArrayAdapter<String>(getContext(), android.R.layout.select_dialog_singlechoice);
                            actions.add("Set store");
                            actions.add("View/Edit");
                            actions.add("Delete");

                            builderSingleActions.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            });

                            builderSingleActions.setTitle("Select action for " + storeChosen);
                            builderSingleActions.setAdapter(actions, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    switch(i){
                                        case 0:
                                            setStore(storeChosen);
                                            ((MainActivity)getActivity()).setActionBarTitle();
                                            fillForms(v, helper.getEntry(storeChosen, new Runnable() {
                                                @Override
                                                public void run() {
                                                    Log.d(TAG, "run: COULD NOT LOAD STORE DATA INTO CONFIG FRAGMENT");
                                                    Toast.makeText(getContext(), "Error 1234 occured. Contact dev.", Toast.LENGTH_LONG).show();
                                                }
                                            }));
                                            Toast.makeText(getContext(), "Store set as " + storeChosen, Toast.LENGTH_LONG).show();
                                            dialogInterface.dismiss();
                                            break;
                                        case 1:
                                            StoreDBHelper.StoreDBEntry entry = helper.getEntry(storeChosen, new Runnable() {
                                                @Override
                                                public void run() {
                                                    Toast.makeText(getContext(), "Could not load store info.", Toast.LENGTH_LONG).show();
                                                }
                                            });
                                            fillForms(v, entry);
                                            Toast.makeText(getContext(), "You are in view/edit mode only- store is not set!", Toast.LENGTH_LONG).show();
                                            dialogInterface.dismiss();
                                            break;
                                        case 2:
                                            showDeletionDialogue(storeChosen, v);
                                            dialogInterface.dismiss();
                                            break;

                                    }
                                }
                            }).show();

                        }
                    });
                    builderSingle.show();
                }

            }
        });

        return v;
    }

    private void setStore(String storeName){
        getActivity().getPreferences(Context.MODE_PRIVATE)
                .edit()
                .putString(getString(R.string.storepreference), storeName)
                .apply();

        ((MainActivity)getActivity()).setActionBarTitle();
    }

    private void showDeletionDialogue(final String storeName, final View v){
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which){
                    case DialogInterface.BUTTON_NEGATIVE:
                        helper.deleteEntry(storeName);
                        clearForm(v);
                        setStore(getString(R.string.defaultstore));
                        ((MainActivity)getActivity()).setActionBarTitle();

                        break;

                    case DialogInterface.BUTTON_POSITIVE:
                        Toast.makeText(getContext(), "Cancelled", Toast.LENGTH_LONG).show();
                        break;
                }
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setMessage("Delete " + storeName + "?").setPositiveButton("No", dialogClickListener)
                .setNegativeButton("Yes", dialogClickListener).show();
    }

    private void fillForms(View v, StoreDBHelper.StoreDBEntry entry){
        final EditText storeName = ((EditText) v.findViewById(R.id.storeNameET));
        final EditText storeAddress = ((EditText) v.findViewById(R.id.storeAddressET));
        final EditText storeContact = ((EditText) v.findViewById(R.id.storeContactNameET));
        final EditText storePhone = ((EditText) v.findViewById(R.id.storePhoneET));
        final EditText storeNotes = ((EditText) v.findViewById(R.id.storeNotesET));

        storeName.setText(entry.getStoreName());
        storeAddress.setText(entry.getStoreAddress());
        storeContact.setText(entry.getContactName());
        storePhone.setText(entry.getContactPhone());
        storeNotes.setText(entry.getStoreNotes());
    }

    private void clearForm(View v){
        final EditText storeName = ((EditText) v.findViewById(R.id.storeNameET));
        final EditText storeAddress = ((EditText) v.findViewById(R.id.storeAddressET));
        final EditText storeContact = ((EditText) v.findViewById(R.id.storeContactNameET));
        final EditText storePhone = ((EditText) v.findViewById(R.id.storePhoneET));
        final EditText storeNotes = ((EditText) v.findViewById(R.id.storeNotesET));

        storeName.setText("");
        storeAddress.setText("");
        storeContact.setText("");
        storePhone.setText("");
        storeNotes.setText("");

    }

}
