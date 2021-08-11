package com.cvte.cameraview.engine.meter;

import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import com.cvte.cameraview.engine.action.ActionHolder;
import com.cvte.cameraview.engine.action.ActionWrapper;
import com.cvte.cameraview.engine.action.Actions;
import com.cvte.cameraview.engine.action.BaseAction;
import com.cvte.cameraview.engine.lock.ExposureLock;
import com.cvte.cameraview.engine.lock.FocusLock;
import com.cvte.cameraview.engine.lock.WhiteBalanceLock;

@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
public class MeterResetAction extends ActionWrapper {

    private final BaseAction action;

    public MeterResetAction() {
        this.action = Actions.together(
                new ExposureReset(),
                new FocusReset(),
                new WhiteBalanceReset()
        );
    }

    @NonNull
    @Override
    public BaseAction getAction() {
        return action;
    }
}
