package com.BinarySeint.vsAuditorias.service;

import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.BinarySeint.vsAuditorias.classes.EventoAuditoria;
import com.BinarySeint.vsAuditorias.dto.CitasEventDTO;
import com.BinarySeint.vsAuditorias.dto.AuditTimelineEventDTO;
import com.BinarySeint.vsAuditorias.repository.AuditoriaRepository;

@Service
public class AuditoriaService {

    private static final Logger log = LoggerFactory.getLogger(AuditoriaService.class);
    private final AuditoriaRepository auditoriaRepository;

    public AuditoriaService(AuditoriaRepository auditoriaRepository) {
        this.auditoriaRepository = auditoriaRepository;
    }

    @Transactional
    public void registrarDesdeAuditTimeline(AuditTimelineEventDTO dto) {
        if (dto == null || dto.getAccion() == null || dto.getUsuarioId() == null) {
            throw new IllegalArgumentException("Payload de audit.timeline inválido: faltan campos obligatorios (usuarioId/accion).");
        }

        String uniqueEventId = dto.getEventId() != null ? "AUDIT-" + dto.getEventId() : null;
        if (uniqueEventId != null && auditoriaRepository.existsByEventId(uniqueEventId)) {
            log.warn("⚠️ Evento duplicado ignorado en audit.timeline: {}", uniqueEventId);
            return;
        }

        EventoAuditoria entidad = new EventoAuditoria();
        entidad.setEventId(uniqueEventId);
        entidad.setAppointmentId(dto.getAppointmentId());
        entidad.setUsuarioId(dto.getUsuarioId());
        entidad.setAccion(dto.getAccion());
        entidad.setFechaHora(dto.getFechaHora() != null ? dto.getFechaHora() : LocalDateTime.now());
        entidad.setOrigenIp(dto.getOrigenIp() != null ? dto.getOrigenIp() : "desconocida");
        entidad.setEntidadId(dto.getEntidadId() != null ? dto.getEntidadId() : "ms-vidasalud-appointments");
        entidad.setDetalles(dto.getDetalles());

        auditoriaRepository.save(entidad);
        log.info("✅ Evento de auditoría persistido [Cita ID: {} | Acción: {}]", dto.getAppointmentId(), dto.getAccion());
    }

    @Transactional
    public void registrarDesdeAppointmentsEvents(CitasEventDTO dto) {
        if (dto == null || dto.getAppointmentId() == null || dto.getEventType() == null) {
            throw new IllegalArgumentException("Payload de appointments.events inválido: falta appointmentId o eventType.");
        }

        String uniqueEventId = dto.getEventId() != null ? "DOMAIN-" + dto.getEventId() : null;
        if (uniqueEventId != null && auditoriaRepository.existsByEventId(uniqueEventId)) {
            log.warn("⚠️ Evento de dominio duplicado ignorado en appointments.events: {}", uniqueEventId);
            return;
        }

        EventoAuditoria entidad = new EventoAuditoria();
        entidad.setEventId(uniqueEventId);
        entidad.setAppointmentId(String.valueOf(dto.getAppointmentId()));
        entidad.setUsuarioId(dto.getPacienteId() != null ? dto.getPacienteId() : "SISTEMA");
        entidad.setAccion("DOMINIO_" + dto.getEventType());
        entidad.setFechaHora(dto.getTimestamp() != null ? dto.getTimestamp() : LocalDateTime.now());
        entidad.setOrigenIp("kafka-stream");
        entidad.setEntidadId("appointments.events");
        entidad.setDetalles(String.format("Estado: %s -> %s | Prestación ID: %s | Paciente: %s",
                dto.getEstadoAnterior() != null ? dto.getEstadoAnterior() : "INICIAL",
                dto.getEstadoActual(),
                dto.getPrestacionId(),
                dto.getNombrePaciente()));

        auditoriaRepository.save(entidad);
        log.info("✅ Traza de dominio persistida desde appointments.events [Cita ID: {} | Tipo: {}]",
                dto.getAppointmentId(), dto.getEventType());
    }
}
