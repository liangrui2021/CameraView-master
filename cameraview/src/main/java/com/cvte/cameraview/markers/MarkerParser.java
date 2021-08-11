package com.cvte.cameraview.markers;

import android.content.Context;
import android.content.res.TypedArray;

import com.cvte.cameraview.R;
import com.cvte.cameraview.controls.Audio;
import com.cvte.cameraview.controls.Facing;
import com.cvte.cameraview.controls.Flash;
import com.cvte.cameraview.controls.Grid;
import com.cvte.cameraview.controls.Hdr;
import com.cvte.cameraview.controls.Mode;
import com.cvte.cameraview.controls.Preview;
import com.cvte.cameraview.controls.VideoCodec;
import com.cvte.cameraview.controls.WhiteBalance;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Parses markers from XML attributes.
 */
public class MarkerParser {

    private AutoFocusMarker autoFocusMarker = null;

    public MarkerParser(@NonNull TypedArray array) {
        String autoFocusName = array.getString(R.styleable.CameraView_cameraAutoFocusMarker);
        if (autoFocusName != null) {
            try {
                Class<?> autoFocusClass = Class.forName(autoFocusName);
                autoFocusMarker = (AutoFocusMarker) autoFocusClass.newInstance();
            } catch (Exception ignore) { }
        }
    }

    @Nullable
    public AutoFocusMarker getAutoFocusMarker() {
        return autoFocusMarker;
    }
}
