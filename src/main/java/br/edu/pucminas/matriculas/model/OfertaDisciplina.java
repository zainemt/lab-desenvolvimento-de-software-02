package br.edu.pucminas.matriculas.model;

import br.edu.pucminas.matriculas.enums.StatusOferta;

/**
 * Representa uma disciplina disponível em um semestre.
 * HU03: no máximo 60 alunos.
 * HU12: ao final do período, oferta com menos de 3 alunos é cancelada.
 */
public class OfertaDisciplina {
    public static final int MAXIMO_ALUNOS = 60;
    public static final int MINIMO_ALUNOS = 3;

    private Long id;
    private Disciplina disciplina;
    private Professor professor;
    private StatusOferta status;

    public OfertaDisciplina() {}

    public OfertaDisciplina(Long id, Disciplina disciplina, Professor professor, StatusOferta status) {
        this.id = id;
        this.disciplina = disciplina;
        this.professor = professor;
        this.status = status;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Disciplina getDisciplina() { return disciplina; }
    public void setDisciplina(Disciplina disciplina) { this.disciplina = disciplina; }
    public Professor getProfessor() { return professor; }
    public void setProfessor(Professor professor) { this.professor = professor; }
    public StatusOferta getStatus() { return status; }
    public void setStatus(StatusOferta status) { this.status = status; }
}
