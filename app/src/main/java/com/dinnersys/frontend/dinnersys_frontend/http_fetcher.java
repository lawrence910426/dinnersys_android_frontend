package com.dinnersys.frontend.dinnersys_frontend;

import android.os.AsyncTask;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLConnection;

public class http_fetcher extends AsyncTask<URLConnection,Void ,String> {
    final int TIMEOUT_VALUE = 5000;

    protected String doInBackground(URLConnection... conn) {
        return urlMethod(conn);
    }

    String urlMethod(URLConnection... conn)
    {
        StringBuilder result = new StringBuilder(100000);
        try {
            conn[0].setConnectTimeout(TIMEOUT_VALUE);
            conn[0].setReadTimeout(TIMEOUT_VALUE);
            BufferedReader in = new BufferedReader(new InputStreamReader(conn[0].getInputStream()));

            String inputLine = new String();
            while ((inputLine = in.readLine()) != null) {
                result.append(inputLine);
                result.append("\n");
            }
            in.close();

        } catch (Exception e) {
            result = new StringBuilder(1000);
            result.append("More than " + TIMEOUT_VALUE + " elapsed." + "\nErrorData" + e.toString());
        }

        return result.toString().replace("\uFEFF" ,""); //remove the BOM header.
    }
}