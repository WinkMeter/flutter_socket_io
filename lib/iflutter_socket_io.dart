abstract class ISocketIO {

  /// Get Id (Url + Namespace) of the socket
  String getId();

  /// Create a new socket and connects the client
  void connect();

  /// Send a message via a channel (i.e. event)
  void sendMessage(String event, dynamic message, [Function callback]);

  /// Subscribe to a channel with a callback
  void subscribe(String event, Function callback);

  /// Unsubscribe from a channel
  ///
  /// When no callback is provided, unsubscribe all subscribers of the channel. Otherwise, unsubscribe only the callback passed in
  void unSubscribe(String event, [Function callback]);

  /// Unsubscribe all subscribers from all channels
  void unSubscribesAll();

  /// Disconnect from the socket
  void disconnect();

  /// Destroy the socket and cleanup all memory usage
  void destroy();
}