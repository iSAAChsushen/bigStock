//package com.bigstock.sharedComponent.infra;
//
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//
//import io.opentracing.Tracer;
//
//@Configuration
//public class TracingConfig {
//
//    @Bean
//    public Tracer jaegerTracer() {
//    	io.jaegertracing.Configuration.SamplerConfiguration
//        return new  io.jaegertracing.Configuration("bigstock-gateway")
//                .withSampler(samplerConfig)
//                .withReporter(reporterConfig)
//                .getTracer();
//    }
//}