package com.starterkit.springboot.produto;

public class produtoRequest {
    private String nome;
    private String fornecedor;
    private String categoria;
    private String quantidadeStock;
    private String preco;
    private Boolean emPromocao;




    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getPreco() { return preco; }
    public void setPreco(String preco) { this.preco = preco; }

    public String getFornecedor() { return fornecedor; }
    public void setFornecedor(String fornecedor) { this.fornecedor = fornecedor; }
    
    public String getQuantidadeStock() { return quantidadeStock; }
    public void setQuantidadeStock(String quantidadeStock) { this.quantidadeStock = quantidadeStock; }

    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }

    public Boolean getEmPromocao() { return emPromocao; }
    public void setEmPromocao(Boolean emPromocao) { this.emPromocao = emPromocao; }



 
}