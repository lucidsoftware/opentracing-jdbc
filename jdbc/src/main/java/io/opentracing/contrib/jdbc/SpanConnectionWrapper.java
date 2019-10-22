package io.opentracing.contrib.jdbc;

import com.p6spy.engine.common.ConnectionInformation;
import com.p6spy.engine.event.JdbcEventListener;
import com.p6spy.engine.wrapper.ConnectionWrapper;
import io.opentracing.Tracer;
import io.opentracing.util.GlobalTracer;
import io.opentracing.threadcontext.ContextSpan;
import java.sql.Connection;

public class SpanConnectionWrapper {

    public static SpanConnectionWrapper createDefault(JdbcPeer peer) {
        return new SpanConnectionWrapper(GlobalTracer.get(), ContextSpan.DEFAULT, peer);
    }

    private final JdbcEventListener eventListener;

    public SpanConnectionWrapper(Tracer tracer, ContextSpan contextSpan, JdbcPeer peer) {
        this.eventListener = new SpanEventListener(tracer, contextSpan, peer);
    }

    public Connection wrap(Connection delegate) {
        return ConnectionWrapper.wrap(
            delegate,
            eventListener,
            ConnectionInformation.fromTestConnection(delegate)
        );
    }

}
