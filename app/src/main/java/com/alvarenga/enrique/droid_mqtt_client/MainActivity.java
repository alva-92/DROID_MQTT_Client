package com.alvarenga.enrique.droid_mqtt_client;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

public class MainActivity extends AppCompatActivity implements MqttCallback {

    private static final String TAG = "MainActivity" ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        try {
            MqttClient client = new MqttClient("tcp://192.168.0.29:1883", "AndroidSub", new MemoryPersistence());
            client.setCallback(this);
            client.connect();
            String topic = "topic/DroidSample";
            client.subscribe(topic);

            MqttMessage message = new MqttMessage("Android Active".getBytes());
            try {
                client.publish("topic/OnlyDroid", message);
            } catch (MqttException e) {
                e.printStackTrace();
            }
            //initMqtt();
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void connectionLost(Throwable cause) {
        Log.d(TAG, "connectionLost");
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
        String payload = new String(message.getPayload());
        Log.d(TAG, payload);
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
        Log.d(TAG, "deliveryComplete");
    }

    public void initMqtt(){
        String clientId = MqttClient.generateClientId();
        MqttAndroidClient client = new MqttAndroidClient(this, "tcp://192.168.0.29:1883", clientId);

        try {
            client.connect().setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Log.d(TAG, "onSuccess: Connected to broker");
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Log.d(TAG, "onFailure: Failed to connect to broker");
                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }

        try {
            client.disconnect().setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Log.d(TAG, "onSuccess: Disconnected");
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Log.d(TAG, "onFailure: Failed to disconnect");
                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }

        MqttMessage message = new MqttMessage("Alive".getBytes());
        try {
            client.publish("topic/DroidSample", message);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }
}
