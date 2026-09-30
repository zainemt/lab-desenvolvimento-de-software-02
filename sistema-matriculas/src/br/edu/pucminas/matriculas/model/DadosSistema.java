package br.edu.pucminas.matriculas.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/** Estado persistido pelo protótipo em arquivo local. */
public class DadosSistema implements Serializable {
    private final List<Usuario> usuarios = new ArrayList<>();
    private final List<Disciplina> disciplinas = new ArrayList<>();
    private final List<Curso> cursos = new ArrayList<>();
    private final List<CurriculoSemestral> curriculos = new ArrayList<>();
    private final List<Matricula> matriculas = new ArrayList<>();

    public List<Usuario> getUsuarios() { return usuarios; }
    public List<Disciplina> getDisciplinas() { return disciplinas; }
    public List<Curso> getCursos() { return cursos; }
    public List<CurriculoSemestral> getCurriculos() { return curriculos; }
    public List<Matricula> getMatriculas() { return matriculas; }
}