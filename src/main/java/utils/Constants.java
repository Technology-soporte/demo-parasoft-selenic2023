package utils;

public class Constants {
    public static ConfigReader configReader = new ConfigReader("config.properties");

    public static String CTP_USER = configReader.getProperty("ctp_user");
    public static String CTP_PASSWORD = configReader.getProperty("ctp_password");
    public static String CTP_BASE_URL = configReader.getProperty("ctp_base_url");
    public static String BODY_POST_EM_JOBS = "src\\main\\resources\\payloads\\POST_em-jobs.json";
    public static String BODY_POST_V6_MESSAGEPROXIES = "src\\main\\resources\\payloads\\POST_v6-messageProxies.json";
    public static String BODY_PUT_EM_API_V3_JOBS_ID = "src\\main\\resources\\payloads\\PUT_em-api-v3-jobs-id.json";
}
