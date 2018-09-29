package y.auto.util;

import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import net.sf.json.JSONObject;


public class Config {
    
    private static Logger logger = Logger.getLogger(Config.class);
    
    private static JSONObject config;
    
    private static String _LOCK = "lock";
    
    private static Config myconfig;
    
    private Config() {
        init();
    }

    public static Config getInstance() {
        if(null == config){
            synchronized(_LOCK){
                if(null == config){
                    myconfig = new Config();
                }
            }
        }
        return myconfig;
    }

    private synchronized void init() {
        try {
            logger.info("MYConfig.json load start..");
            InputStream is = Config.class.getResourceAsStream("/config.json");
            String configstr = IOUtils.toString(is,"utf-8");
            config = JSONObject.fromObject(configstr);
            logger.info("MYConfig.json load end .." + configstr);
        } catch (Exception e) {
            logger.error("MYConfig.json load error..", e);
        }
    }

    public synchronized Object getConfig(String key) {
        if (null == config) {
            logger.info("getConfig init..");
            init();
        }
        return config.get(key);
    }
}
