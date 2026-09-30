package com.mundomanitas.controller;

import com.mundomanitas.entity.Modulo;
import com.mundomanitas.entity.Progreso;
import com.mundomanitas.entity.Usuario;
import com.mundomanitas.repository.ModuloRepository;
import com.mundomanitas.repository.ProgresoRepository;
import com.mundomanitas.repository.UsuarioRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Controller
public class DashboardController {

    private final UsuarioRepository usuarioRepository;
    private final ProgresoRepository progresoRepository;
    private final ModuloRepository moduloRepository;

    public DashboardController(
        UsuarioRepository usuarioRepository,
        ProgresoRepository progresoRepository,
        ModuloRepository moduloRepository
    ) {
        this.usuarioRepository = usuarioRepository;
        this.progresoRepository = progresoRepository;
        this.moduloRepository = moduloRepository;
    }

    @GetMapping("/")
    public String inicio() {
        return "redirect:/dashboard";
    }

    @GetMapping("/dashboard")
    public String dashboard(Authentication authentication, Model model) {

        Usuario usuario = usuarioRepository
            .findByEmailIgnoreCase(authentication.getName())
            .orElseThrow();

        model.addAttribute("nombre", usuario.getNombre());
        model.addAttribute("correo", usuario.getEmail());
        model.addAttribute("tipo", usuario.getTipo());

        if ("estudiante".equalsIgnoreCase(usuario.getTipo())) {
            cargarDashboardEstudiante(usuario, model);
        }

        return "dashboard";
    }

    private void cargarDashboardEstudiante(Usuario usuario, Model model) {

        List<Progreso> progresos =
            progresoRepository.findByIdEstudiante(usuario.getIdUsuario());

        Map<Integer, Modulo> modulos = moduloRepository
            .findAllById(
                progresos.stream()
                    .map(Progreso::getIdModulo)
                    .toList()
            )
            .stream()
            .collect(
                Collectors.toMap(
                    Modulo::getIdModulo,
                    Function.identity()
                )
            );

        int totalCursos = progresos.size();

        long enProgreso = progresos.stream()
            .filter(p -> porcentaje(p).compareTo(BigDecimal.ZERO) > 0)
            .filter(p -> porcentaje(p).compareTo(new BigDecimal("100")) < 0)
            .count();

        long completados = progresos.stream()
            .filter(p -> porcentaje(p).compareTo(new BigDecimal("100")) >= 0)
            .count();

        long porIniciar = progresos.stream()
            .filter(p -> porcentaje(p).compareTo(BigDecimal.ZERO) == 0)
            .count();

        double promedio = progresos.stream()
            .map(Progreso::getPorcentaje)
            .filter(p -> p != null)
            .mapToDouble(BigDecimal::doubleValue)
            .average()
            .orElse(0);

        int progresoGeneral = (int) Math.round(promedio);

        List<Map<String, Object>> cursosActivos = new ArrayList<>();

        progresos.stream()
            .filter(p -> porcentaje(p).compareTo(BigDecimal.ZERO) > 0)
            .filter(p -> porcentaje(p).compareTo(new BigDecimal("100")) < 0)
            .sorted(
                (a, b) -> porcentaje(b).compareTo(porcentaje(a))
            )
            .limit(2)
            .forEach(progreso -> {

                Modulo modulo = modulos.get(progreso.getIdModulo());

                if (modulo == null) {
                    return;
                }

                Map<String, Object> curso = new HashMap<>();

                curso.put("idModulo", modulo.getIdModulo());
                curso.put("titulo", modulo.getTitulo());
                curso.put("nivel", modulo.getNivel());
                curso.put("porcentaje", porcentaje(progreso).intValue());

                cursosActivos.add(curso);
            });

        model.addAttribute("totalCursos", totalCursos);
        model.addAttribute("enProgreso", enProgreso);
        model.addAttribute("completados", completados);
        model.addAttribute("porIniciar", porIniciar);
        model.addAttribute("progresoGeneral", progresoGeneral);
        model.addAttribute("cursosActivos", cursosActivos);
    }

    private BigDecimal porcentaje(Progreso progreso) {
        return progreso.getPorcentaje() == null
            ? BigDecimal.ZERO
            : progreso.getPorcentaje();
    }
}