package com.petfriends.infnet.PetFriends_Almoxarifado.model;

import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Objects;

@Embeddable
@Getter
@NoArgsConstructor
public class Quantidade {

    private int valor;

    private Quantidade(int valor) {
        if (valor < 0) throw new IllegalArgumentException("Quantidade não pode ser negativa");
        this.valor = valor;
    }

    public static Quantidade of(int valor) { return new Quantidade(valor); }
    public static Quantidade zero() { return new Quantidade(0); }

    public Quantidade somar(Quantidade outra)    { return new Quantidade(this.valor + outra.valor); }
    public Quantidade subtrair(Quantidade outra) { return new Quantidade(this.valor - outra.valor); }
    public boolean maiorQue(Quantidade outra)    { return this.valor > outra.valor; }

    @Override public boolean equals(Object o) {
        return o instanceof Quantidade q && valor == q.valor;
    }
    @Override public int hashCode() { return Objects.hash(valor); }
}
