package com.example.n2dmii.database;

import android.content.Context;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import com.example.n2dmii.models.UnidadeSaude;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import java.util.List;
import java.util.UUID;
import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class DatabaseHelperTest {
    private Context context;
    private String bancoTeste;
    private DatabaseHelper banco;

    /* Cria um arquivo exclusivo por teste, sem usar o banco real do aplicativo. */
    @Before
    public void preparar() {
        context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        bancoTeste = "teste_unidades_" + UUID.randomUUID() + ".db";
        banco = new DatabaseHelper(context, bancoTeste);
    }

    /* Remove somente o arquivo temporário criado pelo próprio teste. */
    @After
    public void limpar() {
        banco.close();
        context.deleteDatabase(bancoTeste);
    }

    /* Confere quantidade, ordem, coordenadas e telefone ausente dos dados fornecidos. */
    @Test
    public void carregaDezUnidadesNaOrdemOriginal() {
        List<UnidadeSaude> unidades = banco.listar();
        assertEquals(10, unidades.size());
        for (int i = 0; i < unidades.size(); i++) {
            assertEquals(i + 1, unidades.get(i).getId());
            assertEquals("", unidades.get(i).getTelefone());
        }
        assertEquals("HUERB - Hospital de Urgência e Emergência de Rio Branco", unidades.get(0).getNome());
        assertEquals("URAP Dra. Cláudia Vitorino", unidades.get(4).getNome());
        assertEquals(-9.943, unidades.get(4).getLatitude(), 0.0000001);
        assertEquals(-67.866, unidades.get(4).getLongitude(), 0.0000001);
        assertEquals("07:00 às 17:00", unidades.get(9).getHora());
    }

    /* Excluir e reabrir não deve reinserir os dados nem duplicar as unidades restantes. */
    @Test
    public void reabrirNaoRepeteCargaInicial() {
        assertEquals(10, banco.listar().size());
        assertEquals(1, banco.excluir(3));
        banco.close();
        banco = new DatabaseHelper(context, bancoTeste);
        assertEquals(9, banco.listar().size());
        assertEquals(4, banco.listar().get(2).getId());
    }

    /* Exercita CRUD e valores com aspas sem permitir que alterem outras linhas. */
    @Test
    public void crudVinculaValoresESelecionaSomenteIdSolicitado() {
        banco.listar();
        String nome = "Unidade D'Ávila'); DELETE FROM unidades; --";
        long id = banco.cadastrar(new UnidadeSaude(0, "USF", nome, "Rua de teste",
                "08:00 às 17:00", -9.9, -67.8, "6833330000"));
        assertEquals(11, banco.listar().size());
        assertEquals(nome, banco.listar().get(10).getNome());
        assertEquals(1, banco.atualizar(new UnidadeSaude(id, "USF", "Atualizada",
                "Novo endereço", "24 horas", -9.8, -67.7, "")));
        assertEquals("Atualizada", banco.listar().get(10).getNome());
        assertEquals("UPA da Sobral", banco.listar().get(1).getNome());
        assertEquals(0, banco.excluir(-1));
        assertEquals(1, banco.excluir(id));
        assertEquals(10, banco.listar().size());
    }
}
