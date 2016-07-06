package cl.json.react;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;

import javax.annotation.Nullable;


public class AACStreamingModule extends ReactContextBaseJavaModule implements ServiceConnection {

    private ReactApplicationContext context;

    private Class<?> clsActivity;
    private static Signal signal;
    private Intent bindIntent;
    private String streamingURL;

    public AACStreamingModule(ReactApplicationContext reactContext, Class<?> cls) {
        super(reactContext);
        this.clsActivity = cls;
        this.context = reactContext;
    }

    public ReactApplicationContext getReactApplicationContextModule() {
        return this.context;
    }

    public Class<?> getClassActivity() {
        return this.clsActivity;
    }

    public void stopOncall() {
        this.signal.stop();
    }

    public Signal getSignal() {
        return signal;
    }

    public void sendEvent(ReactContext reactContext, String eventName, @Nullable WritableMap params) {
        this.context
                .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                .emit(eventName, params);
    }

    @Override
    public String getName() {
        return "AACStreamingAndroid";
    }

    @Override
    public void onServiceConnected(ComponentName className, IBinder service) {
        signal = ((Signal.RadioBinder) service).getService();
        signal.setURLStreaming(streamingURL); // URL of MP3 or AAC stream
        signal.setData(this.context, this.clsActivity, this);
        WritableMap params = Arguments.createMap();
        sendEvent(this.getReactApplicationContextModule(), "streamingOpen", params);
    }

    @Override
    public void onServiceDisconnected(ComponentName className) {
        signal = null;
    }


    @ReactMethod
    public void setURLStreaming(String streamingURL) {
        this.streamingURL = streamingURL;

        try {
            bindIntent = new Intent(this.context, Signal.class);
            this.context.bindService(bindIntent, this, Context.BIND_AUTO_CREATE);
        } catch (Exception e) {

        }
    }

    @ReactMethod
    public void play() {
        signal.play();
        signal.showNotification();
    }

    @ReactMethod
    public void stop() {
        signal.stop();
    }

    @ReactMethod
    public void pause() {
        // Not implemented on aac
        this.stop();
    }

    @ReactMethod
    public void resume() {
        // Not implemented on aac
        this.play();
    }

    @ReactMethod
    public void destroyNotification() {
        signal.exitNotification();
    }
}
