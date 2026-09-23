package com.pharmapack.qms.product;
import org.springframework.stereotype.Service; import java.util.*;
@Service public class ProductService { private final ProductRepository repo; public ProductService(ProductRepository r){repo=r;}
 public List<Product> list(){return repo.findAll();} public Product get(Long id){return repo.findById(id).orElseThrow(()->new IllegalArgumentException("Product not found: "+id));}
 public Product create(Product p){if(repo.existsByProductCode(p.getProductCode()))throw new IllegalArgumentException("Duplicate product code"); return repo.save(p);}
 public Product update(Long id, Product req){Product p=get(id); if(req.getProductCode()!=null&&!req.getProductCode().equals(p.getProductCode())&&repo.existsByProductCode(req.getProductCode()))throw new IllegalArgumentException("Duplicate product code");
  if(req.getProductCode()!=null)p.setProductCode(req.getProductCode()); if(req.getProductName()!=null)p.setProductName(req.getProductName()); p.setStrength(req.getStrength()); p.setDosageForm(req.getDosageForm()); p.setPackSize(req.getPackSize()); if(req.getStatus()!=null)p.setStatus(req.getStatus()); return repo.save(p); }
 public void delete(Long id){repo.delete(get(id));}
}
