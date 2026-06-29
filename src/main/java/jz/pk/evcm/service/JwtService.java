package jz.pk.evcm.service;

import io.jsonwebtoken.Claims;
import jz.pk.evcm.entity.UserRole;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class JwtService {
    @Value("${security.jwt.secret")
    private String secret;

    @Value("${security.jwt.expiration}")
    private Long jwtExpiration;




}
