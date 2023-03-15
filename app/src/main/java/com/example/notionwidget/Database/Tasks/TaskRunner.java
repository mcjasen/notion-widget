package com.example.notionwidget.Database.Tasks;


import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

// Quelle: https://stackoverflow.com/questions/58767733/the-asynctask-api-is-deprecated-in-android-11-what-are-the-alternatives
public class TaskRunner {

    private final Executor executor = Executors.newSingleThreadExecutor();
    private final Handler handler = new Handler(Looper.getMainLooper());

    public interface Callback<R>{
        void onComplete(R result);
    }

    public <R> void executeAsync(Callable<R> callable, Callback<R> callback){
        executor.execute(() -> {
            try{
                final R result = callable.call();
                handler.post(() -> {
                    callback.onComplete(result);
                });
            } catch (Exception e){
                e.printStackTrace();
            }
        });
    }

}
