package com.example.n2dmii.fragments;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.n2dmii.R;

public class MapaFragment extends Fragment {
    /* Associa o Fragment ao layout XML e permite sua recriação pelo Android. */
    public MapaFragment() {
        super(R.layout.fragment_mapa);
    }

    /* Vincula os componentes após a criação da View e carrega os textos dos resources. */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        TextView titulo = view.findViewById(R.id.text_title);
        TextView descricao = view.findViewById(R.id.text_description);
        titulo.setText(R.string.mapa_title);
        descricao.setText(R.string.map_pending);
    }
}
