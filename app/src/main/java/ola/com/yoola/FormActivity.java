package ola.com.yoola;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import ola.com.yoola.adapter.CategoriesListAdapter;

import static ola.com.yoola.utils.NetUtils.showToast;


public class FormActivity extends ActionBarActivity {

    // Categories
    CategoriesListAdapter categoriesListAdapter;
    ExpandableListView categoriesListView;
    List<String> listDataHeader;
    private HashMap<String, List<String>> listDataChild;


    // Distance
    SeekBar distance;
    TextView distanceVal;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_form);

        categoriesListView = (ExpandableListView)findViewById(R.id.categoriesList);
        prepareListData();
        categoriesListAdapter= new CategoriesListAdapter(this, listDataHeader, listDataChild);
        // setting list adapter
        categoriesListView.setAdapter(categoriesListAdapter);

        categoriesListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {

                if(!distanceVal.getText().equals("0 KM")) {

                    String distance  = distanceVal.getText().toString().split(" ")[0]+"000";
                    String category = listDataChild.get(
                            listDataHeader.get(groupPosition)).get(
                            childPosition).toLowerCase().replace(" ","_");

                 //   showToast(FormActivity.this,distance+" "+category);
                    Intent intent = new Intent(FormActivity.this,MapsActivity.class);
                    intent.putExtra("distance",distance);
                    intent.putExtra("category",category);
                    startActivity(intent);

                } else {
                    showToast(FormActivity.this,R.string.select_distance);
                }

                return false;
            }
        });

        distance = (SeekBar)findViewById(R.id.seekBar);
        distanceVal = (TextView)findViewById(R.id.distance_val);
        distance.setMax(100);
        distance.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                distanceVal.setText(progress+" KM");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_form, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void prepareListData() {
        listDataHeader = new ArrayList<String>();
        listDataChild = new HashMap<String, List<String>>();

        // Adding child data
        listDataHeader.add("Nomad");
        listDataHeader.add("Artful");
        listDataHeader.add("Foodie");
        listDataHeader.add("Party");

        // Adding child data
        List<String> nomad= new ArrayList<String>();
        nomad.add("Amusement Park");
        nomad.add("Aquarium");
        nomad.add("Zoo");

        List<String> artful = new ArrayList<String>();
        artful.add("Place of Worship");
        artful.add("Museum");
        artful.add("Art Gallery");
        artful.add("Church");
        artful.add("Hindu Temple");
        artful.add("Mosque");

        List<String> foodie = new ArrayList<String>();
        foodie.add("Bakery");
        foodie.add("Bar");
        foodie.add("Cafe");
        foodie.add("Restaurant");

        List<String> party= new ArrayList<String>();
        party.add("Bowling Alley");
        party.add("Campground");
        party.add("Casino");
        party.add("Shopping Mall");

        listDataChild.put(listDataHeader.get(0), nomad); // Header, Child data
        listDataChild.put(listDataHeader.get(1), artful);
        listDataChild.put(listDataHeader.get(2), foodie);
        listDataChild.put(listDataHeader.get(3), party);
    }
}
