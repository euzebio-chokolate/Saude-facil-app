package com.example.n2dmii.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.example.n2dmii.R;
import com.example.n2dmii.models.UnidadeSaude;
import java.util.ArrayList;
import java.util.List;

public class UnidadeAdapter extends RecyclerView.Adapter<UnidadeAdapter.UnidadeViewHolder> {
    public interface Acoes {
        /* Solicita a abertura da localização da unidade selecionada. */
        void abrirMapa(UnidadeSaude unidade);
        /* Solicita a abertura do discador somente para um telefone informado. */
        void abrirTelefone(UnidadeSaude unidade);
    }

    private final List<UnidadeSaude> unidades = new ArrayList<>();
    private final Acoes acoes;

    /* Recebe as ações da tela; o Adapter cuida somente da apresentação. */
    public UnidadeAdapter(Acoes acoes) {
        this.acoes = acoes;
        setHasStableIds(true);
    }

    /* Atualiza a pequena lista local após o carregamento ou a pesquisa. */
    public void atualizar(List<UnidadeSaude> dados) {
        unidades.clear();
        unidades.addAll(dados);
        notifyDataSetChanged();
    }

    /* Infla o cartão XML usado por cada linha do RecyclerView. */
    @NonNull
    @Override
    public UnidadeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new UnidadeViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_unidade, parent, false));
    }

    /* Preenche todos os campos e redefine os listeners para evitar dados de linhas recicladas. */
    @Override
    public void onBindViewHolder(@NonNull UnidadeViewHolder holder, int position) {
        UnidadeSaude unidade = unidades.get(position);
        holder.tipo.setText(unidade.getTipo());
        boolean hospital = "HOSPITAL".equals(unidade.getTipo());
        holder.tipo.setBackgroundResource(hospital ? R.drawable.bg_tipo_hospital : R.drawable.bg_icon);
        holder.tipo.setTextColor(ContextCompat.getColor(holder.itemView.getContext(),
                hospital ? R.color.unit_hospital_text : R.color.primary));
        holder.nome.setText(unidade.getNome());
        holder.endereco.setText(unidade.getEndereco());
        holder.hora.setText(unidade.getHora());
        boolean temTelefone = !unidade.getTelefone().trim().isEmpty();
        holder.telefone.setVisibility(temTelefone ? View.VISIBLE : View.GONE);
        holder.telefone.setText(unidade.getTelefone());
        holder.telefone.setOnClickListener(temTelefone ? view -> acoes.abrirTelefone(unidade) : null);
        holder.mapa.setContentDescription(holder.itemView.getContext()
                .getString(R.string.unit_view_map_description, unidade.getNome()));
        holder.mapa.setOnClickListener(view -> acoes.abrirMapa(unidade));
    }

    /* Retorna a quantidade filtrada de unidades. */
    @Override
    public int getItemCount() { return unidades.size(); }

    /* Mantém a identidade de cada unidade durante alterações na pesquisa. */
    @Override
    public long getItemId(int position) { return unidades.get(position).getId(); }

    static class UnidadeViewHolder extends RecyclerView.ViewHolder {
        final TextView tipo, nome, endereco, hora, telefone;
        final View mapa;

        /* Vincula os componentes do cartão uma única vez com findViewById. */
        UnidadeViewHolder(View view) {
            super(view);
            tipo = view.findViewById(R.id.unit_type);
            nome = view.findViewById(R.id.unit_name);
            endereco = view.findViewById(R.id.unit_address);
            hora = view.findViewById(R.id.unit_hours);
            telefone = view.findViewById(R.id.unit_phone);
            mapa = view.findViewById(R.id.unit_map);
        }
    }
}
