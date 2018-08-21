package com.itsclicking.clickapp.fluttersocketio;

import java.util.Map;

public interface ISocketIO {
    String getId();
    void connect();
    void sendMessage(String eventName, String message, String callback);
    void subscribe(String event, String callback);
    void subscribes(Map<String, String> subscribes);

    void unSubscribe(String eventName, String callback);
    void unSubscribes(Map<String, String> subscribes);

    void unSubscribesAll();
    void disconnect();
    void destroy();

    boolean isConnected();
}
