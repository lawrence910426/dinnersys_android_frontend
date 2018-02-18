package com.dinnersys.frontend.dinnersys_frontend;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.net.URL;
import java.net.URLConnection;

public class MainActivity extends AppCompatActivity {
    String session_id ,user_id;
    JSONObject user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Intent it = getIntent();
        session_id = it.getStringExtra("session_id");
        try
        {
            user = new JSONObject(it.getStringExtra("user_json"));
            user_id = user.getString("user_id");
        }
        catch (Exception e) {}
    }

    public void show_order(View v)
    {
        Intent it = new Intent(this ,get_orders_normal.class);
        it.putExtra("session_id" ,session_id);
        startActivity(it);
    }

    public void show_menu(View v)
    {
        Intent it = new Intent(this ,ShowMenuActivity.class);
        it.putExtra("session_id" ,session_id);
        it.putExtra("user_id" ,user_id);
        startActivity(it);
    }

    public void log_out(View v)
    {
        finish();
    }

    public void change_pswd(View v)
    {
        Intent it = new Intent(this ,change_password.class);
        it.putExtra("session_id" ,session_id);
        startActivity(it);
    }

    public void dinnerman_show_order(View v)
    {
        Intent it = new Intent(this  ,dinnerman_get_orders.class);
        it.putExtra("session_id" ,session_id);
        startActivity(it);
    }

    int count = 0;
    public void special_thanks(View v)
    {
        if(count++ >= 5) Toast.makeText(this  ,R.string.speacial_stuff,Toast.LENGTH_LONG).show();
        else Toast.makeText(this  ,R.string.developer_thanks ,Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onStart()
    {
        try
        {
            ((TextView)findViewById(R.id.main_user_id)).setText("使用者編號: " + user.getString("user_id"));
            ((TextView)findViewById(R.id.main_user_name)).setText("使用者名稱: " + user.getString("user_name"));
            ((TextView)findViewById(R.id.main_server_record)).setText("伺服器已經在紀錄本用戶");

            if(user.getString("previleges").contains("管午餐的人")) /* having dinnerman permission. The previleges column was been set ,won't be hacked. */
            {
                ((Button)findViewById(R.id.main_order_class)).setVisibility(View.VISIBLE);
                ((TextView)findViewById(R.id.main_order_class_txt)).setVisibility(View.VISIBLE);
            }
        }
        catch(Exception e)
        {
            Toast.makeText(this ,e.toString() ,Toast.LENGTH_LONG).show();
        }
        super.onStart();
    }
}


/*


   void UrlConnection()
    {
        String json = "";
        try
        {
            URLConnection conn;
            conn = new URL("http://dinnersys.ddns.net/dinnersys_beta/backend/backend.php?id=11707&password=2rjurrru&cmd=login&plugin=yes").openConnection();
            http_fetcher fetcher = new http_fetcher();
            fetcher.execute(conn);
            json = fetcher.get();
            JSONObject jsobj = new JSONObject(json);
            Toast.makeText(this ,jsobj.getString("user_id") , Toast.LENGTH_LONG).show();

            session = conn.getHeaderField("Set-Cookie").split(";")[0]; //conn.setRequestProperty("Cookie" , session);
        }
        catch(Exception e)
        {

        }
    }
 */