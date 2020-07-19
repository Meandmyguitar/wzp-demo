package com.wzp.cloud.support.tracing;

import brave.Span;
import brave.Tracer;
import brave.Tracing;
import brave.propagation.Propagation;
import brave.propagation.TraceContext;
import brave.propagation.TraceContextOrSamplingFlags;
import com.lanmaoly.util.lang.ExceptionUtils;
import org.springframework.amqp.core.MessageProperties;

import java.util.Map;

public class TracingUtils {

    public static <T> T withSpanInScope(Tracing tracing, Map<String, String> props, TraceableAction<T> action) {
        return withSpanInScope(tracing, props, Map::get, action);
    }

    public static <T> T withSpanInScope(Tracing tracing, MessageProperties props, TraceableAction<T> action) {
        return withSpanInScope(tracing, props, (c, k) -> (String) c.getHeaders().get(k), action);
    }

    public static <T, C> T withSpanInScope(Tracing tracing, C carrier, Propagation.Getter<C, String> getter, TraceableAction<T> action) {

        Tracer tracer = tracing.tracer();
        TraceContext.Extractor<C> extractor = tracing.propagation().extractor(getter);
        TraceContextOrSamplingFlags flags = extractor.extract(carrier);
        Span span = tracer.nextSpan(flags);

        span.start();
        try (Tracer.SpanInScope ignored = tracer.withSpanInScope(span)) {
            return action.run();
        } catch (Throwable e) {
            span.error(e);
            throw ExceptionUtils.throwUnchecked(e);
        } finally {
            span.finish();
        }
    }
}
