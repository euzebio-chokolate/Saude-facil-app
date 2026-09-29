package com.example.n2dmii.utils;

import com.example.n2dmii.models.UnidadeSaude;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class PesquisaUnidades {
    /* Classe utilitária: não necessita de instâncias. */
    private PesquisaUnidades() { }

    /* Pesquisa localmente por nome, tipo ou endereço, sem executar SQL com texto digitado. */
    public static List<UnidadeSaude> filtrar(List<UnidadeSaude> unidades, String consulta) {
        String termo = normalizar(consulta);
        List<UnidadeSaude> resultado = new ArrayList<>();
        for (UnidadeSaude unidade : unidades) {
            String texto = unidade.getNome() + " " + unidade.getTipo() + " " + unidade.getEndereco();
            if (normalizar(texto).contains(termo)) {
                resultado.add(unidade);
            }
        }
        return resultado;
    }

    /* Ignora acentos, maiúsculas e espaços externos ao comparar os textos. */
    private static String normalizar(String texto) {
        return Normalizer.normalize(texto.trim(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "").toLowerCase(Locale.ROOT);
    }
}
