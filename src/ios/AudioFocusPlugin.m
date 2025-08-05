/*
       Licensed to the Apache Software Foundation (ASF) under one
       or more contributor license agreements.  See the NOTICE file
       distributed with this work for additional information
       regarding copyright ownership.  The ASF licenses this file
       to you under the Apache License, Version 2.0 (the
       "License"); you may not use this file except in compliance
       with the License.  You may obtain a copy of the License at

         http://www.apache.org/licenses/LICENSE-2.0

       Unless required by applicable law or agreed to in writing,
       software distributed under the License is distributed on an
       "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
       KIND, either express or implied.  See the License for the
       specific language governing permissions and limitations
       under the License.
*/

#import "AudioFocusPlugin.h"
#import <AVFoundation/AVFoundation.h>

@implementation AudioFocusPlugin

- (void)requestMicFocus:(CDVInvokedUrlCommand*)command
{
    [self.commandDelegate runInBackground:^{
        NSError *error = nil;
        AVAudioSession *audioSession = [AVAudioSession sharedInstance];
        
        // Set the audio session category for recording
        BOOL success = [audioSession setCategory:AVAudioSessionCategoryPlayAndRecord
                                   withOptions:AVAudioSessionCategoryOptionDefaultToSpeaker
                                         error:&error];
        
        if (success) {
            // Activate the audio session
            success = [audioSession setActive:YES error:&error];
            
            if (success) {
                CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK
                                                                  messageAsString:@"focus_granted"];
                [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
            } else {
                CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR
                                                                  messageAsString:@"focus_failed"];
                [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
            }
        } else {
            CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR
                                                              messageAsString:@"focus_failed"];
            [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
        }
    }];
}

- (void)releaseMicFocus:(CDVInvokedUrlCommand*)command
{
    [self.commandDelegate runInBackground:^{
        NSError *error = nil;
        AVAudioSession *audioSession = [AVAudioSession sharedInstance];
        
        // Deactivate the audio session
        BOOL success = [audioSession setActive:NO error:&error];
        
        if (success) {
            CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK
                                                              messageAsString:@"focus_released"];
            [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
        } else {
            CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR
                                                              messageAsString:@"focus_release_failed"];
            [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
        }
    }];
}

@end 