package me.wizos.loread;

/**
 * Created by Wizos on 2019/2/8.
 */

public class Contract {
    public static final String PACKAGE_NAME = "me.wizos.loread";
    public static final String LOREAD = "loread";
    public static final String LOCAL = "local";
    public static final String USER_AGENT = "User-Agent";
    public static final String COOKIE = "Cookie";
    public static final String REFERER = "Referer";

    public static final String ENABLE_REMOTE_URL_REWRITE_RULE = "enable_remote_url_rewrite_rule";
    public static final String ENABLE_REMOTE_HOST_BLOCK_RULE = "enable_remote_host_block_rule";
    public static final String ENABLE_CHECK_UPDATE = "enable_check_update";
    public static final String DEFAULT_USER_AGENT = "default_user-agent"; // 跟随系统, 指定的项目
    public static final String IFRAME_LISTENER = "iframe_listener";
    public static final String BLOCK_ADS = "block_ads";

    public static final String SOCKS5_PROXY = "socks5_proxy";
    public static final String ENABLE_PROXY = "enable_proxy";
    public static final String ENABLE_LOGGING = "enable_logging";
    public static final String ACTIVITY_IS_PORTRAIT = "activity_is_portrait";

    public static final String NOT_LOGGED_IN = "NOT_LOGGED_IN";


    public static final String HOST_BLOCK_URL = "host_block_url";
    public static final String URL_REWRITE_URL = "url_rewrite_url";
    public static final String HEADER_REFERER_URL = "header_referer_url";
    public static final String HEADER_USER_AGENT_URL = "header_user_agent_url";
    public static final String READABILITY_URL = "readability_url";

    public static final String HOST_BLOCK_UPDATE = "host_block_update";
    public static final String URL_REWRITE_UPDATE = "url_rewrite_update";
    public static final String HEADER_REFERER_UPDATE = "header_referer_update";
    public static final String HEADER_USER_AGENT_UPDATE = "header_user_agent_update";
    public static final String READABILITY_UPDATE = "readability_update";

    public static final String PROVIDER = "Provider";
    public static final String PROVIDER_LOCALRSS = "LocalRSS";
    public static final String PROVIDER_INOREADER = "InoReader";
    public static final String PROVIDER_FEEDLY = "Feedly";
    public static final String PROVIDER_TINYRSS = "TinyTinyRSS";
    public static final String PROVIDER_FEVER = "Fever";
    public static final String PROVIDER_FEVER_TINYRSS = "FeverTinyTinyRSS";
    public static final String PROVIDER_LOREAD = "Loread";
    public static final String UID = "UID";
    public static final String SCHEMA_HTTP = "http://";
    public static final String SCHEMA_HTTPS = "https://";
    public static final String SCHEMA_FILE = "file://";
    public static final String SCHEMA_LOREAD = "loread://";

    public static final String SCHEMA_FEEDME = "feedme://";
    public static final String SCHEMA_PDY = "pdy://";
    public static final String SCHEMA_PALABRE = "palabre://";

    public static final String SCHEMA_FEED = "feed/";

    public static final String HTTP = "http";
    public static final String HTTPS = "https";

    public static final String TYPE_GLOBAL = "global";
    public static final String TYPE_CATEGORY = "category";
    public static final String TYPE_FEED = "feed";

    public static final String TYPE = "type";
    public static final String TARGET_ID = "targetId";
    public static final String RULE_ID = "ruleId";
    // public static final String TARGET_NAME = "targetName";

    public static final String ATTR_PICTURES = "pictures";

    public static final String MARK_READ = "mark_read";
    public static final String MARK_STAR = "mark_star";

    public static final String EXT_JSON = ".json";

    // ACCOUNT_TYPE用于我们当前APP获取系统帐户的唯一标识，这个在account_preferences.xml中有，两处的声明必须是一致
    // public static final String ACCOUNT_TYPE = "me.wizos.loread";
}
