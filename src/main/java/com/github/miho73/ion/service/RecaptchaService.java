package com.github.miho73.ion.service;

import com.github.miho73.ion.dto.RecaptchaReply;
import com.google.cloud.recaptchaenterprise.v1.RecaptchaEnterpriseServiceClient;
import com.google.recaptchaenterprise.v1.Assessment;
import com.google.recaptchaenterprise.v1.CreateAssessmentRequest;
import com.google.recaptchaenterprise.v1.Event;
import com.google.recaptchaenterprise.v1.ProjectName;
import com.google.recaptchaenterprise.v1.RiskAnalysis.ClassificationReason;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

@Service
@Slf4j
public class RecaptchaService {
    @Value("${ion.recaptcha.project-id}")
    String projectId;
    @Value("${ion.recaptcha.site-key}")
    String recaptchaSiteKey;

    public RecaptchaReply performAssessment(String token, String recaptchaAction) throws IOException {
        return createAssessment(projectId, recaptchaSiteKey, token, recaptchaAction);
    }

    /**
     * Create an assessment to analyze the risk of an UI action. Assessment approach is the same for
     * both 'score' and 'checkbox' type recaptcha site keys.
     *
     * @param projectID : GCloud Project ID
     * @param recaptchaSiteKey : Site key obtained by registering a domain/app to use recaptcha
     *     services. (score/ checkbox type)
     * @param token : The token obtained from the client on passing the recaptchaSiteKey.
     * @param recaptchaAction : Action name corresponding to the token.
     */
    public RecaptchaReply createAssessment(
            String projectID, String recaptchaSiteKey, String token, String recaptchaAction)
            throws IOException {
        RecaptchaReply rr = new RecaptchaReply();

        // Initialize client that will be used to send requests. This client only needs to be created
        // once, and can be reused for multiple requests. After completing all of your requests, call
        // the `client.close()` method on the client to safely
        // clean up any remaining background resources.
        try (RecaptchaEnterpriseServiceClient client = RecaptchaEnterpriseServiceClient.create()) {

            // Set the properties of the event to be tracked.
            Event event = Event.newBuilder().setSiteKey(recaptchaSiteKey).setToken(token).build();

            // Build the assessment request.
            CreateAssessmentRequest createAssessmentRequest =
                    CreateAssessmentRequest.newBuilder()
                            .setParent(ProjectName.of(projectID).toString())
                            .setAssessment(Assessment.newBuilder().setEvent(event).build())
                            .build();

            Assessment response = client.createAssessment(createAssessmentRequest);

            // Check if the token is valid.
            if (!response.getTokenProperties().getValid()) {
                log.error(
                        "The CreateAssessment call failed because the token was: "
                        + response.getTokenProperties().getInvalidReason().name());
                rr.setOk(false);
                return rr;
            }

            // Check if the expected action was executed.
            // (If the key is checkbox type and 'action' attribute wasn't set, skip this check.)
            if (!response.getTokenProperties().getAction().equals(recaptchaAction)) {
                log.error(
                        "captcha failed: "
                                + response.getTokenProperties().getAction());
                log.error(
                        "recaptcha action mismatch: "
                                + recaptchaAction);
                rr.setOk(false);
                return rr;
            }

            rr.setOk(true);
            List<String> reasons = new Vector<>();

            // Get the reason(s) and the risk score.
            for (ClassificationReason reason : response.getRiskAnalysis().getReasonsList()) {
                reasons.add(reason.toString());
            }
            rr.setReasons(reasons);

            float recaptchaScore = response.getRiskAnalysis().getScore();
            rr.setScore(recaptchaScore);

            // Get the assessment name (id). Use this to annotate the assessment.
            String assessmentName = response.getName();
            rr.setAssessmentName(assessmentName.substring(assessmentName.lastIndexOf("/") + 1));
            return rr;
        }
    }

    /**
     * Send an assessment comment
     * @param assessmentId : Assessment id to comment
     * @param type : true when it's legal. false when it's illegal
     * @return true when success otherwise, false
     */
    public boolean addAssessmentComment(String assessmentId, boolean type) throws IOException {
        String requestUrl = "https://recaptchaenterprise.googleapis.com/v1/projects/elemention/assessments/"+assessmentId+":annotate";
        HttpClient httpclient = HttpClients.createDefault();
        HttpPost httppost = new HttpPost(requestUrl);
        List<NameValuePair> params = new ArrayList<NameValuePair>(2);
        params.add(new BasicNameValuePair("annotation", type ? "LEGITIMATE" : "FRAUDULENT"));
        httppost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));

        HttpResponse response = httpclient.execute(httppost);
        log.info("made "+(type ? "legal" : "illegal")+" comment. assessmentId="+assessmentId);
        return response.getStatusLine().getStatusCode() == 200;
    }
}