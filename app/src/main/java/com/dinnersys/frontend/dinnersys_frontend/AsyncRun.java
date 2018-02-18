package com.dinnersys.frontend.dinnersys_frontend;

import android.os.AsyncTask;

public class AsyncRun extends AsyncTask <Task ,Void ,Void>{

    @Override
    public Void doInBackground(Task... exec)
    {
        for(Task t : exec)
        {
            t.pre_exec();
            t.exec();
            t.after_exec();
        }
        return null;
    }
}


interface Task
{
    void pre_exec();
    void exec();
    void after_exec();
}