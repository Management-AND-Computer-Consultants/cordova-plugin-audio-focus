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

var exec = require('cordova/exec');

/**
 * @namespace navigator
 */

/**
 * @exports audioFocus
 */
var audioFocusExport = {};

/**
 * Callback function that provides an error message.
 * @callback module:audioFocus.onError
 * @param {string} message - The message is provided by the device's native code.
 */

/**
 * Callback function that provides the success message.
 * @callback module:audioFocus.onSuccess
 * @param {string} message - The success message from the native code.
 */

/**
 * @description Requests audio focus for microphone usage.
 * The audio focus allows your app to have priority access to the audio system.
 * This will also request RECORD_AUDIO permission if not already granted.
 *
 * @param {module:audioFocus.onSuccess} successCallback - Function to call when audio focus is granted.
 * @param {module:audioFocus.onError} errorCallback - Function to call when audio focus request fails.
 * @example
 * // Request audio focus
 * navigator.audioFocus.requestMicFocus(
 *     function(message) {
 *         console.log('Audio focus granted: ' + message);
 *     },
 *     function(error) {
 *         console.log('Audio focus failed: ' + error);
 *         if (error === 'permission_denied') {
 *             console.log('User denied microphone permission');
 *         }
 *     }
 * );
 */
audioFocusExport.requestMicFocus = function(successCallback, errorCallback) {
    exec(successCallback, errorCallback, 'AudioFocus', 'requestMicFocus', []);
};

/**
 * @description Releases the audio focus that was previously requested.
 * This should be called when your app no longer needs audio focus.
 *
 * @param {module:audioFocus.onSuccess} successCallback - Function to call when audio focus is released.
 * @param {module:audioFocus.onError} errorCallback - Function to call when audio focus release fails.
 * @example
 * // Release audio focus
 * navigator.audioFocus.releaseMicFocus(
 *     function(message) {
 *         console.log('Audio focus released: ' + message);
 *     },
 *     function(error) {
 *         console.log('Audio focus release failed: ' + error);
 *     }
 * );
 */
audioFocusExport.releaseMicFocus = function(successCallback, errorCallback) {
    exec(successCallback, errorCallback, 'AudioFocus', 'releaseMicFocus', []);
};

/**
 * @description Prepares the audio session specifically for recording on Android 14+.
 * This method sets up the proper audio mode and focus for microphone recording.
 * Use this before calling getUserMedia() on Android 14+ devices.
 *
 * @param {module:audioFocus.onSuccess} successCallback - Function to call when audio session is ready.
 * @param {module:audioFocus.onError} errorCallback - Function to call when preparation fails.
 * @example
 * // Prepare for recording (especially important for Android 14+)
 * navigator.audioFocus.prepareForRecording(
 *     function(message) {
 *         console.log('Recording ready: ' + message);
 *         // Now safe to call getUserMedia()
 *         startRecording();
 *     },
 *     function(error) {
 *         console.log('Recording preparation failed: ' + error);
 *     }
 * );
 */
audioFocusExport.prepareForRecording = function(successCallback, errorCallback) {
    exec(successCallback, errorCallback, 'AudioFocus', 'prepareForRecording', []);
};

/**
 * @description Fixes Android 14+ audio device selection issues.
 * This method specifically addresses the "Unable to select communication device" error
 * by properly configuring audio devices and streams for Android 14+.
 *
 * @param {module:audioFocus.onSuccess} successCallback - Function to call when device is fixed.
 * @param {module:audioFocus.onError} errorCallback - Function to call when fix fails.
 * @example
 * // Fix Android 14+ audio device issues
 * navigator.audioFocus.fixAndroid14AudioDevice(
 *     function(message) {
 *         console.log('Device fixed: ' + message);
 *         // Now safe to call getUserMedia()
 *         startRecording();
 *     },
 *     function(error) {
 *         console.log('Device fix failed: ' + error);
 *     }
 * );
 */
audioFocusExport.fixAndroid14AudioDevice = function(successCallback, errorCallback) {
    exec(successCallback, errorCallback, 'AudioFocus', 'fixAndroid14AudioDevice', []);
};

module.exports = audioFocusExport; 