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
package org.apache.cordova.audiofocus;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.os.Build;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PermissionHelper;
import org.json.JSONArray;
import org.json.JSONException;

/**
 * This class provides audio focus functionality for Cordova applications.
 * It allows requesting and managing audio focus for microphone usage.
 */
public class AudioFocusPlugin extends CordovaPlugin {
    private AudioManager audioManager;
    private AudioFocusRequest focusRequest;
    private static final int PERMISSION_REQUEST_CODE = 1001;
    private CallbackContext pendingCallbackContext;

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        if ("requestMicFocus".equals(action)) {
            // Check for RECORD_AUDIO permission first
            if (!hasRecordAudioPermission()) {
                requestRecordAudioPermission(callbackContext);
                return true;
            }
            
            requestAudioFocus(callbackContext);
            return true;
        } else if ("releaseMicFocus".equals(action)) {
            releaseAudioFocus(callbackContext);
            return true;
        } else if ("prepareForRecording".equals(action)) {
            // Android 14+ specific method to prepare audio session for recording
            prepareAudioSessionForRecording(callbackContext);
            return true;
        } else if ("fixAndroid14AudioDevice".equals(action)) {
            // Specific method to fix Android 14+ audio device selection issues
            fixAndroid14AudioDevice(callbackContext);
            return true;
        }
        return false;
    }

    private boolean hasRecordAudioPermission() {
        return ContextCompat.checkSelfPermission(cordova.getActivity(), Manifest.permission.RECORD_AUDIO) 
               == PackageManager.PERMISSION_GRANTED;
    }

    private void requestRecordAudioPermission(CallbackContext callbackContext) {
        PermissionHelper.requestPermission(this, PERMISSION_REQUEST_CODE, Manifest.permission.RECORD_AUDIO);
        // Store callback for later use
        this.pendingCallbackContext = callbackContext;
    }

    private void requestAudioFocus(CallbackContext callbackContext) {
        cordova.getActivity().runOnUiThread(() -> {
            audioManager = (AudioManager) cordova.getActivity().getSystemService(Context.AUDIO_SERVICE);
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                // For Android 14+ (API 34+), use more specific audio attributes
                AudioAttributes.Builder attributesBuilder = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_VOICE_COMMUNICATION)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH);
                
                // For Android 14+, we need to be more specific about the audio focus
                AudioFocusRequest.Builder focusRequestBuilder = new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                    .setAudioAttributes(attributesBuilder.build())
                    .setOnAudioFocusChangeListener(new AudioManager.OnAudioFocusChangeListener() {
                        @Override
                        public void onAudioFocusChange(int focusChange) {
                            // Handle audio focus changes if needed
                        }
                    });
                
                // For Android 14+, add additional flags
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                    focusRequestBuilder.setAcceptsDelayedFocusGain(true);
                }
                
                focusRequest = focusRequestBuilder.build();

                int result = audioManager.requestAudioFocus(focusRequest);
                if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                    callbackContext.success("focus_granted");
                } else {
                    callbackContext.error("focus_failed");
                }
            } else {
                // For older Android versions, use the deprecated method
                int result = audioManager.requestAudioFocus(null, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
                if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                    callbackContext.success("focus_granted");
                } else {
                    callbackContext.error("focus_failed");
                }
            }
        });
    }

    private void releaseAudioFocus(CallbackContext callbackContext) {
        cordova.getActivity().runOnUiThread(() -> {
            if (audioManager != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && focusRequest != null) {
                    audioManager.abandonAudioFocusRequest(focusRequest);
                } else {
                    audioManager.abandonAudioFocus(null);
                }
                callbackContext.success("focus_released");
            } else {
                callbackContext.error("no_focus_to_release");
            }
        });
    }

    @Override
    public void onRequestPermissionResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, now request audio focus
                requestAudioFocus(this.pendingCallbackContext);
            } else {
                // Permission denied
                this.pendingCallbackContext.error("permission_denied");
            }
        }
    }

    private void prepareAudioSessionForRecording(CallbackContext callbackContext) {
        cordova.getActivity().runOnUiThread(() -> {
            try {
                audioManager = (AudioManager) cordova.getActivity().getSystemService(Context.AUDIO_SERVICE);
                
                // For Android 14+, we need to handle audio device selection properly
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                    // Set communication mode for better microphone access
                    audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
                    
                    // For Android 14+, we need to ensure the communication device is available
                    // This helps with the "Unable to select communication device" error
                    audioManager.setSpeakerphoneOn(false);
                    audioManager.setBluetoothScoOn(false);
                    
                    // Set the audio stream to voice call for better device selection
                    audioManager.setStreamVolume(AudioManager.STREAM_VOICE_CALL, 
                        audioManager.getStreamMaxVolume(AudioManager.STREAM_VOICE_CALL), 0);
                } else {
                    // For older versions, use communication mode
                    audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
                }
                
                // Request audio focus with communication attributes
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    AudioAttributes.Builder attributesBuilder = new AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_VOICE_COMMUNICATION)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH);
                    
                    AudioFocusRequest.Builder focusRequestBuilder = new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                        .setAudioAttributes(attributesBuilder.build())
                        .setOnAudioFocusChangeListener(new AudioManager.OnAudioFocusChangeListener() {
                            @Override
                            public void onAudioFocusChange(int focusChange) {
                                // Handle focus changes
                            }
                        });
                    
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                        focusRequestBuilder.setAcceptsDelayedFocusGain(true);
                    }
                    
                    focusRequest = focusRequestBuilder.build();
                    int result = audioManager.requestAudioFocus(focusRequest);
                    
                    if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                        callbackContext.success("recording_ready");
                    } else {
                        callbackContext.error("recording_failed");
                    }
                } else {
                    // Fallback for older versions
                    int result = audioManager.requestAudioFocus(null, AudioManager.STREAM_VOICE_CALL, AudioManager.AUDIOFOCUS_GAIN);
                    if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                        callbackContext.success("recording_ready");
                    } else {
                        callbackContext.error("recording_failed");
                    }
                }
            } catch (Exception e) {
                callbackContext.error("audio_session_error: " + e.getMessage());
            }
        });
    }

    private void fixAndroid14AudioDevice(CallbackContext callbackContext) {
        cordova.getActivity().runOnUiThread(() -> {
            try {
                audioManager = (AudioManager) cordova.getActivity().getSystemService(Context.AUDIO_SERVICE);
                
                // For Android 14+, we need to explicitly configure audio devices
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                    // Set the audio mode to communication
                    audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
                    
                    // Disable speakerphone and Bluetooth SCO to force earpiece/microphone
                    audioManager.setSpeakerphoneOn(false);
                    audioManager.setBluetoothScoOn(false);
                    
                    // Set the communication stream volume
                    int maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_VOICE_CALL);
                    audioManager.setStreamVolume(AudioManager.STREAM_VOICE_CALL, maxVolume, 0);
                    
                    // Also set the music stream volume for better compatibility
                    int musicMaxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
                    audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, musicMaxVolume, 0);
                    
                    // Request audio focus with specific attributes for communication
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        AudioAttributes.Builder attributesBuilder = new AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_VOICE_COMMUNICATION)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH);
                        
                        AudioFocusRequest.Builder focusRequestBuilder = new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                            .setAudioAttributes(attributesBuilder.build())
                            .setOnAudioFocusChangeListener(new AudioManager.OnAudioFocusChangeListener() {
                                @Override
                                public void onAudioFocusChange(int focusChange) {
                                    // Handle focus changes
                                }
                            });
                        
                        focusRequestBuilder.setAcceptsDelayedFocusGain(true);
                        focusRequest = focusRequestBuilder.build();
                        
                        int result = audioManager.requestAudioFocus(focusRequest);
                        if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                            callbackContext.success("device_fixed");
                        } else {
                            callbackContext.error("device_fix_failed");
                        }
                    } else {
                        int result = audioManager.requestAudioFocus(null, AudioManager.STREAM_VOICE_CALL, AudioManager.AUDIOFOCUS_GAIN);
                        if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                            callbackContext.success("device_fixed");
                        } else {
                            callbackContext.error("device_fix_failed");
                        }
                    }
                } else {
                    // For older versions, just request audio focus normally
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        AudioAttributes.Builder attributesBuilder = new AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_VOICE_COMMUNICATION)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH);
                        
                        AudioFocusRequest.Builder focusRequestBuilder = new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                            .setAudioAttributes(attributesBuilder.build())
                            .setOnAudioFocusChangeListener(new AudioManager.OnAudioFocusChangeListener() {
                                @Override
                                public void onAudioFocusChange(int focusChange) {
                                    // Handle focus changes
                                }
                            });
                        
                        focusRequest = focusRequestBuilder.build();
                        int result = audioManager.requestAudioFocus(focusRequest);
                        if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                            callbackContext.success("device_fixed");
                        } else {
                            callbackContext.error("device_fix_failed");
                        }
                    } else {
                        int result = audioManager.requestAudioFocus(null, AudioManager.STREAM_VOICE_CALL, AudioManager.AUDIOFOCUS_GAIN);
                        if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                            callbackContext.success("device_fixed");
                        } else {
                            callbackContext.error("device_fix_failed");
                        }
                    }
                }
            } catch (Exception e) {
                callbackContext.error("device_fix_error: " + e.getMessage());
            }
        });
    }
} 