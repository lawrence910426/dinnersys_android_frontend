package com.dinnersys.frontend.dinnersys_frontend;


import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URL;
import java.net.URLConnection;


public class getMenu
{
    URLConnection conn;
    http_fetcher fetcher;
    private String[] dates = new String[5];
    String session;
    final String make_order = "http://dinnersys.ddns.net/dinnersys_beta/backend/backend.php?cmd=make_order&plugin=yes"
            ,get_menu = "http://dinnersys.ddns.net/dinnersys_beta/backend/backend.php?cmd=show_menu&plugin=yes"
            ,BOMHeader = "\uFEFF"
            ,JunkDish = "#";
    getTime timer;

    public getMenu(String session)
    {
        try
        {
            timer = new getTime(session);
            String[] adapt = {"Monday" ,"Tuesday" ,"Wednesday" ,"Thursday" ,"Friday"};
            JSONArray arr = timer.get_time().getJSONArray("menu");
            for(int i = 0;i != 5;i++) dates[i] = ((JSONObject)arr.get(i)).getString(adapt[i]);
        }
        catch (Exception e)
        { }


        fetcher = new http_fetcher();
        this.session = session;
    }

    public JSONObject get_menu()
    {
        try
        {
            conn = new URL(get_menu).openConnection();
            conn.setRequestProperty("Cookie" , session);
            fetcher.execute(conn);

            String menu = "{\"menu\":" + fetcher.get().replace(BOMHeader ,"") + "}";
            JSONObject jsmenu = new JSONObject(menu);
            JSONArray dish = jsmenu.getJSONArray("menu");
            for(int i = 0;i != dish.length();i++)
            {
                JSONObject obj = (JSONObject)dish.get(i);
                if(obj.getString("dish_name").equals(JunkDish))
                {
                    dish.remove(i); i -= 1;
                    continue;
                }

                for(int j = 0;j != 5;j++)
                {
                    obj.put(String.valueOf(j) ,make_order +
                            "&date=" + dates[j] +
                            "&dish_id=" + obj.getString("dish_id"));
                }
            }
            return jsmenu;
        }
        catch(Exception e)
        {   }
        return null;
    }
}


