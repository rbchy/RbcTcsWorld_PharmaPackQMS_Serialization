package com.pharmapack.qms.auth; import org.springframework.security.core.context.SecurityContextHolder;
/** Small helper so other modules (audit trail, e-signatures) can read who is making the
 * current request without each duplicating the SecurityContext cast. */
public final class CurrentUser { private CurrentUser(){}
 public static String username(){var a=SecurityContextHolder.getContext().getAuthentication();return a==null?"system":a.getName();}
 public static Long id(){var a=SecurityContextHolder.getContext().getAuthentication();if(a==null||!(a.getPrincipal() instanceof AppUserPrincipal p))return null;return p.getId();}
}
