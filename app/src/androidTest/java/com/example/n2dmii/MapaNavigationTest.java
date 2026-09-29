package com.example.n2dmii;

import android.os.SystemClock;
import android.view.View;
import android.widget.Spinner;
import android.widget.TextView;
import org.maplibre.android.maps.MapView;
import org.maplibre.android.geometry.LatLng;
import androidx.recyclerview.widget.RecyclerView;
import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import com.example.n2dmii.fragments.MapaFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import org.junit.Test;
import org.junit.runner.RunWith;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Predicate;
import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class MapaNavigationTest {
    /* Verifica o botão real da lista, a seleção interna, recriação e escolha de outras unidades. */
    @Test
    public void verAbreAbaInternaEPermiteTrocarUnidade() {
        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {
            scenario.onActivity(activity -> {
                BottomNavigationView barra = activity.findViewById(R.id.bottom_navigation);
                barra.setSelectedItemId(R.id.nav_unidades);
            });
            aguardar(scenario, activity -> {
                RecyclerView lista = activity.findViewById(R.id.unit_list);
                return lista != null && lista.findViewHolderForAdapterPosition(0) != null;
            });
            scenario.onActivity(activity -> {
                RecyclerView lista = activity.findViewById(R.id.unit_list);
                lista.findViewHolderForAdapterPosition(0).itemView.findViewById(R.id.unit_map).performClick();
            });
            aguardarNome(scenario, "HUERB - Hospital de Urgência e Emergência de Rio Branco");
            scenario.onActivity(activity -> {
                assertTrue(activity.getSupportFragmentManager().findFragmentById(R.id.fragment_container)
                        instanceof MapaFragment);
                BottomNavigationView barra = activity.findViewById(R.id.bottom_navigation);
                assertEquals(R.id.nav_mapa, barra.getSelectedItemId());
            });
            scenario.recreate();
            aguardarNome(scenario, "HUERB - Hospital de Urgência e Emergência de Rio Branco");
            scenario.onActivity(activity -> {
                Spinner seletor = activity.findViewById(R.id.map_unit_selector);
                assertEquals(11, seletor.getCount());
                seletor.setSelection(3);
            });
            aguardarNome(scenario, "UPA Cidade do Povo");
            aguardarCamera(scenario, -10.0285, -67.734);
            scenario.onActivity(activity -> activity.findViewById(R.id.map_show_all).performClick());
            aguardar(scenario, activity -> {
                Spinner seletor = activity.findViewById(R.id.map_unit_selector);
                return seletor.getSelectedItemPosition() == 0
                        && activity.findViewById(R.id.map_selected_card).getVisibility() == View.GONE;
            });
        }
    }

    /* Confere o estilo nativo e a centralização real, sem precisar de chave de API. */
    private void aguardarCamera(ActivityScenario<MainActivity> scenario, double latitude, double longitude) {
        AtomicBoolean cameraCorreta = new AtomicBoolean(false);
        aguardar(scenario, activity -> {
            MapView mapa = activity.findViewById(R.id.map_container);
            mapa.getMapAsync(map -> {
                LatLng centro = map.getCameraPosition().target;
                cameraCorreta.set(map.getStyle() != null && centro != null
                        && Math.abs(centro.getLatitude() - latitude) < 0.0001
                        && Math.abs(centro.getLongitude() - longitude) < 0.0001);
            });
            return cameraCorreta.get();
        });
    }

    /* Aguarda a leitura assíncrona do SQLite e confere a unidade mostrada no cartão. */
    private void aguardarNome(ActivityScenario<MainActivity> scenario, String nome) {
        aguardar(scenario, activity -> {
            TextView titulo = activity.findViewById(R.id.map_selected_name);
            return titulo != null && nome.contentEquals(titulo.getText());
        });
    }

    /* Consulta o estado na thread principal com prazo limitado, sem bloquear a interface. */
    private void aguardar(ActivityScenario<MainActivity> scenario, Predicate<MainActivity> condicao) {
        long limite = SystemClock.elapsedRealtime() + 10000;
        AtomicBoolean pronto = new AtomicBoolean(false);
        while (SystemClock.elapsedRealtime() < limite) {
            scenario.onActivity(activity -> pronto.set(condicao.test(activity)));
            if (pronto.get()) return;
            SystemClock.sleep(50);
        }
        fail("A interface não apresentou o estado esperado dentro do prazo");
    }
}
