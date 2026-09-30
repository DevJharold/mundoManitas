package com.mundomanitas.controller;

import com.mundomanitas.entity.Estudiante;
import com.mundomanitas.entity.Progreso;
import com.mundomanitas.entity.Usuario;
import com.mundomanitas.repository.EstudianteRepository;
import com.mundomanitas.repository.ProgresoRepository;
import com.mundomanitas.repository.UsuarioRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.math.BigDecimal;
import java.util.List;

@Controller
public class PerfilController {

    private final UsuarioRepository usuarioRepository;
    private final EstudianteRepository estudianteRepository;
    private final ProgresoRepository progresoRepository;

    public PerfilController(
        UsuarioRepository usuarioRepository,
        EstudianteRepository estudianteRepository,
        ProgresoRepository progresoRepository
    ) {
        this.usuarioRepository = usuarioRepository;
        this.estudianteRepository = estudianteRepository;
        this.progresoRepository = progresoRepository;
    }

    @GetMapping("/perfil")
    public String perfil(Authentication authentication, Model model) {

        Usuario usuario = usuarioRepository
            .findByEmailIgnoreCase(authentication.getName())
            .orElseThrow();

        boolean esEstudiante =
            "estudiante".equalsIgnoreCase(usuario.getTipo());

        model.addAttribute("nombre", usuario.getNombre());
        model.addAttribute("correo", usuario.getEmail());
        model.addAttribute("tipo", usuario.getTipo());
        model.addAttribute("esEstudiante", esEstudiante);

        if (esEstudiante) {

            Estudiante estudiante = estudianteRepository
                .findById(usuario.getIdUsuario())
                .orElse(null);

            List<Progreso> progresos =
                progresoRepository.findByIdEstudiante(
                    usuario.getIdUsuario()
                );

            long completados = progresos.stream()
                .filter(p -> porcentaje(p).compareTo(new BigDecimal("100")) >= 0)
                .count();

            int progresoGeneral = (int) Math.round(
                progresos.stream()
                    .map(Progreso::getPorcentaje)
                    .filter(p -> p != null)
                    .mapToDouble(BigDecimal::doubleValue)
                    .average()
                    .orElse(0)
            );

            model.addAttribute("estudiante", estudiante);
            model.addAttribute("totalModulos", progresos.size());
            model.addAttribute("completados", completados);
            model.addAttribute("progresoGeneral", progresoGeneral);
        }

        return "perfil";
    }

    private BigDecimal porcentaje(Progreso progreso) {
        return progreso.getPorcentaje() == null
            ? BigDecimal.ZERO
            : progreso.getPorcentaje();
    }
}