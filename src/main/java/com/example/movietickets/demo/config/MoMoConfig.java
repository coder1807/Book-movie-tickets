package com.example.movietickets.demo.config;

import com.google.gson.JsonObject;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.Buffer;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class MoMoConfig {
    public static String ACCESS_KEY = "F8BBA842ECF85";
    public static String SECRET_KEY = "K951B6PE1waDMi640xX08PD3vg6EkVlz";
    public static String PARTNER_CODE = "MOMO";
    public static String REDIRECT_URL = "http://localhost:8080/api/payment/handlePayment";
    public static String IPN_URL = "https://a5a3-58-186-75-18.ngrok-free.app/callback";
    public static String ORDER_INFO = "pay with MoMo";
    public static String REQUEST_TYPE = "payWithMethod";
    public static String LANG = "vi";
    public static boolean AUTO_CAPTURE = true;
    public static String EXTRA_DATA = "";

    public static String hmacSHA256(String data, String key) throws Exception {
        Mac sha256Hmac = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        sha256Hmac.init(secretKey);
        byte[] hash = sha256Hmac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        StringBuilder hexString = new StringBuilder();
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }

    public static String sendHttpPost(String url, String jsonBody) throws Exception {
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpPost httpPost = new HttpPost(url);
            httpPost.setHeader("Content-Type", "application/json");
            httpPost.setEntity(new StringEntity(jsonBody));
            try (CloseableHttpResponse response = client.execute(httpPost)) {
                return EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
            }
        }
    }
}
