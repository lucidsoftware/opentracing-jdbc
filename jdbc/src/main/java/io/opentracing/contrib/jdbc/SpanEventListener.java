package io.opentracing.contrib.jdbc;

import com.p6spy.engine.common.PreparedStatementInformation;
import com.p6spy.engine.common.StatementInformation;
import com.p6spy.engine.event.JdbcEventListener;
import io.opentracing.Span;
import io.opentracing.Tracer;
import io.opentracing.tag.Tags;
import io.opentracing.threadcontext.ContextSpan;
import java.sql.Driver;
import java.sql.SQLException;
import java.time.Instant;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

public class SpanEventListener extends JdbcEventListener {

    private final ContextSpan spanContext;
    private final Tracer tracer;
    private final JdbcPeer peer;

    public SpanEventListener(Tracer tracer, ContextSpan spanContext, JdbcPeer peer) {
        this.spanContext = spanContext;
        this.tracer = tracer;
        this.peer = peer;
    }

    @Override
    public void onAfterExecute(StatementInformation statementInformation, long timeElapsedNanos, String sql, SQLException e) {
        onAfterExecute("execute", statementInformation, timeElapsedNanos, e);
    }

    @Override
    public void onAfterExecute(PreparedStatementInformation statementInformation, long timeElapsedNanos, SQLException e) {
        onAfterExecute("execute", statementInformation, timeElapsedNanos, e);
    }

    @Override
    public void onAfterExecuteUpdate(StatementInformation statementInformation, long timeElapsedNanos, String sql, int rowCount, SQLException e) {
        onAfterExecute("update", statementInformation, timeElapsedNanos, e);
    }

    @Override
    public void onAfterExecuteUpdate(PreparedStatementInformation statementInformation, long timeElapsedNanos, int rowCount, SQLException e) {
        onAfterExecute("update", statementInformation, timeElapsedNanos, e);
    }

    @Override
    public void onAfterExecuteQuery(StatementInformation statementInformation, long timeElapsedNanos, String sql, SQLException e) {
        onAfterExecute("query", statementInformation, timeElapsedNanos, e);
    }

    @Override
    public void onAfterExecuteQuery(PreparedStatementInformation statementInformation, long timeElapsedNanos, SQLException e) {
        onAfterExecute("query", statementInformation, timeElapsedNanos, e);
    }

    private void onAfterExecute(String type, StatementInformation statementInformation, long timeElapsedNanos, SQLException e) {
        Instant end = Instant.now();
        Instant start = end.minusNanos(timeElapsedNanos);
        String sql = statementInformation.getSql();
        Span span = tracer.buildSpan(String.format("SQL %s %s", type, new Scanner(sql).next()))
            .asChildOf(spanContext.get())
            .withStartTimestamp(TimeUnit.SECONDS.toMicros(start.getEpochSecond()) + TimeUnit.NANOSECONDS.toMicros(start.getNano()))
            .start();
        Tags.SPAN_KIND.set(span, Tags.SPAN_KIND_CLIENT);
        if (peer.name != null) {
            Tags.PEER_SERVICE.set(span, peer.name);
        }
        if (peer.ipv4 != null) {
            Tags.PEER_HOST_IPV4.set(span, peer.ipv4);
        }
        if (peer.ipv6 != null) {
            Tags.PEER_HOST_IPV6.set(span, peer.ipv6);
        }
        if (peer.port != null) {
            Tags.PEER_PORT.set(span, peer.port);
        }
        Driver driver = statementInformation.getConnectionInformation().getDriver();
        if (driver != null) {
            span.setTag("jdbc.driver", driver.getClass().getName());
        }
        span.setTag("jdbc.query", sql);
        if (e != null) {
            Tags.ERROR.set(span, true);
        }
        span.finish(TimeUnit.SECONDS.toMicros(end.getEpochSecond()) + TimeUnit.NANOSECONDS.toMicros(end.getNano()));
    }

}
