# Sistema de Matrículas

Protótipo acadêmico em Java com interface de console e persistência em arquivo.

## Executar

Na raiz do repositório:

```sh
mkdir -p sistema-matriculas/bin
javac -d sistema-matriculas/bin $(find sistema-matriculas/src -name '*.java')
java -cp sistema-matriculas/bin br.edu.pucminas.matriculas.App
```

No primeiro uso, acesse com ID `1` e senha `secretaria`, ID `2` e senha `professor`, ou ID `3` e senha `aluno`. Os cadastros iniciais incluem ofertas para o próximo semestre e um período de matrícula demonstrativo.

O arquivo persistido fica em `sistema-matriculas/dados/sistema.dat`. Para escolher outro local, informe `-Dmatriculas.arquivo=/caminho/arquivo.dat` na inicialização da JVM.

## Regras implementadas

- Login e menus específicos para aluno, professor e secretaria.
- Até quatro disciplinas como primeira opção e duas como alternativa por aluno/semestre.
- Período de matrícula obrigatório, máximo de 60 alunos por oferta e cancelamento de matrícula durante o período.
- Encerramento automático de ofertas com menos de três alunos ao fim das matrículas, além da opção manual da secretaria.
- Notificação do adaptador de cobranças após cada matrícula.
- Dados de usuários, cursos, disciplinas, currículos, ofertas e matrículas salvos localmente.
