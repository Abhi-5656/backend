package com.wfm.experts.modules.wfm.employee.location.tracking.consumer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wfm.experts.modules.wfm.employee.location.tracking.dto.TrackingCloseMessage;
import com.wfm.experts.modules.wfm.employee.location.tracking.dto.TrackingPointMessage;
import com.wfm.experts.modules.wfm.employee.location.tracking.service.TrackingChunkService;
import com.wfm.experts.tenancy.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.core.Message;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class TrackingConsumer {

    private final TrackingChunkService chunkService;
    private final ObjectMapper mapper;

    @RabbitListener(queues = {
            "${tracking.rabbit.queue.prefix:tracking.points.q}1",
            "${tracking.rabbit.queue.prefix:tracking.points.q}2",
            "${tracking.rabbit.queue.prefix:tracking.points.q}3",
            "${tracking.rabbit.queue.prefix:tracking.points.q}4",
            "${tracking.rabbit.queue.prefix:tracking.points.q}5",
            "${tracking.rabbit.queue.prefix:tracking.points.q}6",
            "${tracking.rabbit.queue.prefix:tracking.points.q}7",
            "${tracking.rabbit.queue.prefix:tracking.points.q}8"
    })
    public void onMessage(Message message,
                          @Header(name = "X-Tenant-ID", required = false) String tenantId) throws Exception {
        try {
            TenantContext.setTenant(tenantId);

            JsonNode node = mapper.readTree(message.getBody());
            boolean isPoint = node.hasNonNull("lat") && node.hasNonNull("lng") && node.hasNonNull("capturedAt");
            if (isPoint) {
                TrackingPointMessage p = mapper.treeToValue(node, TrackingPointMessage.class);
                chunkService.ingestPoint(p);
            } else {
                TrackingCloseMessage c = mapper.treeToValue(node, TrackingCloseMessage.class);
                chunkService.closeSession(c);
            }
        } finally {
            TenantContext.clear();
        }
    }
}
