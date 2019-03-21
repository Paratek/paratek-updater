package io.paratek.rs.loader;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import io.paratek.rs.analysis.hook.Game;


/**
 * Loads all of the configuration data from jav_config.ws
 *
 * @author Parametric
 */
public class ConfigLoader {

    private final Configs configs = new Configs();

    private final Game game;

    public ConfigLoader(Game game) {
        this.game = game;
    }

    public void load() {
        this.configs.clear();
        try {
            HttpResponse<String> response = Unirest.get(this.game.getWorldUrl() + "/jav_config.ws")
                    .asString();

            String res = response.getBody().replace("param=", "").replace("msg=" , "");
            String[] results = res.split("\n");
            for (String result : results) {
                if (result.length() > 0) {
                    int idx = result.indexOf("=");
                    this.configs.put(result.substring(0, idx), result.substring(idx + 1));
                }
            }
        } catch (UnirestException e) {
            e.printStackTrace();
        }
    }

    public Configs getConfigs() {
        return configs;
    }

}
