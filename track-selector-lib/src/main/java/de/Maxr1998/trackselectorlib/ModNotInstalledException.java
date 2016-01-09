package de.Maxr1998.trackselectorlib;

public class ModNotInstalledException extends Exception {

    public ModNotInstalledException() {
        super();
    }

    public ModNotInstalledException(Throwable throwable) {
        super(throwable);
    }

    @Override
    public String getMessage() {
        return "Module or Xposed aren't active or could not be found, is it installed correctly?";
    }
}
