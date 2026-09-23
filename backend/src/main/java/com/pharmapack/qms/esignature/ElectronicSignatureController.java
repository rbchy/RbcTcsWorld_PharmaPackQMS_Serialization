package com.pharmapack.qms.esignature; import com.pharmapack.qms.auth.*; import org.springframework.http.*; import org.springframework.web.bind.annotation.*; import java.time.*; import java.util.*;
/**
 * Captures a CGMP-style electronic signature (21 CFR Part 11 in spirit: who signed, what
 * record, when, and why) against any record in the system - a QA decision, a batch
 * release, a CAPA closure, a reconciliation sign-off, etc. The signer is always the
 * authenticated caller, read from the JWT via CurrentUser - never a client-supplied user
 * id - so a signature can never be forged as someone else.
 */
@RestController @RequestMapping("/api/esignatures") public class ElectronicSignatureController {
 final ElectronicSignatureRepository sr; final UserRepository ur; public ElectronicSignatureController(ElectronicSignatureRepository s,UserRepository u){sr=s;ur=u;}

 @GetMapping public List<ElectronicSignatureResponse> list(){return sr.findAllByOrderBySignedAtDesc().stream().map(this::dto).toList();}
 @GetMapping("/entity/{entityName}/{entityId}") public List<ElectronicSignatureResponse> forEntity(@PathVariable String entityName,@PathVariable String entityId){return sr.findByEntityNameAndEntityIdOrderBySignedAtDesc(entityName,entityId).stream().map(this::dto).toList();}

 public record Sign(String entityName,String entityId,String actionType,String signatureReason){}
 @PostMapping @ResponseStatus(HttpStatus.CREATED) public ElectronicSignatureResponse sign(@RequestBody Sign x){
  if(x.entityName()==null||x.entityName().isBlank())throw new IllegalArgumentException("entityName is required");
  if(x.entityId()==null||x.entityId().isBlank())throw new IllegalArgumentException("entityId is required");
  if(x.actionType()==null||x.actionType().isBlank())throw new IllegalArgumentException("actionType is required");
  Long uid=CurrentUser.id();
  if(uid==null)throw new org.springframework.web.server.ResponseStatusException(HttpStatus.UNAUTHORIZED,"Not authenticated");
  ElectronicSignature e=new ElectronicSignature();
  e.setUser(ur.findById(uid).orElseThrow(()->new IllegalArgumentException("User not found")));
  e.setEntityName(x.entityName()); e.setEntityId(x.entityId()); e.setActionType(x.actionType());
  e.setSignatureReason(x.signatureReason()); e.setSignedAt(LocalDateTime.now());
  return dto(sr.save(e));
 }

 ElectronicSignatureResponse dto(ElectronicSignature e){var u=e.getUser();return new ElectronicSignatureResponse(e.getId(),u.getId(),u.getUsername(),u.getFullName(),e.getEntityName(),e.getEntityId(),e.getActionType(),e.getSignedAt(),e.getSignatureReason());}
}
