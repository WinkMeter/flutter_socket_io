# flutter_socket_io  
  
Flutter Socket IO Plugin, supported Android + iOS (iOS installation guide is coming soon)

## How to install on iOS

- 1. Copy the folder `example/ios/Runner/SocketObj` to `${PROJECT_ROOT}/ios/Runner/`

- 2. [If your app's iOs development language is Objective-C] Replace the `example/ios/Runner/AppDelegate.m` line with `${PROJECT_ROOT}/ios/Runner/AppDelegate.m`. 
(Notice: **You should merge the old one in your project to merge with the new from this plugin if you have some change on that file**)

- 3. [If your app's iOs development language is Swift] Add these lines in `${PROJECT_ROOT}/ios/Runner/AppDelegate.swift` AFTER `GeneratedPluginRegistrant.register(with: self)`
~~~
let socketChannel = FlutterMethodChannel(name: "flutter_socket_io", binaryMessenger: controller.binaryMessenger)
    socketChannel.setMethodCallHandler { (call, result) in
        guard let dict = call.arguments as? NSDictionary,
            let socketNameSpace = dict[SOCKET_NAME_SPACE] as? String,
            let socketDomain = dict[SOCKET_DOMAIN] as? String
        else {
            result(FlutterError(code: "ERROR", message: "Argument is wrong", details: nil));
            return
        }
        
        switch call.method {
        case SOCKET_INIT:
            let query = dict[SOCKET_QUERY] as? String
            let callback = dict[SOCKET_CALLBACK] as? String
            var queryStringDictionary = [String: String]()
            query?.components(separatedBy: "&")
                .forEach { pair in
                    let components = pair.components(separatedBy: "=")
                    guard let key = components.first,
                        let value = components.last else {
                            return
                    }
                    queryStringDictionary[key] = value
                }
            SocketIOManager.shared()?.initSocket(socketChannel, domain: socketDomain, query: queryStringDictionary, namspace: socketNameSpace, callBackStatus: callback)
        case SOCKET_CONNECT:
            SocketIOManager.shared()?.connectSocket(socketDomain, namspace: socketNameSpace)
        case SOCKET_DISCONNECT:
            SocketIOManager.shared()?.disconnectDomain(socketDomain, namspace: socketNameSpace)
        case SOCKET_SUBSCRIBES:
            guard let data = (dict[SOCKET_DATA] as? String)?.data(using: .utf8),
                let json = try? JSONSerialization.jsonObject(with: data, options: []) as? NSMutableDictionary
            else {
                result(FlutterError(code: "ERROR", message: "Data is wrong", details: nil));
                return
            }
            SocketIOManager.shared()?.subscribes(socketDomain, namspace: socketNameSpace, subscribes: json)
        case SOCKET_UNSUBSCRIBES:
            guard let data = (dict[SOCKET_DATA] as? String)?.data(using: .utf8),
                let json = try? JSONSerialization.jsonObject(with: data, options: []) as? NSMutableDictionary
            else {
                result(FlutterError(code: "ERROR", message: "Data is wrong", details: nil));
                return
            }
            SocketIOManager.shared()?.unSubscribes(socketDomain, namspace: socketNameSpace, subscribes: json)
        case SOCKET_UNSUBSCRIBES_ALL:
            SocketIOManager.shared()?.unSubscribesAll(socketDomain, namspace: socketNameSpace)
        case SOCKET_SEND_MESSAGE:
            guard let event = dict[SOCKET_EVENT] as? String,
                let message = dict[SOCKET_MESSAGE] as? String,
                let callback = dict[SOCKET_CALLBACK] as? String
            else {
                result(FlutterError(code: "ERROR", message: "Event or message is wrong", details: nil));
                return
            }
            
            SocketIOManager.shared()?.sendMessage(event, message: message, domain: socketDomain, namspace: socketNameSpace, callBackStatus: callback)
        case SOCKET_DESTROY:
            SocketIOManager.shared()?.destroySocketDomain(socketDomain, namspace: socketNameSpace)
        case SOCKET_DESTROY_ALL:
            SocketIOManager.shared()?.destroyAllSocket()
        default:
            result(FlutterMethodNotImplemented)
        }
    }
~~~

- 4. Open `${PROJECT_ROOT}/ios/Podfile`, paste this line `pod 'Socket.IO-Client-Swift', '~> 13.3.0'` before the end of `target 'Runner' do` block

- 5. Run and Enjoy the plugin :)


## Use the plugin
	

**1. Add the following import to your Dart code:**
~~~
import  'package:flutter_socket_io/flutter_socket_io.dart';
~~~
	
**2. SocketIOManager**: to manage (create/destroy) list of SocketIO 

*Create SocketIO with SocketIOManager*: 
	
~~~
SocketIO socketIO = SocketIOManager().createSocketIO("http://127.0.0.1:3000", "/chat", query: "userId=21031", socketStatusCallback: _socketStatus);  
~~~

Destroy SocketIO with socketIOManager:
		
~~~
SocketIOManager().destroySocket(socketIO);  
~~~
    
**3. SocketIO**:

*Get Id (Url + Namespace) of the socket*
~~~
String getId();
~~~
</br>
</br>

*Create a new socket and connects the client*
~~~
Future<void> connect();
~~~
</br>
</br>

*Init socket before doing anything with socket*  
~~~
Future<void> init();
~~~
</br>
</br>

*Subscribe to a channel with a callback*  
 ~~~
 Future<void> subscribe(String event, Function callback); 
~~~
</br>
</br>

*Unsubscribe from a channel. When no callback is provided, unsubscribe all subscribers of the channel. Otherwise, unsubscribe only the callback passed in*  
 
~~~
Future<void> unSubscribe(String event, [Function callback]); 
~~~
</br>
</br>

*Send a message via a channel (i.e. event, *the native code will convert string message to JsonObject before sending*)*  
~~~
Future<void> sendMessage(String event, dynamic message, [Function callback]);
~~~
</br>
</br>

*Disconnect from the socket*  
~~~
Future<void> disconnect(); 
~~~
</br>
</br>

*Unsubscribe all subscribers from all channels*  
~~~
Future<void> unSubscribesAll();
~~~
</br>
</br>

**4. Example:**
[Link](https://pub.dartlang.org/packages/flutter_socket_io#-example-tab-)
  
~~~~
SocketIO socketIO;
_connectSocket01() { 
	//update your domain before using  
	 socketIO = SocketIOManager().createSocketIO("http://127.0.0.1:3000", "/chat", query: "userId=21031", socketStatusCallback: _socketStatus); 

	//call init socket before doing anything 
	socketIO.init(); 

	//subscribe event
	socketIO.subscribe("socket_info", _onSocketInfo); 

	//connect socket 
	socketIO.connect(); 
}

_socketStatus(dynamic data) { 
	print("Socket status: " + data); 
}

_subscribes() { 
	if (socketIO != null) { 
		socketIO.subscribe("chat_direct", _onReceiveChatMessage); 
	} 
}

void _onReceiveChatMessage(dynamic message) { 
	print("Message from UFO: " + message); 
}

void _sendChatMessage(String msg) async { 
	if (socketIO != null) { 
		String jsonData = '{"message":{"type":"Text","content": ${(msg != null && msg.isNotEmpty) ? '"${msg}"' : '"Hello SOCKET IO PLUGIN :))"'},"owner":"589f10b9bbcd694aa570988d","avatar":"img/avatar-default.png"},"sender":{"userId":"589f10b9bbcd694aa570988d","first":"Ha","last":"Test 2","location":{"lat":10.792273999999999,"long":106.6430356,"accuracy":38,"regionId":null,"vendor":"gps","verticalAccuracy":null},"name":"Ha Test 2"},"receivers":["587e1147744c6260e2d3a4af"],"conversationId":"589f116612aa254aa4fef79f","name":null,"isAnonymous":null}'; 
		socketIO.sendMessage("chat_direct", jsonData, _onReceiveChatMessage); 
	}
 }

_destroySocket() { 
	if (socketIO != null) { 
		SocketIOManager().destroySocket(socketIO); 
	} 
}


