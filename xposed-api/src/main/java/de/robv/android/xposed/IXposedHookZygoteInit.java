package de.robv.android.xposed;

public interface IXposedHookZygoteInit {
    void initZygote(StartupParam startupParam) throws Throwable;
    static class StartupParam {
        // TODO: add fields if needed
    }
}
