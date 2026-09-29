package com.example.n2dmii.fragments;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.ActivityNotFoundException;
import android.net.Uri;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.graphics.RectF;
import android.graphics.PointF;
import android.os.SystemClock;
import androidx.core.location.LocationListenerCompat;
import androidx.core.location.LocationManagerCompat;
import androidx.core.location.LocationRequestCompat;
import com.example.n2dmii.utils.MapaConfig;
import com.example.n2dmii.utils.Rotas;
import org.maplibre.android.maps.MapView;
import org.maplibre.android.maps.MapLibreMap;
import org.maplibre.android.maps.Style;
import org.maplibre.android.camera.CameraPosition;
import org.maplibre.android.camera.CameraUpdateFactory;
import org.maplibre.android.geometry.LatLng;
import org.maplibre.android.geometry.LatLngBounds;
import org.maplibre.android.style.sources.GeoJsonSource;
import org.maplibre.android.style.layers.CircleLayer;
import org.maplibre.android.style.layers.LineLayer;
import org.maplibre.android.style.layers.Property;
import org.maplibre.android.style.layers.BackgroundLayer;
import org.maplibre.android.style.expressions.Expression;
import org.maplibre.geojson.Feature;
import org.maplibre.geojson.FeatureCollection;
import org.maplibre.geojson.LineString;
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
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
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
    private static final String FONTE_ROTA = "rota";
    private static final String CAMADA_ROTA = "linha_rota";
    /* Distância, em metros, a partir da qual a pessoa é considerada fora da rota. */
    private static final double DESVIO_MAXIMO = 60;
    /* Intervalo mínimo entre recálculos, para não sobrecarregar o serviço de rotas. */
    private static final long INTERVALO_RECALCULO = 20_000;
    /* Posição anterior mais antiga que isto não é usada como ponto de partida. */
    private static final long IDADE_MAXIMA_POSICAO = 120_000;
    private final Handler principal = new Handler(Looper.getMainLooper());
    private long unidadeSelecionada = TODAS_UNIDADES;
    private CameraPosition cameraSalva;
    private MapLibreMap mapa;
    private Spinner seletor;
    private TextView status, nome, detalhes;
    private View cartao, areaMapa;
    private Button localizacao, botaoRota;
    private ExecutorService executor;
    private LocationListenerCompat ouvinte;
    private boolean acompanhando, centralizar;
    private Point posicaoAtual;
    private List<Point> rotaAtual;
    private boolean rotaPendente, calculandoRota;
    private int pedidoRota;
    private long ultimoCalculo;
    private final ActivityResultLauncher<String[]> permissoes = registerForActivityResult(
            new ActivityResultContracts.RequestMultiplePermissions(), resultado -> {
                /* A recusa não bloqueia a consulta às unidades nem seus marcadores. */
                if (getView() == null) return;
                if (temPermissaoLocalizacao()) {
                    iniciarAcompanhamento();
                } else {
                    if (rotaPendente) limparRota();
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
        botaoRota = view.findViewById(R.id.map_route);
        localizacao.setEnabled(false);
        botaoRota.setEnabled(false);
        view.findViewById(R.id.map_show_all).setOnClickListener(v -> selecionar(TODAS_UNIDADES));
        localizacao.setOnClickListener(v -> {
            if (acompanhando) pararAcompanhamento();
            else solicitarLocalizacao();
        });
        botaoRota.setOnClickListener(v -> alternarRota());
        view.findViewById(R.id.map_open_external).setOnClickListener(v -> abrirNoAppDeMapas());
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
                estilo.addSource(new GeoJsonSource(FONTE_ROTA, FeatureCollection.fromFeatures(new Feature[0])));
                estilo.addLayer(new LineLayer("contorno_rota", FONTE_ROTA).withProperties(
                        lineColor(ContextCompat.getColor(requireContext(), R.color.map_point_outline)),
                        lineWidth(9f), lineCap(Property.LINE_CAP_ROUND), lineJoin(Property.LINE_JOIN_ROUND)));
                estilo.addLayer(new LineLayer(CAMADA_ROTA, FONTE_ROTA).withProperties(
                        lineColor(ContextCompat.getColor(requireContext(), R.color.map_route_color)),
                        lineWidth(5f), lineCap(Property.LINE_CAP_ROUND), lineJoin(Property.LINE_JOIN_ROUND)));
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
                botaoRota.setEnabled(true);
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
        if (id != unidadeSelecionada) limparRota();
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

    /* Retorna a unidade em foco, ou null quando todas estão sendo exibidas. */
    @Nullable
    private UnidadeSaude unidadeAtual() {
        for (UnidadeSaude unidade : unidades) {
            if (unidade.getId() == unidadeSelecionada) return unidade;
        }
        return null;
    }

    /* Exibe nome, endereço e horário da unidade em foco. */
    private void atualizarCartao() {
        UnidadeSaude unidade = unidadeAtual();
        cartao.setVisibility(unidade == null ? View.GONE : View.VISIBLE);
        if (unidade == null) return;
        nome.setText(unidade.getNome());
        detalhes.setText(getString(R.string.map_unit_details, unidade.getEndereco(), unidade.getHora()));
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
            UnidadeSaude unidade = unidadeAtual();
            if (unidade != null) {
                mapa.moveCamera(CameraUpdateFactory.newLatLngZoom(
                        new LatLng(unidade.getLatitude(), unidade.getLongitude()), 15));
                return;
            }
            LatLngBounds.Builder limites = new LatLngBounds.Builder();
            for (UnidadeSaude item : unidades) {
                limites.include(new LatLng(item.getLatitude(), item.getLongitude()));
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

    /* Solicita as duas permissões somente após o toque em Minha localização ou Traçar rota. */
    private void solicitarLocalizacao() {
        if (temPermissaoLocalizacao()) iniciarAcompanhamento();
        else permissoes.launch(new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION});
    }

    /* Liga o acompanhamento contínuo; a câmera vai até a pessoa na primeira posição recebida. */
    private void iniciarAcompanhamento() {
        if (estilo == null) return;
        boolean aguardandoRota = rotaPendente;
        centralizar = !aguardandoRota;
        if (!aguardandoRota) status.setText(R.string.map_following);
        if (!registrarOuvinte()) {
            if (rotaPendente) limparRota();
            status.setText(R.string.map_explore_hint);
            return;
        }
        acompanhando = true;
        localizacao.setText(R.string.map_stop_following);
    }

    /* Desliga o GPS, remove o ponto azul e mantém uma rota já desenhada sem recálculo. */
    private void pararAcompanhamento() {
        acompanhando = false;
        removerOuvinte();
        posicaoAtual = null;
        if (rotaPendente) limparRota();
        if (localizacao != null) localizacao.setText(R.string.map_my_location);
        if (estilo != null) {
            GeoJsonSource usuario = estilo.getSourceAs("usuario");
            if (usuario != null) usuario.setGeoJson(FeatureCollection.fromFeatures(new Feature[0]));
        }
        if (status != null && rotaAtual == null) status.setText(R.string.map_explore_hint);
    }

    /* Recebe atualizações do LocationManager do Android, sem depender do Google Play Services. */
    @SuppressLint("MissingPermission")
    private boolean registrarOuvinte() {
        if (!temPermissaoLocalizacao()) return false;
        removerOuvinte();
        LocationManager manager = (LocationManager) requireContext().getSystemService(Context.LOCATION_SERVICE);
        boolean precisa = ContextCompat.checkSelfPermission(requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        Criteria criterios = new Criteria();
        criterios.setAccuracy(precisa ? Criteria.ACCURACY_FINE : Criteria.ACCURACY_COARSE);
        try {
            String provedor = manager.getBestProvider(criterios, true);
            if (provedor == null || !LocationManagerCompat.isLocationEnabled(manager)) {
                mensagem(R.string.map_location_unavailable);
                return false;
            }
            LocationRequestCompat pedido = new LocationRequestCompat.Builder(2_000)
                    .setMinUpdateDistanceMeters(5)
                    .setQuality(precisa ? LocationRequestCompat.QUALITY_HIGH_ACCURACY
                            : LocationRequestCompat.QUALITY_BALANCED_POWER_ACCURACY)
                    .build();
            View viewOriginal = getView();
            ouvinte = location -> {
                if (getView() == viewOriginal) atualizarPosicao(location);
            };
            LocationManagerCompat.requestLocationUpdates(manager, provedor, pedido,
                    ContextCompat.getMainExecutor(requireContext()), ouvinte);
            Location ultima = manager.getLastKnownLocation(provedor);
            if (ultima != null && SystemClock.elapsedRealtime() - ultima.getElapsedRealtimeNanos() / 1_000_000
                    < IDADE_MAXIMA_POSICAO) {
                atualizarPosicao(ultima);
            }
            return true;
        } catch (SecurityException erro) {
            removerOuvinte();
            mensagem(R.string.map_location_denied);
        } catch (IllegalArgumentException erro) {
            removerOuvinte();
            mensagem(R.string.map_location_unavailable);
        }
        return false;
    }

    /* Interrompe as atualizações de posição, se houver. */
    private void removerOuvinte() {
        Context context = getContext();
        if (ouvinte != null && context != null) {
            LocationManager manager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
            LocationManagerCompat.removeUpdates(manager, ouvinte);
        }
        ouvinte = null;
    }

    /* Move o ponto azul, inicia uma rota aguardada e recalcula se a pessoa sair do trajeto. */
    private void atualizarPosicao(Location location) {
        if (estilo == null || mapa == null) return;
        Point ponto = Point.fromLngLat(location.getLongitude(), location.getLatitude());
        posicaoAtual = ponto;
        GeoJsonSource usuario = estilo.getSourceAs("usuario");
        if (usuario != null) usuario.setGeoJson(Feature.fromGeometry(ponto));
        if (centralizar) {
            centralizar = false;
            mapa.animateCamera(CameraUpdateFactory.newLatLngZoom(
                    new LatLng(location.getLatitude(), location.getLongitude()), 15));
        }
        if (rotaPendente) {
            rotaPendente = false;
            calcularRota(true);
        } else if (rotaAtual != null && !calculandoRota
                && (!location.hasAccuracy() || location.getAccuracy() <= DESVIO_MAXIMO)
                && SystemClock.elapsedRealtime() - ultimoCalculo > INTERVALO_RECALCULO
                && Rotas.distanciaAteRota(ponto, rotaAtual) > DESVIO_MAXIMO) {
            status.setText(R.string.map_route_recalculating);
            calcularRota(false);
        }
    }

    /* Traça a rota até a unidade em foco, pedindo a localização se ainda não houver; se já existe, limpa. */
    private void alternarRota() {
        if (rotaAtual != null || rotaPendente || calculandoRota) {
            limparRota();
            return;
        }
        if (unidadeAtual() == null) return;
        botaoRota.setText(R.string.map_route_clear);
        if (acompanhando && posicaoAtual != null) {
            calcularRota(true);
        } else {
            rotaPendente = true;
            status.setText(R.string.map_route_waiting);
            if (!acompanhando) solicitarLocalizacao();
        }
    }

    /* Consulta o serviço de rotas fora da thread principal; respostas antigas são descartadas. */
    private void calcularRota(boolean enquadrar) {
        UnidadeSaude destino = unidadeAtual();
        if (destino == null || posicaoAtual == null || executor == null) return;
        int pedido = ++pedidoRota;
        calculandoRota = true;
        ultimoCalculo = SystemClock.elapsedRealtime();
        if (enquadrar) status.setText(R.string.map_route_loading);
        Point origem = posicaoAtual;
        Point fim = Point.fromLngLat(destino.getLongitude(), destino.getLatitude());
        View viewOriginal = getView();
        executor.execute(() -> {
            try {
                Rotas.Rota rota = Rotas.calcular(origem, fim);
                principal.post(() -> {
                    if (getView() != viewOriginal || pedido != pedidoRota) return;
                    calculandoRota = false;
                    mostrarRota(rota, enquadrar);
                });
            } catch (IOException erro) {
                principal.post(() -> {
                    if (getView() != viewOriginal || pedido != pedidoRota) return;
                    calculandoRota = false;
                    if (rotaAtual == null) limparRota();
                    status.setText(R.string.map_route_error);
                });
            }
        });
    }

    /* Desenha o trajeto abaixo dos marcadores e informa distância e tempo estimado. */
    private void mostrarRota(Rotas.Rota rota, boolean enquadrar) {
        if (estilo == null || mapa == null) return;
        rotaAtual = rota.pontos;
        GeoJsonSource fonte = estilo.getSourceAs(FONTE_ROTA);
        if (fonte != null) fonte.setGeoJson(LineString.fromLngLats(rota.pontos));
        botaoRota.setText(R.string.map_route_clear);
        status.setText(getString(R.string.map_route_summary,
                formatarDistancia(rota.distancia), formatarDuracao(rota.duracao)));
        if (!enquadrar) return;
        LatLngBounds.Builder limites = new LatLngBounds.Builder();
        for (Point ponto : rota.pontos) limites.include(new LatLng(ponto.latitude(), ponto.longitude()));
        mapa.animateCamera(CameraUpdateFactory.newLatLngBounds(limites.build(),
                getResources().getDimensionPixelSize(R.dimen.map_bounds_padding)));
    }

    /* Remove o trajeto e cancela cálculos em andamento. */
    private void limparRota() {
        boolean havia = rotaAtual != null || rotaPendente || calculandoRota;
        pedidoRota++;
        rotaAtual = null;
        rotaPendente = false;
        calculandoRota = false;
        if (botaoRota != null) botaoRota.setText(R.string.map_route);
        if (estilo != null) {
            GeoJsonSource fonte = estilo.getSourceAs(FONTE_ROTA);
            if (fonte != null) fonte.setGeoJson(FeatureCollection.fromFeatures(new Feature[0]));
        }
        if (havia && status != null) {
            status.setText(acompanhando ? R.string.map_following : R.string.map_explore_hint);
        }
    }

    /* 850 m ou 3,2 km, conforme o idioma do aparelho. */
    private static String formatarDistancia(double metros) {
        if (metros < 1000) return Math.round(metros) + " m";
        return String.format(Locale.getDefault(), "%.1f km", metros / 1000);
    }

    /* 12 min ou 1 h 05 min. */
    private static String formatarDuracao(double segundos) {
        long minutos = Math.max(1, Math.round(segundos / 60));
        if (minutos < 60) return minutos + " min";
        return String.format(Locale.getDefault(), "%d h %02d min", minutos / 60, minutos % 60);
    }

    /* Entrega o destino ao app de mapas instalado (Google Maps, Waze etc.) para navegação por voz. */
    private void abrirNoAppDeMapas() {
        UnidadeSaude unidade = unidadeAtual();
        if (unidade == null) return;
        Uri destino = Uri.parse(String.format(Locale.US, "geo:%.6f,%.6f?q=%.6f,%.6f(%s)",
                unidade.getLatitude(), unidade.getLongitude(),
                unidade.getLatitude(), unidade.getLongitude(), Uri.encode(unidade.getNome())));
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, destino));
        } catch (ActivityNotFoundException erro) {
            mensagem(R.string.map_external_unavailable);
        }
    }

    /* Encaminha a entrada em primeiro plano ao ciclo de vida da MapView e retoma o acompanhamento. */
    @Override
    public void onStart() {
        super.onStart();
        if (mapView != null) mapView.onStart();
        if (acompanhando && estilo != null && !registrarOuvinte()) pararAcompanhamento();
    }

    /* Retoma a renderização e remove a posição se a permissão tiver sido revogada. */
    @Override
    public void onResume() {
        super.onResume();
        if (mapView != null) mapView.onResume();
        if (localizacao != null) localizacao.setEnabled(estilo != null);
        if (acompanhando && !temPermissaoLocalizacao()) pararAcompanhamento();
    }

    /* Suspende a renderização quando a tela deixa de estar ativa. */
    @Override
    public void onPause() {
        if (mapView != null) mapView.onPause();
        super.onPause();
    }

    /* Desliga o GPS e interrompe o mapa, sem serviços em segundo plano. */
    @Override
    public void onStop() {
        removerOuvinte();
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
        removerOuvinte();
        acompanhando = false;
        posicaoAtual = null;
        rotaAtual = null;
        rotaPendente = false;
        calculandoRota = false;
        pedidoRota++;
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
        botaoRota = null;
        unidades.clear();
        super.onDestroyView();
    }
}
