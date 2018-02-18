package com.dinnersys.frontend.dinnersys_frontend;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.Serializable;
import java.net.URL;
import java.net.URLConnection;

public class login_activity extends AppCompatActivity {
    login_handler handler;
    saveData saver = new saveData(this);
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_activity);
    }

    @Override
    public void onStart()
    {
        super.onStart();
        String[] data = saver.GetPswd();
        if(data[0] != "")
        {
            ((EditText)findViewById(R.id.login_id)).setText(data[0]);
            ((EditText)findViewById(R.id.login_pswd)).setText(data[1]);
        }
    }

    public void login(View v)
    {
        String id = ((EditText)findViewById(R.id.login_id)).getText().toString();
        String pswd = ((EditText)findViewById(R.id.login_pswd)).getText().toString();
        try
        {
            handler = new login_handler(id ,pswd);
        }
        catch(Exception e)
        {
            Toast.makeText(this ,"不合法的帳號密碼" ,Toast.LENGTH_LONG).show();
        }

        login_handler.LoginStatus status = handler.login();
        switch (status)
        {
            case SUCCESS:
                Intent it = new Intent(this ,MainActivity.class);
                it.putExtra("user_json" ,handler.user.toString());
                it.putExtra("session_id" ,handler.session_id);
                if(((CheckBox)findViewById(R.id.login_rempswd)).isChecked()) saver.SavePswd(id ,pswd);
                startActivity(it);
                break;
            case WRONG_IDPS:
                Toast.makeText(this ,"帳號密碼錯誤" ,Toast.LENGTH_LONG).show();
                break;
            case UNREACHABLE_SERVER:
                Toast.makeText(this ,"網路異常。" ,Toast.LENGTH_LONG).show();
                break;
        }
    }

    public void AskHelp(View v)
    {
        startActivity(new Intent(Intent.ACTION_VIEW ,Uri.parse("tel:0905098503")));
    }

}

class saveData {
    private final String filePath = ".dinnersys_password.config";
    Context father;

    public saveData(Context father) {this.father = father;}
    public void SavePswd(String id ,String pswd)
    {
        try {
            FileOutputStream outputStream = father.openFileOutput(filePath, Context.MODE_PRIVATE);
            outputStream.write((id + "\t" + pswd).getBytes());
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String[] GetPswd()
    {
        int c;
        String temp = "";
        try {
            FileInputStream fin = father.openFileInput(filePath);
            while( (c = fin.read()) != -1){
                temp = temp + Character.toString((char)c);
            }
            fin.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return temp.split("\t");
    }
}
class login_handler implements Serializable{
    String login_url = "http://dinnersys.ddns.net/dinnersys_beta/backend/backend.php?cmd=login&plugin=yes",
            denied = "Access denied.\n",
            error_login = "error login\n";
    URLConnection conn;
    public String session_id = "";
    http_fetcher fetcher;
    JSONObject user = null;

    public enum LoginStatus{
        WRONG_IDPS ,
        SUCCESS ,
        UNREACHABLE_SERVER
    }

    public login_handler(String id ,String pswd) throws Exception
    {
        login_url += "&id=" + id + "&password=" + pswd;
        conn = new URL(login_url).openConnection();
        fetcher = new http_fetcher();
    }

    public LoginStatus login()
    {
        fetcher.execute(conn);
        try
        {
            String status = fetcher.get();
            if(status.equals(denied) || status.equals(error_login)) return LoginStatus.WRONG_IDPS;
            user = new JSONObject(status);
        }
        catch(Exception e)
        {
            return LoginStatus.UNREACHABLE_SERVER;
        }
        session_id = conn.getHeaderField("Set-Cookie").split(";")[0];
        return LoginStatus.SUCCESS;
    }
}
