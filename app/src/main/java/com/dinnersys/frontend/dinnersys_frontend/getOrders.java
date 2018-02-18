package com.dinnersys.frontend.dinnersys_frontend;


import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URL;
import java.net.URLConnection;


public class getOrders
{
    URLConnection conn;
    http_fetcher fetcher;
    String session;
    final String delete_order = "http://dinnersys.ddns.net/dinnersys_beta/backend/backend.php?cmd=delete_order" //recv_date ,order_date ,dish_id
            ,get_order = "http://dinnersys.ddns.net/dinnersys_beta/backend/backend.php?cmd=show_order&type=self&plugin=yes"
            ,BOMHeader = "\uFEFF"
            ,JunkDish = "#";
    getTime timer;

    public getOrders(String session)
    {
        timer = new getTime(session);
        fetcher = new http_fetcher();
        this.session = session;
    }

    public JSONObject get_orders()
    {
        try
        {
            conn = new URL(get_order).openConnection();
            conn.setRequestProperty("Cookie" , session);
            fetcher.execute(conn);

            String menu = "{\"orders\":" + fetcher.get().replace(BOMHeader ,"") + "}";
            JSONObject jsmenu = new JSONObject(menu);
            JSONArray orders = jsmenu.getJSONArray("orders");
            for(int i = 0;i != orders.length();i++)
            {
                JSONObject obj = (JSONObject)orders.get(i);
                if(obj.getString("dish_name").equals(JunkDish))
                {
                    orders.remove(i); i -= 1;
                    continue;
                }
                obj.put("delete_url" ,
                        delete_order +
                                "&recv_date=" + obj.getString("recv_date") +
                                "&order_date=" + obj.getString("order_date") +
                                "&dish_id=" + obj.getString("dish_id"));
                char[] date = obj.getString("recv_date").toCharArray();
                String short_date = String.copyValueOf(date ,5 ,date.length - 5);
                obj.put("date" ,short_date
                        + "(" + timer.get_weekday(obj.getString("recv_date")) + ")");
            }
            return jsmenu;
        }
        catch(Exception e)
        {   }
        return null;
    }
}


