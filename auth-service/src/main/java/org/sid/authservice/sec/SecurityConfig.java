package org.sid.authservice.sec;


import org.sid.authservice.sec.filtres.JwtAuthenticationFilter;
import org.sid.authservice.sec.filtres.JwtAuthorizationFilter;
import org.sid.authservice.sec.service.UserDetailsServiceImpl;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {
//    @Autowired
//    private AccountService accountService;
    private UserDetailsServiceImpl userDetailsService;

    public SecurityConfig(UserDetailsServiceImpl userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userDetailsService);

        //super.configure(auth);
//        auth.userDetailsService(new UserDetailsService() {
//            @Override
//            public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
////                AppUser appUser= accountService.loadUserByUserName(username);
////                Collection<GrantedAuthority> authorities= new ArrayList<>();
////                appUser.getAppRoles().forEach(r->{
////                    authorities.add(new SimpleGrantedAuthority(r.getRoleName()));
////                });
////                return new User(appUser.getUsername(),appUser.getPassword(),authorities);
//            }
//        });

    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
       // super.configure(http);
        http.csrf().disable();
        http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
        http.headers().frameOptions().disable();
        http.authorizeRequests().antMatchers("/h2-console/**","/refreshToken/**","/login/**").permitAll();
        //http.formLogin();

       // http.authorizeRequests().antMatchers(HttpMethod.POST,"/users/**").hasAuthority("ADMIN");
        //http.authorizeRequests().antMatchers(HttpMethod.GET,"/users/**").hasAuthority("USER");
        http.authorizeRequests().anyRequest().authenticated();
        http.addFilter(new JwtAuthenticationFilter(authenticationManagerBean()));
        http.addFilterBefore(new JwtAuthorizationFilter(), UsernamePasswordAuthenticationFilter.class);
    }

    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }
}
