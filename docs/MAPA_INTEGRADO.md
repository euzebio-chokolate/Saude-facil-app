# Mapa integrado à aba do aplicativo

> Integração Google substituída por MapLibre e OpenStreetMap. Consulte [MAPA_OPENSTREETMAP.md](MAPA_OPENSTREETMAP.md). Não é mais necessário configurar a chave descrita neste registro histórico.

Esta entrega muda o botão **Ver** da lista: ele abre a aba **Mapa** do Saúde Fácil e seleciona a unidade correspondente. Não abre o aplicativo Google Maps.

## Comportamento

- O ID da unidade passa da lista para MainActivity e para os argumentos de MapaFragment.
- Todas as unidades do SQLite recebem marcadores. O XML inicial e suas coordenadas não foram alterados.
- A unidade escolhida fica em vermelho, com nome e horário na janela do marcador; as outras ficam em azul.
- A câmera centraliza a unidade escolhida. **Todas as unidades** enquadra o conjunto.
- É possível selecionar outra unidade tocando em seu marcador ou pelo seletor de nomes.
- O cartão abaixo do mapa mostra nome, endereço e horário da seleção.
- A seleção e a posição da câmera são salvas para recriação da tela. Abrir a aba diretamente inicia a visão geral.
- A barra de ferramentas do SDK que abriria mapas externos fica desativada.
- **Minha localização** solicita ACCESS_FINE_LOCATION e ACCESS_COARSE_LOCATION juntas e aceita a permissão aproximada. A recusa mantém disponíveis os marcadores das unidades. Não há localização em segundo plano.
- Sem chave configurada ou sem Google Play Services disponível, há uma mensagem de indisponibilidade; a seleção de unidades e seus dados continuam acessíveis. Não é exibido um mapa fictício.

## Configuração obrigatória do mapa real

Não havia `secrets.properties` no projeto ao implementar esta entrega. Por isso, a navegação interna e o seletor podem ser testados agora, mas os mapas reais só carregarão após a configuração abaixo.

1. No Google Cloud Console, selecione ou crie um projeto e configure o faturamento exigido pelo Google Maps Platform.
2. Habilite **Maps SDK for Android** nesse projeto.
3. Em APIs e serviços > Credenciais, crie uma chave de API.
4. Restrinja a chave a **Aplicativos Android**, com o pacote `com.example.n2dmii` e o SHA-1 do certificado utilizado para assinar o APK. Para consultar o SHA-1 de debug, execute `gradlew.bat :app:signingReport` no terminal com o JDK do Android Studio. A versão de publicação precisa do seu próprio certificado; se usar Play App Signing, configure também o certificado de assinatura da Play.
5. Restrinja o uso da chave à API **Maps SDK for Android**.
6. Na raiz do projeto, copie `secrets.properties.example` para `secrets.properties` e preencha localmente:

```properties
MAPS_API_KEY=SUA_CHAVE_REAL
```

7. Sincronize o Gradle, compile e reinstale o aplicativo. A chave é incorporada durante a compilação; reiniciar o app antigo não aplica alterações nesse arquivo.

Não envie a chave por chat nem a coloque em Java, XML ou arquivos versionados. `secrets.properties` está no `.gitignore`. O Gradle lê o arquivo local, injeta o valor no metadado `com.google.android.geo.API_KEY` e gera somente um booleano `MAPS_CONFIGURED` no BuildConfig. A chave ainda é extraível do APK; as restrições por pacote, certificado e API continuam necessárias.

Se a chave estiver preenchida mas inválida, o SDK poderá mostrar o mapa sem carregar os tiles. Confira ativação da API, faturamento, pacote, SHA-1 e restrições no Google Cloud. `MAPS_CONFIGURED` indica apenas que há um valor local, não que o Google o autenticou.

Dependências adicionadas: Maps SDK `20.0.0` e serviços de localização `21.4.0`. O projeto permanece Java, Views XML, findViewById e Gradle Groovy.

Referências oficiais: [configuração do Maps SDK](https://developers.google.com/maps/documentation/android-sdk/config), [configuração dos serviços Google](https://developers.google.com/android/guides/setup) e [permissões de localização](https://developer.android.com/develop/sensors-and-location/location/permissions/runtime).

## Roteiro de teste

1. Em Unidades, toque em **Ver** no HUERB: a aba Mapa deve ficar selecionada e o cartão deve apresentar o HUERB.
2. Com a chave válida, confira o marcador vermelho nas coordenadas fornecidas e a câmera centralizada.
3. Toque em **Todas as unidades**: todos os marcadores devem caber na área visível.
4. Escolha UPA Cidade do Povo pelo seletor e depois toque em outro marcador. Confira cartão, destaque e câmera.
5. Gire o aparelho: a unidade selecionada e a câmera devem ser mantidas.
6. Toque em **Minha localização** e negue a permissão: o mapa das unidades deve continuar utilizável.
7. Repita concedendo localização aproximada e depois precisa. Com localização do aparelho desligada ou sem posição disponível, uma mensagem deve ser exibida.
8. Alterne entre abas durante o carregamento: o aplicativo não deve fechar ou aplicar respostas a uma tela destruída.

O funcionamento dos tiles e da localização real requer chave válida, rede e dispositivo/emulador com Google Play Services. A entrega não altera faturamento, credenciais ou permissões da sua conta Google Cloud.

## Arquivos completos desta alteração

Os caminhos abaixo são relativos à raiz. A base de SQLite e a lista são as entregues em `UNIDADES_SQLITE.md`; este documento substitui o comportamento anterior de abrir mapas externos.


### Modificar: app/src/main/java/com/example/n2dmii/fragments/MapaFragment.java

```java
package com.example.n2dmii.fragments;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import com.example.n2dmii.BuildConfig;
import com.example.n2dmii.R;
import com.example.n2dmii.database.DatabaseHelper;
import com.example.n2dmii.models.UnidadeSaude;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.CancellationTokenSource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MapaFragment extends Fragment {
    public static final long TODAS_UNIDADES = -1L;
    private static final String CHAVE_UNIDADE = "unidade_id";
    private static final String CHAVE_CAMERA = "camera";
    private final List<UnidadeSaude> unidades = new ArrayList<>();
    private final Map<Long, Marker> marcadores = new HashMap<>();
    private final Handler principal = new Handler(Looper.getMainLooper());
    private long unidadeSelecionada = TODAS_UNIDADES;
    private CameraPosition cameraSalva;
    private GoogleMap mapa;
    private Spinner seletor;
    private TextView status, nome, detalhes;
    private View cartao, areaMapa;
    private Button localizacao;
    private ExecutorService executor;
    private CancellationTokenSource consultaLocalizacao;
    private final ActivityResultLauncher<String[]> permissoes = registerForActivityResult(
            new ActivityResultContracts.RequestMultiplePermissions(), resultado -> {
                /* A recusa não bloqueia a consulta às unidades nem seus marcadores. */
                if (getView() == null) return;
                if (temPermissaoLocalizacao()) {
                    localizarUsuario();
                } else {
                    mensagem(R.string.map_location_denied);
                }
            });

    /* Associa a tela ao XML e permite recriação automática pelo FragmentManager. */
    public MapaFragment() {
        super(R.layout.fragment_mapa);
    }

    /* Transporta o ID escolhido em argumentos, que sobrevivem à recriação da tela. */
    public static MapaFragment novaInstancia(long unidadeId) {
        MapaFragment fragment = new MapaFragment();
        Bundle args = new Bundle();
        args.putLong(CHAVE_UNIDADE, unidadeId);
        fragment.setArguments(args);
        return fragment;
    }

    /* Restaura a unidade e a câmera quando o Android recria a Activity. */
    @Override
    public void onCreate(@Nullable Bundle estado) {
        super.onCreate(estado);
        Bundle origem = estado != null ? estado : getArguments();
        if (origem != null) {
            unidadeSelecionada = origem.getLong(CHAVE_UNIDADE, TODAS_UNIDADES);
            cameraSalva = origem.getParcelable(CHAVE_CAMERA);
        }
    }

    /* Vincula os controles e inicia, de forma independente, o mapa e a leitura do banco. */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle estado) {
        super.onViewCreated(view, estado);
        seletor = view.findViewById(R.id.map_unit_selector);
        status = view.findViewById(R.id.map_status);
        nome = view.findViewById(R.id.map_selected_name);
        detalhes = view.findViewById(R.id.map_selected_details);
        cartao = view.findViewById(R.id.map_selected_card);
        areaMapa = view.findViewById(R.id.map_container);
        localizacao = view.findViewById(R.id.map_my_location);
        localizacao.setEnabled(false);
        view.findViewById(R.id.map_show_all).setOnClickListener(v -> selecionar(TODAS_UNIDADES));
        localizacao.setOnClickListener(v -> solicitarLocalizacao());
        inicializarMapa(view);
        carregarUnidades(view);
    }

    /* Mantém a tela utilizável sem chave ou Play Services, sem simular um mapa real. */
    private void inicializarMapa(View viewOriginal) {
        if (!BuildConfig.MAPS_CONFIGURED) {
            status.setText(R.string.map_unavailable);
            return;
        }
        if (GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(requireContext())
                != ConnectionResult.SUCCESS) {
            status.setText(R.string.map_services_unavailable);
            return;
        }
        SupportMapFragment fragment = (SupportMapFragment)
                getChildFragmentManager().findFragmentById(R.id.map_container);
        if (fragment == null) {
            fragment = SupportMapFragment.newInstance();
            getChildFragmentManager().beginTransaction()
                    .replace(R.id.map_container, fragment).commitNow();
        }
        fragment.getMapAsync(googleMap -> {
            if (getView() != viewOriginal) return;
            mapa = googleMap;
            mapa.getUiSettings().setMapToolbarEnabled(false);
            mapa.getUiSettings().setZoomControlsEnabled(true);
            mapa.getUiSettings().setMyLocationButtonEnabled(false);
            mapa.setOnMarkerClickListener(marker -> {
                Object id = marker.getTag();
                if (id instanceof Long) selecionar((Long) id);
                return true;
            });
            localizacao.setEnabled(true);
            status.setText(R.string.map_explore_hint);
            atualizarCamadaLocalizacao();
            desenharMarcadores();
        });
    }

    /* Lê o mesmo SQLite da lista, fora da thread principal, sem alterar os registros. */
    private void carregarUnidades(View viewOriginal) {
        Context context = requireContext().getApplicationContext();
        executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            try (DatabaseHelper banco = new DatabaseHelper(context)) {
                List<UnidadeSaude> dados = banco.listar();
                principal.post(() -> {
                    if (getView() != viewOriginal) return;
                    unidades.clear();
                    unidades.addAll(dados);
                    preencherSeletor();
                    atualizarCartao();
                    desenharMarcadores();
                    if (dados.isEmpty()) status.setText(R.string.map_no_units);
                });
            } catch (RuntimeException erro) {
                principal.post(() -> {
                    if (getView() == viewOriginal) status.setText(R.string.unit_load_error);
                });
            }
        });
    }

    /* Oferece todas as unidades também por nome, inclusive marcadores próximos entre si. */
    private void preencherSeletor() {
        List<String> nomes = new ArrayList<>();
        nomes.add(getString(R.string.map_all_units));
        int posicao = 0;
        for (int i = 0; i < unidades.size(); i++) {
            UnidadeSaude unidade = unidades.get(i);
            nomes.add(unidade.getNome());
            if (unidade.getId() == unidadeSelecionada) posicao = i + 1;
        }
        if (posicao == 0) unidadeSelecionada = TODAS_UNIDADES;
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_dropdown_item, nomes);
        seletor.setAdapter(adapter);
        seletor.setSelection(posicao);
        seletor.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            /* Altera a seleção apenas quando o ID realmente mudou. */
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                long selecionado = position == 0 ? TODAS_UNIDADES : unidades.get(position - 1).getId();
                if (selecionado != unidadeSelecionada) selecionar(selecionado);
            }
            /* Não há ação quando o seletor fica temporariamente sem seleção. */
            @Override public void onNothingSelected(AdapterView<?> parent) { }
        });
    }

    /* Adiciona todas as unidades e associa o ID do banco a cada marcador. */
    private void desenharMarcadores() {
        if (mapa == null || unidades.isEmpty()) return;
        mapa.clear();
        marcadores.clear();
        for (UnidadeSaude unidade : unidades) {
            Marker marker = mapa.addMarker(new MarkerOptions()
                    .position(new LatLng(unidade.getLatitude(), unidade.getLongitude()))
                    .title(unidade.getNome())
                    .snippet(unidade.getHora())
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
            if (marker != null) {
                marker.setTag(unidade.getId());
                marcadores.put(unidade.getId(), marker);
            }
        }
        atualizarDestaque();
        posicionarCamera();
    }

    /* Sincroniza seletor, cartão e marcador; permite trocar de unidade sem sair do mapa. */
    private void selecionar(long id) {
        unidadeSelecionada = id;
        cameraSalva = null;
        int posicao = 0;
        for (int i = 0; i < unidades.size(); i++) {
            if (unidades.get(i).getId() == id) posicao = i + 1;
        }
        seletor.setSelection(posicao);
        atualizarCartao();
        atualizarDestaque();
        posicionarCamera();
    }

    /* Exibe nome, endereço e horário da unidade em foco. */
    private void atualizarCartao() {
        cartao.setVisibility(View.GONE);
        for (UnidadeSaude unidade : unidades) {
            if (unidade.getId() == unidadeSelecionada) {
                nome.setText(unidade.getNome());
                detalhes.setText(getString(R.string.map_unit_details,
                        unidade.getEndereco(), unidade.getHora()));
                cartao.setVisibility(View.VISIBLE);
                break;
            }
        }
    }

    /* Usa vermelho para a unidade escolhida e azul para as demais, com nome na janela. */
    private void atualizarDestaque() {
        for (Map.Entry<Long, Marker> entry : marcadores.entrySet()) {
            boolean selecionado = entry.getKey() == unidadeSelecionada;
            Marker marker = entry.getValue();
            marker.setIcon(BitmapDescriptorFactory.defaultMarker(selecionado
                    ? BitmapDescriptorFactory.HUE_RED : BitmapDescriptorFactory.HUE_AZURE));
            if (selecionado) marker.showInfoWindow();
            else marker.hideInfoWindow();
        }
    }

    /* Centraliza a unidade ou enquadra todas, preservando a câmera após uma rotação. */
    private void posicionarCamera() {
        if (mapa == null || unidades.isEmpty()) return;
        View viewOriginal = getView();
        areaMapa.post(() -> {
            if (getView() != viewOriginal || mapa == null || areaMapa.getWidth() == 0
                    || areaMapa.getHeight() == 0) return;
            if (cameraSalva != null) {
                mapa.moveCamera(CameraUpdateFactory.newCameraPosition(cameraSalva));
                cameraSalva = null;
                return;
            }
            Marker selecionado = marcadores.get(unidadeSelecionada);
            if (selecionado != null) {
                mapa.moveCamera(CameraUpdateFactory.newLatLngZoom(selecionado.getPosition(), 15f));
            } else {
                LatLngBounds.Builder limites = new LatLngBounds.Builder();
                for (UnidadeSaude unidade : unidades) {
                    limites.include(new LatLng(unidade.getLatitude(), unidade.getLongitude()));
                }
                mapa.moveCamera(CameraUpdateFactory.newLatLngBounds(limites.build(),
                        areaMapa.getWidth(), areaMapa.getHeight(),
                        getResources().getDimensionPixelSize(R.dimen.map_bounds_padding)));
            }
        });
    }

    /* Aceita tanto localização precisa quanto aproximada. */
    private boolean temPermissaoLocalizacao() {
        Context context = getContext();
        return context != null && (ContextCompat.checkSelfPermission(context,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(context,
                Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED);
    }

    /* Solicita as duas permissões somente após o toque em Minha localização. */
    private void solicitarLocalizacao() {
        if (temPermissaoLocalizacao()) localizarUsuario();
        else permissoes.launch(new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION});
    }

    /* Atualiza a camada de localização sem impedir a exibição dos marcadores da lista. */
    @SuppressLint("MissingPermission")
    private void atualizarCamadaLocalizacao() {
        if (mapa == null) return;
        try {
            mapa.setMyLocationEnabled(temPermissaoLocalizacao());
        } catch (SecurityException erro) {
            mensagem(R.string.map_location_denied);
        }
    }

    /* Obtém uma posição atual, respeitando permissão aproximada, cancelamento e GPS indisponível. */
    @SuppressLint("MissingPermission")
    private void localizarUsuario() {
        if (!temPermissaoLocalizacao() || mapa == null) return;
        atualizarCamadaLocalizacao();
        if (consultaLocalizacao != null) consultaLocalizacao.cancel();
        consultaLocalizacao = new CancellationTokenSource();
        localizacao.setEnabled(false);
        View viewOriginal = getView();
        int prioridade = ContextCompat.checkSelfPermission(requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                ? Priority.PRIORITY_HIGH_ACCURACY : Priority.PRIORITY_BALANCED_POWER_ACCURACY;
        try {
            LocationServices.getFusedLocationProviderClient(requireContext())
                    .getCurrentLocation(prioridade, consultaLocalizacao.getToken())
                    .addOnSuccessListener(location -> {
                        if (getView() != viewOriginal || mapa == null) return;
                        localizacao.setEnabled(true);
                        if (location == null) mensagem(R.string.map_location_unavailable);
                        else mapa.animateCamera(CameraUpdateFactory.newLatLngZoom(
                                new LatLng(location.getLatitude(), location.getLongitude()), 14f));
                    })
                    .addOnFailureListener(erro -> {
                        if (getView() != viewOriginal) return;
                        localizacao.setEnabled(true);
                        mensagem(R.string.map_location_unavailable);
                    });
        } catch (SecurityException erro) {
            localizacao.setEnabled(true);
            mensagem(R.string.map_location_denied);
        }
    }

    /* Revalida a permissão ao retornar das configurações ou de outro aplicativo. */
    @Override
    public void onResume() {
        super.onResume();
        atualizarCamadaLocalizacao();
        if (localizacao != null) localizacao.setEnabled(mapa != null);
    }

    /* Cancela a consulta ao sair da tela, sem manter localização em segundo plano. */
    @Override
    public void onStop() {
        if (consultaLocalizacao != null) consultaLocalizacao.cancel();
        super.onStop();
    }

    /* Apresenta uma mensagem somente enquanto o Fragment ainda possui contexto. */
    private void mensagem(int recurso) {
        if (getContext() != null) Toast.makeText(requireContext(), recurso, Toast.LENGTH_LONG).show();
    }

    /* Salva a seleção e a posição da câmera para recriação da Activity. */
    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putLong(CHAVE_UNIDADE, unidadeSelecionada);
        if (mapa != null) outState.putParcelable(CHAVE_CAMERA, mapa.getCameraPosition());
    }

    /* Descarta referências da View e ignora respostas assíncronas após sair da aba. */
    @Override
    public void onDestroyView() {
        if (executor != null) executor.shutdown();
        if (consultaLocalizacao != null) consultaLocalizacao.cancel();
        mapa = null;
        seletor = null;
        status = null;
        nome = null;
        detalhes = null;
        cartao = null;
        areaMapa = null;
        localizacao = null;
        unidades.clear();
        marcadores.clear();
        super.onDestroyView();
    }
}
```

### Criar: app/src/main/res/values/mapa.xml

```xml
<resources>
    <string name="map_title">Mapa de unidades</string>
    <string name="map_select_label">Unidade em destaque</string>
    <string name="map_all_units">Todas as unidades</string>
    <string name="map_my_location">Minha localização</string>
    <string name="map_loading">Carregando mapa…</string>
    <string name="map_unavailable">O mapa está indisponível no momento. Você pode consultar as unidades pelo seletor acima.</string>
    <string name="map_services_unavailable">O mapa precisa do Google Play Services atualizado. As informações das unidades continuam disponíveis.</string>
    <string name="map_explore_hint">Toque nos marcadores ou escolha uma unidade acima. Use Todas as unidades para ver o conjunto.</string>
    <string name="map_no_units">Nenhuma unidade cadastrada para mostrar no mapa.</string>
    <string name="map_unit_details">%1$s\n%2$s</string>
    <string name="map_location_denied">Localização não autorizada. Você ainda pode consultar todas as unidades no mapa.</string>
    <string name="map_location_unavailable">Não foi possível obter sua localização. Verifique a localização do aparelho e tente novamente.</string>
    <dimen name="map_bounds_padding">40dp</dimen>
    <dimen name="map_details_max_height">132dp</dimen>
</resources>
```

### Modificar: app/src/main/res/layout/fragment_mapa.xml

```xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent" android:layout_height="match_parent"
    android:orientation="vertical" android:padding="@dimen/screen_padding">
    <TextView style="@style/CardTitle" android:text="@string/map_title"
        android:textSize="@dimen/title_size" />
    <TextView style="@style/CardCaption" android:text="@string/map_select_label"
        android:labelFor="@id/map_unit_selector" android:layout_marginTop="@dimen/spacing_small" />
    <Spinner android:id="@+id/map_unit_selector"
        android:layout_width="match_parent" android:layout_height="wrap_content"
        android:minHeight="@dimen/unit_touch_target"
        android:background="@drawable/bg_surface" android:spinnerMode="dialog"
        android:prompt="@string/map_select_label" />
    <LinearLayout android:layout_width="match_parent" android:layout_height="wrap_content"
        android:orientation="horizontal">
        <Button android:id="@+id/map_show_all"
            android:layout_width="0dp" android:layout_height="wrap_content"
            android:layout_weight="1" android:text="@string/map_all_units"
            android:textAllCaps="false" android:textSize="@dimen/caption_size" />
        <Button android:id="@+id/map_my_location"
            android:layout_width="0dp" android:layout_height="wrap_content"
            android:layout_weight="1" android:layout_marginStart="@dimen/spacing_small"
            android:text="@string/map_my_location" android:textAllCaps="false"
            android:textSize="@dimen/caption_size" />
    </LinearLayout>
    <TextView android:id="@+id/map_status" style="@style/CardCaption"
        android:text="@string/map_loading" android:accessibilityLiveRegion="polite"
        android:layout_marginBottom="@dimen/spacing_small" />
    <androidx.fragment.app.FragmentContainerView android:id="@+id/map_container"
        android:layout_width="match_parent" android:layout_height="0dp"
        android:layout_weight="1" android:background="@drawable/bg_surface" />
    <androidx.core.widget.NestedScrollView android:id="@+id/map_selected_card"
        android:layout_width="match_parent" android:layout_height="wrap_content"
        android:layout_marginTop="@dimen/spacing_small" android:visibility="gone"
        android:background="@drawable/bg_surface">
        <LinearLayout android:layout_width="match_parent" android:layout_height="wrap_content"
            android:orientation="vertical" android:padding="@dimen/card_padding">
            <TextView android:id="@+id/map_selected_name" style="@style/CardTitle"
                android:textSize="@dimen/body_size" />
            <TextView android:id="@+id/map_selected_details" style="@style/CardCaption"
                android:layout_marginTop="@dimen/unit_small_spacing" />
        </LinearLayout>
    </androidx.core.widget.NestedScrollView>
</LinearLayout>
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
```

### Modificar: app/src/main/AndroidManifest.xml

```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android">
    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
    <uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
    <application
        android:allowBackup="true"
        android:dataExtractionRules="@xml/data_extraction_rules"
        android:fullBackupContent="@xml/backup_rules"
        android:icon="@mipmap/ic_launcher"
        android:label="@string/app_name"
        android:roundIcon="@mipmap/ic_launcher_round"
        android:supportsRtl="true"
        android:theme="@style/Theme.SaudeFacil">
        <meta-data android:name="com.google.android.geo.API_KEY" android:value="${MAPS_API_KEY}" />
        <activity
            android:name=".MainActivity"
            android:exported="true"
            android:windowSoftInputMode="adjustResize">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>
    </application>
</manifest>
```

### Modificar: app/build.gradle

```groovy
plugins {
    alias(libs.plugins.android.application)
}

// Lê a chave de um arquivo local ignorado pelo Git, sem registrá-la nos logs.
def mapsProperties = new Properties()
mapsProperties.load(new StringReader(providers.fileContents(
        rootProject.layout.projectDirectory.file('secrets.properties')).asText.orElse('').get()))
def mapsKey = mapsProperties.getProperty('MAPS_API_KEY', '').trim()

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
        manifestPlaceholders = [MAPS_API_KEY: mapsKey]
        buildConfigField 'boolean', 'MAPS_CONFIGURED', String.valueOf(!mapsKey.isEmpty())
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
        buildConfig = true
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
    implementation(libs.maps)
    implementation(libs.location)
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
maps = { group = "com.google.android.gms", name = "play-services-maps", version = "20.0.0" }
location = { group = "com.google.android.gms", name = "play-services-location", version = "21.4.0" }
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

### Modificar: .gitignore

```gitignore
*.iml
.gradle
/local.properties
/.idea/caches
/.idea/libraries
/.idea/modules.xml
/.idea/workspace.xml
/.idea/navEditor.xml
/.idea/assetWizardSettings.xml
.DS_Store
/build
/captures
.externalNativeBuild
.cxx
local.properties
/secrets.properties
```

### Criar: secrets.properties.example

```properties
# Copie para secrets.properties e preencha apenas no seu computador.
MAPS_API_KEY=
```

### Criar: app/src/androidTest/java/com/example/n2dmii/MapaNavigationTest.java

```java
package com.example.n2dmii;

import android.os.SystemClock;
import android.view.View;
import android.widget.Spinner;
import android.widget.TextView;
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
            scenario.onActivity(activity -> activity.findViewById(R.id.map_show_all).performClick());
            aguardar(scenario, activity -> {
                Spinner seletor = activity.findViewById(R.id.map_unit_selector);
                return seletor.getSelectedItemPosition() == 0
                        && activity.findViewById(R.id.map_selected_card).getVisibility() == View.GONE;
            });
        }
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
```

## Validação executada

- assembleDebug, lintDebug, testDebugUnitTest e assembleDebugAndroidTest: BUILD SUCCESSFUL.
- MapaNavigationTest instalado e executado no emulador: OK (1 test).
- O teste acionou o botão Ver da lista, verificou a aba Mapa e o HUERB, recriou a Activity, selecionou UPA Cidade do Povo e voltou para Todas as unidades. Nenhuma chave é necessária para validar esse fluxo e os dados.
- A renderização dos tiles, os marcadores reais, os limites da câmera e as permissões de localização precisam ser testados com uma chave válida. Não foram apresentados como validados sem essa configuração.
