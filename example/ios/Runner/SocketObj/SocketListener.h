//
//  SocketListener.h
//  SocketTest
//
//  Created by AnhNguyen on 8/18/18.
//  Copyright © 2018 ATA_Studio. All rights reserved.
//

#import <Foundation/Foundation.h>
@import Flutter;

@interface SocketListener : NSObject

@property (nonatomic, strong) FlutterMethodChannel *methodChannel;
@property (nonatomic, strong) NSString *event;
@property (nonatomic, strong) NSString *callBack;

+ (instancetype)initSocketListener:(FlutterMethodChannel *)methodChannel
                             event:(NSString *)event
                          callBack:(NSString *)callBack;

- (NSString *)getCallback;
- (void)call:(id)arg;

@end
