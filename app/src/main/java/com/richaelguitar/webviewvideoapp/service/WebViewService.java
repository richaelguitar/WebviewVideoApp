package com.richaelguitar.webviewvideoapp.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;

import com.richaelguitar.webviewvideoapp.IWebviewService;
import com.richaelguitar.webviewvideoapp.entity.User;

public class WebViewService extends Service {
    public WebViewService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        return (IBinder) iWebviewService;
    }

    private IWebviewService iWebviewService = new IWebviewService.Stub() {
        @Override
        public User getUserInfo() throws RemoteException {
            return new User(10010,"张三");
        }
    };

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }
}
