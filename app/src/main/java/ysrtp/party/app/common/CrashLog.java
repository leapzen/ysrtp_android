package ysrtp.party.app.common;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class CrashLog {
    @SerializedName("APP_VERSION_CODE")
    @Expose
    private String APPVERSIONCODE;
    @SerializedName("PHONE_MODEL")
    @Expose
    private String PHONEMODEL;
    @SerializedName("STACK_TRACE")
    @Expose
    private String STACKTRACE;
    @SerializedName("LOGCAT")
    @Expose
    private String LOGCAT;

    public CrashLog(String APPVERSIONCODE, String PHONEMODEL,String STACKTRACE, String LOGCAT) {
        this.APPVERSIONCODE = APPVERSIONCODE;
        this.PHONEMODEL = PHONEMODEL;
        this.STACKTRACE = STACKTRACE;
        this.LOGCAT = LOGCAT;
    }

    public String getAppVersionCode() {
        return APPVERSIONCODE;
    }

    public String getPhoneModel() {
        return PHONEMODEL;
    }

    public String getStacktrace() {
        return STACKTRACE;
    }

    public String getLogcat() {
        return LOGCAT;
    }
}
