package com.dn.customer;

import com.netflix.loadbalancer.IRule;
import com.netflix.loadbalancer.RandomRule;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.netflix.ribbon.RibbonClient;
import org.springframework.cloud.netflix.ribbon.RibbonClients;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
@RestController
@RibbonClients(
        @RibbonClient(value = "hello-server")
)
public class CustomerDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(CustomerDemoApplication.class, args);
    }


    @Bean
    public RestTemplate template() {
        return new RestTemplate();
    }

    @Bean
    public IRule ribbonRule() {

        //自定义规则
        return new RandomRule();
    }

}
