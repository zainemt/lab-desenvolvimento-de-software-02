# Rastreabilidade — Lab01S01 → Lab01S02

Esta tabela demonstra como cada história/caso de uso do README está representado no diagrama de classes e no projeto Java.

| HU | Caso de uso / história | Classe/método Java correspondente |
|---|---|---|
| HU01 | Realizar login | `SistemaMatriculas.realizarLogin(Usuario, String)` + `Usuario.senha` |
| HU02 | Consultar disciplinas | `SistemaMatriculas.consultarDisciplinas(Aluno, CurriculoSemestral)` + `CurriculoSemestral.ofertas` |
| HU03 | Realizar matrícula | `SistemaMatriculas.realizarMatricula(...)` + `Matricula` + `TipoOpcao` + `OfertaDisciplina.MAXIMO_ALUNOS = 60` |
| HU04 | Cancelar matrícula | `SistemaMatriculas.cancelarMatricula(...)` + `StatusMatricula.CANCELADA` |
| HU05 | Consultar minhas matrículas | `SistemaMatriculas.consultarMinhasMatriculas(Aluno, Semestre)` |
| HU06 | Consultar alunos matriculados | `SistemaMatriculas.consultarAlunosMatriculados(Professor, OfertaDisciplina)` |
| HU07 | Gerenciar alunos | `cadastrarAluno(...)` e `atualizarAluno(...)` |
| HU08 | Gerenciar professores | `cadastrarProfessor(...)` e `atualizarProfessor(...)` |
| HU09 | Gerenciar disciplinas | `cadastrarDisciplina(...)` e `atualizarDisciplina(...)` |
| HU10 | Gerar currículo do semestre | `gerarCurriculoDoSemestre(...)` + `CurriculoSemestral` |
| HU11 | Gerenciar período de matrículas | `definirPeriodoMatriculas(...)` + `PeriodoMatricula` |
| HU12 | Encerrar disciplinas com poucas inscrições | `encerrarDisciplinasComPoucasInscricoes(...)` + `OfertaDisciplina.MINIMO_ALUNOS = 3` |
| HU13 | Receber informações de matrícula | `SistemaCobrancas.receberInformacoesMatricula(...)`; dependência usada por HU03 |

## Regras do README refletidas no modelo

- Aluno autenticado para matricular/cancelar: parâmetros `Aluno` + `PeriodoMatricula` e HU01/HU03/HU04 no controlador.
- Até 4 primeiras opções e 2 alternativas: enum `TipoOpcao`; validação ficará na implementação da Sprint 3.
- Máximo de 60 alunos por disciplina: `OfertaDisciplina.MAXIMO_ALUNOS = 60`.
- Matrícula/cancelamento apenas dentro do período: `PeriodoMatricula` participa dos métodos HU03/HU04.
- Matrícula cancelada deixa de contar: `StatusMatricula.CANCELADA`.
- Disciplina com menos de 3 inscrições ao final: `OfertaDisciplina.MINIMO_ALUNOS = 3` + HU12.
- Cobrança notificada após matrícula: `SistemaMatriculas` depende de `SistemaCobrancas`; HU13 é disparada pelo fluxo HU03 na implementação.
