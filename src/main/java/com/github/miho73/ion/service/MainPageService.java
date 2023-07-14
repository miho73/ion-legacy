package com.github.miho73.ion.service;

import com.github.miho73.ion.exceptions.IonException;
import com.github.miho73.ion.utils.RestResponse;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cglib.core.Local;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDate;

@Service
@Slf4j
public class MainPageService {
    LocalDate pictureDate;
    JSONObject image;
    JSONObject fallBack = new JSONObject();

    @Value("${ion.apod.api-key}")
    String APOD_KEY;

    @PostConstruct
    public void init() {
        pictureDate = LocalDate.of(2000, 1, 1);
        fallBack.put("url", "https://apod.nasa.gov/apod/image/2205/M87bh_EHT_960.jpg");
        fallBack.put("type", "image");
    }

    public JSONObject getImage() {
        if(pictureDate.equals(LocalDate.now())) {
            return image;
        }

        try {
            URL url = new URL("https://api.nasa.gov/planetary/apod?thumbs=true&api_key="+APOD_KEY);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            int status = connection.getResponseCode();
            if(status != 200) throw new IOException();
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(connection.getInputStream()));
            String inputLine;
            StringBuilder content = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                content.append(inputLine);
            }
            in.close();
            connection.disconnect();
            String res = content.toString();
            JSONObject r = (JSONObject) new JSONParser().parse(res);
            JSONObject k = new JSONObject();
            k.put("url", r.get("url"));
            k.put("type", r.get("media_type"));
            k.put("title", r.get("title"));
            image = k;
            pictureDate = LocalDate.now();
            log.info("updated APOD for "+pictureDate);
            return image;
        } catch (IOException e) {
            log.error("Failed to update APOD picture", e);
            return fallBack;
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }
}
