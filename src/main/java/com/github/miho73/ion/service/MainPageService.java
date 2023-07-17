package com.github.miho73.ion.service;

import com.github.miho73.ion.exceptions.IonException;
import com.github.miho73.ion.utils.Requests;
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
        fallBack.put("exp", "What does a black hole look like? To find out, radio telescopes from around the Earth coordinated observations of black holes with the largest known event horizons on the sky.  Alone, black holes are just black, but these monster attractors are known to be surrounded by glowing gas.  This first image resolves the area around the black hole at the center of galaxy M87 on a scale below that expected for its event horizon.  Pictured, the dark central region is not the event horizon, but rather the black hole's shadow -- the central region of emitting gas darkened by the central black hole's gravity. The size and shape of the shadow is determined by bright gas near the event horizon, by strong gravitational lensing deflections, and by the black hole's spin.  In resolving this black hole's shadow, the Event Horizon Telescope (EHT) bolstered evidence that Einstein's gravity works even in extreme regions, and gave clear evidence that M87 has a central spinning black hole of about 6 billion solar masses.  Since releasing this featured image in 2019, the EHT has expanded to include more telescopes, observe more black holes, track polarized light,and is working to observe the immediately vicinity of the black hole in the center of our Milky Way Galaxy.    This week is: Black Hole Week  New EHT Results to be Announced: Next Thursday");
        fallBack.put("cpy", "");
    }

    public JSONObject getImage() {
        if(pictureDate.equals(LocalDate.now())) {
            return image;
        }

        try {
            String res = Requests.sendGetRequest("https://api.nasa.gov/planetary/apod?thumbs=true&api_key="+APOD_KEY);
            JSONObject r = (JSONObject) new JSONParser().parse(res);
            JSONObject k = new JSONObject();
            k.put("url", r.get("url"));
            k.put("type", r.get("media_type"));
            k.put("title", r.get("title"));
            k.put("exp", r.get("explanation"));
            k.put("cpy", r.getOrDefault("copyright", ""));
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
