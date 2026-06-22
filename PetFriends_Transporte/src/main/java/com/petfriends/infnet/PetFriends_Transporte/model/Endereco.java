package com.petfriends.infnet.PetFriends_Transporte.model;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Embeddable
@AllArgsConstructor
@Getter
@Setter
@NoArgsConstructor
public class Endereco {

private String logradouro;
private String numero;
private String cidade;
private String uf;
private String cep;

}
