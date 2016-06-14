package ucd.android.rightmove;


import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

import ucd.android.rightmove.Fragments.GmapFragment;


// AsyncTask<Params,Progress,Results>

// Params :- Input, Progress :- passed to onProgressUpdate(), Results :- the output - what returns in doInBackground()

public class AsyncTaskParseLocation extends AsyncTask<String, Void, String> {

    final String TAG = "AsyncTaskParseLocation";

    private final GmapFragment mapFragment;

    // only used to test  ... passing in fragment in order to execute

    public AsyncTaskParseLocation(GmapFragment mapFragment){

        this.mapFragment =  mapFragment;

        //Toast.makeText(this.mapFragment.getActivity(), "AsyncTaskParseLocation Instance...", Toast.LENGTH_LONG).show();
    }

    // test url

    protected void onPreExecute() {
        Log.i(TAG, "onPreExecute()");
    }

    protected String doInBackground(String... urls){
        Log.i(TAG, "doInBackground()");
        HttpURLConnection urlConnection = null;
        String locationJsonUrl = null;
        String response = null;
        URL url = null;
        try {
            url = new URL(urls[0]);
        }catch(MalformedURLException e){}

        try {
            urlConnection = (HttpURLConnection)url.openConnection();
        }catch(IOException e){}

        try {
            urlConnection.setRequestMethod("GET");
        }catch(ProtocolException e){}

        try {
            urlConnection.connect();
        }catch(IOException e){}

        // Read the input

        InputStream inputStream = null;

        try {
           // BufferedReader r = new BufferedReader( new InputStreamReader(urlConnection.getInputStream()));
            inputStream = urlConnection.getInputStream();
        }catch(IOException e){}

        locationJsonUrl = readStream(inputStream);
        return locationJsonUrl;
    }

    private String readStream(InputStream in){
        Log.i(TAG, "readStream()");
        //Toast.makeText(this.mapFragment.getActivity(), "readStream()", Toast.LENGTH_LONG).show();
        BufferedReader reader = null;
        StringBuffer data = new StringBuffer("");
        try {
            reader = new BufferedReader(new InputStreamReader(in));
            String line = "";
            while((line = reader.readLine()) != null){
                data.append(line);
            }
        }catch(IOException e) {}
        finally{
            if (reader != null){
                try {
                    reader.close();
                }catch(IOException e){}
            }
        }
        return data.toString();
    }

    protected void onPostExecute(String result){
        Log.i(TAG, "onPostExecute()");
        String in = result;

        //Toast.makeText(this.mapFragment.getActivity(), "onPostExecute..."+in, Toast.LENGTH_LONG).show();

       if ( in != null )
        try {

            // For OUR example we need to test if single json object or an array

            JSONObject json = new JSONObject(in);

            // Getting JSON Array node - in this example its 'Users' ...

            JSONArray contacts = json.getJSONArray("Users");

            // looping through All Users / Locations ... we will build up an ArrayList of markers

            for (int i = 0; i < contacts.length(); i++) {

                JSONObject c = contacts.getJSONObject(i);
            }


        }catch(JSONException e){}
    }
}
