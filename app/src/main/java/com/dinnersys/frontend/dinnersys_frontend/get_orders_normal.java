package com.dinnersys.frontend.dinnersys_frontend;

import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

public class get_orders_normal extends AppCompatActivity {
    CheckBox[] orders;
    JSONArray arr;
    String[] del_url;
    String session;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_get_orders_normal);
        try
        {
            session = getIntent().getStringExtra("session_id");
            getOrders handler = new getOrders(session);
            arr = handler.get_orders().getJSONArray("orders");
            del_url = new String[arr.length()];
            for(int i = 0;i != arr.length();i++) del_url[i] = ((JSONObject)arr.get(i)).getString("delete_url");
        }
        catch (Exception e){}
    }

    @Override
    protected void onStart()
    {
        findViewById(R.id.button11).setEnabled(true);
        int all = 0,unpaid = 0,paid = 0;

        LinearLayout ll = findViewById(R.id.get_orders_content);
        ll.removeAllViews();

        try
        {
            orders = new CheckBox[arr.length()];
            for(int i = 0;i != arr.length();i++)
            {
                orders[i] = new CheckBox(this);
                orders[i].setText(((JSONObject)arr.get(i)).getString("dish_name") + " " + ((JSONObject) arr.get(i)).getString("dish_charge") + "$.");
                orders[i].setEnabled(!((JSONObject)arr.get(i)).getString("paid_status").equals("您已經成功付款"));
            }
        }
        catch(Exception e) {}

        for(int i = 0;i != orders.length;i++)
        {
            LinearLayout llayout = new LinearLayout(this);
            llayout.setOrientation(LinearLayout.HORIZONTAL);

            TextView status = new TextView(this);
            status.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            status.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_END);
            TextView date = new TextView(this);
            try
            {
                date.setText(" " + ((JSONObject) arr.get(i)).getString("date"));
                String paid_status = (((JSONObject) arr.get(i)).getString("paid_status").equals("您已經成功付款") ? "已付" : "未付");
                if(((JSONObject) arr.get(i)).getString("paid_status").equals("您已經成功付款"))
                    paid += Integer.parseInt(((JSONObject) arr.get(i)).getString("dish_charge"));
                else
                    unpaid += Integer.parseInt(((JSONObject) arr.get(i)).getString("dish_charge"));
                all += Integer.parseInt(((JSONObject) arr.get(i)).getString("dish_charge"));
                status.setText(paid_status + "     ");
            }
            catch (Exception e) {}

            llayout.addView(orders[i]);
            llayout.addView(date);
            llayout.addView(status);
            ll.addView(llayout);
        }

        ((TextView)findViewById(R.id.get_orders_paid)).setText("Σ 已付: " + String.valueOf(paid) + "$");
        ((TextView)findViewById(R.id.get_orders_unpaid)).setText("Σ 未付: " + String.valueOf(unpaid) + "$");
        ((TextView)findViewById(R.id.get_orders_sum)).setText("Σ 全部: " + String.valueOf(all) + "$");

        super.onStart();
    }

    public void delete(View v)
    {
        if(((TextView)findViewById(R.id.get_orders_delete)).getText().toString().equals("刪除訂單")) attempt_delete();
        else exec_delete();
    }

    void attempt_delete()
    {
        String status = "你打算刪除以下的點單:\n";
        for(int i = 0;i != orders.length;i++)
            if(orders[i].isChecked())
                status += orders[i].getText().toString() + "\n";
        ((LinearLayout)findViewById(R.id.get_orders_content)).removeAllViews();
        TextView txv = new TextView(this);
        txv.setText(status);
        txv.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        txv.setTextSize(20);
        ((LinearLayout)findViewById(R.id.get_orders_content)).addView(txv);
        ((TextView)findViewById(R.id.get_orders_delete)).setText("確認刪除點單");
    }

    void exec_delete()
    {
        ArrayList<String> urls = new ArrayList<>();
        for(int i = 0;i != orders.length;i++)
            if(orders[i].isChecked())
                urls.add(del_url[i]);

        normal_getOrders_submitter submitter = new normal_getOrders_submitter(
                urls,
                (ProgressBar)findViewById(R.id.get_orders_status) ,
                this ,
                session ,
                new Handler(Looper.getMainLooper())
        );
        new AsyncRun().execute(submitter);
    }
}

class normal_getOrders_submitter implements Task {
    List<String> url;
    ProgressBar status;
    get_orders_normal activity;
    String session_id;
    Handler main_thread;
    final int LONG_DURATION = 10;

    public normal_getOrders_submitter(List<String> url ,ProgressBar bar ,get_orders_normal cont ,String sess ,Handler main_thread)
    {
        this.url = url;
        status = bar;
        activity = cont;
        session_id = sess;
        this.main_thread = main_thread;
    }

    @Override
    public void pre_exec()
    {
        main_thread.post(new Runnable() {
            public void run()
            {
                try
                {
                    status.setVisibility(View.VISIBLE);
                    if(url.size() >= LONG_DURATION)
                        Toast.makeText(activity ,"可能需要一點時間刪除點單，請稍後" ,Toast.LENGTH_LONG).show();
                }
                catch (Exception e) {}
            }
        });
    }

    @Override
    public void exec()
    {
        for(String this_run : url)
            try
            {
                URLConnection conn = new URL(this_run).openConnection();
                conn.setRequestProperty("Cookie", session_id);
                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder result = new StringBuilder(100000);

                String inputLine = "";
                while ((inputLine = in.readLine()) != null) {
                    result.append(inputLine);
                    result.append("\n");
                }
            }
            catch (Exception e) {}
    }

    @Override
    public void after_exec()
    {
        main_thread.post(new Runnable() {
            public void run()
            {
                try
                {
                    status.setVisibility(View.INVISIBLE);
                    Toast.makeText(activity ,"你已經成功刪除點單" ,Toast.LENGTH_LONG).show();
                    activity.finish();
                }
                catch (Exception e) {}

            }
        });
    }
}
