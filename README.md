# flutter_socket_io  
  
Flutter Socket IO Plugin, supported Android + iOS
  
## Use the plugin
	

 **1. Add the following import to your Dart code:**
	import  'package:flutter_socket_io/flutter_socket_io.dart';
	
 **2. SocketIOManager**: to manage (create/destroy) list of SocketIO 
>Create SocketIO with SocketIOManager*: 
	
SocketIO socketIO = SocketIOManager().createSocketIO("http://127.0.0.1:3000", "/chat", query: "userId=21031", socketStatusCallback: _socketStatus);  


>Destroy SocketIO with socketIOManager:
		
SocketIOManager().destroySocket(socketIO);  
    
**3. SocketIO**:
>*Get Id (Url + Namespace) of the socket*
  

    String getId();

  
<hr>

>Create a new socket and connects the client

    Future<void> connect();

<hr>    

>Init socket before doing anything with socket  
  

    Future<void> init();

 <hr>
    
 >Subscribe to a channel with a callback  
 
 

    Future<void> subscribe(String event, Function callback); 

 
 <hr>
    
 > Unsubscribe from a channel. When no callback is provided, unsubscribe all subscribers of the channel. Otherwise, unsubscribe only the callback passed in  
 
 

    Future<void> unSubscribe(String event, [Function callback]); 

 
<hr>

>Send a message via a channel (i.e. event, *the native code will convert string message to JsonObject before sending*)  

    Future<void> sendMessage(String event, dynamic message, [Function callback]);

  
<hr>

>Disconnect from the socket  

    Future<void> disconnect(); 

 
<hr>

>Unsubscribe all subscribers from all channels  
  

    Future<void> unSubscribesAll();

**4. Example:**
[Link](https://pub.dartlang.org/packages/flutter_socket_io#-example-tab-)
  
~~~~
SocketIO socketIO;
_connectSocket01() { 
	//update your domain before using  
	 socketIO = SocketIOManager().createSocketIO("http://127.0.0.1:3000", "/chat", query: "userId=21031", socketStatusCallback: _socketStatus); 

	//call init socket before doing anything socketIO.init(); 

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


