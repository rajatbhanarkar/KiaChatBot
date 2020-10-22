package com.kiatech.kia;

import com.google.cloud.dialogflow.v2.DetectIntentResponse;

// Interface to get bot reply from dialogflow agent and return it to the code

public interface BotReply {
    void callback(DetectIntentResponse returnResponse);
}