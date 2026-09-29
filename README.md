# Saúde Fácil

Aplicativo Android nativo (Java + layouts XML) para encontrar unidades de saúde em Rio Branco (AC). Mostra as unidades num mapa, acompanha a localização da pessoa em tempo real e traça a rota até a unidade escolhida.

> Este aplicativo não substitui avaliação médica profissional.

## Funcionalidades

- **Início**: telefones de emergência e apoio (SAMU 192, Bombeiros 193, Polícia 190, CVV 188) e atalhos para as outras abas.
- **Unidades**: lista de hospitais, UPAs e postos guardada em SQLite, com pesquisa por nome, tipo ou endereço. O botão **Ver** abre a unidade no mapa.
- **Mapa** (MapLibre + OpenStreetMap, sem Google Maps e sem chave obrigatória):
  - Marcadores das unidades. Dá para escolher uma pelo seletor ou tocando no ponto.
  - **Minha localização em tempo real**: o ponto azul acompanha a pessoa enquanto ela se move. O GPS é desligado ao sair da tela.
  - **Traçar rota**: desenha o trajeto de carro até a unidade e mostra a distância e o tempo estimado. Se a pessoa sair do caminho, a rota é recalculada.
  - **Abrir no app de mapas**: envia o destino ao Google Maps, ao Waze ou a outro app instalado, para navegação por voz.
- **Socorros** e **Sobre**: em desenvolvimento.

## Como executar

1. Abra o projeto no Android Studio e sincronize o Gradle.
2. Execute o módulo `app` num aparelho ou emulador com internet (minSdk 24).
3. Para testar a localização no emulador, defina uma posição em *Extended controls › Location*.

### Serviço de rotas (opcional)

Sem nenhuma configuração, as rotas usam o servidor público de demonstração do [OSRM](https://project-osrm.org/). Ele serve para testes, mas não tem garantia de disponibilidade.

Para usar o [OpenRouteService](https://openrouteservice.org/dev/#/signup), crie uma chave gratuita e adicione ao `local.properties`, que não vai para o Git:

```properties
ORS_API_KEY=sua_chave_aqui
```

## Tecnologias

Java 11 · Views/XML · Fragments + BottomNavigationView · SQLite (`SQLiteOpenHelper`) · MapLibre Native 13 · tiles do OpenStreetMap · `LocationManager` do Android (sem Google Play Services) · OkHttp.

## Documentação

Detalhes de cada etapa em [`docs/`](docs):

- [ETAPA_1.md](docs/ETAPA_1.md) e [DESIGN_ETAPA_1.md](docs/DESIGN_ETAPA_1.md): base nativa e design
- [UNIDADES_SQLITE.md](docs/UNIDADES_SQLITE.md): unidades e banco de dados
- [MAPA_OPENSTREETMAP.md](docs/MAPA_OPENSTREETMAP.md): mapa com MapLibre e OpenStreetMap
- [ROTAS_TEMPO_REAL.md](docs/ROTAS_TEMPO_REAL.md): localização em tempo real e rotas

Mapa © [OpenStreetMap contributors](https://www.openstreetmap.org/copyright).
