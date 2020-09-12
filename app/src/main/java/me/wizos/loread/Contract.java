package me.wizos.loread;

/**
 * Created by Wizos on 2019/2/8.
 */

public class Contract {
    public static final String PROVIDER_LOCALRSS = "LocalRSS";
    public static final String PROVIDER_INOREADER = "InoReader";
    public static final String PROVIDER_FEEDLY = "Feedly";
    public static final String PROVIDER_TINYRSS = "TinyTinyRSS";
    public static final String PROVIDER_LOREAD = "Loread";
    public static final String UID = "UID";
    public static final String SCHEMA_HTTP = "http://";
    public static final String SCHEMA_HTTPS = "https://";
    public static final String SCHEMA_FILE = "file://";
    public static final String SCHEMA_LOREAD = "loread://";

    public static final String HTTP = "http";
    public static final String HTTPS = "https";

    public static final String isPortrait = "isPortrait";
    // ACCOUNT_TYPE用于我们当前APP获取系统帐户的唯一标识，这个在account_preferences.xml中有，两处的声明必须是一致
    // public static final String ACCOUNT_TYPE = "me.wizos.loreadx";
}
