package br.edu.pucminas.matriculas.model;

public class Professor extends Usuario {
    private String registro;

    public Professor() {}

    public Professor(Long id, String nome, String senha, String registro) {
        super(id, nome, senha);
        this.registro = registro;
    }

    public String getRegistro() { return registro; }
    public void setRegistro(String registro) { this.registro = registro; }
}
