package com.example.n2dmii.utils;

import com.example.n2dmii.BuildConfig;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.maplibre.geojson.Point;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public final class Rotas {
    private static final String ORS = "https://api.openrouteservice.org/v2/directions/driving-car";
    private static final String OSRM = "https://router.project-osrm.org/route/v1/driving/";

    /* Trajeto calculado, com distância em metros e duração em segundos. */
    public static final class Rota {
        public final List<Point> pontos;
        public final double distancia;
        public final double duracao;

        Rota(List<Point> pontos, double distancia, double duracao) {
            this.pontos = pontos;
            this.distancia = distancia;
            this.duracao = duracao;
        }
    }

    /* Classe utilitária, sem instâncias. */
    private Rotas() { }

    /* Consulta o OpenRouteService quando há chave; sem chave, usa o servidor público do OSRM.
       Faz rede de forma bloqueante e deve ser chamada fora da thread principal. */
    public static Rota calcular(Point origem, Point destino) throws IOException {
        String chave = BuildConfig.ORS_API_KEY;
        boolean usaOrs = chave != null && !chave.isEmpty();
        Request.Builder requisicao = new Request.Builder();
        if (usaOrs) {
            requisicao.url(ORS + "?start=" + coordenada(origem) + "&end=" + coordenada(destino))
                    .header("Authorization", chave);
        } else {
            requisicao.url(OSRM + coordenada(origem) + ";" + coordenada(destino)
                    + "?overview=full&geometries=geojson");
        }
        try (Response resposta = MapaConfig.cliente().newCall(requisicao.build()).execute()) {
            ResponseBody corpo = resposta.body();
            if (!resposta.isSuccessful() || corpo == null) {
                throw new IOException("Roteamento respondeu " + resposta.code());
            }
            JSONObject json = new JSONObject(corpo.string());
            return usaOrs ? lerOrs(json) : lerOsrm(json);
        } catch (JSONException erro) {
            throw new IOException("Resposta de rota inválida", erro);
        }
    }

    /* Formata longitude,latitude com ponto decimal, independente do idioma do aparelho. */
    private static String coordenada(Point ponto) {
        return String.format(Locale.US, "%.6f,%.6f", ponto.longitude(), ponto.latitude());
    }

    /* Lê o GeoJSON retornado pelo OpenRouteService. */
    private static Rota lerOrs(JSONObject json) throws JSONException, IOException {
        JSONArray features = json.getJSONArray("features");
        if (features.length() == 0) throw new IOException("Nenhuma rota encontrada");
        JSONObject rota = features.getJSONObject(0);
        JSONObject resumo = rota.getJSONObject("properties").optJSONObject("summary");
        return new Rota(lerPontos(rota.getJSONObject("geometry")),
                resumo != null ? resumo.optDouble("distance", 0) : 0,
                resumo != null ? resumo.optDouble("duration", 0) : 0);
    }

    /* Lê a resposta do OSRM solicitada com geometria GeoJSON. */
    private static Rota lerOsrm(JSONObject json) throws JSONException, IOException {
        JSONArray rotas = json.optJSONArray("routes");
        if (!"Ok".equals(json.optString("code")) || rotas == null || rotas.length() == 0) {
            throw new IOException("Nenhuma rota encontrada");
        }
        JSONObject rota = rotas.getJSONObject(0);
        return new Rota(lerPontos(rota.getJSONObject("geometry")),
                rota.optDouble("distance", 0), rota.optDouble("duration", 0));
    }

    /* Converte as coordenadas [lng, lat] de uma LineString em pontos. */
    private static List<Point> lerPontos(JSONObject geometria) throws JSONException, IOException {
        JSONArray coordenadas = geometria.getJSONArray("coordinates");
        List<Point> pontos = new ArrayList<>();
        for (int i = 0; i < coordenadas.length(); i++) {
            JSONArray par = coordenadas.getJSONArray(i);
            pontos.add(Point.fromLngLat(par.getDouble(0), par.getDouble(1)));
        }
        if (pontos.size() < 2) throw new IOException("Rota sem trajeto");
        return pontos;
    }

    /* Menor distância aproximada, em metros, entre um ponto e o trajeto. Usa projeção
       equirretangular local, precisa o bastante para detectar desvio de algumas dezenas de metros. */
    public static double distanciaAteRota(Point ponto, List<Point> rota) {
        double metrosPorGrauLat = 111_320d;
        double metrosPorGrauLng = metrosPorGrauLat * Math.cos(Math.toRadians(ponto.latitude()));
        double menor = Double.MAX_VALUE;
        for (int i = 0; i < rota.size() - 1; i++) {
            double ax = (rota.get(i).longitude() - ponto.longitude()) * metrosPorGrauLng;
            double ay = (rota.get(i).latitude() - ponto.latitude()) * metrosPorGrauLat;
            double bx = (rota.get(i + 1).longitude() - ponto.longitude()) * metrosPorGrauLng;
            double by = (rota.get(i + 1).latitude() - ponto.latitude()) * metrosPorGrauLat;
            double dx = bx - ax, dy = by - ay;
            double comprimento = dx * dx + dy * dy;
            double t = comprimento == 0 ? 0 : Math.max(0, Math.min(1, -(ax * dx + ay * dy) / comprimento));
            menor = Math.min(menor, Math.hypot(ax + t * dx, ay + t * dy));
        }
        return menor;
    }
}
