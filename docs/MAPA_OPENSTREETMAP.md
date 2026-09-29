# Mapa gratuito: MapLibre Native e OpenStreetMap

Esta entrega substitui o Google Maps SDK por MapLibre Native 13.6.1 com os tiles públicos do OpenStreetMap. O aplicativo continua em Java, Views XML e findViewById. O JSON em assets é somente a configuração de dados do mapa, sem JavaScript ou WebView.

## O que foi alterado

- Ver continua abrindo a aba interna Mapa e selecionando a unidade correspondente.
- Os dez registros continuam no SQLite, com as coordenadas fornecidas. Pontos verdes representam as unidades; o selecionado fica vermelho e maior. Seu nome, endereço e horário aparecem no cartão.
- Todas as unidades enquadra os pontos; também é possível escolher por nome ou tocar em um marcador.
- MapView recebe os eventos de criação, início, retomada, pausa, parada, salvamento e destruição do Fragment.
- Minha localização usa LocationManager do Android via AndroidX, sem Google Play Services. Solicita localização precisa/aproximada apenas após o toque. A consulta é cancelada ao sair da tela e a recusa não impede consultar as unidades.
- A chave, o metadado Google e as dependências Google de mapa/localização foram removidos. Não é necessário cadastrar cartão, configurar Google Cloud ou preencher secrets.properties.
- local.properties permanece inalterado, com sua configuração local do Android SDK. Um eventual secrets.properties existente não é lido e não foi apagado. Sua regra no .gitignore foi mantida para evitar publicar credenciais antigas.
- O modelo secrets.properties.example deixou de ser necessário e foi removido.

## OpenStreetMap e rede

O aplicativo usa `https://tile.openstreetmap.org/{z}/{x}/{y}.png`, configurado em `app/src/main/assets/osm_style.json`. Os créditos © OpenStreetMap contributors ficam visíveis junto ao mapa e abrem a página de licença ao toque.

MapaConfig identifica as requisições com User-Agent próprio do Saúde Fácil e mantém cache HTTP de 50 MB. OkHttp e o SDK respeitam os cabeçalhos de cache e validação do servidor. O pré-carregamento de níveis de zoom foi desativado. Não há download de cidades, regiões ou função de mapas offline.

Os dados OSM são abertos; o servidor público é compartilhado e sua disponibilidade não é garantida. Este uso depende de internet para áreas ainda não carregadas e deve respeitar os limites do serviço. Em caso de crescimento do aplicativo, o provedor de tiles pode ser substituído no arquivo de estilo.

Referências: [MapLibre Android](https://maplibre.org/maplibre-native/android/examples/getting-started/), [política de tiles OSM](https://operations.osmfoundation.org/policies/tiles/) e [localização nativa com AndroidX](https://developer.android.com/reference/androidx/core/location/LocationManagerCompat).

## Como testar

1. Sincronize o Gradle e execute app, com internet no aparelho/emulador. Não configure nenhuma chave.
2. Abra Unidades e toque em Ver. Confira o mapa da região, o ponto vermelho e o cartão da unidade.
3. Toque em Todas as unidades e confira o conjunto; selecione outra pelo nome e depois por um ponto no mapa.
4. Mova o mapa, use o gesto de pinça para zoom e gire o aparelho. A seleção deve permanecer.
5. Toque em Minha localização. Negue a permissão e confira que as unidades continuam acessíveis. Repita com permissão aproximada ou precisa.
6. Desative a localização no aparelho e confira o tratamento de posição indisponível.
7. Sem internet, os dados do SQLite continuam acessíveis, mas novas áreas do mapa podem não carregar; o cache não é uma promessa de funcionamento offline.

## Arquivos completos

Os caminhos abaixo são relativos à raiz do projeto. O restante da navegação e do banco permanece como nas entregas anteriores. Este documento substitui a configuração Google descrita em MAPA_INTEGRADO.md.


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
    implementation(libs.maplibre)
    implementation(libs.okhttp)
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
maplibre = { group = "org.maplibre.gl", name = "android-sdk", version = "13.6.1" }
okhttp = { group = "com.squareup.okhttp3", name = "okhttp", version = "4.12.0" }
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

### Criar: app/src/main/assets/osm_style.json

```json
{
  "version": 8,
  "name": "OpenStreetMap",
  "sources": {
    "osm": {
      "type": "raster",
      "tiles": ["https://tile.openstreetmap.org/{z}/{x}/{y}.png"],
      "tileSize": 256,
      "minzoom": 0,
      "maxzoom": 19,
      "attribution": "© OpenStreetMap contributors"
    }
  },
  "layers": [{"id": "osm", "type": "raster", "source": "osm"}]
}
```

### Criar: app/src/main/java/com/example/n2dmii/utils/MapaConfig.java

```java
package com.example.n2dmii.utils;

import android.content.Context;
import com.example.n2dmii.BuildConfig;
import org.maplibre.android.MapLibre;
import org.maplibre.android.module.http.HttpRequestUtil;
import java.io.File;
import okhttp3.Cache;
import okhttp3.Dispatcher;
import okhttp3.OkHttpClient;

public final class MapaConfig {
    private static boolean inicializado;

    /* Classe de configuração única, sem instâncias externas. */
    private MapaConfig() { }

    /* Identifica o aplicativo e respeita Cache-Control, Expires e validação condicional do servidor. */
    public static synchronized void inicializar(Context context) {
        if (inicializado) return;
        Context app = context.getApplicationContext();
        MapLibre.getInstance(app);
        Dispatcher dispatcher = new Dispatcher();
        dispatcher.setMaxRequestsPerHost(2);
        OkHttpClient cliente = new OkHttpClient.Builder()
                .dispatcher(dispatcher)
                .cache(new Cache(new File(app.getCacheDir(), "osm_http"), 50L * 1024 * 1024))
                .addInterceptor(chain -> chain.proceed(chain.request().newBuilder()
                        .header("User-Agent", "SaudeFacil/" + BuildConfig.VERSION_NAME
                                + " (Android; " + BuildConfig.APPLICATION_ID + ")")
                        .build()))
                .build();
        HttpRequestUtil.setOkHttpClient(cliente);
        inicializado = true;
    }
}
```

### Modificar: app/src/main/java/com/example/n2dmii/fragments/MapaFragment.java

```java
package com.example.n2dmii.fragments;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.ActivityNotFoundException;
import android.net.Uri;
import android.location.Criteria;
import android.location.LocationManager;
import android.graphics.RectF;
import android.graphics.PointF;
import androidx.core.location.LocationManagerCompat;
import androidx.core.os.CancellationSignal;
import com.example.n2dmii.utils.MapaConfig;
import org.maplibre.android.maps.MapView;
import org.maplibre.android.maps.MapLibreMap;
import org.maplibre.android.maps.Style;
import org.maplibre.android.camera.CameraPosition;
import org.maplibre.android.camera.CameraUpdateFactory;
import org.maplibre.android.geometry.LatLng;
import org.maplibre.android.geometry.LatLngBounds;
import org.maplibre.android.style.sources.GeoJsonSource;
import org.maplibre.android.style.layers.CircleLayer;
import org.maplibre.android.style.layers.BackgroundLayer;
import org.maplibre.android.style.expressions.Expression;
import org.maplibre.geojson.Feature;
import org.maplibre.geojson.FeatureCollection;
import org.maplibre.geojson.Point;
import static org.maplibre.android.style.layers.PropertyFactory.*;
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
import com.example.n2dmii.R;
import com.example.n2dmii.database.DatabaseHelper;
import com.example.n2dmii.models.UnidadeSaude;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MapaFragment extends Fragment {
    public static final long TODAS_UNIDADES = -1L;
    private static final String CHAVE_UNIDADE = "unidade_id";
    private static final String CHAVE_CAMERA = "camera";
    private final List<UnidadeSaude> unidades = new ArrayList<>();
    private MapView mapView;
    private Style estilo;
    private static final String FONTE = "unidades";
    private static final String CAMADA = "pontos";
    private static final String DESTAQUE = "destaque";
    private static final String ID = "unidade_id";
    private final Handler principal = new Handler(Looper.getMainLooper());
    private long unidadeSelecionada = TODAS_UNIDADES;
    private CameraPosition cameraSalva;
    private MapLibreMap mapa;
    private Spinner seletor;
    private TextView status, nome, detalhes;
    private View cartao, areaMapa;
    private Button localizacao;
    private ExecutorService executor;
    private CancellationSignal consultaLocalizacao;
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
        MapaConfig.inicializar(requireContext());
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
        mapView = view.findViewById(R.id.map_container);
        mapView.onCreate(estado);
        view.findViewById(R.id.map_attribution).setOnClickListener(v -> abrirCreditos());
        inicializarMapa(view);
        carregarUnidades(view);
    }

    /* Cria o mapa nativo com tiles OSM, sem chave, pré-carregamento ou serviços Google. */
    private void inicializarMapa(View viewOriginal) {
        mapView.addOnDidFailLoadingMapListener(erro -> {
            if (getView() == viewOriginal) status.setText(R.string.map_unavailable);
        });
        mapView.getMapAsync(mapLibreMap -> {
            if (getView() != viewOriginal) return;
            mapa = mapLibreMap;
            mapa.setPrefetchZoomDelta(0);
            mapa.setMaxZoomPreference(19);
            mapa.getUiSettings().setLogoEnabled(false);
            mapa.getUiSettings().setAttributionEnabled(false);
            mapa.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(-9.974, -67.8107), 12));
            mapa.addOnMapClickListener(coordenada -> {
                if (estilo == null) return false;
                PointF ponto = mapa.getProjection().toScreenLocation(coordenada);
                float margem = getResources().getDimension(R.dimen.map_hit_radius);
                List<Feature> encontrados = mapa.queryRenderedFeatures(new RectF(
                        ponto.x - margem, ponto.y - margem, ponto.x + margem, ponto.y + margem),
                        DESTAQUE, CAMADA);
                if (encontrados.isEmpty()) return false;
                selecionar(Long.parseLong(encontrados.get(0).getStringProperty(ID)));
                return true;
            });
            mapa.setStyle(new Style.Builder().fromUri("asset://osm_style.json"), novoEstilo -> {
                if (getView() != viewOriginal) return;
                estilo = novoEstilo;
                estilo.addLayerBelow(new BackgroundLayer("fundo").withProperties(
                        backgroundColor(ContextCompat.getColor(requireContext(), R.color.background))), "osm");
                estilo.addSource(new GeoJsonSource(FONTE, FeatureCollection.fromFeatures(new Feature[0])));
                estilo.addLayer(new CircleLayer(CAMADA, FONTE).withProperties(
                        circleColor(ContextCompat.getColor(requireContext(), R.color.map_unit_color)),
                        circleRadius(8f), circleStrokeWidth(2f),
                        circleStrokeColor(ContextCompat.getColor(requireContext(), R.color.map_point_outline))));
                CircleLayer destaque = new CircleLayer(DESTAQUE, FONTE).withProperties(
                        circleColor(ContextCompat.getColor(requireContext(), R.color.map_selected_color)),
                        circleRadius(11f), circleStrokeWidth(3f),
                        circleStrokeColor(ContextCompat.getColor(requireContext(), R.color.map_point_outline)));
                destaque.setFilter(Expression.eq(Expression.get(ID), Expression.literal("-1")));
                estilo.addLayer(destaque);
                estilo.addSource(new GeoJsonSource("usuario", FeatureCollection.fromFeatures(new Feature[0])));
                estilo.addLayer(new CircleLayer("posicao_usuario", "usuario").withProperties(
                        circleColor(ContextCompat.getColor(requireContext(), R.color.map_user_color)),
                        circleRadius(7f), circleStrokeWidth(3f),
                        circleStrokeColor(ContextCompat.getColor(requireContext(), R.color.map_point_outline))));
                localizacao.setEnabled(true);
                status.setText(R.string.map_explore_hint);
                desenharMarcadores();
            });
        });
    }

    /* Abre somente a página de créditos exigida pela licença do OpenStreetMap. */
    private void abrirCreditos() {
        try {
            startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse("https://www.openstreetmap.org/copyright")));
        } catch (ActivityNotFoundException erro) {
            mensagem(R.string.map_credit_unavailable);
        }
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

    /* Atualiza a fonte GeoJSON com as unidades do banco, mantendo seus IDs para seleção. */
    private void desenharMarcadores() {
        if (estilo == null) return;
        List<Feature> pontos = new ArrayList<>();
        for (UnidadeSaude unidade : unidades) {
            Feature ponto = Feature.fromGeometry(Point.fromLngLat(unidade.getLongitude(), unidade.getLatitude()));
            ponto.addStringProperty(ID, String.valueOf(unidade.getId()));
            pontos.add(ponto);
        }
        GeoJsonSource fonte = estilo.getSourceAs(FONTE);
        if (fonte != null) fonte.setGeoJson(FeatureCollection.fromFeatures(pontos));
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

    /* Destaca a unidade escolhida por cor e tamanho, mantendo seu nome no cartão acessível. */
    private void atualizarDestaque() {
        if (estilo == null) return;
        CircleLayer destaque = estilo.getLayerAs(DESTAQUE);
        if (destaque != null) destaque.setFilter(Expression.eq(Expression.get(ID),
                Expression.literal(String.valueOf(unidadeSelecionada))));
    }

    /* Centraliza a unidade ou enquadra todas, preservando a câmera após uma rotação. */
    private void posicionarCamera() {
        if (estilo == null || unidades.isEmpty()) return;
        View viewOriginal = getView();
        areaMapa.post(() -> {
            if (getView() != viewOriginal || mapa == null || areaMapa.getWidth() == 0
                    || areaMapa.getHeight() == 0) return;
            if (cameraSalva != null) {
                mapa.moveCamera(CameraUpdateFactory.newCameraPosition(cameraSalva));
                cameraSalva = null;
                return;
            }
            for (UnidadeSaude unidade : unidades) {
                if (unidade.getId() == unidadeSelecionada) {
                    mapa.moveCamera(CameraUpdateFactory.newLatLngZoom(
                            new LatLng(unidade.getLatitude(), unidade.getLongitude()), 15));
                    return;
                }
            }
            LatLngBounds.Builder limites = new LatLngBounds.Builder();
            for (UnidadeSaude unidade : unidades) {
                limites.include(new LatLng(unidade.getLatitude(), unidade.getLongitude()));
            }
            mapa.moveCamera(CameraUpdateFactory.newLatLngBounds(limites.build(),
                    getResources().getDimensionPixelSize(R.dimen.map_bounds_padding)));
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

    /* Obtém a posição pelo Android, sem depender do Google Play Services. */
    @SuppressLint("MissingPermission")
    private void localizarUsuario() {
        if (!temPermissaoLocalizacao() || estilo == null) return;
        if (consultaLocalizacao != null) consultaLocalizacao.cancel();
        consultaLocalizacao = new CancellationSignal();
        localizacao.setEnabled(false);
        View viewOriginal = getView();
        LocationManager manager = (LocationManager) requireContext().getSystemService(Context.LOCATION_SERVICE);
        boolean precisa = ContextCompat.checkSelfPermission(requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        Criteria criterios = new Criteria();
        criterios.setAccuracy(precisa ? Criteria.ACCURACY_FINE : Criteria.ACCURACY_COARSE);
        try {
            String provedor = manager.getBestProvider(criterios, true);
            if (provedor == null) {
                localizacao.setEnabled(true);
                mensagem(R.string.map_location_unavailable);
                return;
            }
            LocationManagerCompat.getCurrentLocation(manager, provedor, consultaLocalizacao,
                    ContextCompat.getMainExecutor(requireContext()), location -> {
                        if (getView() != viewOriginal || estilo == null) return;
                        localizacao.setEnabled(true);
                        if (location == null) {
                            mensagem(R.string.map_location_unavailable);
                            return;
                        }
                        GeoJsonSource usuario = estilo.getSourceAs("usuario");
                        if (usuario != null) usuario.setGeoJson(Feature.fromGeometry(
                                Point.fromLngLat(location.getLongitude(), location.getLatitude())));
                        mapa.animateCamera(CameraUpdateFactory.newLatLngZoom(
                                new LatLng(location.getLatitude(), location.getLongitude()), 14));
                    });
        } catch (SecurityException erro) {
            localizacao.setEnabled(true);
            mensagem(R.string.map_location_denied);
        } catch (IllegalArgumentException erro) {
            localizacao.setEnabled(true);
            mensagem(R.string.map_location_unavailable);
        }
    }

    /* Encaminha a entrada em primeiro plano ao ciclo de vida da MapView. */
    @Override
    public void onStart() {
        super.onStart();
        if (mapView != null) mapView.onStart();
    }

    /* Retoma a renderização e remove a posição se a permissão tiver sido revogada. */
    @Override
    public void onResume() {
        super.onResume();
        if (mapView != null) mapView.onResume();
        if (localizacao != null) localizacao.setEnabled(estilo != null);
        if (estilo != null && !temPermissaoLocalizacao()) {
            GeoJsonSource usuario = estilo.getSourceAs("usuario");
            if (usuario != null) usuario.setGeoJson(FeatureCollection.fromFeatures(new Feature[0]));
        }
    }

    /* Suspende a renderização quando a tela deixa de estar ativa. */
    @Override
    public void onPause() {
        if (mapView != null) mapView.onPause();
        super.onPause();
    }

    /* Cancela localização e interrompe o mapa sem serviços em segundo plano. */
    @Override
    public void onStop() {
        if (consultaLocalizacao != null) consultaLocalizacao.cancel();
        if (mapView != null) mapView.onStop();
        super.onStop();
    }

    /* Permite que o mapa libere caches de memória sob pressão do Android. */
    @Override
    public void onLowMemory() {
        super.onLowMemory();
        if (mapView != null) mapView.onLowMemory();
    }

    /* Apresenta uma mensagem somente enquanto o Fragment ainda possui contexto. */
    private void mensagem(int recurso) {
        if (getContext() != null) Toast.makeText(requireContext(), recurso, Toast.LENGTH_LONG).show();
    }

    /* Salva a seleção e a posição da câmera para recriação da Activity. */
    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mapView != null) mapView.onSaveInstanceState(outState);
        outState.putLong(CHAVE_UNIDADE, unidadeSelecionada);
        if (mapa != null) outState.putParcelable(CHAVE_CAMERA, mapa.getCameraPosition());
    }

    /* Descarta referências da View e ignora respostas assíncronas após sair da aba. */
    @Override
    public void onDestroyView() {
        if (executor != null) executor.shutdown();
        if (consultaLocalizacao != null) consultaLocalizacao.cancel();
        if (mapView != null) mapView.onDestroy();
        mapView = null;
        estilo = null;
        mapa = null;
        seletor = null;
        status = null;
        nome = null;
        detalhes = null;
        cartao = null;
        areaMapa = null;
        localizacao = null;
        unidades.clear();
        super.onDestroyView();
    }
}
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
    <org.maplibre.android.maps.MapView android:id="@+id/map_container"
        android:layout_width="match_parent" android:layout_height="0dp"
        android:layout_weight="1" android:background="@drawable/bg_surface" />
    <TextView android:id="@+id/map_attribution" style="@style/CardCaption"
        android:text="@string/map_attribution" android:padding="@dimen/spacing_small"
        android:minHeight="@dimen/unit_touch_target" android:gravity="center_vertical"
        android:background="@color/surface" android:clickable="true" android:focusable="true" />
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

### Modificar: app/src/main/res/values/mapa.xml

```xml
<resources>
    <string name="map_title">Mapa de unidades</string>
    <string name="map_select_label">Unidade em destaque</string>
    <string name="map_all_units">Todas as unidades</string>
    <string name="map_my_location">Minha localização</string>
    <string name="map_loading">Carregando mapa…</string>
    <string name="map_unavailable">O mapa está indisponível no momento. Você pode consultar as unidades pelo seletor acima.</string>
    <string name="map_attribution">© OpenStreetMap contributors · MapLibre</string>
    <string name="map_credit_unavailable">Créditos: OpenStreetMap contributors. Licença em openstreetmap.org/copyright.</string>
    <color name="map_unit_color">#008C95</color>
    <color name="map_selected_color">#D93830</color>
    <color name="map_user_color">#2668D7</color>
    <color name="map_point_outline">#FFFFFF</color>
    <dimen name="map_hit_radius">16dp</dimen>
    <string name="map_explore_hint">Toque nos marcadores ou escolha uma unidade acima. Use Todas as unidades para ver o conjunto.</string>
    <string name="map_no_units">Nenhuma unidade cadastrada para mostrar no mapa.</string>
    <string name="map_unit_details">%1$s\n%2$s</string>
    <string name="map_location_denied">Localização não autorizada. Você ainda pode consultar todas as unidades no mapa.</string>
    <string name="map_location_unavailable">Não foi possível obter sua localização. Verifique a localização do aparelho e tente novamente.</string>
    <dimen name="map_bounds_padding">40dp</dimen>
    <dimen name="map_details_max_height">132dp</dimen>
</resources>
```

### Modificar: app/src/androidTest/java/com/example/n2dmii/MapaNavigationTest.java

```java
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
```

## Validação

- Build, testes locais e Lint concluídos; o Lint não apresentou erros.
- MapaNavigationTest passou no emulador, incluindo navegação interna, recriação, troca de unidade e centralização real da câmera em UPA Cidade do Povo.
- Conferência visual: tiles reais do OpenStreetMap carregados em Rio Branco, dez pontos no enquadramento e créditos visíveis.
- Toque em marcador conferido: seleção de URAP Dra. Cláudia Vitorino e atualização de seu cartão.
- Recusa da permissão de localização conferida: o mapa e os dados da unidade continuaram acessíveis.
- Obtenção de uma posição real/aproximada ainda deve ser conferida no seu dispositivo. O emulador pode depender de uma localização simulada.