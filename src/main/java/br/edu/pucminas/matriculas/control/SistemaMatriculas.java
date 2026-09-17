package br.edu.pucminas.matriculas.control;

import br.edu.pucminas.matriculas.enums.TipoOpcao;
import br.edu.pucminas.matriculas.integration.SistemaCobrancas;
import br.edu.pucminas.matriculas.model.*;
import java.util.List;

/**
 * Classe de controle que espelha os casos de uso/HUs do README.
 * Lab01S02: os métodos são stubs; a lógica funcional será implementada na Sprint 3.
 */
public class SistemaMatriculas {
    private SistemaCobrancas sistemaCobrancas;

    public SistemaMatriculas(SistemaCobrancas sistemaCobrancas) {
        this.sistemaCobrancas = sistemaCobrancas;
    }

    // HU01 — Realizar login
    public boolean realizarLogin(Usuario usuario, String senha) {
        throw new UnsupportedOperationException("Stub Lab01S02 - HU01: realizarLogin");
    }

    // HU02 — Consultar disciplinas
    public List<OfertaDisciplina> consultarDisciplinas(Aluno aluno, CurriculoSemestral curriculo) {
        throw new UnsupportedOperationException("Stub Lab01S02 - HU02: consultarDisciplinas");
    }

    // HU03 — Realizar matrícula
    public Matricula realizarMatricula(Aluno aluno, OfertaDisciplina oferta, Semestre semestre,
                                       TipoOpcao tipoOpcao, PeriodoMatricula periodo) {
        throw new UnsupportedOperationException("Stub Lab01S02 - HU03: realizarMatricula");
    }

    // HU04 — Cancelar matrícula
    public void cancelarMatricula(Aluno aluno, Matricula matricula, PeriodoMatricula periodo) {
        throw new UnsupportedOperationException("Stub Lab01S02 - HU04: cancelarMatricula");
    }

    // HU05 — Consultar minhas matrículas
    public List<Matricula> consultarMinhasMatriculas(Aluno aluno, Semestre semestre) {
        throw new UnsupportedOperationException("Stub Lab01S02 - HU05: consultarMinhasMatriculas");
    }

    // HU06 — Consultar alunos matriculados
    public List<Aluno> consultarAlunosMatriculados(Professor professor, OfertaDisciplina oferta) {
        throw new UnsupportedOperationException("Stub Lab01S02 - HU06: consultarAlunosMatriculados");
    }

    // HU07 — Gerenciar alunos: cadastrar e manter dados
    public void cadastrarAluno(FuncionarioSecretaria funcionario, Aluno aluno) {
        throw new UnsupportedOperationException("Stub Lab01S02 - HU07: cadastrarAluno");
    }

    public void atualizarAluno(FuncionarioSecretaria funcionario, Aluno aluno) {
        throw new UnsupportedOperationException("Stub Lab01S02 - HU07: atualizarAluno");
    }

    // HU08 — Gerenciar professores: cadastrar e manter dados
    public void cadastrarProfessor(FuncionarioSecretaria funcionario, Professor professor) {
        throw new UnsupportedOperationException("Stub Lab01S02 - HU08: cadastrarProfessor");
    }

    public void atualizarProfessor(FuncionarioSecretaria funcionario, Professor professor) {
        throw new UnsupportedOperationException("Stub Lab01S02 - HU08: atualizarProfessor");
    }

    // HU09 — Gerenciar disciplinas: cadastrar e manter informações
    public void cadastrarDisciplina(FuncionarioSecretaria funcionario, Disciplina disciplina) {
        throw new UnsupportedOperationException("Stub Lab01S02 - HU09: cadastrarDisciplina");
    }

    public void atualizarDisciplina(FuncionarioSecretaria funcionario, Disciplina disciplina) {
        throw new UnsupportedOperationException("Stub Lab01S02 - HU09: atualizarDisciplina");
    }

    // HU10 — Gerar currículo do semestre
    public CurriculoSemestral gerarCurriculoDoSemestre(FuncionarioSecretaria funcionario, Semestre semestre) {
        throw new UnsupportedOperationException("Stub Lab01S02 - HU10: gerarCurriculoDoSemestre");
    }

    // HU11 — Gerenciar período de matrículas
    public void definirPeriodoMatriculas(FuncionarioSecretaria funcionario, CurriculoSemestral curriculo,
                                         PeriodoMatricula periodo) {
        throw new UnsupportedOperationException("Stub Lab01S02 - HU11: definirPeriodoMatriculas");
    }

    // HU12 — Encerrar disciplinas com poucas inscrições
    public void encerrarDisciplinasComPoucasInscricoes(FuncionarioSecretaria funcionario,
                                                        CurriculoSemestral curriculo) {
        throw new UnsupportedOperationException("Stub Lab01S02 - HU12: encerrarDisciplinasComPoucasInscricoes");
    }

    // HU13 é disparada por HU03 através desta dependência externa.
    public SistemaCobrancas getSistemaCobrancas() { return sistemaCobrancas; }
    public void setSistemaCobrancas(SistemaCobrancas sistemaCobrancas) { this.sistemaCobrancas = sistemaCobrancas; }
}
