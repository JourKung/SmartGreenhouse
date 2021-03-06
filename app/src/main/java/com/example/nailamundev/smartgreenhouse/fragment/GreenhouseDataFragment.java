package com.example.nailamundev.smartgreenhouse.fragment;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.nailamundev.smartgreenhouse.R;
import com.example.nailamundev.smartgreenhouse.activity.ChartActivity;
import com.example.nailamundev.smartgreenhouse.dao.GreenhouseRealTime;
import com.example.nailamundev.smartgreenhouse.dao.GreenhousesItemDao;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.inthecheesefactory.thecheeselibrary.manager.Contextor;

import java.util.Map;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;


/**
 * Created by nuuneoi on 11/16/2014.
 */
@SuppressWarnings("unused")
public class GreenhouseDataFragment extends Fragment implements View.OnClickListener {


    // Variable
    private static final String UID = "uid";
    private static final String KEY_ITEM = "keyItem";
    private static final String DAO = "dao";
    private static final String AMOUNT = "amount";
    private static final String TAG_LOAD_DATA = "onDataChange";
    private static final int COLOR_NO_SENSOR = 0x90000000;
    private static final int COLOR_WATER_LOW = 0xfff44336;
    private static final int COLOR_WATER_NORMAL = 0xff8bc34a;
    private static final int COLOR_WATER_MEDIUM = 0xffffc107;


    private TextView tvAddressData, tvPlant, tvDate, tvExTemperature;
    private TextView tvValueTemperature, tvValueHumidity, tvValueSoil, tvValueLight, tvWaterLevel, tvIncome,
            tvExpense, tvProduct;
    private TextView tvErrorTemperature, tvErrorHumidity, tvErrorSoil, tvErrorLight;
    private View vTemperature, vHumidity, vSoil, vLight, vMap;
    private ImageView ivImgData;
    private ProgressDialog progressDialog;
    private SwipeRefreshLayout swipeRefreshLayoutData;
    private GreenhousesItemDao dao;
    private String keyItem;
    private int sumIncome, sumExpense, sumProduct;
    private int tempIncome, tempExpense, tempProduct;


    //Firebase Variable
    private DatabaseReference mDatabase, mGreenhouseRef;
    private ValueEventListener valueEventListener;
    private ChildEventListener mChildEventListenerIncome, mChildEventListenerExpense,
            mChildEventListenerProduct;
    private Query mQueryIncome, mQueryExpense, mQueryProduct;



    /*****************
     * Functions
     ****************/

    public GreenhouseDataFragment() {
        super();
    }

    @SuppressWarnings("unused")
    public static GreenhouseDataFragment newInstance(String keyItem, GreenhousesItemDao dao) {
        GreenhouseDataFragment fragment = new GreenhouseDataFragment();
        Bundle args = new Bundle();

        args.putString(KEY_ITEM, keyItem);
        args.putParcelable(DAO, dao);

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init(savedInstanceState);

        dao = getArguments().getParcelable(DAO);
        keyItem = getArguments().getString(KEY_ITEM);


        if (savedInstanceState != null)
            onRestoreInstanceState(savedInstanceState);

        mDatabase = FirebaseDatabase.getInstance().getReference();
        mGreenhouseRef = mDatabase.child("realtimeValSensor").child(UID).child(keyItem);
        mQueryIncome = mDatabase.child("income").child(UID).child(keyItem);
        mQueryExpense = mDatabase.child("expense").child(UID).child(keyItem);
        mQueryProduct = mDatabase.child("product").child(UID).child(keyItem);

        progressDialog = new ProgressDialog(getContext());
        progressDialog.setCancelable(false);
        progressDialog.setMessage(getString(R.string.connecting));
        progressDialog.show();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_greenhouse_data, container, false);
        initInstances(rootView, savedInstanceState);
        return rootView;
    }

    private void init(Bundle savedInstanceState) {
        // Init Fragment level's variable(s) here

    }

    @SuppressWarnings("UnusedParameters")
    private void initInstances(View rootView, Bundle savedInstanceState) {
        // Init 'View' instance(s) with rootView.findViewById here
        sumIncome = 0;
        sumExpense = 0;
        sumProduct = 0;

        // TextView
        tvAddressData = (TextView) rootView.findViewById(R.id.tvAddressData);
        tvPlant = (TextView) rootView.findViewById(R.id.tvPlant);
        tvDate = (TextView) rootView.findViewById(R.id.tvDate);
        tvValueTemperature = (TextView) rootView.findViewById(R.id.tvValueTemperature);
        tvValueHumidity = (TextView) rootView.findViewById(R.id.tvValueHumidity);
        tvValueSoil = (TextView) rootView.findViewById(R.id.tvValueSoil);
        tvValueLight = (TextView) rootView.findViewById(R.id.tvValueLight);
        tvWaterLevel = (TextView) rootView.findViewById(R.id.tvWaterLevel);
        tvExTemperature = (TextView) rootView.findViewById(R.id.tvExTemperature);
        tvErrorTemperature = (TextView) rootView.findViewById(R.id.tvErrorTemperature);
        tvErrorHumidity = (TextView) rootView.findViewById(R.id.tvErrorHumidity);
        tvErrorLight = (TextView) rootView.findViewById(R.id.tvErrorLight);
        tvErrorSoil = (TextView) rootView.findViewById(R.id.tvErrorSoil);

        // View Error
        vTemperature = rootView.findViewById(R.id.vTemperature);
        vHumidity = rootView.findViewById(R.id.vHumidity);
        vLight = rootView.findViewById(R.id.vLight);
        vSoil = rootView.findViewById(R.id.vSoil);
        vMap = rootView.findViewById(R.id.vMap);

        //Accout
        tvIncome = (TextView) rootView.findViewById(R.id.tvIncome);
        tvExpense = (TextView) rootView.findViewById(R.id.tvExpense);
        tvProduct = (TextView) rootView.findViewById(R.id.tvProduct);

        vTemperature.setOnClickListener(this);
        vHumidity.setOnClickListener(this);
        vLight.setOnClickListener(this);
        vSoil.setOnClickListener(this);
        vMap.setOnClickListener(this);

        swipeRefreshLayoutData = (SwipeRefreshLayout) rootView.findViewById(R.id.swipeRefreshLayoutData);
        swipeRefreshLayoutData.setOnRefreshListener(refreshDataRealtime);

        ivImgData = (ImageView) rootView.findViewById(R.id.ivImgData);

        // Set View
        tvPlant.setText(dao.plant);
        tvAddressData.setText(dao.address);
        tvDate.setText(getString(R.string.date) + " " + dao.date);
        setImageUrl(dao.img);

        loadData();
        loadDataIncome();
        loadDataExpense();
        loadDataProduct();
    }

    private void loadDataProduct() {
        mChildEventListenerProduct = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Map map = (Map) dataSnapshot.getValue();
                tempProduct = Integer.valueOf(String.valueOf(map.get(AMOUNT)));
                sumProduct = sumProduct + tempProduct;
                updateViewProduct(sumProduct);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                //TODO : Data product change

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                //TODO : Data product remove
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                //TODO : Data product move
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                //TODO : Data product cancel
            }
        };
        mQueryProduct.addChildEventListener(mChildEventListenerProduct);
    }

    private void updateViewProduct(int sumProduct) {
        tvProduct.setText(String.valueOf(sumProduct));
    }

    private void loadDataExpense() {
        mChildEventListenerExpense = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Map map = (Map) dataSnapshot.getValue();
                tempExpense = Integer.valueOf(String.valueOf(map.get(AMOUNT)));
                sumExpense = sumExpense + tempExpense;
                updateViewExpense(sumExpense);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                //TODO : Data expense change
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                //TODO : Data expense remove
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                //TODO : Data expense move
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                //TODO : Data expense cancel
            }
        };
        mQueryExpense.addChildEventListener(mChildEventListenerExpense);
    }

    private void updateViewExpense(int sumExpense) {
        tvExpense.setText(String.valueOf(sumExpense));
    }

    private void loadDataIncome() {

        mChildEventListenerIncome = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Map map = (Map) dataSnapshot.getValue();
                tempIncome = Integer.valueOf(String.valueOf(map.get(AMOUNT)));
                sumIncome = sumIncome + tempIncome;
                updateViewIncome(sumIncome);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                //TODO : Data income change
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                //TODO : Data income remove
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                //TODO : Data income move
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                //TODO : Data income cancel
            }
        };
        mQueryIncome.addChildEventListener(mChildEventListenerIncome);
    }

    private void updateViewIncome(int sumIncome) {
        tvIncome.setText(String.valueOf(sumIncome));
    }

    private void loadData() {
        valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                swipeRefreshLayoutData.setRefreshing(false);
                GreenhouseRealTime data = dataSnapshot.getValue(GreenhouseRealTime.class);
                Log.d(TAG_LOAD_DATA, "GreenhouseDataFragment");
                updateView(data);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                //TODO : Data cancel
            }
        };
        mGreenhouseRef.addValueEventListener(valueEventListener);


    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadData();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (valueEventListener != null) {
            mGreenhouseRef.removeEventListener(valueEventListener);
        }
        if (mChildEventListenerIncome != null) {
            mQueryIncome.removeEventListener(mChildEventListenerIncome);
        }
        if (mChildEventListenerExpense != null) {
            mQueryExpense.removeEventListener(mChildEventListenerExpense);
        }
        if (mChildEventListenerProduct != null) {
            mQueryProduct.removeEventListener(mChildEventListenerProduct);
        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mDatabase.onDisconnect();
    }




    /*
         * Save Instance State Here
         */
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        // Save Instance State here
    }

    /*
     * Restore Instance State Here
     */
    @SuppressWarnings("UnusedParameters")
    private void onRestoreInstanceState(Bundle savedInstanceState) {
        // Restore Instance State here
    }

    public void setImageUrl(String url) {
        Glide.with(GreenhouseDataFragment.this)
                .load(url)
                .placeholder(R.drawable.loading)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .crossFade()
                .into(ivImgData);
    }

    public void updateView(GreenhouseRealTime data) {

        progressDialog.dismiss();

        if (data.a5 == 0){
            // No sensor external temperature
            tvExTemperature.setText(" ");
        } else {
            tvExTemperature.setText(String.valueOf(data.a5 / 10));
        }


        if (data.a7 == 0) {

            // No sensor temperature and humidity
            tvErrorTemperature.setText(R.string.error_sensor);
            vTemperature.setBackgroundColor(COLOR_NO_SENSOR);
            vTemperature.setEnabled(false);
            tvValueTemperature.setText(" ");

            tvErrorHumidity.setText(R.string.error_sensor);
            vHumidity.setBackgroundColor(COLOR_NO_SENSOR);
            vHumidity.setEnabled(false);
            tvValueHumidity.setText(" ");

        } else {

            tvErrorTemperature.setText(" ");
            tvErrorHumidity.setText(" ");
            vTemperature.setEnabled(true);
            vHumidity.setEnabled(true);

            vTemperature.setBackground(ContextCompat.getDrawable(getActivity(), R.drawable.selector_data));
            vHumidity.setBackground(ContextCompat.getDrawable(getActivity(), R.drawable.selector_data));


            tvValueTemperature.setText(String.valueOf(data.a7 / 10) + " °C");
            tvValueHumidity.setText(String.valueOf(data.a6 / 10) + " %");


        }


        // Check light
        if (data.a1 == 0) {
            // No sensor light
            vLight.setBackgroundColor(COLOR_NO_SENSOR);
            vLight.setEnabled(false);
            tvErrorLight.setText(R.string.error_sensor);
            tvValueLight.setText(" ");

        } else {

            tvErrorLight.setText(" ");
            vLight.setEnabled(true);
            vLight.setBackground(ContextCompat.getDrawable(getActivity(), R.drawable.selector_data));
            tvValueLight.setText(String.valueOf(data.a1) + " lux");
        }

        //Check soil
        if (data.a3 == 0) {
            // No sensor soil
            vSoil.setBackgroundColor(COLOR_NO_SENSOR);
            vSoil.setEnabled(false);
            tvErrorSoil.setText(R.string.error_sensor);
            tvValueSoil.setText(" ");

        } else {
            tvErrorSoil.setText(" ");
            vSoil.setEnabled(true);
            vSoil.setBackground(ContextCompat.getDrawable(getActivity(), R.drawable.selector_data));
            tvValueSoil.setText(String.valueOf(data.a3) + " %");

        }


        // Check water level
        if (data.a10 > 100 && data.a10 <= 400) {
            tvWaterLevel.setText(R.string.water_normal);
            tvWaterLevel.setTextColor(COLOR_WATER_NORMAL);
        }
        else if (data.a10 > 400 && data.a10 <= 700) {
            tvWaterLevel.setText(R.string.water_medium);
            tvWaterLevel.setTextColor(COLOR_WATER_MEDIUM);
        }
        else if (data.a10 > 700) {
            tvWaterLevel.setText(R.string.water_low);
            tvWaterLevel.setTextColor(COLOR_WATER_LOW);
        }
        else {
            // No sensor water
            tvWaterLevel.setText(R.string.error_sensor);
            tvWaterLevel.setTextColor(COLOR_NO_SENSOR);
        }
        progressDialog.dismiss();

    }


    private void doFunctionMap() {
        String search = (dao.map + "(" + dao.name + ")");
        Uri gmmIntentUri = Uri.parse("geo:0,0?q=" + search);
        Intent googleMapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        googleMapIntent.setPackage("com.google.android.apps.maps");
        if (googleMapIntent.resolveActivity(getActivity().getPackageManager()) != null) {
            startActivity(googleMapIntent);
        }
    }


    /********************
     * Listener
     ********************/

    SwipeRefreshLayout.OnRefreshListener refreshDataRealtime = new SwipeRefreshLayout.OnRefreshListener() {
        @Override
        public void onRefresh() {
            loadData();
            loadDataIncome();
            loadDataExpense();
            loadDataProduct();
        }
    };


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.vMap:
                doFunctionMap();
                break;

            case R.id.vTemperature:
                Intent intentTemperature = new Intent(getActivity(), ChartActivity.class);
                startActivity(intentTemperature);
                break;
            case R.id.vHumidity:
                Intent intentHumidity = new Intent(getActivity(), ChartActivity.class);
                startActivity(intentHumidity);
                break;

            case R.id.vSoil:
                Intent intentSoil = new Intent(getActivity(), ChartActivity.class);
                startActivity(intentSoil);
                break;

            case R.id.vLight:
                Intent intentLight = new Intent(getActivity(), ChartActivity.class);
                startActivity(intentLight);
                break;

            default:
                break;

        }

    }


}
