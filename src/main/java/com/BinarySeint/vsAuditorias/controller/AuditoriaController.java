package com.BinarySeint.vsAuditorias.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.BinarySeint.vsAuditorias.classes.EventoAuditoria;
import com.BinarySeint.vsAuditorias.repository.AuditoriaRepository;

import java.util.List;

@RestController
@RequestMapping("/api/audit")
public class AuditoriaController {

    private final AuditoriaRepository auditoriaRepository;

    public AuditoriaController(AuditoriaRepository auditoriaRepository) {
        this.auditoriaRepository = auditoriaRepository;
    }

    @PostMapping("/event")
    public ResponseEntity<EventoAuditoria> registrarEventoAuditoria(@RequestBody EventoAuditoria evento) {
        System.out.println("Registrando evento de auditoría vía REST: " + evento.getAccion());
        EventoAuditoria guardado = auditoriaRepository.save(evento);
        return ResponseEntity.status(HttpStatus.CREATED).body(guardado);
    }

    @GetMapping
    public ResponseEntity<List<EventoAuditoria>> obtenerTodos() {
        return ResponseEntity.ok(auditoriaRepository.findAll());
    }

    @GetMapping("/user/{usuarioId}")
    public ResponseEntity<List<EventoAuditoria>> obtenerPorUsuario(@PathVariable String usuarioId) {
        return ResponseEntity.ok(auditoriaRepository.findByUsuarioId(usuarioId));
    }

    @GetMapping("/event/{accion}")
    public ResponseEntity<List<EventoAuditoria>> obtenerPorAccion(@PathVariable String accion) {
        return ResponseEntity.ok(auditoriaRepository.findByAccion(accion));
    }
}