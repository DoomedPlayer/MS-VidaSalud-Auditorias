package com.BinarySeint.vsAuditorias.controller;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.*;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.BinarySeint.vsAuditorias.classes.EventoAuditoria;
import com.BinarySeint.vsAuditorias.dto.AuditTimelineEventDTO;
import com.BinarySeint.vsAuditorias.repository.AuditoriaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/audit")
public class AuditoriaController {

    private static final String AUDIT_TOPIC = "audit.timeline";

    private final AuditoriaRepository auditoriaRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public AuditoriaController(AuditoriaRepository auditoriaRepository,
                               KafkaTemplate<String, String> kafkaTemplate,
                               ObjectMapper objectMapper) {
        this.auditoriaRepository = auditoriaRepository;
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }
    
    @GetMapping
    public ResponseEntity<List<EventoAuditoria>> obtenerTodos() {
        return ResponseEntity.ok(auditoriaRepository.findAll());
    }

    @GetMapping("/appointment/{appointmentId}")
    public ResponseEntity<List<EventoAuditoria>> obtenerTimelinePorCita(@PathVariable String appointmentId) {
        return ResponseEntity.ok(auditoriaRepository.findByAppointmentIdOrderByFechaHoraAsc(appointmentId));
    }

    @GetMapping("/user/{usuarioId}")
    public ResponseEntity<List<EventoAuditoria>> obtenerPorUsuario(@PathVariable String usuarioId) {
        return ResponseEntity.ok(auditoriaRepository.findByUsuarioId(usuarioId));
    }

    @GetMapping("/event/{accion}")
    public ResponseEntity<List<EventoAuditoria>> obtenerPorAccion(@PathVariable String accion) {
        return ResponseEntity.ok(auditoriaRepository.findByAccion(accion));
    }

    @GetMapping("/range")
    public ResponseEntity<List<EventoAuditoria>> obtenerPorFechas(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fin) {
        return ResponseEntity.ok(auditoriaRepository.findByFechaHoraBetween(inicio, fin));
    }

    @PostMapping("/event")
    public ResponseEntity<Void> registrarEventoDesdeFrontend(@RequestBody Map<String, Object> body) throws Exception {
        String eventId = UUID.randomUUID().toString();

        AuditTimelineEventDTO evento = new AuditTimelineEventDTO();
        evento.setEventId(eventId);
        evento.setAppointmentId(String.valueOf(body.getOrDefault("entidadId", "AUTH")));
        evento.setUsuarioId(String.valueOf(body.getOrDefault("usuarioId", "anonimo")));
        evento.setAccion(String.valueOf(body.getOrDefault("accion", "LOGIN_SUCCESS")));
        evento.setFechaHora(LocalDateTime.now());
        evento.setOrigenIp(String.valueOf(body.getOrDefault("origenIp", "Frontend React")));
        evento.setEntidadId("Auth MSAL");
        evento.setDetalles(String.valueOf(body.getOrDefault("detalles", "Inicio de sesión registrado")));

        String payloadJson = objectMapper.writeValueAsString(evento);

        // Key = eventId (UUID) para que la compactación de audit.timeline no sobreescriba logins previos
        kafkaTemplate.send(AUDIT_TOPIC, eventId, payloadJson);

        return ResponseEntity.accepted().build();
    }
}