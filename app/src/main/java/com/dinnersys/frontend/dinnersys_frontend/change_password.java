package com.dinnersys.frontend.dinnersys_frontend;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.net.URL;
import java.net.URLConnection;
import java.util.regex.Pattern;

public class change_password extends AppCompatActivity {
    change_pswd handler;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);
        handler = new change_pswd(getIntent().getStringExtra("session_id"));
    }

    public void change_password(View v)
    {
        String new_pswd = ((EditText)findViewById(R.id.cpswd_new_pswd)).getText().toString();
        String old_pswd = ((EditText)findViewById(R.id.cpswd_old_pswd)).getText().toString();

        if(!old_pswd.matches("^[A-Za-z0-9 ]+")) {
            Toast.makeText(this ,"舊密碼只能用上數字元組合" ,Toast.LENGTH_LONG).show();
            return;
        }
        if(!new_pswd.matches("^[A-Za-z0-9 ]+")) {
            Toast.makeText(this ,"新密碼只能以用數字元組合" ,Toast.LENGTH_LONG).show();
            return;
        }

        if(handler.change_password(old_pswd ,new_pswd) == change_pswd.status.WRONG_PSWD)
            Toast.makeText(this ,"舊密碼錯誤" ,Toast.LENGTH_LONG).show();
        else
        {
            Toast.makeText(this ,"已成功更改密碼" ,Toast.LENGTH_LONG).show();
            finish();
        }
    }
}

class change_pswd
{
    http_fetcher fetcher = new http_fetcher();
    String change_url = "http://dinnersys.ddns.net/dinnersys_beta/backend/backend.php?cmd=change_password" ,
        session_id = "" ,
        wrong_msg = "error login\n";

    public enum status {
        SUCCESS,
        WRONG_PSWD
    }

    public change_pswd(String session)
    {
        session_id = session;
    }

    public status change_password(String old_pswd ,String new_pswd)
    {
        change_url += "&old_pswd=" + old_pswd +
                    "&new_pswd=" + new_pswd;
        try
        {
            URLConnection conn = new URL(change_url).openConnection();
            conn.setRequestProperty("Cookie" ,session_id);
            fetcher.execute(conn);

            String serv_data = fetcher.get();
            if(serv_data.equals(wrong_msg)) return status.WRONG_PSWD;
        }
        catch (Exception e) {}
        return status.SUCCESS;
    }
}