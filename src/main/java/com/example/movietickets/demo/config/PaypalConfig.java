package com.example.movietickets.demo.config;

import com.paypal.base.rest.APIContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PaypalConfig {
    @Value("Aa6UfTIPyxi9B8IXIzpAfmWNxQpHan2-OoDkJYShKgIFE5PcyfVNNNMoDqZkZqRsyxFpOuIDKPSo-NcF")
    private String clientID;
    @Value("EAOtBXBRv8V_dqgaInprSkxnhyliEddkqydqXeoPiec-2XqQ0FdZIE0SnZmXy5e-1Z0qZcwezt9ioOs4")
    private String clientSecret;
    @Value("sandbox")
    private String mode;
    @Bean
    public APIContext apiContext(){
        return new APIContext(clientID, clientSecret,mode);
    }
}
