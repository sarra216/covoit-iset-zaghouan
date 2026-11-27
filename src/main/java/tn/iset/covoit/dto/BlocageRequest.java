package tn.iset.covoit.dto;

import jakarta.validation.constraints.NotNull;

public record BlocageRequest(@NotNull Long bloqueurId, @NotNull Long bloqueId) {
}
