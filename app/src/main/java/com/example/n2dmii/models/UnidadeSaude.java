package com.example.n2dmii.models;

public class UnidadeSaude {
    private final long id;
    private final String tipo, nome, endereco, hora, telefone;
    private final double latitude, longitude;

    /* Representa uma unidade; telefone vazio significa que o número não foi informado. */
    public UnidadeSaude(long id, String tipo, String nome, String endereco, String hora,
                        double latitude, double longitude, String telefone) {
        this.id = id;
        this.tipo = tipo;
        this.nome = nome;
        this.endereco = endereco;
        this.hora = hora;
        this.latitude = latitude;
        this.longitude = longitude;
        this.telefone = telefone == null ? "" : telefone;
    }

    /* Retorna o campo id sem alterar os dados do modelo. */
    public long getId() { return id; }

    /* Retorna o campo tipo sem alterar os dados do modelo. */
    public String getTipo() { return tipo; }

    /* Retorna o campo nome sem alterar os dados do modelo. */
    public String getNome() { return nome; }

    /* Retorna o campo endereco sem alterar os dados do modelo. */
    public String getEndereco() { return endereco; }

    /* Retorna o campo hora sem alterar os dados do modelo. */
    public String getHora() { return hora; }

    /* Retorna o campo latitude sem alterar os dados do modelo. */
    public double getLatitude() { return latitude; }

    /* Retorna o campo longitude sem alterar os dados do modelo. */
    public double getLongitude() { return longitude; }

    /* Retorna o campo telefone sem alterar os dados do modelo. */
    public String getTelefone() { return telefone; }
}
