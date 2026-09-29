package com.example.n2dmii;

import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import com.example.n2dmii.fragments.HomeFragment;
import com.example.n2dmii.fragments.MapaFragment;
import com.example.n2dmii.fragments.SobreFragment;
import com.example.n2dmii.fragments.SocorrosFragment;
import com.example.n2dmii.fragments.UnidadesFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {
    private static final String CHAVE_ABA = "aba_selecionada";
    private int abaSelecionada = R.id.nav_inicio;
    private long unidadeParaAbrir = MapaFragment.TODAS_UNIDADES;

    /* Seleciona a aba Mapa com a unidade solicitada pela lista, sem abrir outro app. */
    public void abrirUnidadeNoMapa(long unidadeId) {
        unidadeParaAbrir = unidadeId;
        BottomNavigationView navegacao = findViewById(R.id.bottom_navigation);
        if (navegacao.getSelectedItemId() == R.id.nav_mapa) {
            abrirAba(R.id.nav_mapa);
        } else {
            navegacao.setSelectedItemId(R.id.nav_mapa);
        }
    }

    /* Inicializa o XML e restaura a aba sem duplicar o Fragment recriado pelo Android. */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        setContentView(R.layout.activity_main);
        configurarMargensDoSistema();

        BottomNavigationView navegacao = findViewById(R.id.bottom_navigation);
        navegacao.setItemActiveIndicatorEnabled(false);
        if (savedInstanceState != null) {
            abaSelecionada = savedInstanceState.getInt(CHAVE_ABA, R.id.nav_inicio);
        }
        navegacao.setSelectedItemId(abaSelecionada);
        navegacao.setOnItemSelectedListener(item -> abrirAba(item.getItemId()));
        navegacao.setOnItemReselectedListener(item -> {
            /* A aba já está aberta; não recriamos seu conteúdo. */
        });
        if (savedInstanceState == null) {
            abrirAba(abaSelecionada);
        }
    }

    /* Reserva espaço para barras do sistema e recortes, inclusive ao girar o aparelho. */
    private void configurarMargensDoSistema() {
        View raiz = findViewById(R.id.main);
        boolean modoClaro = (getResources().getConfiguration().uiMode
                & Configuration.UI_MODE_NIGHT_MASK) != Configuration.UI_MODE_NIGHT_YES;
        WindowCompat.getInsetsController(getWindow(), raiz)
                .setAppearanceLightNavigationBars(modoClaro);
        ViewCompat.setOnApplyWindowInsetsListener(raiz, (view, windowInsets) -> {
            Insets margens = windowInsets.getInsets(
                    WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.displayCutout()
                            | WindowInsetsCompat.Type.ime());
            view.setPadding(margens.left, margens.top, margens.right, margens.bottom);
            return WindowInsetsCompat.CONSUMED;
        });
        ViewCompat.requestApplyInsets(raiz);
    }

    /* Seleciona a aba e troca somente o conteúdo acima da barra inferior. */
    private boolean abrirAba(int id) {
        Fragment fragment;
        if (id == R.id.nav_inicio) {
            fragment = new HomeFragment();
        } else if (id == R.id.nav_unidades) {
            fragment = new UnidadesFragment();
        } else if (id == R.id.nav_mapa) {
            fragment = MapaFragment.novaInstancia(unidadeParaAbrir);
            unidadeParaAbrir = MapaFragment.TODAS_UNIDADES;
        } else if (id == R.id.nav_socorros) {
            fragment = new SocorrosFragment();
        } else if (id == R.id.nav_sobre) {
            fragment = new SobreFragment();
        } else {
            return false;
        }
        getSupportFragmentManager().beginTransaction()
                .setReorderingAllowed(true)
                .replace(R.id.fragment_container, fragment)
                .commit();
        abaSelecionada = id;
        return true;
    }

    /* Salva a seleção para manter conteúdo e barra sincronizados após recriação. */
    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putInt(CHAVE_ABA, abaSelecionada);
        super.onSaveInstanceState(outState);
    }
}
