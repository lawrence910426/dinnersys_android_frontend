package com.dinnersys.frontend.dinnersys_frontend;

import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

public class dinnerman_get_orders_handler extends Fragment implements View.OnClickListener {
    public Weekday date;
    public String session_id = "";
    public enum Weekday{MONDAY ,TUESDAY ,WEDNESDAY ,THURSDAY ,FRIDAY}
    dinnerman_getOrders getOrders_handler;
    UI_helper ui_handler;
    String[][] urls = new String[2][];      //urls[payment(0) / reverse(1)][order_index]

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        getOrders_handler = new dinnerman_getOrders(session_id);
    }

    Handler main_thread;
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate( R.layout.fragment_dinnerman_get_orders, container, false);
        int date_number = -1;
        switch(date)
        {
            case MONDAY:
                date_number = 0;
                break;
            case TUESDAY:
                date_number = 1;
                break;
            case WEDNESDAY:
                date_number = 2;
                break;
            case THURSDAY:
                date_number = 3;
                break;
            case FRIDAY:
                date_number = 4;
                break;
        }
        ui_handler = new UI_helper(rootView ,this ,date_number);
        main_thread = new Handler(Looper.getMainLooper());
        return rootView;
    }
    @Override
    public void onClick(View v)
    {
        int i;
        for(i = 0;i != ui_handler.payment_btn.length;i++)
            if(ui_handler.payment_btn[i] == v) break;

        new dinnerman_submitter(
                (ui_handler.payment_status[i] ? urls[0][i] : urls[1][i]) ,
                session_id ,
                i ,
                ui_handler ,
                main_thread
        ).Run();
    }
}

class dinnerman_submitter implements Task {
    String url ,session_id;
    int index;
    UI_helper ui_handler;
    Handler main_thread;

    public dinnerman_submitter(String url ,String session_id ,int index ,UI_helper helper ,Handler main_t)
    {
        this.url = url;
        this.session_id = session_id;
        this.index = index;
        this.ui_handler = helper;
        this.main_thread = main_t;
    }

    @Override
    public void pre_exec()
    {
        main_thread.post(new Runnable() {
            @Override
            public void run() {
                ui_handler.start_payment(index);
            }
        });
    }

    @Override
    public void exec()
    {
        try
        {
            URLConnection conn = new URL(url).openConnection();
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
            @Override
            public void run() {
                ui_handler.finish_payment(index);
            }
        });
    }

    public void Run()
    {
        new Thread(new Runnable() {
            @Override
            public void run() {
                pre_exec();
                exec();
                after_exec();
            }
        }).start();
    }
}

class UI_helper
{
    LinearLayout content;
    LinearLayout[] lines;

    ProgressBar[] payment_progress;
    Button[] payment_btn;
    TextView[] person_info ,dish_info;

    TextView paid_sum ,unpaid_sum ,today_sum;
    int[] charges; int paid ,unpaid ,sum;
    boolean[] payment_status;

    dinnerman_get_orders_handler father;
    View root;

    public UI_helper(View rootView ,dinnerman_get_orders_handler father ,int date)
    {
        this.father = father;
        content = rootView.findViewById(R.id.dinnerman_content);
        paid_sum = rootView.findViewById(R.id.dinnerman_paid);
        unpaid_sum = rootView.findViewById(R.id.dinnerman_unpaid);
        today_sum = rootView.findViewById(R.id.dinnerman_sum);
        root = rootView;
        init(date);
    }

    void init(int date)
    {
        JSONArray arr = father.getOrders_handler.get_data(date);
        try
        {
            lines = new LinearLayout[arr.length()];
            payment_progress = new ProgressBar[arr.length()];
            payment_btn = new Button[arr.length()];
            person_info = new TextView[arr.length()];
            dish_info = new TextView[arr.length()];
            charges = new int[arr.length()];
            payment_status = new boolean[arr.length()];
            father.urls = new String[2][arr.length()];

            for(int i = 0;i != arr.length();i++)
            {
                JSONObject obj = (JSONObject)arr.get(i);
                lines[i] = new LinearLayout(root.getContext());
                payment_btn[i] = new Button(root.getContext());
                person_info[i] = new TextView(root.getContext());
                dish_info[i] = new TextView(root.getContext());
                payment_progress[i] = new ProgressBar(root.getContext());

                payment_btn[i].setOnClickListener(father);
                payment_status[i] = obj.getString("paid_status").equals("您已經成功付款");
                lines[i].setOrientation(LinearLayout.HORIZONTAL);
                payment_btn[i].setText(payment_status[i] ? "取消繳款" : "繳款");
                payment_btn[i].setTextColor(payment_status[i] ? Color.BLUE : Color.RED);
                dish_info[i].setText(" " + obj.getString("dish_name") + " " + obj.getString("dish_charge") + "$.");
                person_info[i].setText(obj.getString("user_id") + "  ");
                father.urls[0][i] = obj.getString("reverse_payment"); father.urls[1][i] = obj.getString("make_payment");
                person_info[i].setTextAlignment(View.TEXT_ALIGNMENT_VIEW_END);
                person_info[i].setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                payment_progress[i].setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));

                lines[i].addView(payment_btn[i]); lines[i].addView(dish_info[i]); lines[i].addView(person_info[i]);
                content.addView(lines[i]);

                charges[i] = Integer.parseInt(obj.getString("dish_charge"));
                if(payment_status[i]) paid += charges[i];
                else unpaid += charges[i];
                sum += charges[i];
            }
        }catch (Exception e) {}
        InitCharge();
    }

    public void InitCharge()
    {
        today_sum.setText("Σ 本日: "+ sum + " $.");
        paid_sum.setText("Σ 已付: "+ paid + " $.");
        unpaid_sum.setText("Σ 未付: "+ unpaid + " $.");
    }

    public void start_payment(int index)
    {
        lines[index].removeAllViews();
        lines[index].addView(payment_progress[index]);
        lines[index].addView(dish_info[index]); lines[index].addView(person_info[index]);
    }

    public void finish_payment(int index)
    {
        int change = (payment_status[index] ? 1 : -1) * charges[index];
        unpaid += change; paid -= change;
        InitCharge();

        payment_status[index] = !payment_status[index];
        lines[index].removeAllViews();
        payment_btn[index].setText(payment_status[index] ? "取消繳款" : "繳款");
        payment_btn[index].setTextColor(payment_status[index] ? Color.BLUE : Color.RED);
        lines[index].addView(payment_btn[index]); lines[index].addView(dish_info[index]); lines[index].addView(person_info[index]);
    }
}