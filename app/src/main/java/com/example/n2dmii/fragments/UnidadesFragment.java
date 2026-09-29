package com.example.n2dmii.fragments;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.n2dmii.R;
import com.example.n2dmii.MainActivity;
import com.example.n2dmii.adapters.UnidadeAdapter;
import com.example.n2dmii.database.DatabaseHelper;
import com.example.n2dmii.models.UnidadeSaude;
import com.example.n2dmii.utils.PesquisaUnidades;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class UnidadesFragment extends Fragment implements UnidadeAdapter.Acoes {
    private final List<UnidadeSaude> todas = new ArrayList<>();
    private final Handler principal = new Handler(Looper.getMainLooper());
    private ExecutorService executor;
    private UnidadeAdapter adapter;
    private EditText pesquisa;
    private TextView status;
    private boolean carregando;
    private boolean erroCarregamento;

    /* Associa a tela à interface XML de pesquisa e lista. */
    public UnidadesFragment() {
        super(R.layout.fragment_unidades);
    }

    /* Configura os componentes e inicia a leitura do SQLite fora da thread da interface. */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        pesquisa = view.findViewById(R.id.unit_search);
        status = view.findViewById(R.id.unit_status);
        RecyclerView lista = view.findViewById(R.id.unit_list);
        lista.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new UnidadeAdapter(this);
        adapter.setStateRestorationPolicy(RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY);
        lista.setAdapter(adapter);
        pesquisa.addTextChangedListener(new TextWatcher() {
            /* A pesquisa só é executada após a alteração completa do campo. */
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            /* Não há ação intermediária durante a edição. */
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { }
            /* Refaz o filtro com o conteúdo atual, inclusive após restauração da tela. */
            @Override public void afterTextChanged(Editable s) { filtrar(); }
        });
        carregando = true;
        erroCarregamento = false;
        filtrar();
        Context appContext = requireContext().getApplicationContext();
        executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> carregarUnidades(appContext, view));
    }

    /* Abre e fecha o helper no mesmo trabalhador; ignora respostas de uma View destruída. */
    private void carregarUnidades(Context context, View viewOriginal) {
        try (DatabaseHelper banco = new DatabaseHelper(context)) {
            List<UnidadeSaude> dados = banco.listar();
            principal.post(() -> {
                if (getView() != viewOriginal) return;
                todas.clear();
                todas.addAll(dados);
                carregando = false;
                filtrar();
            });
        } catch (RuntimeException erro) {
            principal.post(() -> {
                if (getView() != viewOriginal) return;
                carregando = false;
                erroCarregamento = true;
                filtrar();
            });
        }
    }

    /* Filtra os dez registros em memória e informa carregamento, erro ou lista vazia. */
    private void filtrar() {
        if (adapter == null) return;
        List<UnidadeSaude> resultado = PesquisaUnidades.filtrar(todas, pesquisa.getText().toString());
        adapter.atualizar(resultado);
        if (carregando) {
            status.setText(R.string.unit_loading);
        } else if (erroCarregamento) {
            status.setText(R.string.unit_load_error);
        } else {
            status.setText(R.string.unit_empty);
        }
        status.setVisibility(carregando || erroCarregamento || resultado.isEmpty()
                ? View.VISIBLE : View.GONE);
    }

    /* Abre a aba interna de mapa e transmite o ID da unidade escolhida. */
    @Override
    public void abrirMapa(UnidadeSaude unidade) {
        ((MainActivity) requireActivity()).abrirUnidadeNoMapa(unidade.getId());
    }

    /* Abre apenas o discador; ACTION_DIAL não realiza ligação automática nem exige CALL_PHONE. */
    @Override
    public void abrirTelefone(UnidadeSaude unidade) {
        if (unidade.getTelefone().trim().isEmpty()) return;
        abrirIntent(new Intent(Intent.ACTION_DIAL,
                Uri.fromParts("tel", unidade.getTelefone(), null)), R.string.unit_no_dialer);
    }

    /* Trata dispositivos sem aplicativo capaz de atender ao Intent. */
    private void abrirIntent(Intent intent, int mensagemErro) {
        try {
            startActivity(intent);
        } catch (ActivityNotFoundException erro) {
            Toast.makeText(requireContext(), mensagemErro, Toast.LENGTH_LONG).show();
        }
    }

    /* Libera a lista e as referências da interface ao sair da aba. */
    @Override
    public void onDestroyView() {
        RecyclerView lista = requireView().findViewById(R.id.unit_list);
        lista.setAdapter(null);
        executor.shutdown();
        adapter = null;
        pesquisa = null;
        status = null;
        todas.clear();
        super.onDestroyView();
    }
}
