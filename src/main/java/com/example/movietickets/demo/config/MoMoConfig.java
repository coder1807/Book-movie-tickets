package com.example.movietickets.demo.config;

import com.google.gson.JsonObject;
import okhttp3.*;
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
    public static String REDIRECT_URL = "/api/payment/handlePayment";
    public static String IPN_URL = "/api/payment/handlePayment";
    public static String ORDER_INFO = "Thanh toán đặt vé ";
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
//        try (CloseableHttpClient client = HttpClients.createDefault()) {
//            HttpPost httpPost = new HttpPost(url);
//            httpPost.setHeader("Content-Type", "application/json");
//            httpPost.setEntity(new StringEntity(jsonBody));
//            try (CloseableHttpResponse response = client.execute(httpPost)) {
//                return EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
//            }
//        }
        OkHttpClient client = new OkHttpClient().newBuilder().build();
        MediaType mediaType = MediaType.parse("application/json");
        RequestBody body = RequestBody.create(mediaType, jsonBody);
        Request request = new Request.Builder()
                .url("https://test-payment.momo.vn/v2/gateway/api/create")
                .method("POST", body)
                .addHeader("Content-Type", "application/json")
                .build();
        Response response = client.newCall(request).execute();
        return response.body().string();
    }
}
