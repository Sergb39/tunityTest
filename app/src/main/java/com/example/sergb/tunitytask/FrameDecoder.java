package com.example.sergb.tunitytask;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.util.Log;

public class FrameDecoder {

    // decode method to get rgba from yuv byte array.
    public int[] decodeYUV420SP(byte[] yuv420sp, int width, int height) {
        int rgb[] = new int[width * height];
        final int frameSize = width * height;

        for (int j = 0, yp = 0; j < height; j++) {
            int uvp = frameSize + (j >> 1) * width, u = 0, v = 0;
            for (int i = 0; i < width; i++, yp++) {
                int y = (0xff & ((int) yuv420sp[yp])) - 16;
                if (y < 0) y = 0;
                if ((i & 1) == 0) {
                    v = (0xff & yuv420sp[uvp++]) - 128;
                    u = (0xff & yuv420sp[uvp++]) - 128;
                }

                int y1192 = 1192 * y;
                int r = (y1192 + 1634 * v);
                int g = (y1192 - 833 * v - 400 * u);
                int b = (y1192 + 2066 * u);

                if (r < 0) r = 0;
                else if (r > 262143) r = 262143;
                if (g < 0) g = 0;
                else if (g > 262143) g = 262143;
                if (b < 0) b = 0;
                else if (b > 262143) b = 262143;

                rgb[yp] = 0xff000000 | ((r << 6) & 0xff0000) | ((g >> 2) & 0xff00) | ((b >> 10) & 0xff);
            }
        }

        return rgb;
    }

    //
    public Bitmap createBitmapFromRGBArray(int[] rgb, int width, int height) {

        return Bitmap.createBitmap(rgb, width, height, Bitmap.Config.ARGB_8888);
    }

    public int getRGBColorFromCentralPixel(Bitmap bitmap) throws IllegalArgumentException {

        // get middle pixel
        int pixel = bitmap.getPixel(bitmap.getWidth() / 2, bitmap.getHeight() / 2);
        // get rgb values
        int redValue1 = Color.red(pixel);
        int blueValue1 = Color.blue(pixel);
        int greenValue1 = Color.green(pixel);
        return Color.rgb(redValue1, greenValue1, blueValue1);

    }
}
