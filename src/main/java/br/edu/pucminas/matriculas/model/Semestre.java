package br.edu.pucminas.matriculas.model;

public class Semestre {
    private int ano;
    private int periodo;

    public Semestre() {}

    public Semestre(int ano, int periodo) {
        this.ano = ano;
        this.periodo = periodo;
    }

    public int getAno() { return ano; }
    public void setAno(int ano) { this.ano = ano; }
    public int getPeriodo() { return periodo; }
    public void setPeriodo(int periodo) { this.periodo = periodo; }
}
