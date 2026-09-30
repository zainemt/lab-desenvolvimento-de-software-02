package br.edu.pucminas.matriculas.model;

public class Aluno extends Usuario {
    private String matriculaAcademica;
    private Curso curso;

    public Aluno() {}

    public Aluno(Long id, String nome, String senha, String matriculaAcademica) {
        super(id, nome, senha);
        this.matriculaAcademica = matriculaAcademica;
    }

    public String getMatriculaAcademica() { return matriculaAcademica; }
    public void setMatriculaAcademica(String matriculaAcademica) { this.matriculaAcademica = matriculaAcademica; }
    public Curso getCurso() { return curso; }
    public void setCurso(Curso curso) { this.curso = curso; }
}
