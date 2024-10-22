package com.example.movietickets.demo.service;
import com.paypal.api.payments.*;
import com.paypal.base.rest.APIContext;
import com.paypal.base.rest.PayPalRESTException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class PaypalService {


    private final APIContext apiContext;

    public Payment createPayment(long total,  String currency, String method, String intent, String description,  Long cancelUrl, String successUrl) throws PayPalRESTException {

   Amount amount = new Amount();
   amount.setCurrency(currency);
   amount.setTotal(String.format(Locale.forLanguageTag(currency),"%d",total));
    Transaction transaction = new Transaction();
    transaction.setDescription(description);
    transaction.setAmount(amount);

    List<Transaction> transactions = new ArrayList<>();
    transactions.add(transaction);
    Payer payer = new Payer();
    payer.setPaymentMethod(method);
    Payment payment = new Payment();
    payment.setIntent(intent);
    payment.setPayer(payer);
    payment.setTransactions(transactions);
    RedirectUrls redirectUrls = new RedirectUrls();
    redirectUrls.setCancelUrl("http://localhost:8080/purchase?scheduleId="+cancelUrl);
    redirectUrls.setReturnUrl(successUrl);
    payment.setRedirectUrls(redirectUrls);
    return payment.create(apiContext);
    }

    public Payment excutePayment(String paymentId, String payerId) throws PayPalRESTException {
        Payment payment = new Payment();
        payment.setId(paymentId);

        PaymentExecution paymentExecution = new PaymentExecution();
        paymentExecution.setPayerId(payerId);
        return payment.execute(apiContext, paymentExecution);
    }
}
