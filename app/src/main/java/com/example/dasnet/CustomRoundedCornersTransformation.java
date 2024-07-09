package com.example.dasnet;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Shader;

import androidx.annotation.NonNull;
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation;

import java.nio.ByteBuffer;
import java.security.MessageDigest;

public class CustomRoundedCornersTransformation extends BitmapTransformation {

    private static final String ID = "com.example.CustomRoundedCornersTransformation";
    private static final byte[] ID_BYTES = ID.getBytes(CHARSET);

    private final float radius;

    public CustomRoundedCornersTransformation(float radius) {
        this.radius = radius;
    }

    @Override
    protected Bitmap transform(@NonNull BitmapPool pool, @NonNull Bitmap toTransform, int outWidth, int outHeight) {
        return roundCorners(pool, toTransform);
    }

    private Bitmap roundCorners(BitmapPool pool, Bitmap source) {
        if (source == null) {
            return null;
        }

        Bitmap result = pool.get(source.getWidth(), source.getHeight(), Bitmap.Config.ARGB_8888);
        result.setHasAlpha(true);

        Canvas canvas = new Canvas(result);
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setShader(new BitmapShader(source, BitmapShader.TileMode.CLAMP, BitmapShader.TileMode.CLAMP));

        float radius = 20; // Adjust the radius as needed
        float offset = 0.5522847498f * radius; // Approximation of cubic Bezier curve control point distance

        Path path = createRoundedRectPath(0, 0, source.getWidth(), source.getHeight(), radius, offset);

        // Apply gradient effect to the rounded corners
        //LinearGradient gradient = new LinearGradient(0, 0, source.getWidth(), source.getHeight(), Color.RED, Color.BLUE, Shader.TileMode.CLAMP);
        //paint.setShader(gradient);

        canvas.drawPath(path, paint);

        return result;
    }

    private Path createRoundedRectPath(float left, float top, float right, float bottom, float radius, float offset) {
        Path path = new Path();
        path.moveTo(left + radius, top);
        path.lineTo(right - radius, top);
        path.cubicTo(right - radius + offset, top, right, top + radius - offset, right, top + radius);
        path.lineTo(right, bottom - radius);
        path.cubicTo(right, bottom - radius + offset, right - radius + offset, bottom, right - radius, bottom);
        path.lineTo(left + radius, bottom);
        path.cubicTo(left + radius - offset, bottom, left, bottom - radius + offset, left, bottom - radius);
        path.lineTo(left, top + radius);
        path.cubicTo(left, top + radius - offset, left + radius - offset, top, left + radius, top);
        path.close();
        return path;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof CustomRoundedCornersTransformation && radius == ((CustomRoundedCornersTransformation) o).radius;
    }

    @Override
    public int hashCode() {
        return ID.hashCode() + (int) (radius * 100);
    }

    @Override
    public void updateDiskCacheKey(@NonNull MessageDigest messageDigest) {
        messageDigest.update(ID_BYTES);
        ByteBuffer radiusBuffer = ByteBuffer.allocate(4).putFloat(radius);
        messageDigest.update(radiusBuffer.array());
    }
}
