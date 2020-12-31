package org.sid.authservice.sec.web;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import org.sid.authservice.sec.JWTUtil;
import org.sid.authservice.sec.entities.AppRole;
import org.sid.authservice.sec.entities.AppUser;
import org.sid.authservice.sec.service.AccountService;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.security.Principal;
import java.util.*;
import java.util.stream.Collectors;

@RestController
public class AccountRestController {

    private AccountService accountService;

    public AccountRestController(AccountService accountService) {
        this.accountService = accountService;
    }
@GetMapping(path = "/users")
@PostAuthorize("hasAuthority('USER')")
    public List<AppUser> appUsers(){
        return accountService.allUsers();
    }
    @PostMapping(path = "/users")
    @PostAuthorize("hasAuthority('ADMIN')")
    public AppUser saveUser(@RequestBody AppUser appUser){
        return accountService.addNewUser(appUser);
    }
    @PostMapping(path = "/roles")
    @PostAuthorize("hasAuthority('ADMIN')")
    public AppRole saveRole(@RequestBody AppRole appRole){
        return accountService.addNewRole(appRole);
    }
    @PostMapping(path = "/addRoleToUser")
    public void addRoleToUser(@RequestBody RoleUserForm roleUserForm){
        accountService.addRoleToUser(roleUserForm.getUsername(),roleUserForm.getRoleName());
    }
    @GetMapping(path = "/refreshToken")
    public void refreshToken(HttpServletRequest request, HttpServletResponse response) throws Exception{
        String auhToken=request.getHeader(JWTUtil.AUTH_HEADER);
        if (auhToken!=null && auhToken.startsWith(JWTUtil.PREFIX)){
            try {
                String jwt=auhToken.substring(JWTUtil.PREFIX.length());
                Algorithm algorithm = Algorithm.HMAC256(JWTUtil.SECRET);
                JWTVerifier jwtVerifier= JWT.require(algorithm).build();
                DecodedJWT decodedJWT = jwtVerifier.verify(jwt);
                String username=decodedJWT.getSubject();
//                String[] roles=decodedJWT.getClaim("roles").asArray(String.class);
//                Collection<GrantedAuthority> authorities = new ArrayList<>();
//                for (String r:roles){
//                    authorities.add(new SimpleGrantedAuthority(r));
//                }
//                UsernamePasswordAuthenticationToken authenticationToken= new UsernamePasswordAuthenticationToken(username,null,authorities);
//                SecurityContextHolder.getContext().setAuthentication(authenticationToken);
//                filterChain.doFilter(request,response);

                AppUser appUser=accountService.loadUserByUserName(username);
                String jwtAccessToken= JWT.create().withSubject(appUser.getUsername())
                        .withExpiresAt(new Date(System.currentTimeMillis()+JWTUtil.EXPIRE_ACCESS_TOKEN))
                        .withIssuer(request.getRequestURL().toString())
                        .withClaim("roles",appUser.getAppRoles().stream().map(r-> r.getRoleName()).collect(Collectors.toList()))
                        .sign(algorithm);
                Map<String,String> idToken=new HashMap<>();
                idToken.put("access-token", jwtAccessToken);
                idToken.put("refresh-token", jwt);
                response.setContentType("application/json");
                new ObjectMapper().writeValue(response.getOutputStream(),idToken);
            }
            catch (Exception e){
                throw e;

            }

//            String refreshToken=auhToken.substring(7);
//            Algorithm algorithm = Algorithm.HMAC256("mySecret1234");
//            JWTVerifier jwtVerifier= JWT.require(algorithm).build();
//            DecodedJWT decodedJWT = jwtVerifier.verify(refreshToken);
//            String username=decodedJWT.getSubject();
        }
        else {
            throw new RuntimeException("Refresh token required !!");
        }
    }
    @GetMapping(path = "/profile")
    public AppUser profile(Principal principal){
        return accountService.loadUserByUserName(principal.getName());
    }
}
@Data
class RoleUserForm{
    private String username;
    private String roleName;
}
