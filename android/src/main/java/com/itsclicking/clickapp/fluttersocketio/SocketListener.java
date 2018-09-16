package com.itsclicking.clickapp.fluttersocketio;

import io.flutter.plugin.common.MethodChannel;
import io.socket.emitter.Emitter;

public class SocketListener implements Emitter.Listener {

    private MethodChannel _methodChannel;
    private String _socketId;
    private String _event;
    private String _callback;

    public SocketListener(MethodChannel methodChannel, String socketId, String event, String callback) {
        _methodChannel = methodChannel;
        _socketId = socketId;
        _event = event;
        _callback = callback;
    }

    public String getCallback() {
        return _callback;
    }

    @Override
    public void call(Object... args) {
        if (args != null && _methodChannel != null && !Utils.isNullOrEmpty(_event)
                && !Utils.isNullOrEmpty(_callback)) {
            String data = "";
            if (args[0] != null) {
                data = args[0].toString();
            }
            _methodChannel.invokeMethod(_socketId + "|" +_event + "|" + _callback, data);
        }
    }
}
