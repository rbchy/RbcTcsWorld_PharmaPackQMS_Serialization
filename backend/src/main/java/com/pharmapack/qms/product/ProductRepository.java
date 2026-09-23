package com.pharmapack.qms.product; import org.springframework.data.jpa.repository.JpaRepository; import java.util.*;
public interface ProductRepository extends JpaRepository<Product,Long>{ Optional<Product> findByProductCode(String code); boolean existsByProductCode(String code); }
