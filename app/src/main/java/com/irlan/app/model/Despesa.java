package com.irlan.app.model;

/**
 * Classe modelo que representa uma Despesa
 * Contém todos os campos necessários para armazenar informações de uma despesa
 */
public class Despesa {

    // Atributos privados
    private int id;
    private double valor;
    private String descricao;
    private String cartao;
    private String data; // Formato: yyyy-MM-dd

    // Construtor vazio (necessário para algumas operações)
    public Despesa() {
    }

    // Construtor completo (sem ID - para inserção no banco)
    public Despesa(double valor, String descricao, String cartao, String data) {
        this.valor = valor;
        this.descricao = descricao;
        this.cartao = cartao;
        this.data = data;
    }

    // Construtor com ID (usado ao buscar do banco)
    public Despesa(int id, double valor, String descricao, String cartao, String data) {
        this.id = id;
        this.valor = valor;
        this.descricao = descricao;
        this.cartao = cartao;
        this.data = data;
    }

    // ==================== GETTERS E SETTERS ====================

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public double getValor() {
        return valor;
    }

    public void setValor(double valor) {
        this.valor = valor;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public String getCartao() {
        return cartao;
    }

    public void setCartao(String cartao) {
        this.cartao = cartao;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    // ==================== MÉTODOS AUXILIARES ====================

    /**
     * Retorna o valor formatado em formato brasileiro (R$ 1.234,56)
     */
    public String getValorFormatado() {
        return String.format("R$ %.2f", valor).replace(".", ",");
    }

    /**
     * Retorna a data formatada no padrão brasileiro (dd/MM/yyyy)
     */
    public String getDataFormatada() {
        if (data == null || data.isEmpty()) {
            return "";
        }

        try {
            // Converte de yyyy-MM-dd para dd/MM/yyyy
            String[] partes = data.split("-");
            if (partes.length == 3) {
                return partes[2] + "/" + partes[1] + "/" + partes[0];
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return data;
    }

    /**
     * Método toString para debug
     */
    @Override
    public String toString() {
        return "Despesa{" +
                "id=" + id +
                ", valor=" + valor +
                ", descricao='" + descricao + '\'' +
                ", cartao='" + cartao + '\'' +
                ", data='" + data + '\'' +
                '}';
    }
}