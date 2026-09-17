package br.edu.pucminas.matriculas.model;

public class Aluno extends Usuario {
    private String matriculaAcademica;

    public Aluno() {}

    public Aluno(Long id, String nome, String senha, String matriculaAcademica) {
        super(id, nome, senha);
        this.matriculaAcademica = matriculaAcademica;
    }

    public String getMatriculaAcademica() { return matriculaAcademica; }
    public void setMatriculaAcademica(String matriculaAcademica) { this.matriculaAcademica = matriculaAcademica; }
}
