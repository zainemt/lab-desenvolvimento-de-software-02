package br.edu.pucminas.matriculas.model;

import java.time.LocalDateTime;

/** Período definido pela Secretaria para realizar/cancelar matrículas. */
public class PeriodoMatricula {
    private LocalDateTime inicio;
    private LocalDateTime fim;

    public PeriodoMatricula() {}

    public PeriodoMatricula(LocalDateTime inicio, LocalDateTime fim) {
        this.inicio = inicio;
        this.fim = fim;
    }

    public LocalDateTime getInicio() { return inicio; }
    public void setInicio(LocalDateTime inicio) { this.inicio = inicio; }
    public LocalDateTime getFim() { return fim; }
    public void setFim(LocalDateTime fim) { this.fim = fim; }
}
