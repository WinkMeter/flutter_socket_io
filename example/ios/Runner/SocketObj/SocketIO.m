//
//  SocketIO.m
//  SocketTest
//
//  Created by AnhNguyen on 8/18/18.
//  Copyright © 2018 ATA_Studio. All rights reserved.
//

#import "SocketIO.h"
#import "UtilsSocket.h"
#import "SocketListener.h"

/*--------------------------------------------------------------------
 * Macro weak and strong self
 ---------------------------------------------------------------------*/
#define weakify(object) __weak typeof(object)weakSelf = object
#define strongify(object) __strong typeof(object)strongSelf = object

#define EVENT_CONNECT               @"connect"
#define EVENT_CONNECTING            @"connecting"
#define EVENT_DISCONNECT            @"disconnect"
#define EVENT_ERROR                 @"error"
#define EVENT_MESSAGE               @"message"
#define EVENT_CONNECT_ERROR         @"connect_error"
#define EVENT_CONNECT_TIMEOUT       @"connect_timeout"
#define EVENT_RECONNECT             @"reconnect"
#define EVENT_RECONNECT_ERROR       @"reconnect_error"
#define EVENT_RECONNECT_FAILED      @"reconnect_failed"
#define EVENT_RECONNECT_ATTEMPT     @"reconnect_attempt"
#define EVENT_RECONNECTING          @"reconnecting"
#define EVENT_PING                  @"ping"
#define EVENT_PONG                  @"pong"

@interface SocketIO()
@end

@implementation SocketIO

+ (instancetype)initSocketIO:(FlutterMethodChannel *)methodChannel
                       query:(NSDictionary *)query
                      domain:(NSString *)domain
                   nameSpace:(NSString *)nameSpace
              statusCallback:(NSString *)callBack {
    SocketIO *obj = [[SocketIO alloc] init];
    obj.methodChannel = methodChannel;
    obj.domain = domain;
    obj.query = query;
    obj.nameSpace = nameSpace;
    obj.statusCallback = callBack;
    obj.subscribes = [[NSMutableDictionary alloc] init];
    return obj;
}

- (void)removeChannelAll {
    if (self.subscribes) {
        [self.subscribes removeAllObjects];
        self.subscribes = nil;
    }
}

- (NSString *)getId {
    return [self getSocketUrl];
}

- (NSString *)getSocketUrl {
    if (self.nameSpace) {
        return [NSString stringWithFormat:@"%@%@", self.domain, self.nameSpace];
    } else {
        return self.domain;
    }
}

- (void)initSocket {
    if (self.socket) {
        if ([self isConnected]) {
            [self disconnect];
            self.socket = nil;
        }
    }
    @try {
        NSURL *url = [NSURL URLWithString:self.domain];
        self.manager = [[SocketManager alloc] initWithSocketURL:url config:@{@"log": @YES, @"compress": @YES, @"connectParams": self.query, @"forceWebsockets": @YES }];
        self.socket = self.manager.defaultSocket;
        self.socket = [self.manager socketForNamespace:self.nameSpace];
        weakify(self);
        [self.socket on:EVENT_CONNECT callback:^(NSArray* data, SocketAckEmitter* ack) {
            strongify(weakSelf);
            if (strongSelf.methodChannel && strongSelf.statusCallback) {
                [strongSelf.methodChannel invokeMethod:strongSelf.statusCallback arguments:EVENT_CONNECT];
            }
        }];
        
        [self.socket on:EVENT_RECONNECT callback:^(NSArray* data, SocketAckEmitter* ack) {
            strongify(weakSelf);
            if (strongSelf.methodChannel && strongSelf.statusCallback) {
                [strongSelf.methodChannel invokeMethod:strongSelf.statusCallback arguments:EVENT_RECONNECT];
            }
        }];
        
        [self.socket on:EVENT_RECONNECTING callback:^(NSArray* data, SocketAckEmitter* ack) {
            strongify(weakSelf);
            if (strongSelf.methodChannel && strongSelf.statusCallback) {
                [strongSelf.methodChannel invokeMethod:strongSelf.statusCallback arguments:EVENT_RECONNECTING];
            }
        }];
        
        [self.socket on:EVENT_RECONNECT_ATTEMPT callback:^(NSArray* data, SocketAckEmitter* ack) {
            strongify(weakSelf);
            if (strongSelf.methodChannel && strongSelf.statusCallback) {
                [strongSelf.methodChannel invokeMethod:strongSelf.statusCallback arguments:EVENT_RECONNECT_ATTEMPT];
            }
        }];
        
        [self.socket on:EVENT_RECONNECT_FAILED callback:^(NSArray* data, SocketAckEmitter* ack) {
            strongify(weakSelf);
            if (strongSelf.methodChannel && strongSelf.statusCallback) {
                [strongSelf.methodChannel invokeMethod:strongSelf.statusCallback arguments:EVENT_RECONNECT_FAILED];
            }
        }];
        
        [self.socket on:EVENT_RECONNECT_ERROR callback:^(NSArray* data, SocketAckEmitter* ack) {
            strongify(weakSelf);
            if (strongSelf.methodChannel && strongSelf.statusCallback) {
                [strongSelf.methodChannel invokeMethod:strongSelf.statusCallback arguments:EVENT_RECONNECT_ERROR];
            }
        }];
        
        [self.socket on:EVENT_CONNECT_TIMEOUT callback:^(NSArray* data, SocketAckEmitter* ack) {
            strongify(weakSelf);
            if (strongSelf.methodChannel && strongSelf.statusCallback) {
                [strongSelf.methodChannel invokeMethod:strongSelf.statusCallback arguments:EVENT_CONNECT_TIMEOUT];
            }
        }];
        
        [self.socket on:EVENT_DISCONNECT callback:^(NSArray* data, SocketAckEmitter* ack) {
            strongify(weakSelf);
            if (strongSelf.methodChannel && strongSelf.statusCallback) {
                [strongSelf.methodChannel invokeMethod:strongSelf.statusCallback arguments:EVENT_DISCONNECT];
            }
        }];
        
        [self.socket on:EVENT_CONNECT_ERROR callback:^(NSArray* data, SocketAckEmitter* ack) {
            strongify(weakSelf);
            if (strongSelf.methodChannel && strongSelf.statusCallback) {
                [strongSelf.methodChannel invokeMethod:strongSelf.statusCallback arguments:EVENT_CONNECT_ERROR];
            }
        }];
        
        [self.socket on:@"socket_info" callback:^(NSArray* data, SocketAckEmitter* ack) {
            NSLog(@"aa");
        }];

    } @catch (NSException *exception) {
        NSLog(@"CONNECT FAIL : %@", exception.description);
        if (self.methodChannel && self.statusCallback) {
            [self.methodChannel invokeMethod:self.statusCallback arguments:@"failed"];
        }
    }
    
}

- (void)connect {
    if (!self.socket) {
        NSLog(@"SOCKET %@ IS NOT INITIALIZED", [self getId]);
        return;
    }
    if ([self isConnected]) {
        NSLog(@"SOCKET %@ IS ALREADY CONNECTED", [self getId]);
        return;
    }
    NSLog(@"CONNECTING SOCKET %@", [self getId]);
    [self.socket connect];
    
}

- (void)sendMessage:(NSString *)eventName mess:(NSString *)message callBack:(NSString *)callBack {
    if ([self isConnected] && eventName && message) {
        SocketListener *listener = [SocketListener initSocketListener:self.methodChannel event:eventName callBack:callBack];
        NSArray *jsonObject = [NSJSONSerialization JSONObjectWithData:[message dataUsingEncoding:NSUTF8StringEncoding]
                                                              options:0 error:NULL];
        [[self.socket emitWithAck:eventName with:@[jsonObject]] timingOutAfter:0 callback:^(NSArray* data) {
            [listener call:data];
        }];
    }
}

- (void)subscribe:(NSString *)eventName callBack:(NSString *)callBack {
    if (self.socket && self.manager && eventName) {
        NSMutableArray *listeners = [self.subscribes objectForKey:eventName];
        if (!listeners) {
            listeners = [[NSMutableArray alloc] init];
        }
        
        SocketListener *listener = [SocketListener initSocketListener:self.methodChannel event:eventName callBack:callBack];
        if (callBack && [UtilsSocket isExistedCallback:listeners callBack:callBack]) {
            [listeners addObject:listener];
        }
        
        [self.subscribes setValue:listeners forKey:eventName];
        [self.socket on:eventName callback:^(NSArray* data, SocketAckEmitter* ack) {
            [listener call:data];
        }];
    }
}

- (void)subscribeList:(NSMutableDictionary *)sub {
    if (self.socket && self.manager && ![UtilsSocket isNullOrEmpty:sub]) {
        NSArray *keys = [sub allKeys];
        for (id key in keys) {
            [self subscribe:key callBack:[sub objectForKey:key]];
        }
    }
}

- (void)unSubscribe:(NSString *)eventName callBack:(NSString *)callBack {
    if (self.socket && self.manager && ![UtilsSocket isNullOrEmpty:self.subscribes]) {
        NSMutableArray *listeners = [self.subscribes objectForKey:eventName];
        if ([UtilsSocket isNullOrEmptyArray:listeners]) {
            [self.subscribes removeObjectForKey:eventName];
            [self.socket off:eventName];
        } else {
            SocketListener *listener = [UtilsSocket findListener:listeners callBack:callBack];
            if (listener) {
                [listeners removeObject:listener];
                if (listeners.count < 1) {
                    [self.subscribes removeObjectForKey:eventName];
                    [self.socket off:eventName];
                } else {
                    [self.subscribes setValue:listeners forKey:eventName];
                    [self.socket off:eventName];
                }
            }
        }
    }
}

- (void)unSubscribeList:(NSMutableDictionary *)sub {
    if (self.socket && self.manager && ![UtilsSocket isNullOrEmpty:sub]) {
        NSArray *keys = [sub allKeys];
        for (id key in keys) {
            [self unSubscribe:key callBack:[sub objectForKey:key]];
        }
    }
}

- (void)unSubscribeAll {
    if (self.socket && self.manager && ![UtilsSocket isNullOrEmpty:self.subscribes]) {
        NSArray *keys = [self.subscribes allKeys];
        for (id key in keys) {
            [self unSubscribe:key callBack:nil];
        }
    }
}

- (BOOL)isConnected {
    if (self.socket && self.manager) {
        if (self.socket.status == SocketIOStatusConnected) {
            return YES;
        }
    }
    return NO;
}

- (void)disconnect {
    if (self.socket && self.manager) {
        [self.socket disconnect];
        [self.manager disconnect];
    }
}

- (void)destroy {
    [self disconnect];
    [self unSubscribeAll];
    [self removeChannelAll];
    self.socket = nil;
    self.manager = nil;
    self.methodChannel = nil;
    self.nameSpace = nil;
    self.domain = nil;
    self.statusCallback = nil;
    [self.subscribes removeAllObjects];
    self.subscribes = nil;
}

@end
