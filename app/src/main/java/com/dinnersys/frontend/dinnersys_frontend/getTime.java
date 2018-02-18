package com.dinnersys.frontend.dinnersys_frontend;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URL;
import java.net.URLConnection;
import java.util.Dictionary;
import java.util.Enumeration;

public class getTime {
    private String url = "http://dinnersys.ddns.net/dinnersys_beta/backend/backend.php?cmd=get_date";
    private final String[] exceptions = {"\uFEFF" ,"\r" ,"\n"};
    http_fetcher fetcher;
    String session_id;

    JSONObject obj;
    String[] dates = new String[5];

    public getTime(String sid)
    {
        session_id = sid;
        try
        {
            URLConnection conn = new URL(url).openConnection();
            conn.setRequestProperty("Cookie" ,session_id);
            fetcher = new http_fetcher();
            fetcher.execute(conn);

            String serv_json = fetcher.get();
            for(String e : exceptions) serv_json = serv_json.replace(e ,"");
            serv_json = "{\"menu\":" + serv_json + "}";
            obj = new JSONObject(serv_json);

            JSONArray arr = obj.getJSONArray("menu");
            String[] adapt = {"Monday" ,"Tuesday" ,"Wednesday" ,"Thursday" ,"Friday"};
            for(int i = 0;i != 5;i++)
                dates[i] = ((JSONObject)arr.get(i)).getString(adapt[i]);
        }
        catch(Exception e) {}


    }
    public JSONObject get_time()
    {
        return obj;
    }

    public String[] get_dates()
    {
        return dates;
    }

    public String get_weekday(String s)
    {
        String[] adapt = {"一" ,"二", "三" ,"四" ,"五"};
        int i;
        for(i = 0;i != 5;i++) if(dates[i].equals(s)) break;
        return adapt[i];
    }
}