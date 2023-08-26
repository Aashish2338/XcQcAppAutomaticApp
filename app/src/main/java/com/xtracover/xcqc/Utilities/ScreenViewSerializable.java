package com.xtracover.xcqc.Utilities;

import java.io.Serializable;

public class ScreenViewSerializable implements Serializable {

    public boolean isTouchDown;
    public int SIZE = 40;
    public float mPreTouchedX = 0.0F;
    public float mPreTouchedY = 0.0F;
    public int mScreenHeight;
    public int mScreenWidth;
    public float mTouchedX = 0.0F;
    public float mTouchedY = 0.0F;
    public boolean[][] drawCopy;
}