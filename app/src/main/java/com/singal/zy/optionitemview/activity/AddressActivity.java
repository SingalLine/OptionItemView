package com.singal.zy.optionitemview.activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import com.google.gson.Gson;
import com.singal.zy.normal_libs.address.AddressSelector;
import com.singal.zy.normal_libs.address.CityInterface;
import com.singal.zy.normal_libs.address.OnItemClickListener;
import com.singal.zy.optionitemview.R;
import com.singal.zy.optionitemview.model.City;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;

public class AddressActivity extends AppCompatActivity {

    private ArrayList<City> cities1 = new ArrayList<>();
    private ArrayList<City> cities2 = new ArrayList<>();
    private ArrayList<City> cities3 = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_address);

        setData();

    }

    private void setData() {
        //拿到本地JSON 并转成String
        try {
            JSONArray jsonArray= new JSONArray(getString(R.string.cities1));
            for(int i =0;i<jsonArray.length();i++){
                cities1.add(new Gson().fromJson(jsonArray.get(i).toString(),City.class));
            }
            JSONArray jsonArray2= new JSONArray(getString(R.string.cities2));
            for(int i =0;i<jsonArray2.length();i++){
                cities2.add(new Gson().fromJson(jsonArray2.get(i).toString(),City.class));
            }
            JSONArray jsonArray3= new JSONArray(getString(R.string.cities3));
            for(int i =0;i<jsonArray3.length();i++){
                cities3.add(new Gson().fromJson(jsonArray3.get(i).toString(),City.class));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        AddressSelector addressSelector = (AddressSelector) findViewById(R.id.address);
        addressSelector.setTabAmount(3);
        addressSelector.setCities(cities1);
        addressSelector.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void itemClick(AddressSelector addressSelector, CityInterface city, int tabPosition) {
                switch (tabPosition){
                    case 0:
                        addressSelector.setCities(cities2);
                        break;
                    case 1:
                        addressSelector.setCities(cities3);
                        break;
                    case 2:
                        Toast.makeText(AddressActivity.this,"tabPosition ："+tabPosition+" "+city.getCityName(), Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        });
        addressSelector.setOnTabSelectedListener(new AddressSelector.OnTabSelectedListener() {
            @Override
            public void onTabSelected(AddressSelector addressSelector, AddressSelector.Tab tab) {
                switch (tab.getIndex()){
                    case 0:
                        addressSelector.setCities(cities1);
                        break;
                    case 1:
                        addressSelector.setCities(cities2);
                        break;
                    case 2:
                        addressSelector.setCities(cities3);
                        break;
                }
            }

            @Override
            public void onTabReselected(AddressSelector addressSelector, AddressSelector.Tab tab) {

            }
        });

    }
}
