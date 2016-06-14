package ucd.android.rightmove.Fragments;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;
import android.widget.Toast;

import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;

import android.os.Handler;
import android.os.ResultReceiver;


import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import android.location.Address;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import ucd.android.rightmove.AsyncTaskParseLocation;
import ucd.android.rightmove.Constants;
import ucd.android.rightmove.FetchAddressIntentService;
import ucd.android.rightmove.MyMarker;
import ucd.android.rightmove.R;

public class GmapFragment extends Fragment
        implements
            GoogleApiClient.ConnectionCallbacks,
                GoogleApiClient.OnConnectionFailedListener,
                        OnMapReadyCallback {


    private static final String TAG = "GmapFragment";
    private GoogleApiClient client;
    private Location mlocation;
    private GoogleMap gMap;
    private LatLng marker;
    private Marker mlocmarker;
    private Intent intent;

    private static final String PLACES_API_BASE = "https://maps.googleapis.com/maps/api/place";
    private static final String TYPE_AUTOCOMPLETE = "/autocomplete";
    private static final String OUT_JSON = "/json";
    private static final String API_KEY = "AIzaSyAfYexK4St8MAPNTjgK5LQTPGz-LVUuHbI";

    private HashMap<Marker, MyMarker> mMarkersHashMap;

    //Receiver registered with this activity to get the response from FetchAddressIntentService.

    private AddressResultReceiver mResultReceiver;




    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // keep the fragment and all its data across screen rotation

        setRetainInstance(true);

        View layout = inflater.inflate(R.layout.fragment_gmaps, container, false);

        AutoCompleteTextView autoCompleteView = (AutoCompleteTextView)layout.findViewById(R.id.autoCompleteTextView);
        autoCompleteView.setAdapter(new GooglePlacesAutocompleteAdapter(getActivity(), R.layout.list_item));
        autoCompleteView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                // do something ..

                postionMarker( getAddress(parent.getItemAtPosition(position).toString()));
            }
        });

        return layout;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);
        buildGoogleApiClient();

        MapFragment fragment = (MapFragment)getChildFragmentManager().findFragmentById(R.id.map);
        fragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        gMap = googleMap;

        gMap.getUiSettings().setZoomControlsEnabled(true);
        gMap.getUiSettings().setMapToolbarEnabled(false);



        gMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
            LatLng temp = null;
            @Override

            public void onMarkerDragStart( Marker marker ) {
                temp = marker.getPosition();
            }

            @Override
            public void onMarkerDragEnd(Marker marker) {
                LatLng dragPosition = marker.getPosition();
                mlocmarker.setPosition(dragPosition);

                mlocation.setLatitude(dragPosition.latitude);
                mlocation.setLongitude(dragPosition.longitude);

                if (client.isConnected() ) {
                    startIntentService();
                }
            }

            @Override
            public void onMarkerDrag(Marker marker) {
                LatLng temp = marker.getPosition();
                mlocmarker.setPosition(temp);
            }
        });


        /*
        gMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {

                // check if mlocation marker already exists.

                if ( mlocmarker != null)
                    mlocmarker.remove();

                // change mlocation position

                mlocation.setLatitude(latLng.latitude);
                mlocation.setLongitude(latLng.longitude);

                postionMarker(latLng);

                //mlocmarker = gMap.addMarker(new MarkerOptions().position(latLng));
            }
        });
        */
    }

    public void mapPosition(){

        // try - catch hack to get around Permissions ...

        try {
            mlocation = LocationServices.FusedLocationApi.getLastLocation( client );

            if (mlocation != null ){
                LatLng marker = new LatLng(mlocation.getLatitude(), mlocation.getLongitude());
                postionMarker(marker);
                String jsonUrl = "http://demo.codeofaninja.com/tutorials/json-example-with-php/index.php";
                AsyncTaskParseLocation task = new AsyncTaskParseLocation(this);
                task.execute(jsonUrl);

            }
        } catch (SecurityException e){}
    }

    private void plotMarkers(){

        ArrayList<MyMarker> markers = new ArrayList<MyMarker>();
        mMarkersHashMap = new HashMap<Marker, MyMarker>();

        markers.add(new MyMarker("21/05/2016","13 Portersgate Way, Clonsilla","Dublin",123.13,"3 bed House", 53.38307, -6.41031));

        if ( markers.size() > 0 ){

            for ( MyMarker myMarker : markers ){

                MarkerOptions markerOptions = new MarkerOptions().position( new LatLng(myMarker.getLatitude(), myMarker.getLongitude()) );

                Marker currentMarker = gMap.addMarker( markerOptions );

                mMarkersHashMap.put(currentMarker, myMarker);

                gMap.setInfoWindowAdapter( new MarkerInfoWindowAdaptor() );
            }
        }
    }

    public class MarkerInfoWindowAdaptor implements GoogleMap.InfoWindowAdapter {

        public MarkerInfoWindowAdaptor(){

        }

        @Override
        public View getInfoWindow(Marker marker) {
            return null;
        }

        public View getInfoContents(Marker marker) {

            View v = getActivity().getLayoutInflater().inflate(R.layout.infowindow_layout, null);



                MyMarker m = mMarkersHashMap.get(marker);
                TextView date = (TextView) v.findViewById(R.id.date_label);
                TextView address = (TextView) v.findViewById(R.id.address_label);
                TextView county = (TextView) v.findViewById(R.id.county_label);
                TextView price = (TextView) v.findViewById(R.id.price_label);
                TextView description = (TextView) v.findViewById(R.id.description_label);
            try {
                date.setText("Date Sold: "+ m.getDate());
                address.setText("Address: "+ m.getAddress());
                county.setText("County: " +m.getCounty());
                price.setText("Price : £" +m.getPrice());
                description.setText("Desc : " +m.getDescription());
            }
            catch( Exception e) {

                // mlocation currently not saved as a HashMap object.

                date.setText("                 ");
                address.setText(mlocmarker.getTitle());
                address.setTextColor(Color.RED);
                county.setText(mlocmarker.getSnippet());
                county.setTextColor(Color.RED);
                //county.setText(mlocmarker.getTitle());
                //price.setText(mlocmarker.getTitle());

            }

            return v;
        }
    }

    public void postionMarker(LatLng marker){

        // Only want a single instance of our position.

        if ( mlocmarker != null )
            mlocmarker.remove();

        gMap.moveCamera(CameraUpdateFactory.newLatLngZoom(marker, 13));
       // mlocmarker = gMap.addMarker(new MarkerOptions().position(marker).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)).draggable(true));


        mlocmarker = gMap.addMarker(new MarkerOptions().position(marker).icon(BitmapDescriptorFactory.fromResource(R.drawable.logo)).draggable(true));
        // get Address - using an IntentService to get the task of the main thread - as in the case of getAddress()

        if (client.isConnected() ) {
            startIntentService();
        }

        plotMarkers();

    }

    // Will need to get this off the main thread ... hence the IntentService.

    public LatLng getAddress(String address){

        Geocoder geocoder = new Geocoder(getActivity(), Locale.ENGLISH);
        LatLng latlng = null;

        try{
            List<Address>addressList = geocoder.getFromLocationName(address,1);
            if ( addressList != null && addressList.size() > 0){

                latlng = new LatLng(addressList.get(0).getLatitude(),addressList.get(0).getLongitude() );

                mlocation.setLatitude(addressList.get(0).getLatitude());
                mlocation.setLongitude(addressList.get(0).getLongitude());
            }
        }
        catch( Exception e){}

        return latlng;
    }

    public String getAddress() {

        StringBuilder sb = new StringBuilder();
        sb.append("");
        try {
            Geocoder geocoder = new Geocoder(getActivity(), Locale.ENGLISH);
            List<Address> addressList = geocoder.getFromLocation(mlocation.getLatitude(), mlocation.getLongitude(), 1);

            if (geocoder.isPresent()) {
                Address address = addressList.get(0);

                for (int i = 0; i < address.getMaxAddressLineIndex(); i++) {
                    sb.append(address.getAddressLine(i)).append("\n");
                }

                sb.append(address.getCountryName());
            }

        }catch (IOException e) { }

        Toast.makeText(getActivity(), sb.toString(), Toast.LENGTH_LONG).show();

        return( sb.toString());
    }

    protected synchronized void buildGoogleApiClient(){
        client = new GoogleApiClient.Builder(getActivity())
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Places.GEO_DATA_API)
                .addApi(LocationServices.API).build();
    }

    public void onConnected(Bundle bundle){
        Log.d(TAG,"onConnected ... ");
        mapPosition();
    }

    public void onConnectionFailed(ConnectionResult connectionResult){
        Log.d(TAG,"onConnectionFailed ... . ");
    }

    public void onConnectionSuspended(int i ){
        Log.d(TAG,"onConnectionSuspended ...  ");
    }

    public void onStart() {
        super.onStart();
        client.connect();
    }


    protected void startIntentService() {

        // Create an intent for passing to the intent service responsible for fetching the address.

        intent = new Intent(getActivity(), FetchAddressIntentService.class);

        // Pass the result receiver as an extra to the service.

        mResultReceiver = new AddressResultReceiver(new Handler());

        intent.putExtra(Constants.RECEIVER , mResultReceiver);

        // Pass the location data as an extra to the service.

        intent.putExtra(Constants.LOCATION_DATA_EXTRA, mlocation);

        getActivity().startService(intent);
    }


    // Technically we should implement an address class that would implement Parcelable

    // see - http://alexzh.com/uncategorized/passing-object-by-intent/ - for a simple explanation.

    @SuppressLint("ParcelCreator")
    class AddressResultReceiver extends ResultReceiver  {
        public AddressResultReceiver(Handler handler) {
            super(handler);
        }
        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {

            String address = resultData.getString(Constants.RESULT_DATA_KEY);

            String town = resultData.getString(Constants.RESULT_DATA_TOWN);

            String street  = resultData.getString(Constants.RESULT_DATA_STREET);

            if (resultCode == Constants.SUCCESS_RESULT) {
                mlocmarker.setTitle(town);
                mlocmarker.setSnippet(street);
            }
        }
    }

    class GooglePlacesAutocompleteAdapter extends ArrayAdapter implements Filterable {

        private ArrayList resultList;

        public GooglePlacesAutocompleteAdapter(Context context, int textViewResourceId) {
            super(context, textViewResourceId);
        }

        @Override
        public int getCount() {
            return resultList.size();
        }

        @Override
        public String getItem(int index) {
            return resultList.get(index).toString();
        }

        @Override
        public Filter getFilter() {

            Filter filter = new Filter() {

                @Override
                protected FilterResults performFiltering(CharSequence constraint) {
                    FilterResults filterResults = new FilterResults();
                    if (constraint != null) {

                        // Retrieve the autocomplete results.

                        resultList = autocomplete(constraint.toString());

                        // Assign the data to the FilterResults

                        filterResults.values = resultList;

                        filterResults.count = resultList.size();
                    }
                    return filterResults;
                }

                @Override
                protected void publishResults(CharSequence constraint, Filter.FilterResults results) {

                    if (results != null && results.count > 0) {
                        notifyDataSetChanged();
                    } else {
                        notifyDataSetInvalidated();
                    }
                }
            };
            return filter;
        }

    }


    public static ArrayList autocomplete(String input) {

        ArrayList resultList = null;
        HttpURLConnection conn = null;
        StringBuilder jsonResults = new StringBuilder();
        try {
            StringBuilder sb = new StringBuilder(PLACES_API_BASE + TYPE_AUTOCOMPLETE + OUT_JSON);

            sb.append("?key=" + API_KEY +"" +"&components=country:ie" +"&input=" + URLEncoder.encode(input, "utf8"));

            URL url = new URL(sb.toString());
            conn = (HttpURLConnection) url.openConnection();

            InputStreamReader in = new InputStreamReader(conn.getInputStream());

            // Load the results into a StringBuilder

            int read;
            char[] buff = new char[1024];
            while ((read = in.read(buff)) != -1) {
                jsonResults.append(buff, 0, read);
            }
        } catch (MalformedURLException e) {
            Log.e(TAG, "Error processing Places API URL", e);
            return resultList;
        } catch (IOException e) {
            Log.e(TAG, "Error connecting to Places API", e);
            return resultList;
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
        try {
            // Create a JSON object hierarchy from the results

            JSONObject jsonObj = new JSONObject(jsonResults.toString());

            JSONArray predsJsonArray = jsonObj.getJSONArray("predictions");

            // Extract the Place descriptions from the results

            resultList = new ArrayList(predsJsonArray.length());

            for (int i = 0; i < predsJsonArray.length(); i++) {
                resultList.add(predsJsonArray.getJSONObject(i).getString("description"));
            }

        } catch (JSONException e) {
            Log.e(TAG, "Cannot process JSON results", e);
        }

        return resultList;
    }
}