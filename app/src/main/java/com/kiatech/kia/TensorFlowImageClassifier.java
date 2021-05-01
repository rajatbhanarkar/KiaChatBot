package com.kiatech.kia;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.util.Log;

import com.google.common.primitives.UnsignedInteger;
import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.Tensor;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

// Image classification via tflite

public class TensorFlowImageClassifier implements Classifier {
    private static final int MAX_RESULTS = 1;
    private static final int BATCH_SIZE = 1;
    private static final int PIXEL_SIZE = 1;
    private static final float THRESHOLD = 0.1f;

    private static final float IMAGE_MEAN = 127.5f;
    private static final float IMAGE_STD = 127.5f;

    private Interpreter interpreter;
    private int inputSize;
    private List<String> labelList;
    private boolean quant;

    private TensorFlowImageClassifier() {

    }

    static Classifier create(AssetManager assetManager, String modelPath, String labelPath, int inputSize, boolean quant) throws IOException {

        TensorFlowImageClassifier classifier = new TensorFlowImageClassifier();
        classifier.interpreter = new Interpreter(classifier.loadModelFile(assetManager, modelPath), new Interpreter.Options());
        classifier.labelList = classifier.loadLabelList(assetManager, labelPath);
        classifier.inputSize = inputSize;
        classifier.quant = quant;

        return classifier;
    }

    @Override
    public String recognizeImage(Bitmap bitmap) {
        //ByteBuffer byteBuffer = convertBitmapToByteBuffer(bitmap);

        /*Log.d("ByteBuffer", ""+byteBuffer.capacity());
        Log.d("Interpreter", interpreter.toString());
        Log.d("InputTensor", ""+interpreter.getInputTensor(0).toString());
        Log.d("OutputTensor", ""+interpreter.getOutputTensor(0).toString());*/

        Log.d("---- Count ----", ""+interpreter.getInputTensorCount());
        Log.d("---- Type ----", ""+interpreter.getInputTensor(0).toString());


        float[][][][] myArray = new float[1][inputSize][inputSize][1];

        int[] intPixels = new int[inputSize*inputSize];

        bitmap.getPixels(intPixels, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());

        int pixel = 0;
        int max = Integer.MIN_VALUE;
        int min = Integer.MAX_VALUE;

        for(int i=0 ; i<intPixels.length ; i++){
            max = Math.max(max, intPixels[i]);
            min = Math.max(min, intPixels[i]);
        }

        for (int i=0; i < inputSize ; i++) {
            for(int j=0 ; j<inputSize ; j++){
                //myArray[0][i][j][0] = mapInRange(max, min, intPixels[pixel++]);
                myArray[0][i][j][0] = ((intPixels[pixel++]&0xff)-IMAGE_MEAN)/IMAGE_STD;
                //myArray[0][i][j][0] = (float) Math.random();
            }
        }

        float[][] result = new float[1][6];

        interpreter.run(myArray, result);

        //String[] cats = new String[]{"Angry", "Disgust", "Fear", "Happy", "Sad", "Surprised", "Neutral"};

        String[] cats = new String[]{"Sad", "Sad", "Happy", "Neutral", "Sad", "Happy"};

        //String[] cats = new String[]{"Happy", "Neutral", "Sad"};

        Log.d("---- RESULT ----", Arrays.toString(result[0]));

        return cats[maxIndex(result[0])];
        //return "Happy";
    }

    int maxIndex(float[] arr){
        int max = -1;
        float maxVal = -1;

        for(int i=0 ; i<arr.length ; i++){
            if (maxVal<arr[i]){
                max = i;
                maxVal = arr[i];
            }
        }

        Log.d("---- MAXVAL ----", ""+maxVal);
        Log.d("---- MAXIDX ----", ""+max);

        return max;
    }

    float mapInRange(int max, int min, int value){
        float oldRange = max-min;
        float newRange = 1;

        if (oldRange == 0){ return 0; }

        return (((value-min)*newRange)/oldRange);
    }

    @Override
    public void close() {
        interpreter.close();
        interpreter = null;
    }

    private MappedByteBuffer loadModelFile(AssetManager assetManager, String modelPath) throws IOException {
        AssetFileDescriptor fileDescriptor = assetManager.openFd(modelPath);
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }

    private List<String> loadLabelList(AssetManager assetManager, String labelPath) throws IOException {
        List<String> labelList = new ArrayList<>();
        BufferedReader reader = new BufferedReader(new InputStreamReader(assetManager.open(labelPath)));
        String line;
        while ((line = reader.readLine()) != null) {
            labelList.add(line);
        }
        reader.close();
        return labelList;
    }

    private ByteBuffer convertBitmapToByteBuffer(Bitmap bitmap) {
        ByteBuffer byteBuffer;

        if (quant) {
            byteBuffer = ByteBuffer.allocateDirect(BATCH_SIZE * inputSize * inputSize * PIXEL_SIZE);
        } else {
            byteBuffer = ByteBuffer.allocateDirect(4 * BATCH_SIZE * inputSize * inputSize * PIXEL_SIZE);
        }

        Log.d("ByteBuffer", ""+byteBuffer.capacity());


        byteBuffer.order(ByteOrder.nativeOrder());
        int[] intValues = new int[inputSize * inputSize];
        bitmap.getPixels(intValues, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());
        int pixel = 0;
        for (int i = 0; i < inputSize; i++) {
            for (int j = 0; j < inputSize; j++) {
                final int val = intValues[pixel++];
                if (quant) {
                    byteBuffer.put((byte) ((val >> 16) & 0xFF));
                    byteBuffer.put((byte) ((val >> 8) & 0xFF));
                    byteBuffer.put((byte) (val & 0xFF));
                } else {
                    byteBuffer.putFloat((((val >> 16) & 0xFF) - IMAGE_MEAN) / IMAGE_STD);
                    byteBuffer.putFloat((((val >> 8) & 0xFF) - IMAGE_MEAN) / IMAGE_STD);
                    byteBuffer.putFloat((((val) & 0xFF) - IMAGE_MEAN) / IMAGE_STD);

                }
            }
        }
        return byteBuffer;
    }

    @SuppressLint("DefaultLocale")
    private List<Recognition> getSortedResultByte(byte[][] labelProbArray) {

        PriorityQueue<Recognition> pq = new PriorityQueue<>(MAX_RESULTS, new Comparator<Recognition>() {
            @Override
            public int compare(Recognition lhs, Recognition rhs) {
                return Float.compare(rhs.getConfidence(), lhs.getConfidence());
            }
        });

        for (int i = 0; i < labelList.size(); ++i) {
            float confidence = (labelProbArray[0][i] & 0xff) / 255.0f;
            if (confidence > THRESHOLD) {
                pq.add(new Recognition("" + i, labelList.size() > i ? labelList.get(i) : "unknown", confidence, quant));
            }
        }

        final ArrayList<Recognition> recognitions = new ArrayList<>();
        int recognitionsSize = Math.min(pq.size(), MAX_RESULTS);
        for (int i = 0; i < recognitionsSize; ++i) {
            recognitions.add(pq.poll());
        }

        return recognitions;
    }

    @SuppressLint("DefaultLocale")
    private List<Recognition> getSortedResultFloat(float[][] labelProbArray) {

        PriorityQueue<Recognition> pq = new PriorityQueue<>(MAX_RESULTS, new Comparator<Recognition>() {
            @Override
            public int compare(Recognition lhs, Recognition rhs) {
                return Float.compare(rhs.getConfidence(), lhs.getConfidence());
            }
        });

        for (int i = 0; i < labelList.size(); ++i) {
            float confidence = labelProbArray[0][i];
            if (confidence > THRESHOLD) {
                pq.add(new Recognition("" + i, labelList.size() > i ? labelList.get(i) : "unknown", confidence, quant));
            }
        }

        final ArrayList<Recognition> recognitions = new ArrayList<>();
        int recognitionsSize = Math.min(pq.size(), MAX_RESULTS);
        for (int i = 0; i < recognitionsSize; ++i) {
            recognitions.add(pq.poll());
        }

        return recognitions;
    }

}
