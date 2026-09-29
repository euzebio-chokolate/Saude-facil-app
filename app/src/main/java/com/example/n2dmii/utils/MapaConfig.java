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
    private static OkHttpClient cliente;

    /* Classe de configuração única, sem instâncias externas. */
    private MapaConfig() { }

    /* Identifica o aplicativo e respeita Cache-Control, Expires e validação condicional do servidor. */
    public static synchronized void inicializar(Context context) {
        if (inicializado) return;
        Context app = context.getApplicationContext();
        MapLibre.getInstance(app);
        Dispatcher dispatcher = new Dispatcher();
        dispatcher.setMaxRequestsPerHost(2);
        cliente = new OkHttpClient.Builder()
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

    /* Mesmo cliente HTTP do mapa, com User-Agent do app, para as consultas de rota. */
    public static synchronized OkHttpClient cliente() {
        if (cliente == null) throw new IllegalStateException("MapaConfig não inicializado");
        return cliente;
    }
}
