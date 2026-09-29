package com.example.n2dmii.utils;

import com.example.n2dmii.models.UnidadeSaude;
import org.junit.Test;
import java.util.Arrays;
import java.util.List;
import static org.junit.Assert.*;

public class PesquisaUnidadesTest {
    private final List<UnidadeSaude> unidades = Arrays.asList(
            new UnidadeSaude(1, "URAP", "URAP São Francisco", "Rua Joaquim, Bosque",
                    "24 horas", -9, -67, ""),
            new UnidadeSaude(2, "UPA", "UPA Sobral", "Estrada da Sobral",
                    "24 horas", -9, -67, ""));

    /* Permite digitação sem acentos, espaços externos e diferenças de caixa. */
    @Test
    public void pesquisaNormalizaNome() {
        assertEquals(1, PesquisaUnidades.filtrar(unidades, " SAO FRANCISCO ").size());
        assertEquals(1, PesquisaUnidades.filtrar(unidades, "São").get(0).getId());
    }

    /* Inclui endereço e tipo na busca e restaura a lista ao limpar o campo. */
    @Test
    public void pesquisaEnderecoTipoEVazio() {
        assertEquals(1, PesquisaUnidades.filtrar(unidades, "bosque").size());
        assertEquals(2, PesquisaUnidades.filtrar(unidades, "upa").get(0).getId());
        assertEquals(2, PesquisaUnidades.filtrar(unidades, "").size());
        assertTrue(PesquisaUnidades.filtrar(unidades, "inexistente").isEmpty());
    }

    /* Trata caracteres SQL como texto comum, sem retornar registros indevidos. */
    @Test
    public void caracteresEspeciaisSaoLiterais() {
        assertTrue(PesquisaUnidades.filtrar(unidades, "%").isEmpty());
        assertTrue(PesquisaUnidades.filtrar(unidades, "' OR 1=1 --").isEmpty());
    }
}
