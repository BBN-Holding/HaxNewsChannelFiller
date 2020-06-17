package com.hax.newschannelfiller;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.json.JSONObject;

import javax.security.auth.login.LoginException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Main {

    public static void main(String[] args) {
        new Main().main();
    }

    public void main() {
        File file = new File("config.json");
        if (!file.exists()) {
            try {
                file.createNewFile();
                Files.writeString(file.toPath(),
                        new JSONObject()
                                .put("BOT_TOKEN", "")
                                .put("BBNBOT_ID", "")
                                .put("VCLOG_ID", "")
                                .put("NC_ID", "")
                                .toString(2));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        JSONObject json = null;
        try {
            json = new JSONObject(Files.readString(Paths.get(file.getPath())));
            JDA jda = JDABuilder.create(json.getString("BOT_TOKEN"), GatewayIntent.getIntents(GatewayIntent.ALL_INTENTS))
                    .addEventListeners(new Listener(json)).build();
        } catch (LoginException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
