package com.dinnersys.frontend.dinnersys_frontend;

import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

public class ShowMenuActivity extends AppCompatActivity {
    String session_id, user_id;
    getMenu handler;
    JSONObject menu;

    CheckBox[] dishes;
    int[] charge;
    String[] dish_name;
    String[][] make_order_url;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.show_menu);
        Intent it = getIntent();
        session_id = it.getStringExtra("session_id");
        user_id = it.getStringExtra("user_id");
        handler = new getMenu(session_id);
        menu = handler.get_menu();
    }

    @Override
    protected void onStart() {
        (findViewById(R.id.some_button)).setVisibility(View.VISIBLE);

        ((LinearLayout)findViewById(R.id.show_menu_ll)).removeAllViews();

        ArrayAdapter<CharSequence> nAdapter = ArrayAdapter.createFromResource(this, R.array.date, android.R.layout.simple_spinner_item);
        ((Spinner) findViewById(R.id.show_menu_date_picker)).setAdapter(nAdapter);
        try {
            JSONArray arr = menu.getJSONArray("menu");

                dishes = new CheckBox[arr.length()];    charge = new int[arr.length()]; dish_name = new String[arr.length()];   make_order_url = new String[arr.length()][5];
                for (int i = 0; i != arr.length(); i++) {
                    LinearLayout ls = new LinearLayout(this);
                    ls.setOrientation(LinearLayout.HORIZONTAL);
                    dishes[i] = new CheckBox(this);
                    dishes[i].setText(((JSONObject) arr.get(i)).getString("dish_name"));
                    TextView txv = new TextView(this);
                    txv.setText(((JSONObject) arr.get(i)).getString("dish_cost") + "$.     ");
                txv.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                txv.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_END);
                charge[i] = Integer.parseInt(((JSONObject) arr.get(i)).getString("dish_cost"));
                dish_name[i] = ((JSONObject) arr.get(i)).getString("dish_name");
                for (int j = 0; j != 5; j++)
                    make_order_url[i][j] = ((JSONObject) arr.get(i)).getString(String.valueOf(j));
                ls.addView(dishes[i]); ls.addView(txv);
                ((LinearLayout)findViewById(R.id.show_menu_ll)).addView(ls);
            }
        } catch (Exception e) {}
        super.onStart();
    }

    public void order(View v)
    {
        String status = ((TextView)findViewById(R.id.show_menu_buy)).getText().toString();
        if(status.equals("購買"))
        {
            ((TextView)findViewById(R.id.show_menu_buy)).setText("下單");
            attempt_order();
        }
        else make_order();
    }

    void attempt_order() {
        ((Button)findViewById(R.id.some_button)).setVisibility(View.VISIBLE);
        ((LinearLayout)findViewById(R.id.show_menu_ll)).removeAllViews();

        int charge_sum = 0;
        String show_text = "\n你總共點了:\n";
        for (int i = 0; i != dishes.length; i++)
            if (dishes[i].isChecked()) {
                charge_sum += charge[i];
                show_text += dish_name[i] + "\n";
            }
        show_text += "\nΣ :" + charge_sum + "$.\n";

        Spinner dpicker = findViewById(R.id.show_menu_date_picker);
        show_text += "\n預計於: " + dpicker.getSelectedItem() + "送達";

        TextView recipt = new TextView(this);
        recipt.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        recipt.setTextSize(20);
        recipt.setText(show_text);
        ((LinearLayout)findViewById(R.id.show_menu_ll)).addView(recipt);
    }

    void make_order() {
        Spinner dpicker = findViewById(R.id.show_menu_date_picker);
        int data_code = -1;
        switch (dpicker.getSelectedItem().toString()) {
            case "星期一":
                data_code = 0;
                break;
            case "星期二":
                data_code = 1;
                break;
            case "星期三":
                data_code = 2;
                break;
            case "星期四":
                data_code = 3;
                break;
            case "星期五":
                data_code = 4;
                break;
        }


        ArrayList<String> urls = new ArrayList<>();
        for (int i = 0; i != dishes.length; i++)
            if (dishes[i].isChecked())
                urls.add(make_order_url[i][data_code]);

        final menu_submitter submit = new menu_submitter(urls , (ProgressBar)findViewById(R.id.show_menu_buy_status) , this , session_id ,new Handler(Looper.getMainLooper()));
        new AsyncRun().execute(submit);
    }

}

class menu_submitter implements Task {
    List<String> url;
    ProgressBar status;
    ShowMenuActivity activity;
    String session_id;
    Handler main_thread;
    final int LONG_DURATION = 10;

    public menu_submitter(List<String> url ,ProgressBar bar ,ShowMenuActivity cont ,String sess ,Handler main_thread)
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
                        Toast.makeText(activity ,"可能會需要一點時間送出訂單，請稍後" ,Toast.LENGTH_LONG).show();
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
                    Toast.makeText(activity ,"已經送出點單" ,Toast.LENGTH_LONG).show();
                    activity.finish();
                }
                catch (Exception e) {}

            }
        });
    }
}
