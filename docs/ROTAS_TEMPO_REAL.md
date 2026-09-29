# Localização em tempo real e rotas

## O que foi adicionado

- **Minha localização** passa a acompanhar a pessoa continuamente (a cada ~2 s ou 5 m) pelo `LocationManager` do Android, sem Google Play Services. O botão vira **Parar de acompanhar**. O GPS é desligado ao sair da tela e religado ao voltar.
- O cartão da unidade ganhou dois botões:
  - **Traçar rota**: pede a localização se preciso, calcula o trajeto de carro e o desenha em azul abaixo dos marcadores, com distância e tempo estimado. Se a pessoa se afastar mais de 60 m do trajeto, a rota é recalculada (no máximo a cada 20 s). Tocar de novo em **Limpar rota**, ou trocar de unidade, remove o trajeto.
  - **Abrir no app de mapas**: envia o destino ao app instalado (Google Maps, Waze etc.) pela URI `geo:`, para navegação por voz.

## Serviço de rotas

`utils/Rotas.java` escolhe o serviço automaticamente:

- **Sem configuração**: usa o servidor público de demonstração do [OSRM](https://project-osrm.org/). Funciona sem cadastro, mas não tem garantia de disponibilidade e não deve ser usado em produção.
- **Com chave do [OpenRouteService](https://openrouteservice.org/dev/#/signup)** (gratuita): adicione ao `local.properties`, que não vai para o Git:

  ```properties
  ORS_API_KEY=sua_chave_aqui
  ```

  Sincronize o Gradle. A chave entra em `BuildConfig.ORS_API_KEY`.

As requisições usam o mesmo cliente HTTP do mapa (`MapaConfig.cliente()`), com o User-Agent do app.

## Como testar

1. No emulador, defina uma localização em Rio Branco (Extended controls › Location) ou use um aparelho com GPS.
2. Toque em **Minha localização** e permita o acesso. O ponto azul deve aparecer e se mover com a posição.
3. Selecione uma unidade e toque em **Traçar rota**. A rota aparece e o status mostra distância e tempo.
4. No emulador, reproduza uma rota GPX/KML ou mude a posição para longe do trajeto. A rota deve ser recalculada.
5. Toque em **Abrir no app de mapas** e confira que o destino abre no app instalado.
6. Negue a permissão, ou desligue a localização, e confira que a tela continua utilizável.
