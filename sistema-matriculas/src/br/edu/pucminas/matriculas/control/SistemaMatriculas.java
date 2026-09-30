package br.edu.pucminas.matriculas.control;

import br.edu.pucminas.matriculas.enums.TipoOpcao;
import br.edu.pucminas.matriculas.enums.StatusMatricula;
import br.edu.pucminas.matriculas.enums.StatusOferta;
import br.edu.pucminas.matriculas.integration.SistemaCobrancas;
import br.edu.pucminas.matriculas.model.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Regras de negócio dos casos de uso descritos no README.
 */
public class SistemaMatriculas {
    private SistemaCobrancas sistemaCobrancas;
    private final DadosSistema dados;
    private final Set<Long> usuariosAutenticados = new HashSet<>();

    public SistemaMatriculas(SistemaCobrancas sistemaCobrancas) {
        this(sistemaCobrancas, new DadosSistema());
    }

    public SistemaMatriculas(SistemaCobrancas sistemaCobrancas, DadosSistema dados) {
        this.sistemaCobrancas = sistemaCobrancas;
        this.dados = Objects.requireNonNull(dados, "Os dados do sistema são obrigatórios.");
    }

    // HU01 — Realizar login
    public boolean realizarLogin(Usuario usuario, String senha) {
        if (usuario == null || senha == null) return false;
        Usuario cadastrado = localizarUsuario(usuario.getId());
        boolean valido = cadastrado != null && Objects.equals(cadastrado.getSenha(), senha);
        if (valido) usuariosAutenticados.add(cadastrado.getId());
        return valido;
    }

    public void encerrarSessao(Usuario usuario) {
        if (usuario != null) usuariosAutenticados.remove(usuario.getId());
    }

    // HU02 — Consultar disciplinas
    public List<OfertaDisciplina> consultarDisciplinas(Aluno aluno, CurriculoSemestral curriculo) {
        exigirAutenticado(aluno);
        if (curriculo == null) throw new IllegalArgumentException("Currículo não informado.");
        List<OfertaDisciplina> disponiveis = new ArrayList<>();
        for (OfertaDisciplina oferta : curriculo.getOfertas()) {
            if (oferta.getStatus() == StatusOferta.DISPONIVEL
                    && contarMatriculasAtivas(oferta, curriculo.getSemestre()) < OfertaDisciplina.MAXIMO_ALUNOS) {
                disponiveis.add(oferta);
            }
        }
        return disponiveis;
    }

    // HU03 — Realizar matrícula
    public Matricula realizarMatricula(Aluno aluno, OfertaDisciplina oferta, Semestre semestre,
                                       TipoOpcao tipoOpcao, PeriodoMatricula periodo) {
        exigirAutenticado(aluno);
        if (oferta == null || semestre == null || tipoOpcao == null) {
            throw new IllegalArgumentException("Oferta, semestre e tipo de opção são obrigatórios.");
        }
        CurriculoSemestral curriculo = localizarCurriculo(semestre);
        if (curriculo == null || !curriculo.getOfertas().contains(oferta)) {
            throw new IllegalArgumentException("A disciplina não está ofertada neste semestre.");
        }
        exigirPeriodoConfigurado(periodo, curriculo);
        if (oferta.getStatus() != StatusOferta.DISPONIVEL) {
            throw new IllegalArgumentException("A oferta não está disponível para matrícula.");
        }
        List<Matricula> doAluno = consultarMinhasMatriculas(aluno, semestre);
        for (Matricula matricula : doAluno) {
            if (mesmaOferta(matricula.getOfertaDisciplina(), oferta)) {
                throw new IllegalArgumentException("O aluno já está matriculado nesta disciplina.");
            }
        }
        long quantidade = doAluno.stream().filter(m -> m.getTipoOpcao() == tipoOpcao).count();
        int limite = tipoOpcao == TipoOpcao.PRIMEIRA_OPCAO ? 4 : 2;
        if (quantidade >= limite) {
            throw new IllegalArgumentException("Limite de " + limite + " disciplina(s) para esta opção atingido.");
        }
        if (contarMatriculasAtivas(oferta, semestre) >= OfertaDisciplina.MAXIMO_ALUNOS) {
            oferta.setStatus(StatusOferta.LOTADA);
            throw new IllegalArgumentException("A disciplina atingiu o limite de 60 alunos.");
        }

        Matricula matricula = new Matricula(proximoIdMatricula(), aluno, oferta, semestre,
                tipoOpcao, StatusMatricula.ATIVA, LocalDateTime.now());
        dados.getMatriculas().add(matricula);
        if (contarMatriculasAtivas(oferta, semestre) == OfertaDisciplina.MAXIMO_ALUNOS) {
            oferta.setStatus(StatusOferta.LOTADA);
        }
        if (sistemaCobrancas != null) {
            sistemaCobrancas.receberInformacoesMatricula(aluno, semestre, consultarMinhasMatriculas(aluno, semestre));
        }
        return matricula;
    }

    // HU04 — Cancelar matrícula
    public void cancelarMatricula(Aluno aluno, Matricula matricula, PeriodoMatricula periodo) {
        exigirAutenticado(aluno);
        if (matricula == null || matricula.getAluno() == null
                || !Objects.equals(matricula.getAluno().getId(), aluno.getId())
                || !dados.getMatriculas().contains(matricula)
                || matricula.getStatus() != StatusMatricula.ATIVA) {
            throw new IllegalArgumentException("Matrícula ativa do aluno não encontrada.");
        }
        CurriculoSemestral curriculo = localizarCurriculo(matricula.getSemestre());
        if (curriculo == null) throw new IllegalArgumentException("Currículo da matrícula não encontrado.");
        exigirPeriodoConfigurado(periodo, curriculo);
        matricula.setStatus(StatusMatricula.CANCELADA);
        if (matricula.getOfertaDisciplina().getStatus() == StatusOferta.LOTADA) {
            matricula.getOfertaDisciplina().setStatus(StatusOferta.DISPONIVEL);
        }
    }

    // HU05 — Consultar minhas matrículas
    public List<Matricula> consultarMinhasMatriculas(Aluno aluno, Semestre semestre) {
        exigirAutenticado(aluno);
        List<Matricula> resultado = new ArrayList<>();
        for (Matricula matricula : dados.getMatriculas()) {
            if (matricula.getStatus() == StatusMatricula.ATIVA
                    && matricula.getAluno() != null
                    && Objects.equals(matricula.getAluno().getId(), aluno.getId())
                    && mesmoSemestre(matricula.getSemestre(), semestre)) {
                resultado.add(matricula);
            }
        }
        return resultado;
    }

    // HU06 — Consultar alunos matriculados
    public List<Aluno> consultarAlunosMatriculados(Professor professor, OfertaDisciplina oferta) {
        exigirAutenticado(professor);
        if (oferta == null || oferta.getProfessor() == null
                || !Objects.equals(oferta.getProfessor().getId(), professor.getId())) {
            throw new IllegalArgumentException("O professor só pode consultar suas próprias disciplinas.");
        }
        List<Aluno> alunos = new ArrayList<>();
        for (Matricula matricula : dados.getMatriculas()) {
            if (matricula.getStatus() == StatusMatricula.ATIVA
                    && mesmaOferta(matricula.getOfertaDisciplina(), oferta)
                    && !alunos.contains(matricula.getAluno())) {
                alunos.add(matricula.getAluno());
            }
        }
        return alunos;
    }

    // HU07 — Gerenciar alunos: cadastrar e manter dados
    public void cadastrarAluno(FuncionarioSecretaria funcionario, Aluno aluno) {
        exigirSecretaria(funcionario);
        validarNovoUsuario(aluno);
        dados.getUsuarios().add(aluno);
    }

    public void atualizarAluno(FuncionarioSecretaria funcionario, Aluno aluno) {
        exigirSecretaria(funcionario);
        Aluno atual = localizarAluno(aluno == null ? null : aluno.getId());
        if (atual == null) throw new IllegalArgumentException("Aluno não encontrado.");
        atual.setNome(aluno.getNome());
        atual.setSenha(aluno.getSenha());
        atual.setMatriculaAcademica(aluno.getMatriculaAcademica());
        atual.setCurso(aluno.getCurso());
    }

    // HU08 — Gerenciar professores: cadastrar e manter dados
    public void cadastrarProfessor(FuncionarioSecretaria funcionario, Professor professor) {
        exigirSecretaria(funcionario);
        validarNovoUsuario(professor);
        dados.getUsuarios().add(professor);
    }

    public void atualizarProfessor(FuncionarioSecretaria funcionario, Professor professor) {
        exigirSecretaria(funcionario);
        Professor atual = localizarProfessor(professor == null ? null : professor.getId());
        if (atual == null) throw new IllegalArgumentException("Professor não encontrado.");
        atual.setNome(professor.getNome());
        atual.setSenha(professor.getSenha());
        atual.setRegistro(professor.getRegistro());
    }

    // HU09 — Gerenciar disciplinas: cadastrar e manter informações
    public void cadastrarDisciplina(FuncionarioSecretaria funcionario, Disciplina disciplina) {
        exigirSecretaria(funcionario);
        validarDisciplina(disciplina);
        if (localizarDisciplina(disciplina.getId()) != null) throw new IllegalArgumentException("ID de disciplina já cadastrado.");
        dados.getDisciplinas().add(disciplina);
    }

    public void atualizarDisciplina(FuncionarioSecretaria funcionario, Disciplina disciplina) {
        exigirSecretaria(funcionario);
        validarDisciplina(disciplina);
        Disciplina atual = localizarDisciplina(disciplina.getId());
        if (atual == null) throw new IllegalArgumentException("Disciplina não encontrada.");
        atual.setCodigo(disciplina.getCodigo());
        atual.setNome(disciplina.getNome());
    }

    // HU10 — Gerar currículo do semestre
    public CurriculoSemestral gerarCurriculoDoSemestre(FuncionarioSecretaria funcionario, Semestre semestre) {
        exigirSecretaria(funcionario);
        validarSemestre(semestre);
        CurriculoSemestral existente = localizarCurriculo(semestre);
        if (existente != null) return existente;
        CurriculoSemestral curriculo = new CurriculoSemestral(proximoIdCurriculo(), semestre);
        dados.getCurriculos().add(curriculo);
        return curriculo;
    }

    // HU11 — Gerenciar período de matrículas
    public void definirPeriodoMatriculas(FuncionarioSecretaria funcionario, CurriculoSemestral curriculo,
                                         PeriodoMatricula periodo) {
        exigirSecretaria(funcionario);
        if (curriculo == null || !dados.getCurriculos().contains(curriculo) || periodo == null
                || periodo.getInicio() == null || periodo.getFim() == null
                || !periodo.getFim().isAfter(periodo.getInicio())) {
            throw new IllegalArgumentException("Informe um currículo e um período válido.");
        }
        curriculo.setPeriodoMatricula(periodo);
    }

    // HU12 — Encerrar disciplinas com poucas inscrições
    public void encerrarDisciplinasComPoucasInscricoes(FuncionarioSecretaria funcionario,
                                                        CurriculoSemestral curriculo) {
        exigirSecretaria(funcionario);
        if (curriculo == null || !dados.getCurriculos().contains(curriculo)) {
            throw new IllegalArgumentException("Currículo não encontrado.");
        }
        finalizarOfertas(curriculo);
    }

    public boolean encerrarPeriodosExpirados() {
        LocalDateTime agora = LocalDateTime.now();
        boolean alterado = false;
        for (CurriculoSemestral curriculo : dados.getCurriculos()) {
            PeriodoMatricula periodo = curriculo.getPeriodoMatricula();
            if (periodo != null && periodo.getFim() != null && !agora.isBefore(periodo.getFim())) {
                alterado |= finalizarOfertas(curriculo);
            }
        }
        return alterado;
    }

    private boolean finalizarOfertas(CurriculoSemestral curriculo) {
        boolean alterado = false;
        for (OfertaDisciplina oferta : curriculo.getOfertas()) {
            long inscritos = contarMatriculasAtivas(oferta, curriculo.getSemestre());
            StatusOferta statusFinal = inscritos < OfertaDisciplina.MINIMO_ALUNOS
                    ? StatusOferta.CANCELADA
                    : inscritos >= OfertaDisciplina.MAXIMO_ALUNOS ? StatusOferta.LOTADA : StatusOferta.DISPONIVEL;
            if (oferta.getStatus() != statusFinal) {
                oferta.setStatus(statusFinal);
                alterado = true;
            }
        }
        return alterado;
    }

    public Curso cadastrarCurso(FuncionarioSecretaria funcionario, Curso curso) {
        exigirSecretaria(funcionario);
        if (curso == null || curso.getNome() == null || curso.getNome().trim().isEmpty() || curso.getCreditos() <= 0) {
            throw new IllegalArgumentException("Informe nome e quantidade de créditos válidos para o curso.");
        }
        if (curso.getId() == null) curso.setId(proximoIdCurso());
        else if (dados.getCursos().stream().anyMatch(c -> Objects.equals(c.getId(), curso.getId()))) {
            throw new IllegalArgumentException("ID de curso já cadastrado.");
        }
        dados.getCursos().add(curso);
        return curso;
    }

    public void atualizarCurso(FuncionarioSecretaria funcionario, Curso curso) {
        exigirSecretaria(funcionario);
        if (curso == null || curso.getNome() == null || curso.getNome().trim().isEmpty() || curso.getCreditos() <= 0) {
            throw new IllegalArgumentException("Informe nome e quantidade de créditos válidos para o curso.");
        }
        Curso atual = null;
        for (Curso cadastrado : dados.getCursos()) {
            if (Objects.equals(cadastrado.getId(), curso.getId())) atual = cadastrado;
        }
        if (atual == null) throw new IllegalArgumentException("Curso não encontrado.");
        atual.setNome(curso.getNome());
        atual.setCreditos(curso.getCreditos());
    }

    public void vincularDisciplinaAoCurso(FuncionarioSecretaria funcionario, Curso curso, Disciplina disciplina) {
        exigirSecretaria(funcionario);
        if (curso == null || disciplina == null || !dados.getCursos().contains(curso)
                || !dados.getDisciplinas().contains(disciplina)) {
            throw new IllegalArgumentException("Curso ou disciplina não cadastrado.");
        }
        if (!curso.getDisciplinas().contains(disciplina)) curso.getDisciplinas().add(disciplina);
    }

    public OfertaDisciplina adicionarOferta(FuncionarioSecretaria funcionario, CurriculoSemestral curriculo,
                                             Disciplina disciplina, Professor professor) {
        exigirSecretaria(funcionario);
        if (curriculo == null || !dados.getCurriculos().contains(curriculo)
                || localizarDisciplina(disciplina == null ? null : disciplina.getId()) == null
                || localizarProfessor(professor == null ? null : professor.getId()) == null) {
            throw new IllegalArgumentException("Currículo, disciplina e professor devem estar cadastrados.");
        }
        for (OfertaDisciplina oferta : curriculo.getOfertas()) {
            if (oferta.getDisciplina().getId().equals(disciplina.getId())) {
                throw new IllegalArgumentException("Disciplina já incluída neste currículo.");
            }
        }
        OfertaDisciplina oferta = new OfertaDisciplina(proximoIdOferta(), disciplina, professor, StatusOferta.DISPONIVEL);
        curriculo.getOfertas().add(oferta);
        return oferta;
    }

    public List<Usuario> getUsuarios() { return dados.getUsuarios(); }
    public List<Disciplina> getDisciplinas() { return dados.getDisciplinas(); }
    public List<Curso> getCursos() { return dados.getCursos(); }
    public List<CurriculoSemestral> getCurriculos() { return dados.getCurriculos(); }
    public DadosSistema getDadosSistema() { return dados; }

    private void exigirPeriodoAberto(PeriodoMatricula periodo) {
        LocalDateTime agora = LocalDateTime.now();
        if (periodo == null || periodo.getInicio() == null || periodo.getFim() == null
                || agora.isBefore(periodo.getInicio()) || agora.isAfter(periodo.getFim())) {
            throw new IllegalArgumentException("O período de matrículas está fechado.");
        }
    }

    private void exigirPeriodoConfigurado(PeriodoMatricula periodo, CurriculoSemestral curriculo) {
        PeriodoMatricula configurado = curriculo.getPeriodoMatricula();
        if (periodo == null || configurado == null
                || !Objects.equals(periodo.getInicio(), configurado.getInicio())
                || !Objects.equals(periodo.getFim(), configurado.getFim())) {
            throw new IllegalArgumentException("Use o período de matrículas definido pela Secretaria.");
        }
        exigirPeriodoAberto(configurado);
    }

    private void exigirAutenticado(Usuario usuario) {
        if (usuario == null || usuario.getId() == null || !usuariosAutenticados.contains(usuario.getId())
            || localizarUsuario(usuario.getId()) == null
            || !localizarUsuario(usuario.getId()).getClass().equals(usuario.getClass())) {
            throw new IllegalStateException("É necessário realizar login para continuar.");
        }
    }

    private void exigirSecretaria(FuncionarioSecretaria funcionario) {
        exigirAutenticado(funcionario);
    }

    private void validarNovoUsuario(Usuario usuario) {
        if (usuario == null || usuario.getId() == null || usuario.getNome() == null || usuario.getNome().trim().isEmpty()
                || usuario.getSenha() == null || usuario.getSenha().isEmpty()) {
            throw new IllegalArgumentException("Informe ID, nome e senha para o usuário.");
        }
        if (localizarUsuario(usuario.getId()) != null) throw new IllegalArgumentException("ID de usuário já cadastrado.");
    }

    private void validarDisciplina(Disciplina disciplina) {
        if (disciplina == null || disciplina.getId() == null || disciplina.getCodigo() == null
                || disciplina.getCodigo().trim().isEmpty() || disciplina.getNome() == null
                || disciplina.getNome().trim().isEmpty()) {
            throw new IllegalArgumentException("Informe ID, código e nome para a disciplina.");
        }
    }

    private void validarSemestre(Semestre semestre) {
        if (semestre == null || semestre.getAno() < 2000 || semestre.getPeriodo() < 1 || semestre.getPeriodo() > 2) {
            throw new IllegalArgumentException("Semestre inválido.");
        }
    }

    private Usuario localizarUsuario(Long id) {
        for (Usuario usuario : dados.getUsuarios()) if (Objects.equals(usuario.getId(), id)) return usuario;
        return null;
    }

    private Aluno localizarAluno(Long id) {
        Usuario usuario = localizarUsuario(id);
        return usuario instanceof Aluno ? (Aluno) usuario : null;
    }

    private Professor localizarProfessor(Long id) {
        Usuario usuario = localizarUsuario(id);
        return usuario instanceof Professor ? (Professor) usuario : null;
    }

    private Disciplina localizarDisciplina(Long id) {
        for (Disciplina disciplina : dados.getDisciplinas()) if (Objects.equals(disciplina.getId(), id)) return disciplina;
        return null;
    }

    private CurriculoSemestral localizarCurriculo(Semestre semestre) {
        for (CurriculoSemestral curriculo : dados.getCurriculos()) {
            if (mesmoSemestre(curriculo.getSemestre(), semestre)) return curriculo;
        }
        return null;
    }

    private boolean mesmoSemestre(Semestre primeiro, Semestre segundo) {
        return primeiro != null && segundo != null && primeiro.getAno() == segundo.getAno()
                && primeiro.getPeriodo() == segundo.getPeriodo();
    }

    private boolean mesmaOferta(OfertaDisciplina primeira, OfertaDisciplina segunda) {
        return primeira != null && segunda != null && Objects.equals(primeira.getId(), segunda.getId());
    }

    private long contarMatriculasAtivas(OfertaDisciplina oferta, Semestre semestre) {
        return dados.getMatriculas().stream().filter(m -> m.getStatus() == StatusMatricula.ATIVA
                && mesmaOferta(m.getOfertaDisciplina(), oferta) && mesmoSemestre(m.getSemestre(), semestre)).count();
    }

    private long proximoIdMatricula() {
        return dados.getMatriculas().stream().map(Matricula::getId).filter(Objects::nonNull).mapToLong(Long::longValue).max().orElse(0) + 1;
    }

    private long proximoIdCurriculo() {
        return dados.getCurriculos().stream().map(CurriculoSemestral::getId).filter(Objects::nonNull).mapToLong(Long::longValue).max().orElse(0) + 1;
    }

    private long proximoIdCurso() {
        return dados.getCursos().stream().map(Curso::getId).filter(Objects::nonNull).mapToLong(Long::longValue).max().orElse(0) + 1;
    }

    private long proximoIdOferta() {
        return dados.getCurriculos().stream().flatMap(c -> c.getOfertas().stream()).map(OfertaDisciplina::getId)
                .filter(Objects::nonNull).mapToLong(Long::longValue).max().orElse(0) + 1;
    }

    // HU13 é disparada por HU03 através desta dependência externa.
    public SistemaCobrancas getSistemaCobrancas() { return sistemaCobrancas; }
    public void setSistemaCobrancas(SistemaCobrancas sistemaCobrancas) { this.sistemaCobrancas = sistemaCobrancas; }
}
