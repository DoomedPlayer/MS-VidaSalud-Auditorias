package com.BinarySeint.vsAuditorias.consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import com.BinarySeint.vsAuditorias.dto.CitasEventDTO;
import com.BinarySeint.vsAuditorias.dto.AuditTimelineEventDTO;
import com.BinarySeint.vsAuditorias.service.AuditoriaService;

@Component
public class AuditoriaKafkaConsumer {

    private static final Logger log = LoggerFactory.getLogger(AuditoriaKafkaConsumer.class);
    private final AuditoriaService auditoriaService;

    public AuditoriaKafkaConsumer(AuditoriaService auditoriaService) {
        this.auditoriaService = auditoriaService;
    }

    @KafkaListener(
            topics = "${kafka.topics.audit-timeline}",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consumirAuditTimeline(
            @Payload AuditTimelineEventDTO evento,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset) {

        log.info("📥 [audit.timeline] Partición: {} | Offset: {} | Cita ID: {}",
                partition, offset, evento.getAppointmentId());
        auditoriaService.registrarDesdeAuditTimeline(evento);
    }

    @KafkaListener(
            topics = "${kafka.topics.appointments-events}",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consumirAppointmentsEvents(
            @Payload CitasEventDTO evento,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset) {

        log.info("📥 [appointments.events] Partición: {} | Offset: {} | Cita ID: {}",
                partition, offset, evento.getAppointmentId());
        auditoriaService.registrarDesdeAppointmentsEvents(evento);
    }
}