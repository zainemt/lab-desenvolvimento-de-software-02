package br.edu.pucminas.matriculas;

import br.edu.pucminas.matriculas.control.SistemaMatriculas;
import br.edu.pucminas.matriculas.enums.TipoOpcao;
import br.edu.pucminas.matriculas.enums.StatusMatricula;
import br.edu.pucminas.matriculas.integration.SistemaCobrancas;
import br.edu.pucminas.matriculas.model.*;
import br.edu.pucminas.matriculas.persistence.ArquivoDadosSistema;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Scanner;

/** Interface de linha de comando do Sistema de Matrículas. */
public class App {
    private static final Scanner ENTRADA = new Scanner(System.in);
    private static final DateTimeFormatter FORMATO_DATA = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static ArquivoDadosSistema arquivo;
    private static SistemaMatriculas sistema;

    public static void main(String[] args) {
        arquivo = new ArquivoDadosSistema(System.getProperty("matriculas.arquivo", "sistema-matriculas/dados/sistema.dat"));
        DadosSistema dados;
        try {
            dados = arquivo.carregar();
        } catch (IOException erro) {
            System.out.println("Falha ao carregar os dados: " + erro.getMessage());
            return;
        }

        SistemaCobrancas cobrancas = (aluno, semestre, matriculas) -> System.out.println(
                "[Cobranças] Aluno " + aluno.getNome() + " notificado para " + formatarSemestre(semestre)
                        + " com " + matriculas.size() + " disciplina(s).");
        sistema = new SistemaMatriculas(cobrancas, dados);
        if (dados.getUsuarios().isEmpty()) {
            criarDadosIniciais(dados);
            salvar();
        }
        if (sistema.encerrarPeriodosExpirados()) salvar();
        executarLogin();
    }

    private static void executarLogin() {
        while (true) {
            limparTela();
            titulo("SISTEMA DE MATRÍCULAS");
            System.out.println("Entre com seu ID e senha. Digite 0 no ID para sair.\n");
            long id = lerLong("ID: ");
            if (id == 0) return;
            String senha = lerTexto("Senha: ");
            Usuario usuario = buscarUsuario(id);
            if (usuario == null || !sistema.realizarLogin(usuario, senha)) {
                System.out.println("\nID ou senha inválidos.");
                pausar();
                continue;
            }
            if (usuario instanceof Aluno) menuAluno((Aluno) usuario);
            else if (usuario instanceof Professor) menuProfessor((Professor) usuario);
            else if (usuario instanceof FuncionarioSecretaria) menuSecretaria((FuncionarioSecretaria) usuario);
            sistema.encerrarSessao(usuario);
        }
    }

    private static void menuAluno(Aluno aluno) {
        boolean continuar = true;
        while (continuar) {
            if (sistema.encerrarPeriodosExpirados()) salvar();
            limparTela();
            titulo("ALUNO: " + aluno.getNome());
            System.out.println("1. Consultar disciplinas disponíveis");
            System.out.println("2. Realizar matrícula");
            System.out.println("3. Consultar minhas matrículas");
            System.out.println("4. Cancelar matrícula");
            System.out.println("0. Sair da conta");
            switch (lerInteiro("Opção: ")) {
                case 1:
                    executarAcao(() -> exibirOfertas(curriculoAtual()));
                    break;
                case 2:
                    executarAcao(() -> realizarMatricula(aluno));
                    break;
                case 3:
                    executarAcao(() -> exibirMatriculas(sistema.consultarMinhasMatriculas(aluno, semestreAtual())));
                    break;
                case 4:
                    executarAcao(() -> cancelarMatricula(aluno));
                    break;
                case 0:
                    continuar = false;
                    break;
                default:
                    mensagem("Opção inválida.");
            }
        }
    }

    private static void realizarMatricula(Aluno aluno) {
        CurriculoSemestral curriculo = curriculoAtual();
        List<OfertaDisciplina> ofertas = sistema.consultarDisciplinas(aluno, curriculo);
        if (ofertas.isEmpty()) {
            System.out.println("Não há disciplinas disponíveis para matrícula.");
            return;
        }
        exibirOfertas(ofertas);
        long idOferta = lerLong("ID da oferta: ");
        OfertaDisciplina oferta = buscarOferta(curriculo, idOferta);
        if (oferta == null) throw new IllegalArgumentException("Oferta não encontrada.");
        System.out.println("1. Primeira opção (até 4)\n2. Alternativa (até 2)");
        int escolha = lerInteiro("Tipo: ");
        if (escolha != 1 && escolha != 2) throw new IllegalArgumentException("Tipo de opção inválido.");
        sistema.realizarMatricula(aluno, oferta, curriculo.getSemestre(),
                escolha == 1 ? TipoOpcao.PRIMEIRA_OPCAO : TipoOpcao.ALTERNATIVA,
                curriculo.getPeriodoMatricula());
        salvar();
        System.out.println("Matrícula realizada.");
    }

    private static void cancelarMatricula(Aluno aluno) {
        List<Matricula> matriculas = sistema.consultarMinhasMatriculas(aluno, semestreAtual());
        if (matriculas.isEmpty()) {
            System.out.println("Você não possui matrículas ativas neste semestre.");
            return;
        }
        exibirMatriculas(matriculas);
        long id = lerLong("ID da matrícula a cancelar: ");
        Matricula selecionada = null;
        for (Matricula matricula : matriculas) if (matricula.getId() == id) selecionada = matricula;
        if (selecionada == null) throw new IllegalArgumentException("Matrícula não encontrada.");
        CurriculoSemestral curriculo = curriculoAtual();
        sistema.cancelarMatricula(aluno, selecionada, curriculo.getPeriodoMatricula());
        salvar();
        System.out.println("Matrícula cancelada.");
    }

    private static void menuProfessor(Professor professor) {
        boolean continuar = true;
        while (continuar) {
            if (sistema.encerrarPeriodosExpirados()) salvar();
            limparTela();
            titulo("PROFESSOR: " + professor.getNome());
            System.out.println("1. Consultar alunos matriculados nas minhas disciplinas");
            System.out.println("0. Sair da conta");
            switch (lerInteiro("Opção: ")) {
                case 1:
                    executarAcao(() -> {
                        CurriculoSemestral curriculo = curriculoAtual();
                        boolean encontrou = false;
                        for (OfertaDisciplina oferta : curriculo.getOfertas()) {
                            if (oferta.getProfessor() != null && oferta.getProfessor().getId().equals(professor.getId())) {
                                encontrou = true;
                                System.out.println("\n" + oferta.getDisciplina().getCodigo() + " - "
                                        + oferta.getDisciplina().getNome());
                                List<Aluno> alunos = sistema.consultarAlunosMatriculados(professor, oferta);
                                if (alunos.isEmpty()) System.out.println("  Nenhum aluno matriculado.");
                                for (Aluno matriculado : alunos) {
                                    System.out.println("  " + matriculado.getMatriculaAcademica() + " - " + matriculado.getNome());
                                }
                            }
                        }
                        if (!encontrou) System.out.println("Não há ofertas suas neste semestre.");
                    });
                    break;
                case 0:
                    continuar = false;
                    break;
                default:
                    mensagem("Opção inválida.");
            }
        }
    }

    private static void menuSecretaria(FuncionarioSecretaria funcionario) {
        boolean continuar = true;
        while (continuar) {
            if (sistema.encerrarPeriodosExpirados()) salvar();
            limparTela();
            titulo("SECRETARIA: " + funcionario.getNome());
            System.out.println("1. Consultar cadastros");
            System.out.println("2. Cadastrar aluno");
            System.out.println("3. Atualizar aluno");
            System.out.println("4. Cadastrar professor");
            System.out.println("5. Atualizar professor");
            System.out.println("6. Cadastrar disciplina");
            System.out.println("7. Atualizar disciplina");
            System.out.println("8. Cadastrar curso");
            System.out.println("9. Vincular disciplina a curso");
            System.out.println("10. Gerar currículo do semestre");
            System.out.println("11. Adicionar oferta ao currículo");
            System.out.println("12. Definir período de matrículas");
            System.out.println("13. Encerrar ofertas com menos de 3 alunos");
            System.out.println("14. Atualizar curso");
            System.out.println("0. Sair da conta");
            switch (lerInteiro("Opção: ")) {
                case 1: executarAcao(App::exibirCadastros); break;
                case 2: executarAcao(() -> cadastrarAluno(funcionario)); break;
                case 3: executarAcao(() -> atualizarAluno(funcionario)); break;
                case 4: executarAcao(() -> cadastrarProfessor(funcionario)); break;
                case 5: executarAcao(() -> atualizarProfessor(funcionario)); break;
                case 6: executarAcao(() -> cadastrarDisciplina(funcionario)); break;
                case 7: executarAcao(() -> atualizarDisciplina(funcionario)); break;
                case 8: executarAcao(() -> cadastrarCurso(funcionario)); break;
                case 9: executarAcao(() -> vincularDisciplina(funcionario)); break;
                case 10: executarAcao(() -> gerarCurriculo(funcionario)); break;
                case 11: executarAcao(() -> adicionarOferta(funcionario)); break;
                case 12: executarAcao(() -> definirPeriodo(funcionario)); break;
                case 13: executarAcao(() -> encerrarOfertas(funcionario)); break;
                case 14: executarAcao(() -> atualizarCurso(funcionario)); break;
                case 0: continuar = false; break;
                default: mensagem("Opção inválida.");
            }
        }
    }

    private static void cadastrarAluno(FuncionarioSecretaria funcionario) {
        long id = lerLong("ID do usuário: ");
        String nome = lerTexto("Nome: ");
        String senha = lerTexto("Senha: ");
        String matricula = lerTexto("Matrícula acadêmica: ");
        Curso curso = selecionarCurso(false);
        Aluno aluno = new Aluno(id, nome, senha, matricula);
        aluno.setCurso(curso);
        sistema.cadastrarAluno(funcionario, aluno);
        salvar();
        System.out.println("Aluno cadastrado.");
    }

    private static void atualizarAluno(FuncionarioSecretaria funcionario) {
        long id = lerLong("ID do aluno: ");
        Aluno atual = buscarAluno(id);
        if (atual == null) throw new IllegalArgumentException("Aluno não encontrado.");
        Aluno atualizado = new Aluno(id, lerTexto("Novo nome: "), lerTexto("Nova senha: "),
                lerTexto("Nova matrícula acadêmica: "));
        atualizado.setCurso(selecionarCurso(false));
        sistema.atualizarAluno(funcionario, atualizado);
        salvar();
        System.out.println("Aluno atualizado.");
    }

    private static void cadastrarProfessor(FuncionarioSecretaria funcionario) {
        Professor professor = new Professor(lerLong("ID do usuário: "), lerTexto("Nome: "),
                lerTexto("Senha: "), lerTexto("Registro: "));
        sistema.cadastrarProfessor(funcionario, professor);
        salvar();
        System.out.println("Professor cadastrado.");
    }

    private static void atualizarProfessor(FuncionarioSecretaria funcionario) {
        long id = lerLong("ID do professor: ");
        if (buscarProfessor(id) == null) throw new IllegalArgumentException("Professor não encontrado.");
        sistema.atualizarProfessor(funcionario, new Professor(id, lerTexto("Novo nome: "),
                lerTexto("Nova senha: "), lerTexto("Novo registro: ")));
        salvar();
        System.out.println("Professor atualizado.");
    }

    private static void cadastrarDisciplina(FuncionarioSecretaria funcionario) {
        Disciplina disciplina = new Disciplina(lerLong("ID: "), lerTexto("Código: "), lerTexto("Nome: "));
        sistema.cadastrarDisciplina(funcionario, disciplina);
        salvar();
        System.out.println("Disciplina cadastrada.");
    }

    private static void atualizarDisciplina(FuncionarioSecretaria funcionario) {
        long id = lerLong("ID da disciplina: ");
        sistema.atualizarDisciplina(funcionario, new Disciplina(id, lerTexto("Novo código: "), lerTexto("Novo nome: ")));
        salvar();
        System.out.println("Disciplina atualizada.");
    }

    private static void cadastrarCurso(FuncionarioSecretaria funcionario) {
        Curso curso = new Curso(null, lerTexto("Nome do curso: "), lerInteiro("Créditos totais: "));
        sistema.cadastrarCurso(funcionario, curso);
        salvar();
        System.out.println("Curso cadastrado com ID " + curso.getId() + ".");
    }

    private static void atualizarCurso(FuncionarioSecretaria funcionario) {
        exibirCursos();
        long id = lerLong("ID do curso: ");
        Curso atual = buscarCurso(id);
        if (atual == null) throw new IllegalArgumentException("Curso não encontrado.");
        sistema.atualizarCurso(funcionario, new Curso(id, lerTexto("Novo nome: "), lerInteiro("Novos créditos totais: ")));
        salvar();
        System.out.println("Curso atualizado.");
    }

    private static void vincularDisciplina(FuncionarioSecretaria funcionario) {
        exibirCursos();
        Curso curso = buscarCurso(lerLong("ID do curso: "));
        exibirDisciplinas();
        Disciplina disciplina = buscarDisciplina(lerLong("ID da disciplina: "));
        sistema.vincularDisciplinaAoCurso(funcionario, curso, disciplina);
        salvar();
        System.out.println("Disciplina vinculada ao curso.");
    }

    private static void gerarCurriculo(FuncionarioSecretaria funcionario) {
        Semestre semestre = lerSemestre();
        CurriculoSemestral curriculo = sistema.gerarCurriculoDoSemestre(funcionario, semestre);
        salvar();
        System.out.println("Currículo " + formatarSemestre(curriculo.getSemestre()) + " disponível com ID "
                + curriculo.getId() + ".");
    }

    private static void adicionarOferta(FuncionarioSecretaria funcionario) {
        exibirCurriculos();
        CurriculoSemestral curriculo = selecionarCurriculo();
        exibirDisciplinas();
        Disciplina disciplina = buscarDisciplina(lerLong("ID da disciplina: "));
        exibirProfessores();
        Professor professor = buscarProfessor(lerLong("ID do professor: "));
        OfertaDisciplina oferta = sistema.adicionarOferta(funcionario, curriculo, disciplina, professor);
        salvar();
        System.out.println("Oferta criada com ID " + oferta.getId() + ".");
    }

    private static void definirPeriodo(FuncionarioSecretaria funcionario) {
        exibirCurriculos();
        CurriculoSemestral curriculo = selecionarCurriculo();
        LocalDateTime inicio = lerData("Início (yyyy-MM-dd HH:mm): ");
        LocalDateTime fim = lerData("Fim (yyyy-MM-dd HH:mm): ");
        sistema.definirPeriodoMatriculas(funcionario, curriculo, new PeriodoMatricula(inicio, fim));
        salvar();
        System.out.println("Período de matrículas atualizado.");
    }

    private static void encerrarOfertas(FuncionarioSecretaria funcionario) {
        exibirCurriculos();
        CurriculoSemestral curriculo = selecionarCurriculo();
        sistema.encerrarDisciplinasComPoucasInscricoes(funcionario, curriculo);
        salvar();
        System.out.println("Ofertas encerradas conforme o mínimo de 3 inscrições.");
    }

    private static void exibirCadastros() {
        System.out.println("\nALUNOS");
        for (Usuario usuario : sistema.getUsuarios()) if (usuario instanceof Aluno) {
            Aluno aluno = (Aluno) usuario;
            System.out.println(aluno.getId() + " - " + aluno.getNome() + " / matrícula " + aluno.getMatriculaAcademica());
        }
        exibirProfessores();
        exibirDisciplinas();
        exibirCursos();
        exibirCurriculos();
    }

    private static void exibirOfertas(CurriculoSemestral curriculo) {
        exibirOfertas(curriculo.getOfertas());
    }

    private static void exibirOfertas(List<OfertaDisciplina> ofertas) {
        System.out.println("\nOfertas:");
        for (OfertaDisciplina oferta : ofertas) {
            long inscritos = sistema.getDadosSistema().getMatriculas().stream()
                    .filter(m -> m.getStatus() == StatusMatricula.ATIVA
                        && m.getOfertaDisciplina().getId().equals(oferta.getId()))
                    .count();
            System.out.println(oferta.getId() + " - " + oferta.getDisciplina().getCodigo() + " / "
                    + oferta.getDisciplina().getNome() + " | Professor: " + oferta.getProfessor().getNome()
                    + " | Vagas: " + (OfertaDisciplina.MAXIMO_ALUNOS - inscritos) + " | " + oferta.getStatus());
        }
    }

    private static void exibirMatriculas(List<Matricula> matriculas) {
        if (matriculas.isEmpty()) System.out.println("Nenhuma matrícula ativa.");
        for (Matricula matricula : matriculas) {
            System.out.println(matricula.getId() + " - " + matricula.getOfertaDisciplina().getDisciplina().getCodigo()
                    + " / " + matricula.getOfertaDisciplina().getDisciplina().getNome() + " | " + matricula.getTipoOpcao());
        }
    }

    private static void exibirProfessores() {
        System.out.println("\nPROFESSORES");
        for (Usuario usuario : sistema.getUsuarios()) if (usuario instanceof Professor) {
            Professor professor = (Professor) usuario;
            System.out.println(professor.getId() + " - " + professor.getNome() + " / registro " + professor.getRegistro());
        }
    }

    private static void exibirDisciplinas() {
        System.out.println("\nDISCIPLINAS");
        for (Disciplina disciplina : sistema.getDisciplinas()) {
            System.out.println(disciplina.getId() + " - " + disciplina.getCodigo() + " / " + disciplina.getNome());
        }
    }

    private static void exibirCursos() {
        System.out.println("\nCURSOS");
        for (Curso curso : sistema.getCursos()) {
            System.out.println(curso.getId() + " - " + curso.getNome() + " / " + curso.getCreditos() + " créditos");
        }
    }

    private static void exibirCurriculos() {
        System.out.println("\nCURRÍCULOS");
        for (CurriculoSemestral curriculo : sistema.getCurriculos()) {
            System.out.println(curriculo.getId() + " - " + formatarSemestre(curriculo.getSemestre()) + " / "
                    + curriculo.getOfertas().size() + " oferta(s)");
        }
    }

    private static CurriculoSemestral curriculoAtual() {
        Semestre semestre = semestreAtual();
        for (CurriculoSemestral curriculo : sistema.getCurriculos()) {
            if (curriculo.getSemestre().getAno() == semestre.getAno()
                    && curriculo.getSemestre().getPeriodo() == semestre.getPeriodo()) return curriculo;
        }
        throw new IllegalStateException("Não há currículo do próximo semestre cadastrado.");
    }

    private static Semestre semestreAtual() {
        LocalDate hoje = LocalDate.now();
        if (hoje.getMonthValue() <= 6) return new Semestre(hoje.getYear(), 2);
        return new Semestre(hoje.getYear() + 1, 1);
    }

    private static String formatarSemestre(Semestre semestre) {
        return semestre.getAno() + "." + semestre.getPeriodo();
    }

    private static OfertaDisciplina buscarOferta(CurriculoSemestral curriculo, long id) {
        for (OfertaDisciplina oferta : curriculo.getOfertas()) if (oferta.getId() == id) return oferta;
        return null;
    }

    private static Usuario buscarUsuario(long id) {
        for (Usuario usuario : sistema.getUsuarios()) if (usuario.getId() == id) return usuario;
        return null;
    }

    private static Aluno buscarAluno(long id) {
        Usuario usuario = buscarUsuario(id);
        return usuario instanceof Aluno ? (Aluno) usuario : null;
    }

    private static Professor buscarProfessor(long id) {
        Usuario usuario = buscarUsuario(id);
        return usuario instanceof Professor ? (Professor) usuario : null;
    }

    private static Disciplina buscarDisciplina(long id) {
        for (Disciplina disciplina : sistema.getDisciplinas()) if (disciplina.getId() == id) return disciplina;
        return null;
    }

    private static Curso buscarCurso(long id) {
        for (Curso curso : sistema.getCursos()) if (curso.getId() == id) return curso;
        return null;
    }

    private static Curso selecionarCurso(boolean opcional) {
        exibirCursos();
        if (opcional && sistema.getCursos().isEmpty()) return null;
        long id = lerLong("ID do curso (0 para nenhum): ");
        return id == 0 ? null : buscarCurso(id);
    }

    private static CurriculoSemestral selecionarCurriculo() {
        long id = lerLong("ID do currículo: ");
        for (CurriculoSemestral curriculo : sistema.getCurriculos()) if (curriculo.getId() == id) return curriculo;
        throw new IllegalArgumentException("Currículo não encontrado.");
    }

    private static Semestre lerSemestre() {
        int ano = lerInteiro("Ano: ");
        int periodo = lerInteiro("Período (1 ou 2): ");
        return new Semestre(ano, periodo);
    }

    private static LocalDateTime lerData(String prompt) {
        while (true) {
            try {
                return LocalDateTime.parse(lerTexto(prompt), FORMATO_DATA);
            } catch (DateTimeParseException erro) {
                System.out.println("Formato inválido. Use yyyy-MM-dd HH:mm.");
            }
        }
    }

    private static int lerInteiro(String prompt) {
        while (true) {
            try {
                return Integer.parseInt(lerTexto(prompt));
            } catch (NumberFormatException erro) {
                System.out.println("Digite um número inteiro válido.");
            }
        }
    }

    private static long lerLong(String prompt) {
        while (true) {
            try {
                return Long.parseLong(lerTexto(prompt));
            } catch (NumberFormatException erro) {
                System.out.println("Digite um número válido.");
            }
        }
    }

    private static String lerTexto(String prompt) {
        System.out.print(prompt);
        return ENTRADA.nextLine().trim();
    }

    private static void executarAcao(Runnable acao) {
        try {
            acao.run();
        } catch (IllegalArgumentException | IllegalStateException erro) {
            System.out.println("\nNão foi possível concluir: " + erro.getMessage());
        } catch (RuntimeException erro) {
            System.out.println("\nErro inesperado: " + erro.getMessage());
        }
        pausar();
    }

    private static void mensagem(String texto) {
        System.out.println(texto);
        pausar();
    }

    private static void pausar() {
        System.out.print("\nPressione Enter para continuar...");
        ENTRADA.nextLine();
    }

    private static void limparTela() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }

    private static void titulo(String texto) {
        System.out.println("============================================================");
        System.out.println(" " + texto);
        System.out.println("============================================================\n");
    }

    private static void salvar() {
        try {
            arquivo.salvar(sistema.getDadosSistema());
        } catch (IOException erro) {
            System.out.println("Atenção: não foi possível salvar os dados: " + erro.getMessage());
        }
    }

    private static void criarDadosIniciais(DadosSistema dados) {
        FuncionarioSecretaria secretaria = new FuncionarioSecretaria(1L, "Secretaria", "secretaria", "SEC001");
        Professor professor = new Professor(2L, "Professora Ana", "professor", "PROF001");
        Curso curso = new Curso(1L, "Engenharia de Software", 300);
        Aluno aluno = new Aluno(3L, "Aluno Demonstração", "aluno", "2026001");
        aluno.setCurso(curso);
        dados.getUsuarios().add(secretaria);
        dados.getUsuarios().add(professor);
        dados.getUsuarios().add(aluno);
        dados.getCursos().add(curso);

        String[][] nomes = {
            {"ES101", "Programação I"}, {"ES102", "Engenharia de Requisitos"},
            {"ES201", "Estruturas de Dados"}, {"ES202", "Projeto de Software"},
            {"ES301", "Banco de Dados"}, {"ES302", "Arquitetura de Software"}
        };
        for (int indice = 0; indice < nomes.length; indice++) {
            Disciplina disciplina = new Disciplina((long) indice + 1, nomes[indice][0], nomes[indice][1]);
            dados.getDisciplinas().add(disciplina);
            curso.getDisciplinas().add(disciplina);
        }

        Semestre semestre = semestreAtual();
        CurriculoSemestral curriculo = new CurriculoSemestral(1L, semestre);
        curriculo.setPeriodoMatricula(new PeriodoMatricula(LocalDateTime.now().minusDays(1), LocalDateTime.now().plusDays(365)));
        for (int indice = 0; indice < nomes.length; indice++) {
            curriculo.getOfertas().add(new OfertaDisciplina((long) indice + 1, dados.getDisciplinas().get(indice),
                    professor, br.edu.pucminas.matriculas.enums.StatusOferta.DISPONIVEL));
        }
        dados.getCurriculos().add(curriculo);
        System.out.println("Dados iniciais criados. Acesso: Secretaria 1/secretaria, Professor 2/professor, Aluno 3/aluno.");
    }
}