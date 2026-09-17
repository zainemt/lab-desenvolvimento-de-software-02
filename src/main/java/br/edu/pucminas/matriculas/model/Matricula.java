package br.edu.pucminas.matriculas.model;

import br.edu.pucminas.matriculas.enums.StatusMatricula;
import br.edu.pucminas.matriculas.enums.TipoOpcao;
import java.time.LocalDateTime;

/**
 * Matrícula do aluno em uma disciplina oferecida no semestre.
 * Esta granularidade permite que HU04 cancele uma matrícula específica e que ela
 * deixe de contar imediatamente na quantidade de inscritos da disciplina.
 */
public class Matricula {
    private Long id;
    private Aluno aluno;
    private OfertaDisciplina ofertaDisciplina;
    private Semestre semestre;
    private TipoOpcao tipoOpcao;
    private StatusMatricula status;
    private LocalDateTime dataHora;

    public Matricula() {}

    public Matricula(Long id, Aluno aluno, OfertaDisciplina ofertaDisciplina, Semestre semestre,
                     TipoOpcao tipoOpcao, StatusMatricula status, LocalDateTime dataHora) {
        this.id = id;
        this.aluno = aluno;
        this.ofertaDisciplina = ofertaDisciplina;
        this.semestre = semestre;
        this.tipoOpcao = tipoOpcao;
        this.status = status;
        this.dataHora = dataHora;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Aluno getAluno() { return aluno; }
    public void setAluno(Aluno aluno) { this.aluno = aluno; }
    public OfertaDisciplina getOfertaDisciplina() { return ofertaDisciplina; }
    public void setOfertaDisciplina(OfertaDisciplina ofertaDisciplina) { this.ofertaDisciplina = ofertaDisciplina; }
    public Semestre getSemestre() { return semestre; }
    public void setSemestre(Semestre semestre) { this.semestre = semestre; }
    public TipoOpcao getTipoOpcao() { return tipoOpcao; }
    public void setTipoOpcao(TipoOpcao tipoOpcao) { this.tipoOpcao = tipoOpcao; }
    public StatusMatricula getStatus() { return status; }
    public void setStatus(StatusMatricula status) { this.status = status; }
    public LocalDateTime getDataHora() { return dataHora; }
    public void setDataHora(LocalDateTime dataHora) { this.dataHora = dataHora; }
}
