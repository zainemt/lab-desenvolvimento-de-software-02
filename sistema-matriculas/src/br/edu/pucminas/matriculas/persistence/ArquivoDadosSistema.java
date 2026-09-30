package br.edu.pucminas.matriculas.persistence;

import br.edu.pucminas.matriculas.model.DadosSistema;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

/** Persistência simples em arquivo para o protótipo de console. */
public class ArquivoDadosSistema {
    private final File arquivo;

    public ArquivoDadosSistema(String caminho) {
        this.arquivo = new File(caminho);
    }

    public DadosSistema carregar() throws IOException {
        if (!arquivo.exists()) return new DadosSistema();
        try (ObjectInputStream entrada = new ObjectInputStream(new FileInputStream(arquivo))) {
            Object dados = entrada.readObject();
            if (!(dados instanceof DadosSistema)) {
                throw new IOException("O arquivo de dados possui um formato incompatível.");
            }
            return (DadosSistema) dados;
        } catch (ClassNotFoundException erro) {
            throw new IOException("Não foi possível interpretar os dados salvos.", erro);
        }
    }

    public void salvar(DadosSistema dados) throws IOException {
        File diretorio = arquivo.getParentFile();
        if (diretorio != null && !diretorio.exists() && !diretorio.mkdirs()) {
            throw new IOException("Não foi possível criar o diretório de dados.");
        }
        File temporario = new File(arquivo.getPath() + ".tmp");
        try (ObjectOutputStream saida = new ObjectOutputStream(new FileOutputStream(temporario))) {
            saida.writeObject(dados);
        }
        try {
            Files.move(temporario.toPath(), arquivo.toPath(), StandardCopyOption.REPLACE_EXISTING,
                    StandardCopyOption.ATOMIC_MOVE);
        } catch (AtomicMoveNotSupportedException erro) {
            Files.move(temporario.toPath(), arquivo.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }
    }
}