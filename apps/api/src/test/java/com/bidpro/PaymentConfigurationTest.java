package com.bidpro;
import com.bidpro.payment.PaymentService;
import com.bidpro.payment.RazorpayGateway;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.core.env.StandardEnvironment;
import static org.junit.jupiter.api.Assertions.*;
class PaymentConfigurationTest {
    @Test void requestedRealProviderNeverFallsBackToLocalSimulation() {
        StandardEnvironment environment=new StandardEnvironment();environment.setActiveProfiles("local");
        RazorpayGateway gateway=new RazorpayGateway(new ObjectMapper(),environment,true,"rzp_test_incomplete","","",false,"https://api.razorpay.com/v1");
        PaymentService service=new PaymentService(null,null,null,gateway,null,null,new ObjectMapper(),environment,true,null);
        assertFalse(service.configuration().enabled());assertEquals("RAZORPAY",service.configuration().provider());
    }
}
