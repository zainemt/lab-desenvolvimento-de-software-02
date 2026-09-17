package br.edu.pucminas.matriculas.model;

import java.util.ArrayList;
import java.util.List;

/** HU10: currículo gerado para o semestre, definindo as disciplinas oferecidas. */
public class CurriculoSemestral {
    private Long id;
    private Semestre semestre;
    private PeriodoMatricula periodoMatricula;
    private List<OfertaDisciplina> ofertas = new ArrayList<>();

    public CurriculoSemestral() {}

    public CurriculoSemestral(Long id, Semestre semestre) {
        this.id = id;
        this.semestre = semestre;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Semestre getSemestre() { return semestre; }
    public void setSemestre(Semestre semestre) { this.semestre = semestre; }
    public PeriodoMatricula getPeriodoMatricula() { return periodoMatricula; }
    public void setPeriodoMatricula(PeriodoMatricula periodoMatricula) { this.periodoMatricula = periodoMatricula; }
    public List<OfertaDisciplina> getOfertas() { return ofertas; }
    public void setOfertas(List<OfertaDisciplina> ofertas) { this.ofertas = ofertas; }
}
