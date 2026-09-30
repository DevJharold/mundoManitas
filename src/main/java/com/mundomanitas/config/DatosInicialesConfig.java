package com.mundomanitas.config;

import com.mundomanitas.entity.Usuario;
import com.mundomanitas.repository.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DatosInicialesConfig {

    @Bean
    public CommandLineRunner crearUsuariosIniciales(
        UsuarioRepository usuarioRepository,
        PasswordEncoder passwordEncoder
    ) {
        return args -> {
            crearUsuario(
                usuarioRepository,
                passwordEncoder,
                "Alumno Mundo Manitas",
                "alumno@mundomanitas.com",
                "123456",
                "estudiante"
            );

            crearUsuario(
                usuarioRepository,
                passwordEncoder,
                "Padre Mundo Manitas",
                "padre@mundomanitas.com",
                "123456",
                "padre"
            );

            crearUsuario(
                usuarioRepository,
                passwordEncoder,
                "Docente Mundo Manitas",
                "docente@mundomanitas.com",
                "123456",
                "docente"
            );

            crearUsuario(
                usuarioRepository,
                passwordEncoder,
                "Administrador Mundo Manitas",
                "admin@mundomanitas.com",
                "123456",
                "administrador"
            );
        };
    }

    private void crearUsuario(
        UsuarioRepository usuarioRepository,
        PasswordEncoder passwordEncoder,
        String nombre,
        String email,
        String password,
        String tipo
    ) {

        if (usuarioRepository.existsByEmailIgnoreCase(email)) {
            return;
        }

        Usuario usuario = new Usuario();
        usuario.setNombre(nombre);
        usuario.setEmail(email);
        usuario.setContrasena(passwordEncoder.encode(password));
        usuario.setTipo(tipo);

        usuarioRepository.save(usuario);
    }
}