package br.edu.pucminas.matriculas.integration;

import br.edu.pucminas.matriculas.model.Aluno;
import br.edu.pucminas.matriculas.model.Matricula;
import br.edu.pucminas.matriculas.model.Semestre;
import java.util.List;

/** HU13 — ator externo Sistema de Cobranças. */
public interface SistemaCobrancas {
    void receberInformacoesMatricula(Aluno aluno, Semestre semestre, List<Matricula> matriculas);
}
