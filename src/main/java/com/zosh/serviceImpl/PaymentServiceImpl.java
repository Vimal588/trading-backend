package com.zosh.serviceImpl;

import com.razorpay.Payment;
import com.razorpay.PaymentLink;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import com.zosh.domain.PaymentMethod;
import com.zosh.domain.PaymentOrderStaus;
import com.zosh.model.PaymentOrder;
import com.zosh.model.User;
import com.zosh.repository.PaymentOrderRepository;
import com.zosh.response.PaymentResponse;
import com.zosh.services.PaymentService;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class PaymentServiceImpl implements PaymentService {

    @Autowired
    private PaymentOrderRepository paymentOrderRepository;

    @Value("${stripe.api.key:${stipe.api.key:mock_stripe_key}}")
    private String stripeSecretKey;

    @Value("${razorpay.api.key:rzp_test_TdbxW5X1dmjr78}")
    private String apiKey;

    @Value("${razorpay.api.secret:u0O8fXQzUrRBo7g6aXaZg1VM}")
    private String apiSecretKey;
    @Override
    public PaymentOrder createOrder(User user, Long amount, PaymentMethod paymentMethod) {

        PaymentOrder paymentOrder = new PaymentOrder();
        paymentOrder.setUser(user);
        paymentOrder.setAmount(amount);
        paymentOrder.setPaymentMethod(paymentMethod);
        paymentOrder.setStatus(PaymentOrderStaus.PENDING);
        return paymentOrderRepository.save(paymentOrder);
    }

    @Override
    public PaymentOrder getPaymentOrderById(Long id) throws Exception {
        return  paymentOrderRepository.findById(id).orElseThrow(()->new Exception("Payment Order Not Found"));
    }
    @Override
    public Boolean ProccedPaymentOrder(
            PaymentOrder paymentOrder,
            String paymentId
    ) throws RazorpayException, StripeException {

        // Prevent duplicate processing of successful orders.
        if (paymentOrder.getStatus() == PaymentOrderStaus.SUCCESS) {
            return false;
        }

        // Only pending orders can be processed.
        if (paymentOrder.getStatus() != PaymentOrderStaus.PENDING) {
            return false;
        }

        // ==================== RAZORPAY ====================
        if (paymentOrder.getPaymentMethod() == PaymentMethod.RAZORPAY) {

            RazorpayClient razorpay =
                    new RazorpayClient(apiKey, apiSecretKey);

            Payment payment = razorpay.payments.fetch(paymentId);

            String status = payment.get("status");

            // Verify that Razorpay captured the payment.
            if (!"captured".equals(status)) {

                paymentOrder.setStatus(PaymentOrderStaus.FAILED);
                paymentOrderRepository.save(paymentOrder);

                return false;
            }

            // Verify the amount (Razorpay uses paise).
            Integer paidAmount = payment.get("amount");

            long expectedAmountInPaise =
                    paymentOrder.getAmount() * 100L;

            if (paidAmount == null
                    || paidAmount.longValue() != expectedAmountInPaise) {

                paymentOrder.setStatus(PaymentOrderStaus.FAILED);
                paymentOrderRepository.save(paymentOrder);

                return false;
            }

            // Verify currency.
            String currency = payment.get("currency");

            if (!"INR".equalsIgnoreCase(currency)) {

                paymentOrder.setStatus(PaymentOrderStaus.FAILED);
                paymentOrderRepository.save(paymentOrder);

                return false;
            }

            // Razorpay payment verified successfully.
            paymentOrder.setStatus(PaymentOrderStaus.SUCCESS);
            paymentOrderRepository.save(paymentOrder);

            return true;
        }

        // ==================== STRIPE ====================
        if (paymentOrder.getPaymentMethod() == PaymentMethod.STRIPE) {

            Stripe.apiKey = stripeSecretKey;

            // paymentId must be the Stripe Checkout Session ID.
            Session session = Session.retrieve(paymentId);

            // Verify that Stripe has completed the payment.
            boolean paid =
                    "paid".equals(session.getPaymentStatus());

            // Stripe amounts are expressed in the smallest currency unit.
            Long paidAmount = session.getAmountTotal();

            long expectedAmountInCents =
                    paymentOrder.getAmount() * 100L;

            boolean amountMatches =
                    paidAmount != null
                            && paidAmount == expectedAmountInCents;

            // Verify currency.
            String currency = session.getCurrency();

            boolean currencyMatches =
                    "inr".equalsIgnoreCase(currency);

            if (paid && amountMatches && currencyMatches) {

                // Stripe payment verified successfully.
                paymentOrder.setStatus(PaymentOrderStaus.SUCCESS);
                paymentOrderRepository.save(paymentOrder);

                return true;
            }

            // Payment verification failed.
            paymentOrder.setStatus(PaymentOrderStaus.FAILED);
            paymentOrderRepository.save(paymentOrder);

            return false;
        }

        // Unsupported payment method.
        return false;
    }


    @Override
    public PaymentResponse createRazorpayPaymentLink(User user, Long amount ,Long orderId)
            throws RazorpayException {

        try {

            // Convert rupees to paise
            Long amountInPaise = amount * 100;

            // Create Razorpay client
            RazorpayClient razorpay =
                    new RazorpayClient(apiKey, apiSecretKey);

            // Payment Link request
            JSONObject paymentLinkRequest = new JSONObject();

            paymentLinkRequest.put("amount", amountInPaise);
            paymentLinkRequest.put("currency", "INR");

            // Customer details
            JSONObject customer = new JSONObject();

            customer.put("name", user.getFullName());
            customer.put("email", user.getEmail());

            paymentLinkRequest.put("customer", customer);

            // Notification
            JSONObject notify = new JSONObject();

            notify.put("email", true);

            paymentLinkRequest.put("notify", notify);

            // Enable reminder
            paymentLinkRequest.put("reminder_enable", true);

            // Callback
            paymentLinkRequest.put(
                    "callback_url",
                    "http://localhost:5173/wallet?order_id="+orderId
            );

            paymentLinkRequest.put(
                    "callback_method",
                    "get"
            );

            // Create payment link
            PaymentLink payment =
                    razorpay.paymentLink.create(paymentLinkRequest);

            // Get payment link URL
            String paymentLinkUrl =
                    payment.get("short_url");

            // Create response
            PaymentResponse response =
                    new PaymentResponse();

            response.setPayment_url(paymentLinkUrl);

            return response;

        } catch (RazorpayException e) {

            System.out.println(
                    "Error creating payment Link: "
                            + e.getMessage()
            );

            throw new RazorpayException(e.getMessage());
        }
    }
    @Override
    public PaymentResponse createStripePaymentLink(User user, Long amount, Long orderId) throws StripeException {
        Stripe.apiKey = stripeSecretKey;

        SessionCreateParams params = SessionCreateParams.builder()
                .addPaymentMethodType(SessionCreateParams.PaymentMethodType.CARD)
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setSuccessUrl("http://localhost:5173/wallet?order_id="+orderId)
                .setCancelUrl("http://localhost:5173/payment/cancel")
                .addLineItem(SessionCreateParams.LineItem.builder().setQuantity(1L).setPriceData(SessionCreateParams.LineItem.PriceData.builder().setCurrency("usd").setUnitAmount(amount*100).setProductData(SessionCreateParams.LineItem.PriceData.ProductData.builder().setName("Top up wallet").build()).build()).build()).build();
        Session session = Session.create(params);
        System.out.println("session ________"+session);
        PaymentResponse res =new PaymentResponse();

        res.setPayment_url(session.getUrl());

        return res;
    }
}
