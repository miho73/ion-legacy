package com.github.miho73.ion.dto;

import jakarta.persistence.Entity;
import lombok.Data;

import java.util.List;

@Data
public class RecaptchaReply {
    float score;
    List<String> reasons;
    String assessmentName;
    boolean ok;
}
