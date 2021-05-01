package com.kiatech.kia;

import android.graphics.Bitmap;

import java.util.List;

// This is the ML/DL Model integration code using tensorflow lite

// This code is for image classification

public interface Classifier {
    class Recognition {
        private final String id; //unique identifier
        private final String title; //display name
        private final boolean quant; //quantized or float weights
        private final Float confidence; //how good the recognition is relative to others

        public Recognition(final String id, final String title, final Float confidence, final boolean quant) {
            this.id = id;
            this.title = title;
            this.confidence = confidence;
            this.quant = quant;
        }

        public String getId() {
            return id;
        }

        public String getTitle() {
            return title;
        }

        public Float getConfidence() {
            return confidence;
        }

        @Override
        public String toString() {
            String resultString = "";
            if (id != null) {
                resultString += "[" + id + "] ";
            }

            if (title != null) {
                resultString += title + " ";
            }

            if (confidence != null) {
                resultString += String.format("(%.1f%%) ", confidence * 100.0f);
            }

            return resultString.trim();
        }
    }

    String recognizeImage(Bitmap bitmap);

    void close();
}
