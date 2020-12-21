package com.alvarenga.enrique.droid_mqtt_client;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Environment;
import android.util.Log;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.Security;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;

public class MainActivity extends AppCompatActivity implements MqttCallback {

    /* Class logging TAG */
    private static final String TAG = "MainActivity" ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


    }

    private void openMqttConnection(){
        try {
            String serverURI = "tcp://192.168.0.71:1883";

            MqttClient client = new MqttClient(serverURI, "pulseDroid", new MemoryPersistence());

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
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    private void sslMqttConnection(){

        String caFilePath =  Environment.getExternalStorageDirectory().getAbsolutePath() +
                "/DroidMQTT/" + "ca.crt";

        try {
            String serverURI = "ssl://192.168.0.71:8883";

            MqttClient client = new MqttClient(serverURI, "DroidClient", new MemoryPersistence());

            MqttConnectOptions options = new MqttConnectOptions();
            SSLSocketFactory socketFactory = getSocketFactory(caFilePath);
            options.setSocketFactory(socketFactory);

            client.setCallback(this);
            client.connect(options);
            String topic = "topic/DroidSample";
            client.subscribe(topic);

            MqttMessage message = new MqttMessage("Android Active".getBytes());
            try {
                client.publish("topic/OnlyDroid", message);
            } catch (MqttException e) {
                e.printStackTrace();
            }
        } catch (MqttException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private SSLSocketFactory getSocketFactoryUsingBKS(final String caCrtFile)
            throws Exception {

        KeyStore trustStore = KeyStore.getInstance("BKS");

        InputStream input = this.getResources().openRawResource(R.raw.mystore);
        trustStore.load(input, null);

        TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        tmf.init(trustStore);

        SSLContext sslCtx = SSLContext.getInstance("TLS");
        sslCtx.init(null, tmf.getTrustManagers(), null);

        return sslCtx.getSocketFactory();
    }

    private SSLSocketFactory getSocketFactory(final String caCrtFile)
            throws Exception {

        Security.addProvider(new BouncyCastleProvider());

        // Load CAs from an InputStream
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        File file = new File(caCrtFile);
        InputStream caInput = new BufferedInputStream(new FileInputStream(file));
        Certificate ca;
        try {
            ca = cf.generateCertificate(caInput);
            Log.d(TAG, "getSocketFactory: ca=" + ((X509Certificate) ca).getSubjectDN());
            Log.d(TAG, "getSocketFactory: caFull=" + ca.toString());
        } finally {
            caInput.close();
        }

        // Create a KeyStore containing our trusted CAs
        String keyStoreType = KeyStore.getDefaultType();
        KeyStore keyStore = KeyStore.getInstance(keyStoreType);
        keyStore.load(null, null);
        keyStore.setCertificateEntry("ca", ca);

        // Create a TrustManager that trusts the CAs in our KeyStore
        String tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
        TrustManagerFactory tmf = TrustManagerFactory.getInstance(tmfAlgorithm);
        tmf.init(keyStore);

        // Create an SSLContext that uses our TrustManager
        SSLContext context = SSLContext.getInstance("TLS");
        context.init(null, tmf.getTrustManagers(), null);

        return context.getSocketFactory();
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
