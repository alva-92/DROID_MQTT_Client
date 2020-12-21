# DROID_MQTT_Client

## MQTT

MQTT works as Pub-Sub. MQTT uses very small bytes of data to express the details of the content being transported.
In general, MQTT is more suitable for IOT projects because of its reliability and small header footprint.


# Requirements

+ Mosquitto
Install Paho MQTT
Once install open a terminal and navigate to the installation folder and run the following command to start the subscriber

```bat
mosquitto_sub -t topic/DroidSample
```

mosquitto_sub -t topic/TestTopic -v

mosquitto_pub -t ResponseTopic -m 'Damn'

## Setup

+ Add the following to the build gradle 

repositories {
    jcenter()
    maven {
        url "https://repo.eclipse.org/content/repositories/paho-snapshots/"
    }
}

+ Add the following to the app gradle dependencies

compileOnly 'com.google.android.things:androidthings:0.1-devpreview'
implementation 'org.eclipse.paho:org.eclipse.paho.client.mqttv3:1.0.2'

+ Sample implementation on Main Activity

try {
    MqttClient client = new MqttClient("tcp://192.168.1.5:1883", "AndroidThingSub", new MemoryPersistence());
    client.setCallback(this);
    client.connect();
    String topic = "topic/ayushgemini";
    client.subscribe(topic);
} catch (MqttException e) {
    e.printStackTrace();
}


## Resources

https://medium.com/@gaikwadchetan93/android-real-time-communication-using-mqtt-9ea42551475d
