package com.github.natanbc.coroutine.impl.win32;

import com.sun.jna.Pointer;
import com.sun.jna.win32.StdCallLibrary;

interface FiberStartRoutine extends StdCallLibrary.StdCallCallback {
    void run(Pointer arg);
}
