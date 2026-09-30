package com.mundomanitas.controller;

import com.mundomanitas.entity.Ejercicio;
import com.mundomanitas.entity.Modulo;
import com.mundomanitas.entity.Progreso;
import com.mundomanitas.entity.Usuario;
import com.mundomanitas.entity.Video;
import com.mundomanitas.repository.EjercicioRepository;
import com.mundomanitas.repository.ModuloRepository;
import com.mundomanitas.repository.ProgresoRepository;
import com.mundomanitas.repository.UsuarioRepository;
import com.mundomanitas.repository.VideoRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Controller
public class CursosController {

    private final UsuarioRepository usuarioRepository;
    private final ModuloRepository moduloRepository;
    private final ProgresoRepository progresoRepository;
    private final VideoRepository videoRepository;
    private final EjercicioRepository ejercicioRepository;

    public CursosController(
        UsuarioRepository usuarioRepository,
        ModuloRepository moduloRepository,
        ProgresoRepository progresoRepository,
        VideoRepository videoRepository,
        EjercicioRepository ejercicioRepository
    ) {
        this.usuarioRepository = usuarioRepository;
        this.moduloRepository = moduloRepository;
        this.progresoRepository = progresoRepository;
        this.videoRepository = videoRepository;
        this.ejercicioRepository = ejercicioRepository;
    }

    @GetMapping("/cursos")
    public String cursos(Authentication authentication, Model model) {

        Usuario usuario = obtenerUsuario(authentication);

        model.addAttribute("nombre", usuario.getNombre());
        model.addAttribute("correo", usuario.getEmail());
        model.addAttribute("tipo", usuario.getTipo());

        List<Modulo> modulos =
            moduloRepository.findAllByOrderByOrdenAsc();

        Map<Integer, Progreso> progresoPorModulo =
            new HashMap<>();

        if ("estudiante".equalsIgnoreCase(usuario.getTipo())) {

            progresoPorModulo =
                progresoRepository
                    .findByIdEstudiante(usuario.getIdUsuario())
                    .stream()
                    .collect(
                        Collectors.toMap(
                            Progreso::getIdModulo,
                            Function.identity(),
                            (primero, segundo) -> primero
                        )
                    );
        }

        List<Map<String, Object>> cursos =
            new ArrayList<>();

        for (Modulo modulo : modulos) {

            Progreso progreso =
                progresoPorModulo.get(modulo.getIdModulo());

            int porcentaje = progreso != null
                ? porcentaje(progreso).intValue()
                : 0;

            Map<String, Object> item =
                new HashMap<>();

            item.put("idModulo", modulo.getIdModulo());
            item.put("titulo", modulo.getTitulo());
            item.put("descripcion", modulo.getDescripcion());
            item.put("nivel", modulo.getNivel());
            item.put("orden", modulo.getOrden());
            item.put("porcentaje", porcentaje);
            item.put("estado", obtenerEstado(porcentaje));
            item.put("estadoClase", obtenerEstadoClase(porcentaje));
            item.put("accion", obtenerAccion(porcentaje));
            item.put("coverClase", obtenerCoverClase(modulo.getNivel()));
            item.put("inicial", obtenerInicial(modulo.getTitulo()));

            cursos.add(item);
        }

        model.addAttribute("cursos", cursos);

        return "cursos";
    }

    @GetMapping("/cursos/{id}")
    public String detalleModulo(
        @PathVariable Integer id,
        Authentication authentication,
        Model model
    ) {

        Usuario usuario = obtenerUsuario(authentication);

        Modulo modulo = moduloRepository
            .findById(id)
            .orElseThrow(
                () -> new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Módulo no encontrado"
                )
            );

        List<Video> videos =
            videoRepository.findByIdModuloOrderByOrdenAsc(id);

        List<Ejercicio> ejercicios =
            ejercicioRepository.findByIdModuloOrderByIdEjercicioAsc(id);

        int porcentaje = 0;
        BigDecimal calificacion = null;

        boolean esEstudiante =
            "estudiante".equalsIgnoreCase(usuario.getTipo());

        if (esEstudiante) {

            Progreso progreso = progresoRepository
                .findByIdEstudianteAndIdModulo(
                    usuario.getIdUsuario(),
                    modulo.getIdModulo()
                )
                .orElse(null);

            if (progreso != null) {
                porcentaje = porcentaje(progreso).intValue();
                calificacion = progreso.getCalificacion();
            }
        }

        int duracionTotal = videos.stream()
            .map(Video::getDuracion)
            .filter(duracion -> duracion != null)
            .mapToInt(Integer::intValue)
            .sum();

        int totalMinutos =
            (int) Math.ceil(duracionTotal / 60.0);

        Video siguienteVideo =
            videos.isEmpty() ? null : videos.get(0);

        model.addAttribute("nombre", usuario.getNombre());
        model.addAttribute("correo", usuario.getEmail());
        model.addAttribute("tipo", usuario.getTipo());

        model.addAttribute("modulo", modulo);
        model.addAttribute("videos", videos);
        model.addAttribute("ejercicios", ejercicios);
        model.addAttribute("totalContenidos", videos.size() + ejercicios.size());
        model.addAttribute("totalVideos", videos.size());
        model.addAttribute("totalEjercicios", ejercicios.size());
        model.addAttribute("totalMinutos", totalMinutos);
        model.addAttribute("siguienteVideo", siguienteVideo);

        model.addAttribute("porcentaje", porcentaje);
        model.addAttribute("estado", obtenerEstado(porcentaje));
        model.addAttribute("estadoClase", obtenerEstadoClase(porcentaje));
        model.addAttribute("calificacion", calificacion);
        model.addAttribute("esEstudiante", esEstudiante);

        return "curso-detalle";
    }

    private Usuario obtenerUsuario(Authentication authentication) {
        return usuarioRepository
            .findByEmailIgnoreCase(authentication.getName())
            .orElseThrow();
    }

    private BigDecimal porcentaje(Progreso progreso) {
        return progreso.getPorcentaje() == null
            ? BigDecimal.ZERO
            : progreso.getPorcentaje();
    }

    private String obtenerEstado(int porcentaje) {

        if (porcentaje >= 100) {
            return "COMPLETADO";
        }

        if (porcentaje > 0) {
            return "EN PROGRESO";
        }

        return "POR INICIAR";
    }

    private String obtenerEstadoClase(int porcentaje) {

        if (porcentaje >= 100) {
            return "completed";
        }

        if (porcentaje > 0) {
            return "progress";
        }

        return "new";
    }

    private String obtenerAccion(int porcentaje) {

        if (porcentaje >= 100) {
            return "Revisar módulo";
        }

        if (porcentaje > 0) {
            return "Continuar módulo";
        }

        return "Comenzar módulo";
    }

    private String obtenerCoverClase(String nivel) {

        if ("intermedio".equalsIgnoreCase(nivel)) {
            return "cover-intermedio";
        }

        if ("avanzado".equalsIgnoreCase(nivel)) {
            return "cover-avanzado";
        }

        return "cover-basico";
    }

    private String obtenerInicial(String titulo) {

        if (titulo == null || titulo.isBlank()) {
            return "M";
        }

        return titulo.substring(0, 1).toUpperCase();
    }
}