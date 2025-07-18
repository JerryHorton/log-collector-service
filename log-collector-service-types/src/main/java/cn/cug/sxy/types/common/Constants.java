package cn.cug.sxy.types.common;

public class Constants {

    public final static String SPLIT = ",";
    public final static String COLON = ":";
    public final static String SPACE = " ";
    public final static String UNDERLINE = "_";
    public final static String VERTICAL = "|";
    public final static String EQUAL = "=";

    public static class RedisKey {

        public static final String LOG_APP_ACCESS_BY_ID_KEY = "log_collector:log_app_access_by_id_key_";
        public static final String LOG_APP_ACCESS_BY_ACCESSKEY_KEY = "log_collector:log_app_access_by_accesskey_key_";
        public static final String RECEIVER_ENDPOINT_KEY = "log_collector:receiver_endpoint_key_";

        public static final String ELASTICSEARCH_STRUCTURED_FIELD_KEY = "log_collector:elasticsearch_structured_field_key_";
    }


}
