package br.edu.pucminas.matriculas.model;

/** Representa o ator "Secretaria" das histórias de usuário. */
public class FuncionarioSecretaria extends Usuario {
    private String registro;

    public FuncionarioSecretaria() {}

    public FuncionarioSecretaria(Long id, String nome, String senha, String registro) {
        super(id, nome, senha);
        this.registro = registro;
    }

    public String getRegistro() { return registro; }
    public void setRegistro(String registro) { this.registro = registro; }
}
