package com.example.neuralstyler;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import com.example.neuralstyler.ml.MagentaArbitraryImageStylizationV1256Int8Prediction1;
import com.example.neuralstyler.ml.MagentaArbitraryImageStylizationV1256Int8Transfer1;

import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.IOException;


public class MagentaModel {
    // TFLite models
    private MagentaArbitraryImageStylizationV1256Int8Prediction1 embedder;
    private MagentaArbitraryImageStylizationV1256Int8Transfer1 transformer;
    // internal variables
    final Context context;
    final String loggerTag = "MagentaModelLogger";

    /**
     * @param runtimeContext constructor requires context which model will be launched in
     */
    MagentaModel(Context runtimeContext) {
        context = runtimeContext;
    }

    /**
     * Creates embedding with prediction model
     *
     * @param style style image as android Bitmap
     *
     * @return TensorBuffer containing generated embedding,
     *         it has shade [1, 1, 1, 100] for used model
     */
    private TensorBuffer createEmbedding(Bitmap style) {
        TensorImage styleImage = TensorImage.fromBitmap(style);
        MagentaArbitraryImageStylizationV1256Int8Prediction1.Outputs outputs = embedder.process(styleImage);

        return outputs.getStyleBottleneckAsTensorBuffer();
    }

    /**
     * transfers style embedding onto an image
     *
     * @param content content image passed as Bitmap
     * @param styleEmbedding generated style embedding
     *
     * @return TensorImage with style transferred
     */
    private TensorImage transferEmbedding(Bitmap content, TensorBuffer styleEmbedding) {
        TensorImage contentImage = TensorImage.fromBitmap(content);
        MagentaArbitraryImageStylizationV1256Int8Transfer1.Outputs outputs = transformer.process(contentImage, styleEmbedding);

        return outputs.getStyledImageAsTensorImage();
    }

    /**
     * Main model entry point, transfer style from style image to given content image
     *
     * @param content image to be modified passed as Bitmap
     * @param style style image as android Bitmap
     *
     * @return android Bitmap with new style
     */
    public Bitmap transferStyle(Bitmap content, Bitmap style) {
        try {
            embedder = MagentaArbitraryImageStylizationV1256Int8Prediction1.newInstance(context);
            transformer = MagentaArbitraryImageStylizationV1256Int8Transfer1.newInstance(context);
        } catch (IOException e) {
            Log.e(loggerTag,"Error!" + e.toString());
        }

        TensorBuffer embedding = createEmbedding(style);
        TensorImage styledImage = transferEmbedding(content, embedding);

        return styledImage.getBitmap();
    }
}
