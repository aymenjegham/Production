package com.nfctag.printsecure.androidnfc;

/**
 * Created by Print Secure on 4/15/2016.
 */
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.TextView;

public class SigninActivity  extends AsyncTask<String,Void,String>{
    private TextView roleField;
    private Context context;
    private int byGetOrPost = 0;

    //flag 0 means get and 1 means post.(By default it is get.)
    public SigninActivity(Context context,TextView roleField,int flag) {
        this.context = context;

        this.roleField = roleField;
        byGetOrPost = flag;
    }

    protected void onPreExecute(){

    }

    @Override
    protected String doInBackground(String... arg0) {


        try {
            String username = (String) arg0[0];

            String link = "http://192.168.1.179:1999/nfc/getuser.php?id=" + username;

            URL url = new URL(link);
            HttpClient client = new DefaultHttpClient();
            HttpGet request = new HttpGet();
            request.setURI(new URI(link));
            HttpResponse response = client.execute(request);
            BufferedReader in = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));

            StringBuffer sb = new StringBuffer("");
            String line = "";

            while ((line = in.readLine()) != null) {
                sb.append(line);
                Log.e("login", sb.toString());
                break;
            }
            in.close();
            Log.e("login",sb.toString());
            return sb.toString();
        } catch (Exception e) {
            return new String("Exception: " + e.getMessage());
        }


    }


    @Override
    protected void onPostExecute(String result){

        this.roleField.setText(result);
    }
}
