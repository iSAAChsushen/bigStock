package com.bigstock.sharedComponent.jaeger;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.jaegertracing.internal.JaegerTracer;
import io.opentracing.Tracer;
import io.opentracing.noop.NoopTracerFactory;
import io.opentracing.util.GlobalTracer;

@Configuration
public class JaegerConfig {

    @Value("${opentracing.jaeger.enabled:false}")
    private boolean jaegerEnabled;

    @Value("${opentracing.jaeger.http-sender.url}")
    private String opentracingHttpSenderUrl;

	@Value("${spring.application.name}")
	private String applicationName;
  
	@Value("${opentracing.jaeger.sampler.param}")
	private float samplerParam;
	
	@Value("${opentracing.jaeger.sampler.type}")
	private String samplerType;
	
    @Bean
    public Tracer jaegerTracer() {

		if (!jaegerEnabled) {
			return NoopTracerFactory.create();
		}

        JaegerTracer tracer = new io.jaegertracing.Configuration(applicationName)
                .withSampler(new io.jaegertracing.Configuration.SamplerConfiguration().withType(samplerType).withParam(samplerParam))
                .withReporter(new io.jaegertracing.Configuration.ReporterConfiguration()
                        .withSender(new io.jaegertracing.Configuration.SenderConfiguration().withEndpoint(opentracingHttpSenderUrl)
                                ).withLogSpans(true)).getTracer();

        GlobalTracer.registerIfAbsent(tracer);

        return tracer;
    }
}
