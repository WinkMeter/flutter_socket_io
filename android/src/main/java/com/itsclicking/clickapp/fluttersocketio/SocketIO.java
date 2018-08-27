package com.itsclicking.clickapp.fluttersocketio;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.engineio.client.transports.WebSocket;
import com.github.nkzawa.socketio.client.Ack;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;

import io.flutter.plugin.common.MethodChannel;

public class SocketIO {

    private static final String TAG = "SocketIO";

    private MethodChannel _methodChannel;
    private String _domain;
    private String _namespace;
    private String _statusCallback;
    private String _query;
    private Socket _socket;
    private IO.Options mOptions;
    private ConcurrentMap<String, ConcurrentLinkedQueue<SocketListener>> _subscribes;

    public SocketIO(MethodChannel methodChannel, String domain,
                    String namespace, String query, String statusCallback) {
        _methodChannel = methodChannel;
        _domain = domain;
        _namespace = namespace;
        _query = query;
        _statusCallback = statusCallback;
        _subscribes = new ConcurrentHashMap<>();
    }

    private void removeChannelAll() {
        if(_subscribes != null) {
            _subscribes.clear();
        }
    }

    private String getSocketUrl() {
        return _domain + (_namespace == null ? "" : _namespace);
    }

    public void dumpSocketInfo() {
        Utils.log("SocketIO info", "--- BEFORE DUMPING SOCKET INFO ---");

        Utils.log("socketInfo", "SOCKET ID: " + getId());

        if(_socket != null) {
            Utils.log("socketInfo", "status: " + String.valueOf(isConnected()));
        } else {
            Utils.log("socketInfo", "status: SOCKET IS NULL");
        }

        dumpChannelsInfo();

        Utils.log("SocketIO info", "--- END DUMPING SOCKET INFO ---");
    }

    private void dumpChannelsInfo() {
        if(!Utils.isNullOrEmpty(_subscribes)) {
            Utils.log("socketInfo", "SUBSCRIBES SIZES: " + _subscribes.size());
            for(Map.Entry<String, ConcurrentLinkedQueue<SocketListener>> item : _subscribes.entrySet()) {
                Utils.log("socketInfo", "--- START channel: " + item.getKey());
                ConcurrentLinkedQueue<SocketListener> listeners = item.getValue();
                if(Utils.isNullOrEmpty(listeners)) {
                    Utils.log("socketInfo", "LISTENER: NULL");
                } else {
                    for (SocketListener listener : listeners) {
                        Utils.log("socketInfo", "LISTENER: " + listener.getCallback());
                    }
                }
                Utils.log("socketInfo", "--- END channel: " + item.getKey());
            }
        } else {
            Utils.log("socketInfo", "SUBSCRIBES SIZES: NULL or EMPTY");
        }
    }

    private void dumpChannelsCount() {
        if(!Utils.isNullOrEmpty(_subscribes)) {
            Utils.log("socketInfo", "SUBSCRIBES SIZES: " + _subscribes.size());
            for(Map.Entry<String, ConcurrentLinkedQueue<SocketListener>> item : _subscribes.entrySet()) {
                ConcurrentLinkedQueue<SocketListener> listeners = item.getValue();
                if(listeners == null) {
                    Utils.log("socketInfo", "CHANNEL: " + item.getKey() + " with TOTAL LISTENERS: NULL");
                } else {
                    Utils.log("socketInfo", "CHANNEL: " + item.getKey() + " with TOTAL LISTENERS: " + listeners.size());
                }
            }
        } else {
            Utils.log("socketInfo", "SUBSCRIBES SIZES: NULL or EMPTY");
        }
    }

    public String getId() {
        return getSocketUrl();
    }

    private void init() {
        if(_socket != null) {
            if(_socket.connected()) {
                _socket.disconnect();
            }
            _socket = null;
        }
        try {
            mOptions = new IO.Options();
            mOptions.transports = new String[]{WebSocket.NAME};

            if(!Utils.isNullOrEmpty(_query)) {
                Utils.log(TAG, "query: " + _query);
                mOptions.query = _query;
            }

            //init socket
            _socket = (mOptions == null ? IO.socket(getSocketUrl()) : IO.socket(getSocketUrl(), mOptions));

            Utils.log(TAG, "connecting...");

            //start listen connection events

            _socket.on(Socket.EVENT_CONNECT, new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    Utils.log(TAG, "socket connected");
                    if (_methodChannel != null && !Utils.isNullOrEmpty(_statusCallback)) {
                        _methodChannel.invokeMethod(_statusCallback, Socket.EVENT_CONNECT);
                    }
                }
            });

            _socket.on(Socket.EVENT_RECONNECT, new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    Utils.log(TAG, "socket reconnected");
                    if (_methodChannel != null && !Utils.isNullOrEmpty(_statusCallback)) {
                        _methodChannel.invokeMethod(_statusCallback, Socket.EVENT_RECONNECT);
                    }
                }
            });

            _socket.on(Socket.EVENT_RECONNECTING, new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    if (_methodChannel != null && !Utils.isNullOrEmpty(_statusCallback)) {
                        _methodChannel.invokeMethod(_statusCallback, Socket.EVENT_RECONNECTING);
                    }
                }
            });

            _socket.on(Socket.EVENT_RECONNECT_ATTEMPT, new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    if (_methodChannel != null && !Utils.isNullOrEmpty(_statusCallback)) {
                        _methodChannel.invokeMethod(_statusCallback, Socket.EVENT_RECONNECT_ATTEMPT);
                    }
                }
            });

            _socket.on(Socket.EVENT_RECONNECT_FAILED, new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    if (_methodChannel != null && !Utils.isNullOrEmpty(_statusCallback)) {
                        _methodChannel.invokeMethod(_statusCallback, Socket.EVENT_RECONNECT_FAILED);
                    }
                }
            });

            _socket.on(Socket.EVENT_RECONNECT_ERROR, new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    if (_methodChannel != null && !Utils.isNullOrEmpty(_statusCallback)) {
                        _methodChannel.invokeMethod(_statusCallback, Socket.EVENT_RECONNECT_ERROR);
                    }
                }
            });

            //listen connect timeout event
            _socket.on(Socket.EVENT_CONNECT_TIMEOUT, new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    if (_methodChannel != null && !Utils.isNullOrEmpty(_statusCallback)) {
                        _methodChannel.invokeMethod(_statusCallback, Socket.EVENT_CONNECT_TIMEOUT);
                    }
                }
            });

            _socket.on(Socket.EVENT_DISCONNECT, new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    if (_methodChannel != null && !Utils.isNullOrEmpty(_statusCallback)) {
                        _methodChannel.invokeMethod(_statusCallback, Socket.EVENT_DISCONNECT);
                    }
                }
            });

            _socket.on(Socket.EVENT_CONNECT_ERROR, new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    if (_methodChannel != null && !Utils.isNullOrEmpty(_statusCallback)) {
                        if(args != null) {
                            Utils.log(TAG, "EVENT_CONNECT_ERROR: " + new Gson().toJson(args));
                        }
                        _methodChannel.invokeMethod(_statusCallback, Socket.EVENT_CONNECT_ERROR);
                    }
                }
            });

            //end listen connection events
        } catch (URISyntaxException e) {
            Utils.log(TAG, "connect fail : " + e.toString());
            if (_methodChannel != null && !Utils.isNullOrEmpty(_statusCallback)) {
                _methodChannel.invokeMethod(_statusCallback, "failed");
            }
        }
    }

    public void setQuery(String query) {
        _query = query;
    }

    public void connect() {
        if(_socket == null) {
            Utils.log(TAG, "socket is not initialized!");
            return;
        }
        if(_socket.connected()) {
            Utils.log(TAG, "socket is already connected");
            return;
        }
        _socket.connect();
    }

    public void sendMessage(String event, String message, final String callback) {
        if (isConnected() && !Utils.isNullOrEmpty(event) && message != null) {

            Utils.log("sendMessage", "Event: " + event + " - with message: " + message);
            JSONObject jb = null;
            try {
                jb = new JSONObject(message);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            if(jb != null) {
                final SocketListener listener = new SocketListener(_methodChannel, event, callback);
                _socket.emit(event, jb, new Ack() {
                    @Override
                    public void call(Object... args) {
                        listener.call(args);
                    }
                });
            }
        }
    }

    public void subscribe(String event, final String callback) {
        if(_socket != null && !Utils.isNullOrEmpty(event)) {
            Utils.log("subscribe", "channel: " + event + " - with callback: " + callback);

            dumpChannelsCount();

            ConcurrentLinkedQueue<SocketListener> listeners = _subscribes.get(event);

            if(listeners == null) {
                listeners = new ConcurrentLinkedQueue<>();
            }

            SocketListener listener = new SocketListener(_methodChannel, event, callback);

            if(!Utils.isNullOrEmpty(callback) && !Utils.isExisted(listeners, callback)) {
                listeners.add(listener);
            }

            _subscribes.put(event, listeners);
            _socket.on(event, listener);

            dumpChannelsCount();
        }
    }

    public void subscribes(Map<String, String> subscribes) {
        if (_socket != null && !Utils.isNullOrEmpty(subscribes)) {
            for (Map.Entry<String, String> sub : subscribes.entrySet()) {
                if (!Utils.isNullOrEmpty(sub.getKey())) {
                    subscribe(sub.getKey(), sub.getValue());
                }
            }
        }
    }

    public void unSubscribe(String eventName, final String callback) {
        if(_socket != null && !Utils.isNullOrEmpty(eventName)) {

            dumpChannelsCount();

            Utils.log("unSubscribe", "channel: " + eventName + " - with callback: " + callback);

            ConcurrentLinkedQueue<SocketListener> listeners = _subscribes.get(eventName);

            if(listeners == null || Utils.isNullOrEmpty(callback)) {
                _subscribes.remove(eventName);
                _socket.off(eventName);
            } else {
                SocketListener listener = Utils.findListener(listeners, callback);
                if(listener != null) {

                    listeners.remove(listener);
                    if(listeners.size() < 1) {
                        _subscribes.remove(eventName);
                        _socket.off(eventName);
                    } else {
                        _subscribes.put(eventName, listeners);
                        _socket.off(eventName, listener);
                    }
                }
            }

            dumpChannelsCount();
        }
    }

    public void unSubscribes(Map<String, String> subscribes) {
        if (_socket != null && !Utils.isNullOrEmpty(subscribes)) {
            for (Map.Entry<String, String> sub : subscribes.entrySet()) {
                if (!Utils.isNullOrEmpty(sub.getKey())) {
                    unSubscribe(sub.getKey(), sub.getValue());
                }
            }
        }
    }

    public void unSubscribesAll() {
        if (_socket != null && !Utils.isNullOrEmpty(_subscribes)) {
            for (Map.Entry<String, ConcurrentLinkedQueue<SocketListener>> sub : _subscribes.entrySet()) {
                if (!Utils.isNullOrEmpty(sub.getKey())) {
                    unSubscribe(sub.getKey(), null);
                }
            }
        }
    }

    public boolean isConnected() {
        if(_socket != null) {
            if(!_socket.connected()) {
                Utils.log(TAG, "socket id: " + getId() + " is disconnected");
            }
            return _socket.connected();
        } else {
            Utils.log(TAG, "socket id: " + getId() + " is NULL");
        }
        return false;
    }

    public void disconnect() {
        if (_socket != null) {
            _socket.disconnect();
        }
    }

    public void destroy() {
        Utils.log(TAG, "--- START destroy ---");
        disconnect();
        unSubscribesAll();
        removeChannelAll();
        _socket = null;
        _namespace = null;
        _statusCallback = null;
        mOptions = null;
        _methodChannel = null;
        _subscribes = null;
        Utils.log(TAG, "--- END destroy ---");
    }
}
