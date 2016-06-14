package ucd.android.rightmove;


import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

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

    // only used to test  ...

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
        String test = result;

        Toast.makeText(this.mapFragment.getActivity(), "onPostExecute..."+test, Toast.LENGTH_LONG).show();
        try {
            JSONObject jsonResult = new JSONObject(test);
        }catch(JSONException e){}
    }
}
