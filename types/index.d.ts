/*
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
*/

declare global {
    interface Navigator {
        audioFocus: AudioFocusPlugin;
    }
}

export interface AudioFocusPlugin {
    /**
     * Requests audio focus for microphone usage.
     * @param successCallback Function to call when audio focus is granted.
     * @param errorCallback Function to call when audio focus request fails.
     */
    requestMicFocus(successCallback: (message: string) => void, errorCallback: (error: string) => void): void;

    /**
     * Releases the audio focus that was previously requested.
     * @param successCallback Function to call when audio focus is released.
     * @param errorCallback Function to call when audio focus release fails.
     */
    releaseMicFocus(successCallback: (message: string) => void, errorCallback: (error: string) => void): void;

    /**
     * Prepares the audio session specifically for recording on Android 14+.
     * This method sets up the proper audio mode and focus for microphone recording.
     * Use this before calling getUserMedia() on Android 14+ devices.
     * @param successCallback Function to call when audio session is ready.
     * @param errorCallback Function to call when preparation fails.
     */
    prepareForRecording(successCallback: (message: string) => void, errorCallback: (error: string) => void): void;

    /**
     * Fixes Android 14+ audio device selection issues.
     * This method specifically addresses the "Unable to select communication device" error
     * by properly configuring audio devices and streams for Android 14+.
     * @param successCallback Function to call when device is fixed.
     * @param errorCallback Function to call when fix fails.
     */
    fixAndroid14AudioDevice(successCallback: (message: string) => void, errorCallback: (error: string) => void): void;
}

export {}; 