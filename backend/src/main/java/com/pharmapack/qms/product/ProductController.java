package com.pharmapack.qms.product;
import jakarta.validation.Valid; import org.springframework.http.*; import org.springframework.web.bind.annotation.*; import java.util.*;
@RestController @RequestMapping("/api/products") public class ProductController { private final ProductService s; public ProductController(ProductService s){this.s=s;}
 @GetMapping public List<Product> list(){return s.list();} @GetMapping("/{id}") public Product get(@PathVariable Long id){return s.get(id);}
 @PostMapping @ResponseStatus(HttpStatus.CREATED) public Product create(@Valid @RequestBody Product p){return s.create(p);} @PutMapping("/{id}") public Product update(@PathVariable Long id,@RequestBody Product p){return s.update(id,p);}
 @DeleteMapping("/{id}") @ResponseStatus(HttpStatus.NO_CONTENT) public void delete(@PathVariable Long id){s.delete(id);}
}
