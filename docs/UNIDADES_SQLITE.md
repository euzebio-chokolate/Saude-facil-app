# Unidades fornecidas — Java, SQLite e RecyclerView

> Atualização posterior: o botão Ver agora navega para a aba interna Mapa. Consulte [MAPA_INTEGRADO.md](MAPA_INTEGRADO.md) para o comportamento e os arquivos atualizados.

Esta entrega implementa especificamente os dez registros enviados pelo usuário e a aba Unidades. Os dados já estão aplicados no projeto; não é necessário copiar os arquivos abaixo. As outras funcionalidades continuam aguardando suas respectivas etapas e seu teste.

## Dados e comportamento

- Foram preservados IDs de 1 a 10, tipos, nomes, endereços, horários, latitudes e longitudes, na ordem enviada. Os dados são de referência fornecidos para o projeto, sem verificação externa nesta entrega.
- Os registros ficam em `res/xml/unidades_iniciais.xml`, sem JavaScript. O SQLiteOpenHelper os insere somente em `onCreate`, na primeira criação do banco `saude_facil.db`.
- Reabrir a tela, girar o aparelho ou reiniciar o app não repete a carga inicial. Alterar o XML posteriormente não modifica um banco já criado; novas versões precisarão de migração explícita.
- A lista usa RecyclerView e Adapter próprio com cartões no estilo da referência: tipo, nome, endereço, horário e botão Ver.
- A pesquisa filtra nome, tipo e endereço, sem diferenciar maiúsculas e acentos. `claudia`, `sao francisco`, `UPA` e `bosque` são exemplos. O texto digitado é comparado em memória, não concatenado em SQL.
- Ver abre as coordenadas em um aplicativo externo usando ACTION_VIEW e URI geo. Não depende da chave do Google Maps e não pede a localização do usuário. A aba Mapa integrada continua para a etapa do Maps SDK.
- Não foram fornecidos telefones. O campo fica vazio no banco e não é exibido. O Adapter já permite abrir ACTION_DIAL caso um telefone seja cadastrado no futuro, sem ligação automática e sem CALL_PHONE.
- O helper oferece cadastrar, listar, atualizar e excluir. IDs de novos cadastros são gerados pelo SQLite. Atualizações e exclusões usam parâmetros; valores são gravados com ContentValues. Não foi criada uma interface administrativa de CRUD.
- A abertura e leitura do banco são feitas fora da thread principal. Cursor e helper são fechados; respostas de leituras de uma View destruída são ignoradas.
- MainActivity passou a considerar o teclado nos insets para manter a lista visível durante a pesquisa.
- HomeFragment mantém seu atalho já existente para Unidades; os registros pertencem à aba Unidades, não à tela inicial.

## Arquitetura desta entrega

```text
res/xml/unidades_iniciais.xml
          ↓ primeira criação
database/DatabaseHelper.java → models/UnidadeSaude.java
          ↓ leitura em segundo plano
fragments/UnidadesFragment.java
          ↓ pesquisa por nome, tipo e endereço
utils/PesquisaUnidades.java
          ↓
adapters/UnidadeAdapter.java → layout/item_unidade.xml
```

Dependência adicionada: `androidx.recyclerview:recyclerview:1.3.2`, declarada pelo catálogo Gradle. O restante da configuração Java/XML e das permissões permanece igual.

Referências de implementação: [SQLite no Android](https://developer.android.com/training/data-storage/sqlite) e [RecyclerView com Views](https://developer.android.com/develop/ui/views/layout/recyclerview).

## Como testar

1. Sincronize o Gradle e execute `app`.
2. Abra Unidades pela barra inferior ou pelo atalho do Início.
3. Confira os dez registros rolando até USF Francisca Barbosa Guerra.
4. Pesquise `claudia`, `sao francisco`, `UPA` e `bosque`. Confira nome/endereço correspondente; `UPA` retorna as duas UPAs.
5. Digite um termo inexistente e confira a mensagem de lista vazia. Limpe o campo e confira a lista completa.
6. Gire o aparelho com uma pesquisa preenchida. O Android deve restaurar o campo e a lista deve aplicar o filtro novamente.
7. Toque em Ver para abrir uma unidade em um aplicativo de mapas. Sem aplicativo compatível, a tela mostra uma mensagem e continua funcionando.
8. Feche e reabra o app: os dados devem permanecer sem duplicação.

Os testes de banco usam um arquivo temporário exclusivo e não apagam o banco real do aplicativo. Não é necessário limpar os dados do app para testar a primeira instalação desta funcionalidade.

## Arquivos completos

Os caminhos abaixo são relativos à raiz do projeto.


### Criar: app/src/main/res/xml/unidades_iniciais.xml

```xml
<?xml version="1.0" encoding="utf-8"?>
<!-- Dados fornecidos pelo responsável pelo projeto; carregados somente na criação do banco. -->
<unidades>
    <unidade id="1" tipo="HOSPITAL" nome="HUERB - Hospital de Urgência e Emergência de Rio Branco"
        endereco="Rua Alvorada, Bosque, Rio Branco, AC" hora="24 horas"
        latitude="-9.974" longitude="-67.8107" />
    <unidade id="2" tipo="UPA" nome="UPA da Sobral"
        endereco="Estrada da Sobral, Rio Branco, AC" hora="24 horas"
        latitude="-9.9953" longitude="-67.8258" />
    <unidade id="3" tipo="UPA" nome="UPA Cidade do Povo"
        endereco="Cidade do Povo, Rio Branco, AC" hora="24 horas"
        latitude="-10.0285" longitude="-67.734" />
    <unidade id="4" tipo="URAP" nome="URAP São Francisco"
        endereco="Rua Joaquim Macedo, 26, São Francisco, Rio Branco, AC" hora="07:00 às 19:00"
        latitude="-9.957" longitude="-67.82" />
    <unidade id="5" tipo="URAP" nome="URAP Dra. Cláudia Vitorino"
        endereco="Rua Baguari, 40, Taquari, Rio Branco, AC" hora="07:00 às 17:00"
        latitude="-9.943" longitude="-67.866" />
    <unidade id="6" tipo="URAP" nome="URAP Vila Ivonete"
        endereco="Av. Antônio da Rocha Viana, Vila Ivonete, Rio Branco, AC" hora="07:00 às 18:00"
        latitude="-9.961" longitude="-67.795" />
    <unidade id="7" tipo="URAP" nome="URAP Francisco Roney Rodrigues Meireles"
        endereco="Rua Arara, 132, Adalberto Sena, Rio Branco, AC" hora="07:00 às 18:00"
        latitude="-9.9585" longitude="-67.8017" />
    <unidade id="8" tipo="URAP" nome="URAP Augusto Hidalgo de Lima"
        endereco="Travessa Tiao Natureza, 271, Bahia, Rio Branco, AC" hora="07:00 às 19:00"
        latitude="-9.97" longitude="-67.835" />
    <unidade id="9" tipo="USF" nome="USF Base"
        endereco="Rua Estado do Acre, 200, Base, Rio Branco, AC" hora="07:00 às 17:00"
        latitude="-9.975" longitude="-67.815" />
    <unidade id="10" tipo="USF" nome="USF Francisca Barbosa Guerra"
        endereco="Travessa Comara, 8, Comara, Rio Branco, AC" hora="07:00 às 17:00"
        latitude="-9.99" longitude="-67.81" />
</unidades>
```

### Criar: app/src/main/java/com/example/n2dmii/models/UnidadeSaude.java

```java
package com.example.n2dmii.models;

public class UnidadeSaude {
    private final long id;
    private final String tipo, nome, endereco, hora, telefone;
    private final double latitude, longitude;

    /* Representa uma unidade; telefone vazio significa que o número não foi informado. */
    public UnidadeSaude(long id, String tipo, String nome, String endereco, String hora,
                        double latitude, double longitude, String telefone) {
        this.id = id;
        this.tipo = tipo;
        this.nome = nome;
        this.endereco = endereco;
        this.hora = hora;
        this.latitude = latitude;
        this.longitude = longitude;
        this.telefone = telefone == null ? "" : telefone;
    }

    /* Retorna o campo id sem alterar os dados do modelo. */
    public long getId() { return id; }

    /* Retorna o campo tipo sem alterar os dados do modelo. */
    public String getTipo() { return tipo; }

    /* Retorna o campo nome sem alterar os dados do modelo. */
    public String getNome() { return nome; }

    /* Retorna o campo endereco sem alterar os dados do modelo. */
    public String getEndereco() { return endereco; }

    /* Retorna o campo hora sem alterar os dados do modelo. */
    public String getHora() { return hora; }

    /* Retorna o campo latitude sem alterar os dados do modelo. */
    public double getLatitude() { return latitude; }

    /* Retorna o campo longitude sem alterar os dados do modelo. */
    public double getLongitude() { return longitude; }

    /* Retorna o campo telefone sem alterar os dados do modelo. */
    public String getTelefone() { return telefone; }
}
```

### Criar: app/src/main/java/com/example/n2dmii/database/DatabaseHelper.java

```java
package com.example.n2dmii.database;

import android.content.ContentValues;
import android.content.Context;
import android.content.res.XmlResourceParser;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.example.n2dmii.R;
import com.example.n2dmii.models.UnidadeSaude;
import org.xmlpull.v1.XmlPullParser;
import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String BANCO = "saude_facil.db";
    private static final int VERSAO = 1;
    private static final String TABELA = "unidades";
    private final Context context;

    /* Usa o contexto da aplicação para que o banco não retenha uma Activity. */
    public DatabaseHelper(Context context) {
        this(context, BANCO);
    }

    /* Permite aos testes do mesmo pacote usar um arquivo isolado, sem tocar no banco real. */
    DatabaseHelper(Context context, String nomeBanco) {
        super(context.getApplicationContext(), nomeBanco, null, VERSAO);
        this.context = context.getApplicationContext();
    }

    /* Cria a tabela e insere o conjunto inicial uma única vez, na transação do helper. */
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE unidades (id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "tipo TEXT NOT NULL, nome TEXT NOT NULL, endereco TEXT NOT NULL, "
                + "hora TEXT NOT NULL, latitude REAL NOT NULL, longitude REAL NOT NULL, "
                + "telefone TEXT NOT NULL DEFAULT '')");
        try (XmlResourceParser xml = context.getResources().getXml(R.xml.unidades_iniciais)) {
            int evento;
            while ((evento = xml.next()) != XmlPullParser.END_DOCUMENT) {
                if (evento == XmlPullParser.START_TAG && "unidade".equals(xml.getName())) {
                    UnidadeSaude unidade = new UnidadeSaude(
                            Long.parseLong(xml.getAttributeValue(null, "id")),
                            xml.getAttributeValue(null, "tipo"),
                            xml.getAttributeValue(null, "nome"),
                            xml.getAttributeValue(null, "endereco"),
                            xml.getAttributeValue(null, "hora"),
                            Double.parseDouble(xml.getAttributeValue(null, "latitude")),
                            Double.parseDouble(xml.getAttributeValue(null, "longitude")), "");
                    ContentValues dados = valores(unidade);
                    dados.put("id", unidade.getId());
                    db.insertOrThrow(TABELA, null, dados);
                }
            }
        } catch (Exception erro) {
            throw new IllegalStateException("Falha ao carregar as unidades iniciais", erro);
        }
    }

    /* Exige uma migração explícita em versões futuras; nunca apaga dados automaticamente. */
    @Override
    public void onUpgrade(SQLiteDatabase db, int antiga, int nova) {
        throw new IllegalStateException("Migração não implementada: " + antiga + " -> " + nova);
    }

    /* Cadastra com ID gerado pelo SQLite e valores vinculados, sem concatenar entradas. */
    public long cadastrar(UnidadeSaude unidade) {
        return getWritableDatabase().insertOrThrow(TABELA, null, valores(unidade));
    }

    /* Lê na ordem dos IDs fornecidos e fecha o cursor mesmo se ocorrer uma exceção. */
    public List<UnidadeSaude> listar() {
        List<UnidadeSaude> unidades = new ArrayList<>();
        try (Cursor cursor = getReadableDatabase().query(TABELA,
                new String[]{"id", "tipo", "nome", "endereco", "hora", "latitude", "longitude", "telefone"},
                null, null, null, null, "id ASC")) {
            while (cursor.moveToNext()) {
                unidades.add(new UnidadeSaude(cursor.getLong(0), cursor.getString(1),
                        cursor.getString(2), cursor.getString(3), cursor.getString(4),
                        cursor.getDouble(5), cursor.getDouble(6), cursor.getString(7)));
            }
        }
        return unidades;
    }

    /* Atualiza somente o ID solicitado; o valor da condição é passado como parâmetro. */
    public int atualizar(UnidadeSaude unidade) {
        return getWritableDatabase().update(TABELA, valores(unidade),
                "id = ?", new String[]{String.valueOf(unidade.getId())});
    }

    /* Exclui somente o ID informado, sem reinserir os dados iniciais ao reabrir o banco. */
    public int excluir(long id) {
        return getWritableDatabase().delete(TABELA, "id = ?", new String[]{String.valueOf(id)});
    }

    /* Prepara os campos para insert/update usando a vinculação de ContentValues. */
    private ContentValues valores(UnidadeSaude unidade) {
        ContentValues dados = new ContentValues();
        dados.put("tipo", unidade.getTipo());
        dados.put("nome", unidade.getNome());
        dados.put("endereco", unidade.getEndereco());
        dados.put("hora", unidade.getHora());
        dados.put("latitude", unidade.getLatitude());
        dados.put("longitude", unidade.getLongitude());
        dados.put("telefone", unidade.getTelefone());
        return dados;
    }
}
```

### Criar: app/src/main/java/com/example/n2dmii/utils/PesquisaUnidades.java

```java
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
```

### Criar: app/src/main/java/com/example/n2dmii/adapters/UnidadeAdapter.java

```java
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
```

### Modificar: app/src/main/java/com/example/n2dmii/fragments/UnidadesFragment.java

```java
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

    /* Abre as coordenadas fornecidas em aplicativo externo, sem solicitar localização. */
    @Override
    public void abrirMapa(UnidadeSaude unidade) {
        String coordenadas = unidade.getLatitude() + "," + unidade.getLongitude();
        Uri uri = Uri.parse("geo:" + coordenadas + "?q="
                + Uri.encode(coordenadas + "(" + unidade.getNome() + ")"));
        abrirIntent(new Intent(Intent.ACTION_VIEW, uri), R.string.unit_no_map);
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
```

### Criar: app/src/main/res/values/unidades.xml

```xml
<resources>
    <string name="unit_search_hint">Pesquise por nome ou bairro…</string>
    <string name="unit_view_map">Ver</string>
    <string name="unit_view_map_description">Ver %1$s no mapa</string>
    <string name="unit_loading">Carregando unidades…</string>
    <string name="unit_load_error">Não foi possível carregar as unidades. Abra esta aba novamente para tentar.</string>
    <string name="unit_empty">Nenhuma unidade encontrada.</string>
    <string name="unit_no_map">Nenhum aplicativo de mapas disponível neste dispositivo.</string>
    <string name="unit_no_dialer">Nenhum discador disponível neste dispositivo.</string>
    <string name="unit_reference_notice">Dados de referência. Confirme endereço e horário com a unidade antes de se deslocar.</string>
    <color name="unit_hospital_background">#FDE8E6</color>
    <color name="unit_hospital_text">#AC2C25</color>
    <dimen name="unit_touch_target">48dp</dimen>
    <dimen name="unit_small_spacing">4dp</dimen>
    <dimen name="unit_badge_size">10sp</dimen>
</resources>
```

### Criar: app/src/main/res/drawable/bg_tipo_hospital.xml

```xml
<shape xmlns:android="http://schemas.android.com/apk/res/android">
    <solid android:color="@color/unit_hospital_background" />
    <corners android:radius="@dimen/spacing_small" />
</shape>
```

### Modificar: app/src/main/res/layout/fragment_unidades.xml

```xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent" android:layout_height="match_parent"
    android:orientation="vertical" android:padding="@dimen/screen_padding">
    <TextView style="@style/CardTitle" android:text="@string/unidades_title"
        android:textSize="@dimen/title_size" />
    <EditText android:id="@+id/unit_search"
        android:layout_width="match_parent" android:layout_height="wrap_content"
        android:minHeight="@dimen/unit_touch_target"
        android:layout_marginTop="@dimen/spacing_medium"
        android:background="@drawable/bg_surface"
        android:padding="@dimen/card_padding"
        android:drawableStart="@drawable/ic_search"
        android:drawablePadding="@dimen/spacing_small"
        android:hint="@string/unit_search_hint"
        android:textColor="@color/text_primary" android:textColorHint="@color/text_secondary"
        android:textSize="@dimen/body_size" android:inputType="text"
        android:singleLine="true" android:imeOptions="actionDone" />
    <TextView style="@style/CardCaption" android:text="@string/unit_reference_notice"
        android:layout_marginTop="@dimen/spacing_small" />
    <TextView android:id="@+id/unit_status" style="@style/CardCaption"
        android:text="@string/unit_loading" android:accessibilityLiveRegion="polite"
        android:layout_marginTop="@dimen/spacing_medium" />
    <androidx.recyclerview.widget.RecyclerView android:id="@+id/unit_list"
        android:layout_width="match_parent" android:layout_height="0dp"
        android:layout_weight="1" android:layout_marginTop="@dimen/spacing_medium"
        android:clipToPadding="false" android:paddingBottom="@dimen/spacing_small" />
</LinearLayout>
```

### Criar: app/src/main/res/layout/item_unidade.xml

```xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent" android:layout_height="wrap_content"
    android:layout_marginBottom="@dimen/spacing_medium" android:padding="@dimen/card_padding"
    android:background="@drawable/bg_surface" android:orientation="horizontal"
    android:gravity="center_vertical">
    <LinearLayout android:layout_width="0dp" android:layout_height="wrap_content"
        android:layout_weight="1" android:orientation="vertical"
        android:layout_marginEnd="@dimen/spacing_small">
        <TextView android:id="@+id/unit_type"
            android:layout_width="wrap_content" android:layout_height="wrap_content"
            android:paddingStart="@dimen/spacing_small" android:paddingEnd="@dimen/spacing_small"
            android:paddingTop="@dimen/unit_small_spacing" android:paddingBottom="@dimen/unit_small_spacing"
            android:background="@drawable/bg_icon" android:textColor="@color/primary"
            android:textStyle="bold" android:textSize="@dimen/unit_badge_size" />
        <TextView android:id="@+id/unit_name" style="@style/CardTitle"
            android:layout_marginTop="@dimen/spacing_small" android:textSize="@dimen/body_size" />
        <TextView android:id="@+id/unit_address" style="@style/CardCaption"
            android:layout_marginTop="@dimen/spacing_small" />
        <TextView android:id="@+id/unit_hours" style="@style/CardCaption"
            android:layout_marginTop="@dimen/unit_small_spacing" />
        <TextView android:id="@+id/unit_phone" style="@style/CardCaption"
            android:minHeight="@dimen/unit_touch_target" android:gravity="center_vertical"
            android:clickable="true" android:focusable="true"
            android:background="?attr/selectableItemBackground" android:visibility="gone" />
    </LinearLayout>
    <androidx.appcompat.widget.AppCompatButton android:id="@+id/unit_map"
        android:layout_width="@dimen/unit_touch_target" android:layout_height="wrap_content"
        android:minHeight="@dimen/unit_touch_target" android:minWidth="@dimen/unit_touch_target"
        android:padding="@dimen/unit_small_spacing" android:background="@drawable/bg_icon"
        android:drawableTop="@drawable/ic_map" android:text="@string/unit_view_map"
        android:textAllCaps="false" android:textColor="@color/primary"
        android:textSize="@dimen/caption_size" />
</LinearLayout>
```

### Criar: app/src/androidTest/java/com/example/n2dmii/database/DatabaseHelperTest.java

```java
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
```

### Criar: app/src/test/java/com/example/n2dmii/utils/PesquisaUnidadesTest.java

```java
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
```

### Modificar: app/src/main/java/com/example/n2dmii/MainActivity.java

```java
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
            fragment = new MapaFragment();
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
```

### Modificar: app/build.gradle

```groovy
plugins {
    alias(libs.plugins.android.application)
}
android {
    namespace = 'com.example.n2dmii'
    compileSdk = 37
    defaultConfig {
        applicationId = 'com.example.n2dmii'
        minSdk = 24
        targetSdk = 37
        versionCode = 1
        versionName = '1.0'
        testInstrumentationRunner = 'androidx.test.runner.AndroidJUnitRunner'
    }
    buildTypes {
        release {
            minifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = false
        viewBinding = false
    }
}
dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.fragment)
    implementation(libs.recyclerview)
    testImplementation(libs.junit)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(libs.ext.junit)
}
```

### Modificar: gradle/libs.versions.toml

```toml
[versions]
agp = "9.3.3"
junit = "4.13.2"
junitVersion = "1.1.5"
espressoCore = "3.5.1"
appcompat = "1.6.1"
material = "1.10.0"
activity = "1.8.0"
fragment = "1.6.2"
recyclerview = "1.3.2"

[libraries]
recyclerview = { group = "androidx.recyclerview", name = "recyclerview", version.ref = "recyclerview" }
junit = { group = "junit", name = "junit", version.ref = "junit" }
ext-junit = { group = "androidx.test.ext", name = "junit", version.ref = "junitVersion" }
espresso-core = { group = "androidx.test.espresso", name = "espresso-core", version.ref = "espressoCore" }
appcompat = { group = "androidx.appcompat", name = "appcompat", version.ref = "appcompat" }
material = { group = "com.google.android.material", name = "material", version.ref = "material" }
activity = { group = "androidx.activity", name = "activity", version.ref = "activity" }
fragment = { group = "androidx.fragment", name = "fragment", version.ref = "fragment" }

[plugins]
android-application = { id = "com.android.application", version.ref = "agp" }
```

## Resultado da validação

Comando executado: gradlew.bat :app:assembleDebug :app:lintDebug :app:testDebugUnitTest :app:connectedDebugAndroidTest --console=plain

Resultado: BUILD SUCCESSFUL. Os três novos testes locais de pesquisa e os três novos testes instrumentados de SQLite passaram, além dos dois testes de exemplo existentes. O Lint terminou sem erros, com 28 avisos. Os testes de SQLite usaram bancos exclusivos de teste.

A lista foi conferida visualmente no emulador. A pesquisa real por "claudia" retornou somente "URAP Dra. Cláudia Vitorino". O APK atualizado foi instalado no emulador.

A abertura de mapas externos, rotação e aparência em outros dispositivos continuam no roteiro de teste acima. Aguarda-se seu teste antes de prosseguir com outra etapa.
