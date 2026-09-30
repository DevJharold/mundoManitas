package com.mundomanitas.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "video")
public class Video {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idVideo")
    private Integer idVideo;

    @Column(name = "idModulo", nullable = false)
    private Integer idModulo;

    @Column(name = "titulo", nullable = false, length = 100)
    private String titulo;

    @Column(name = "url", nullable = false, length = 255)
    private String url;

    @Column(name = "duracion")
    private Integer duracion;

    @Column(name = "orden", nullable = false)
    private Integer orden;

    public Integer getIdVideo() {
        return idVideo;
    }

    public void setIdVideo(Integer idVideo) {
        this.idVideo = idVideo;
    }

    public Integer getIdModulo() {
        return idModulo;
    }

    public void setIdModulo(Integer idModulo) {
        this.idModulo = idModulo;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Integer getDuracion() {
        return duracion;
    }

    public void setDuracion(Integer duracion) {
        this.duracion = duracion;
    }

    public Integer getOrden() {
        return orden;
    }

    public void setOrden(Integer orden) {
        this.orden = orden;
    }

    public int getDuracionMinutos() {

        if (duracion == null || duracion <= 0) {
            return 0;
        }

        return Math.max(
            1,
            (int) Math.ceil(duracion / 60.0)
        );
    }
}