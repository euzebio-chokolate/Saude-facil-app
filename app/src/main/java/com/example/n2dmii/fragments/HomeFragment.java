package com.example.n2dmii.fragments;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.n2dmii.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class HomeFragment extends Fragment {
    /* Associa o Fragment ao layout XML e permite sua recriação pelo Android. */
    public HomeFragment() {
        super(R.layout.fragment_home);
    }

    /* Vincula os componentes após a criação da View e carrega os textos dos resources. */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        TextView titulo = view.findViewById(R.id.text_title);
        TextView descricao = view.findViewById(R.id.text_description);
        titulo.setText(R.string.app_name);
        descricao.setText(R.string.home_greeting);
        configurarAtalho(view, R.id.action_units, R.id.nav_unidades);
        configurarAtalho(view, R.id.action_aid, R.id.nav_socorros);
        configurarAtalho(view, R.id.action_map, R.id.nav_mapa);
    }

    /* Reutiliza a navegação existente para abrir as abas a partir dos cartões. */
    private void configurarAtalho(View raiz, int cartaoId, int abaId) {
        View cartao = raiz.findViewById(cartaoId);
        cartao.setOnClickListener(view -> {
            BottomNavigationView navegacao = requireActivity().findViewById(R.id.bottom_navigation);
            navegacao.setSelectedItemId(abaId);
        });
    }
}
