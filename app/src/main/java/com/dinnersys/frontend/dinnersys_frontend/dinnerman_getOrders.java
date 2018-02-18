package com.dinnersys.frontend.dinnersys_frontend;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URL;
import java.net.URLConnection;

public class dinnerman_getOrders {
    private final String make_payment = "http://dinnersys.ddns.net/dinnersys_beta/backend/backend.php?cmd=make_payment" ,
        reverse_payment = "http://dinnersys.ddns.net/dinnersys_beta/backend/backend.php?cmd=reverse_payment" ,
        getOrders = "http://dinnersys.ddns.net/dinnersys_beta/backend/backend.php?cmd=show_order&payment_filter=nothing&date_filter=week&person_filter=class&plugin=yes";
    JSONArray[] everyday_data = new JSONArray[5];

    public dinnerman_getOrders(String sess_id)
    {
        http_fetcher fetcher = new http_fetcher();
        for(int i = 0;i != 5;i++) everyday_data[i] = new JSONArray();

        getTime time_handler = new getTime(sess_id);
        try
        {
            URLConnection conn = new URL(getOrders).openConnection();
            conn.setRequestProperty("Cookie" ,sess_id);
            fetcher.execute(conn);
            JSONObject serv_data = new JSONObject("{\"orders\":" + fetcher.get() + "}");
            JSONArray orders = serv_data.getJSONArray("orders");
            for(int i = 0;i != orders.length();i++)
            {
                JSONObject obj = (JSONObject)orders.get(i);
                obj.put("make_payment" ,
                        make_payment
                        + "&user_id=" + obj.getString("user_id")
                        + "&dish_id=" + obj.getString("dish_id")
                        + "&recv_date=" + obj.getString("recv_date")
                        + "&order_date=" + obj.getString("order_date"));
                obj.put("reverse_payment" ,
                        reverse_payment
                                + "&user_id=" + obj.getString("user_id")
                                + "&dish_id=" + obj.getString("dish_id")
                                + "&recv_date=" + obj.getString("recv_date")
                                + "&order_date=" + obj.getString("order_date"));


                String weekday = time_handler.get_weekday(obj.getString("recv_date"));
                switch (weekday)
                {
                    case "一":
                        everyday_data[0].put(obj);
                        break;
                    case "二":
                        everyday_data[1].put(obj);
                        break;
                    case "三":
                        everyday_data[2].put(obj);
                        break;
                    case "四":
                        everyday_data[3].put(obj);
                        break;
                    case "五":
                        everyday_data[4].put(obj);
                        break;
                }
            }
        }
        catch (Exception e) {}
    }

    public JSONArray get_data(int date){return everyday_data[date];}
} 