package br.natanael.android.uber.model;

public class Requisicao {
    private String id;
    private String status;
    private Usuario passageiro;
    private Usuario motorista;

    public static final String STATUS_AGUARDANDO = "aguardando";

}
