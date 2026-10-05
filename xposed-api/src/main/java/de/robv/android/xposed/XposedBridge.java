package de.robv.android.xposed;

public final class XposedBridge {
    private XposedBridge() {}
    public static void hookMethod(Object methodHook, Object callback) {}
    public static void log(String msg) {}
    public static void logThrowable(String msg, Throwable thr) {}
}
