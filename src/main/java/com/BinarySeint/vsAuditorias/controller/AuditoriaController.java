package com.BinarySeint.vsAuditorias.controller;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.BinarySeint.vsAuditorias.classes.EventoAuditoria;
import com.BinarySeint.vsAuditorias.repository.AuditoriaRepository;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/audit")
public class AuditoriaController {

    private final AuditoriaRepository auditoriaRepository;

    public AuditoriaController(AuditoriaRepository auditoriaRepository) {
        this.auditoriaRepository = auditoriaRepository;
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
}