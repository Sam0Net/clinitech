package com.utp.clinitech.dto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
public record EspecialidadRequest(@NotBlank @Size(max = 100) String nombre, @Size(max = 255) String descripcion) { }
