package de.Maxr1998.xposed.mnts;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

import static de.Maxr1998.xposed.mnts.TrackSelector.PACKAGE_NAME;

public class Main implements IXposedHookZygoteInit, IXposedHookLoadPackage {

    @Override
    public void initZygote(StartupParam startupParam) throws Throwable {

    }

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lPParam) throws Throwable {
        if (lPParam.packageName.equals(PACKAGE_NAME)) {
            TrackSelector.initUI(lPParam);
        }
    }
}