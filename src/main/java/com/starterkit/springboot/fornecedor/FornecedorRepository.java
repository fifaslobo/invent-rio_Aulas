package com.starterkit.springboot.fornecedor;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface FornecedorRepository extends JpaRepository<Fornecedor, Long> {

    Optional<Fornecedor> findByCodigoUnico(String codigo);
}