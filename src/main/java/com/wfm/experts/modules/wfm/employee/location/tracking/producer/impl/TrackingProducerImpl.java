package com.wfm.experts.modules.wfm.employee.location.tracking.producer.impl;

import com.wfm.experts.modules.wfm.employee.location.tracking.dto.TrackingCloseMessage;
import com.wfm.experts.modules.wfm.employee.location.tracking.dto.TrackingPointMessage;
import com.wfm.experts.modules.wfm.employee.location.tracking.producer.TrackingProducer;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class TrackingProducerImpl implements TrackingProducer {

    private final RabbitTemplate rabbitTemplate;
    private final String exchange;
    private final String queuePrefix;
    private final int shards;

    public TrackingProducerImpl(
            RabbitTemplate rabbitTemplate,
            @Value("${tracking.rabbit.exchange:tracking.points}") String exchange,
            @Value("${tracking.rabbit.queue.prefix:tracking.points.q}") String queuePrefix,
            @Value("${tracking.rabbit.shards:8}") int shards) {
        this.rabbitTemplate = rabbitTemplate;
        this.exchange = exchange;
        this.queuePrefix = queuePrefix;
        this.shards = shards;
    }

    private String routingKey(Long sessionId) {
        int idx = (int)((sessionId % shards) + 1);
        return queuePrefix + idx;
    }

    @Override
    public void publishPoint(TrackingPointMessage msg) {
        rabbitTemplate.convertAndSend(exchange, routingKey(msg.getSessionId()), msg, m -> {
            m.getMessageProperties().setHeader("X-Tenant-ID", msg.getTenantId());
            return m;
        });
    }

    @Override
    public void publishClose(TrackingCloseMessage msg) {
        rabbitTemplate.convertAndSend(exchange, routingKey(msg.getSessionId()), msg, m -> {
            m.getMessageProperties().setHeader("X-Tenant-ID", msg.getTenantId());
            return m;
        });
    }
}
