# Saúde Fácil — etapa 1

> Registro da entrega inicial. Para a revisão visual solicitada posteriormente, use [DESIGN_ETAPA_1.md](DESIGN_ETAPA_1.md), que contém os arquivos atualizados completos.

Esta entrega configura somente a base Android nativa e as cinco abas. O código já foi aplicado ao projeto. Não é necessário criar outro projeto nem copiar novamente os arquivos. Abaixo está o conteúdo completo de cada arquivo criado ou modificado para consulta e reprodução.

## Referência e decisões

A referência é a apostila **Apostila 3 - Desenvolvimento Nativo.pdf**, de Flávio Miranda de Farias (IFAC, 2026-2): Empty Views Activity, Java, layouts XML, findViewById, Intents, permissões, SQLite e resources. Fragments e BottomNavigationView complementam a apostila conforme o requisito deste projeto. O banco será implementado com SQLiteOpenHelper e parâmetros, conforme solicitado, sem reproduzir a concatenação SQL do exemplo didático.

O projeto existente já corresponde à base Java/XML. Mantivemos o pacote `com.example.n2dmii`, minSdk 24, compileSdk/targetSdk 37, AGP 9.3.3 e Gradle Wrapper 9.5.0. O nome exibido passa a ser Saúde Fácil.

Os três scripts `.gradle.kts` foram substituídos por `.gradle` em Groovy. Não há código de aplicativo em Kotlin, Compose, React Native, Expo, HTML, CSS ou JavaScript. Bibliotecas AndroidX/Material podem conter implementações internas em Kotlin; isso não muda a linguagem Java do aplicativo.

## 1. Arquitetura

- **MainActivity:** mantém a barra inferior e hospeda um Fragment por vez em FragmentContainerView. A seleção é salva no Bundle; o FragmentManager restaura o Fragment após recriação.
- **Fragments:** Início, Unidades, Mapa, Socorros e Sobre. Cada um tem seu próprio XML, construtor público e vinculação com findViewById em onViewCreated.
- **Activities separadas, futuras:** AvaliacaoActivity, ResultadoActivity e DetalheSocorroActivity. Serão abertas por Intent e não terão barra inferior. Resultado receberá os campos por putExtra/getExtra.
- **Models e Adapters, futuros:** modelos Java e RecyclerView.Adapter próprios para as unidades e os guias.
- **DatabaseHelper, futuro:** SQLiteOpenHelper com cadastrar, listar, atualizar e excluir; dados iniciais apenas em onCreate; ContentValues e parâmetros de seleção.
- **EmergenciaUtils, futuro:** confirmação e ACTION_DIAL para 192, com tratamento de ausência de aplicativo discador.

As abas não criam uma pilha de navegação entre si. Nesta base, Voltar usa o comportamento padrão da Activity. Ao alternar abas, o Fragment anterior é substituído; a preservação de filtros será tratada com a implementação da lista.

## 2. Telas

| Tela | Componente | Nesta entrega |
|---|---|---|
| Início | HomeFragment | Nome, apresentação e aviso médico |
| Unidades | UnidadesFragment | Tela inicial da aba |
| Mapa | MapaFragment | Tela inicial, sem carregar SDK ou solicitar localização |
| Socorros | SocorrosFragment | Tela inicial da aba |
| Sobre | SobreFragment | Apresentação inicial e aviso médico |
| Avaliação | AvaliacaoActivity | Etapa futura |
| Resultado | ResultadoActivity | Etapa futura |
| Detalhes de socorro | DetalheSocorroActivity | Etapa futura |

## 3. Estrutura planejada

```text
app/src/main/
├── AndroidManifest.xml
├── java/com/example/n2dmii/
│   ├── MainActivity.java
│   ├── fragments/
│   │   ├── HomeFragment.java
│   │   ├── UnidadesFragment.java
│   │   ├── MapaFragment.java
│   │   ├── SocorrosFragment.java
│   │   └── SobreFragment.java
│   ├── activities/                    [futuro]
│   │   ├── AvaliacaoActivity.java
│   │   ├── ResultadoActivity.java
│   │   └── DetalheSocorroActivity.java
│   ├── adapters/                      [futuro]
│   │   ├── UnidadeAdapter.java
│   │   └── SocorroAdapter.java
│   ├── models/                        [futuro]
│   │   ├── UnidadeSaude.java
│   │   └── GuiaSocorro.java
│   ├── database/DatabaseHelper.java   [futuro]
│   └── utils/EmergenciaUtils.java      [futuro]
└── res/
    ├── layout/      activity_main.xml e cinco fragment_*.xml
    ├── drawable/    ícones vetoriais XML
    ├── menu/        bottom_navigation_menu.xml
    ├── color/       navigation_item.xml
    ├── values/      strings.xml, colors.xml, dimens.xml, themes.xml
    ├── values-night/ colors.xml, themes.xml
    └── mipmap/      ícones existentes do projeto
```

## 4. Gradle

| Dependência nesta etapa | Versão | Finalidade |
|---|---|---|
| androidx.appcompat:appcompat | 1.6.1 | AppCompatActivity |
| com.google.android.material:material | 1.10.0 | BottomNavigationView e tema Material Views |
| androidx.activity:activity | 1.8.0 | Base AndroidX para Activities |
| androidx.fragment:fragment | 1.6.2 | Fragments e FragmentContainerView |

As versões de AppCompat e Material já existentes foram mantidas. As dependências de teste existentes permanecem no catálogo. Removemos as dependências diretas activity-ktx e ConstraintLayout, que não são usadas nesta etapa.

Etapas futuras adicionarão `androidx.recyclerview:recyclerview`, `com.google.android.gms:play-services-maps` e `com.google.android.gms:play-services-location`, com versões verificadas na implementação correspondente. SQLite é parte do Android e não precisa de biblioteca externa.

`android.builtInKotlin=false` desativa o suporte automático de compilação Kotlin do AGP 9 nesta base Java. O AGP 9.3.3 emite aviso de descontinuação dessa opção para o AGP 10; o aviso não deve ser ocultado e a migração será reavaliada quando houver atualização do plugin.

## 5. Permissões e chave do Maps

O manifesto declara INTERNET, ACCESS_FINE_LOCATION e ACCESS_COARSE_LOCATION. INTERNET não gera diálogo em tempo de execução. As permissões de localização só serão solicitadas na etapa do mapa, juntas, com Activity Result API, aceitando localização aproximada e tratando recusa. Não solicitaremos localização em segundo plano.

ACTION_DIAL abre o discador e não exige CALL_PHONE. O botão de emergência primeiro pedirá confirmação; a ligação continuará dependendo da ação da pessoa no discador. Nenhuma chamada é realizada nesta etapa.

Na etapa do mapa, uma chave será armazenada em arquivo local ignorado pelo Git e injetada pelo Gradle no metadado `com.google.android.geo.API_KEY`. Será necessário habilitar Maps SDK for Android no projeto Google Cloud e configurar o faturamento exigido pelo serviço. A chave deverá ser restrita ao pacote `com.example.n2dmii`, ao SHA-1 do certificado utilizado e ao Maps SDK for Android. Mantê-la fora do código evita exposição no repositório; a chave ainda pode ser extraída do APK, portanto as restrições são necessárias. A configuração exata e os arquivos completos serão entregues naquela etapa; a etapa 1 compila sem chave.

Referências oficiais: [FragmentContainerView](https://developer.android.com/reference/androidx/fragment/app/FragmentContainerView), [Maps SDK](https://developers.google.com/maps/documentation/android-sdk/start) e [configuração local da chave](https://developers.google.com/maps/documentation/android-sdk/secrets-gradle-plugin).

## 6. Ordem de implementação

1. Base, resources, manifesto e navegação — esta entrega.
2. Tela inicial, atalhos e discador com confirmação.
3. SQLite, modelo de unidade, CRUD, pesquisa e RecyclerView de unidades.
4. Maps SDK, configuração da chave, marcadores e localização com permissões.
5. Lista de primeiros socorros e Activity de detalhes; conteúdo revisado em fontes oficiais.
6. Avaliação com validação, Intent/putExtra e resultado informativo sem diagnóstico; critérios conservadores para sinais de urgência.
7. Sobre, versão, informações legais e revisão integrada.

Cada etapa termina aguardando sua confirmação de compilação e teste.

## Como testar a etapa 1

1. Abra este projeto no Android Studio. Para reproduzir do zero, escolha **New Project > Empty Views Activity**, nome Saúde Fácil, pacote `com.example.n2dmii`, linguagem Java e mínimo API 24. Nesta entrega foi aproveitado o projeto existente.
2. Use o JDK incorporado do Android Studio em **Settings > Build, Execution, Deployment > Build Tools > Gradle > Gradle JDK**. O nível de código Java do app é 11; o JDK que executa o Gradle é o incorporado da IDE.
3. Confira no SDK Manager que a plataforma API 37 está instalada. Mantenha `local.properties` com o caminho do seu SDK, sem copiar caminhos de outro computador.
4. Execute **Sync Project with Gradle Files**.
5. Execute o módulo `app` em aparelho/emulador API 24 ou superior.
6. Confira o nome Saúde Fácil, a apresentação e o aviso médico no Início.
7. Toque em todas as cinco abas. O conteúdo e o item selecionado devem corresponder.
8. Na aba Unidades, gire o aparelho. A aba Unidades deve continuar aberta e selecionada.
9. Confira o modo claro e o modo escuro, a rolagem em tela pequena e a barra inferior sem sobreposição aos controles do sistema.
10. Não é esperado diálogo de localização, mapa, lista de unidades, formulário ou discador nesta etapa.

Não prossiga para a etapa 2 antes de confirmar a compilação e o teste.

## Arquivos completos

Todos os caminhos abaixo são relativos à raiz do projeto. Os arquivos `build.gradle.kts`, `settings.gradle.kts` e `app/build.gradle.kts` foram removidos e substituídos pelas versões Groovy; não mantenha ambos os formatos.

Permanecem os arquivos gerados pelo template: Gradle Wrapper, `local.properties`, ícones de lançamento em mipmap/drawable, regras de backup em res/xml e testes de exemplo. As alterações prévias em `.idea` não fazem parte desta entrega.


### Criar: build.gradle

```groovy
plugins {
    alias(libs.plugins.android.application) apply false
}
```

### Criar: settings.gradle

```groovy
pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}
rootProject.name = 'SaudeFacil'
include ':app'
```

### Criar: app/build.gradle

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

[libraries]
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

### Modificar: gradle.properties

```properties
org.gradle.jvmargs=-Xmx2048m -Dfile.encoding=UTF-8
org.gradle.configuration-cache=true
android.useAndroidX=true
android.builtInKotlin=false
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
        <!-- As outras Activities e a configuração do Maps entrarão nas suas etapas. -->
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
                    WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.displayCutout());
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

### Modificar: app/src/main/res/layout/activity_main.xml

```xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:id="@+id/main"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:background="@color/background"
    android:orientation="vertical">
    <androidx.fragment.app.FragmentContainerView
        android:id="@+id/fragment_container"
        android:layout_width="match_parent"
        android:layout_height="@dimen/zero"
        android:layout_weight="1" />
    <com.google.android.material.bottomnavigation.BottomNavigationView
        android:id="@+id/bottom_navigation"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:background="@color/surface"
        app:itemIconTint="@color/navigation_item"
        app:itemTextColor="@color/navigation_item"
        app:labelVisibilityMode="labeled"
        app:menu="@menu/bottom_navigation_menu" />
</LinearLayout>
```

### Modificar: app/src/main/res/values/colors.xml

```xml
<resources>
    <color name="primary">#006B5B</color>
    <color name="on_primary">#FFFFFF</color>
    <color name="background">#F4F8F7</color>
    <color name="surface">#FFFFFF</color>
    <color name="text_primary">#172E29</color>
    <color name="text_secondary">#465D57</color>
    <color name="warning_background">#FFF0CE</color>
    <color name="warning_text">#614500</color>
</resources>
```

### Modificar: app/src/main/res/values/strings.xml

```xml
<resources>
    <string name="app_name">Saúde Fácil</string>
    <string name="nav_inicio">Início</string>
    <string name="nav_unidades">Unidades</string>
    <string name="nav_mapa">Mapa</string>
    <string name="nav_socorros">Socorros</string>
    <string name="nav_sobre">Sobre</string>
    <string name="home_description">Acesso rápido a informações de saúde e emergência.</string>
    <string name="medical_disclaimer">Este aplicativo não substitui avaliação médica profissional. As informações são educativas e não representam diagnóstico.</string>
    <string name="stage_one_notice">Etapa 1: navegação inicial. Os atalhos e as funcionalidades serão adicionados nas próximas etapas.</string>
    <string name="unidades_title">Unidades de saúde</string>
    <string name="unidades_description">A pesquisa e a lista de hospitais, UPAs e postos de saúde serão implementadas na etapa de unidades.</string>
    <string name="mapa_title">Mapa de unidades</string>
    <string name="mapa_description">O mapa, os marcadores e a solicitação de localização serão implementados na etapa de mapas.</string>
    <string name="socorros_title">Primeiros socorros</string>
    <string name="socorros_description">As categorias e as instruções serão implementadas na etapa de primeiros socorros.</string>
    <string name="sobre_title">Sobre o Saúde Fácil</string>
    <string name="sobre_description">Aplicativo Android nativo desenvolvido em Java, com interface em Views e layouts XML. As informações completas serão adicionadas na etapa Sobre.</string>
</resources>
```

### Criar: app/src/main/res/values/dimens.xml

```xml
<resources>
    <dimen name="zero">0dp</dimen>
    <dimen name="screen_padding">24dp</dimen>
    <dimen name="spacing_medium">16dp</dimen>
    <dimen name="spacing_large">24dp</dimen>
    <dimen name="title_size">28sp</dimen>
    <dimen name="body_size">16sp</dimen>
    <dimen name="icon_size">24dp</dimen>
</resources>
```

### Criar: app/src/main/res/values-night/colors.xml

```xml
<resources>
    <color name="primary">#79D8BE</color>
    <color name="on_primary">#00382D</color>
    <color name="background">#101916</color>
    <color name="surface">#1C2823</color>
    <color name="text_primary">#E2EEE7</color>
    <color name="text_secondary">#B4C8BC</color>
    <color name="warning_background">#453719</color>
    <color name="warning_text">#FFE0A0</color>
</resources>
```

### Criar: app/src/main/res/color/navigation_item.xml

```xml
<?xml version="1.0" encoding="utf-8"?>
<selector xmlns:android="http://schemas.android.com/apk/res/android">
    <item android:color="@color/primary" android:state_checked="true" />
    <item android:color="@color/text_secondary" />
</selector>
```

### Criar: app/src/main/res/menu/bottom_navigation_menu.xml

```xml
<?xml version="1.0" encoding="utf-8"?>
<menu xmlns:android="http://schemas.android.com/apk/res/android">
    <item android:id="@+id/nav_inicio" android:icon="@drawable/ic_home" android:title="@string/nav_inicio" />
    <item android:id="@+id/nav_unidades" android:icon="@drawable/ic_hospital" android:title="@string/nav_unidades" />
    <item android:id="@+id/nav_mapa" android:icon="@drawable/ic_map" android:title="@string/nav_mapa" />
    <item android:id="@+id/nav_socorros" android:icon="@drawable/ic_aid" android:title="@string/nav_socorros" />
    <item android:id="@+id/nav_sobre" android:icon="@drawable/ic_info" android:title="@string/nav_sobre" />
</menu>
```

### Modificar: app/src/main/res/values/themes.xml

```xml
<resources>
    <style name="Theme.SaudeFacil" parent="Theme.Material3.DayNight.NoActionBar">
        <item name="colorPrimary">@color/primary</item>
        <item name="colorOnPrimary">@color/on_primary</item>
        <item name="colorSecondary">@color/primary</item>
        <item name="colorSurface">@color/surface</item>
        <item name="colorOnSurface">@color/text_primary</item>
        <item name="android:colorBackground">@color/background</item>
        <item name="android:windowLightStatusBar">true</item>
    </style>
</resources>
```

### Modificar: app/src/main/res/values-night/themes.xml

```xml
<resources>
    <style name="Theme.SaudeFacil" parent="Theme.Material3.DayNight.NoActionBar">
        <item name="colorPrimary">@color/primary</item>
        <item name="colorOnPrimary">@color/on_primary</item>
        <item name="colorSecondary">@color/primary</item>
        <item name="colorSurface">@color/surface</item>
        <item name="colorOnSurface">@color/text_primary</item>
        <item name="android:colorBackground">@color/background</item>
        <item name="android:windowLightStatusBar">false</item>
    </style>
</resources>
```

### Criar: app/src/main/java/com/example/n2dmii/fragments/HomeFragment.java

```java
package com.example.n2dmii.fragments;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.n2dmii.R;

public class HomeFragment extends Fragment {
    /* Associa o Fragment ao layout XML e permite sua recriação pelo Android. */
    public HomeFragment() {
        super(R.layout.fragment_home);
    }

    /* Vincula os componentes após a criação da View e carrega os textos dos resources. */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        TextView titulo = view.findViewById(R.id.text_title);
        TextView descricao = view.findViewById(R.id.text_description);
        titulo.setText(R.string.app_name);
        descricao.setText(R.string.home_description);
    }
}
```

### Criar: app/src/main/res/layout/fragment_home.xml

```xml
<?xml version="1.0" encoding="utf-8"?>
<androidx.core.widget.NestedScrollView xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:fillViewport="true">
    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="vertical"
        android:padding="@dimen/screen_padding">
        <TextView
            android:id="@+id/text_title"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:accessibilityHeading="true"
            android:textColor="@color/text_primary"
            android:textSize="@dimen/title_size"
            android:textStyle="bold"
            tools:text="@string/app_name" />
        <TextView
            android:id="@+id/text_description"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:layout_marginTop="@dimen/spacing_medium"
            android:textColor="@color/text_secondary"
            android:textSize="@dimen/body_size"
            tools:text="@string/home_description" />
        <TextView
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:layout_marginTop="@dimen/spacing_large"
            android:background="@color/warning_background"
            android:padding="@dimen/spacing_medium"
            android:text="@string/medical_disclaimer"
            android:textColor="@color/warning_text"
            android:textSize="@dimen/body_size" />
        <TextView
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:layout_marginTop="@dimen/spacing_large"
            android:text="@string/stage_one_notice"
            android:textColor="@color/text_secondary"
            android:textSize="@dimen/body_size" />
    </LinearLayout>
</androidx.core.widget.NestedScrollView>
```

### Criar: app/src/main/java/com/example/n2dmii/fragments/UnidadesFragment.java

```java
package com.example.n2dmii.fragments;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.n2dmii.R;

public class UnidadesFragment extends Fragment {
    /* Associa o Fragment ao layout XML e permite sua recriação pelo Android. */
    public UnidadesFragment() {
        super(R.layout.fragment_unidades);
    }

    /* Vincula os componentes após a criação da View e carrega os textos dos resources. */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        TextView titulo = view.findViewById(R.id.text_title);
        TextView descricao = view.findViewById(R.id.text_description);
        titulo.setText(R.string.unidades_title);
        descricao.setText(R.string.unidades_description);
    }
}
```

### Criar: app/src/main/res/layout/fragment_unidades.xml

```xml
<?xml version="1.0" encoding="utf-8"?>
<androidx.core.widget.NestedScrollView xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:fillViewport="true">
    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="vertical"
        android:padding="@dimen/screen_padding">
        <TextView
            android:id="@+id/text_title"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:accessibilityHeading="true"
            android:textColor="@color/text_primary"
            android:textSize="@dimen/title_size"
            android:textStyle="bold"
            tools:text="@string/unidades_title" />
        <TextView
            android:id="@+id/text_description"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:layout_marginTop="@dimen/spacing_medium"
            android:textColor="@color/text_secondary"
            android:textSize="@dimen/body_size"
            tools:text="@string/unidades_description" />
    </LinearLayout>
</androidx.core.widget.NestedScrollView>
```

### Criar: app/src/main/java/com/example/n2dmii/fragments/MapaFragment.java

```java
package com.example.n2dmii.fragments;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.n2dmii.R;

public class MapaFragment extends Fragment {
    /* Associa o Fragment ao layout XML e permite sua recriação pelo Android. */
    public MapaFragment() {
        super(R.layout.fragment_mapa);
    }

    /* Vincula os componentes após a criação da View e carrega os textos dos resources. */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        TextView titulo = view.findViewById(R.id.text_title);
        TextView descricao = view.findViewById(R.id.text_description);
        titulo.setText(R.string.mapa_title);
        descricao.setText(R.string.mapa_description);
    }
}
```

### Criar: app/src/main/res/layout/fragment_mapa.xml

```xml
<?xml version="1.0" encoding="utf-8"?>
<androidx.core.widget.NestedScrollView xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:fillViewport="true">
    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="vertical"
        android:padding="@dimen/screen_padding">
        <TextView
            android:id="@+id/text_title"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:accessibilityHeading="true"
            android:textColor="@color/text_primary"
            android:textSize="@dimen/title_size"
            android:textStyle="bold"
            tools:text="@string/mapa_title" />
        <TextView
            android:id="@+id/text_description"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:layout_marginTop="@dimen/spacing_medium"
            android:textColor="@color/text_secondary"
            android:textSize="@dimen/body_size"
            tools:text="@string/mapa_description" />
    </LinearLayout>
</androidx.core.widget.NestedScrollView>
```

### Criar: app/src/main/java/com/example/n2dmii/fragments/SocorrosFragment.java

```java
package com.example.n2dmii.fragments;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.n2dmii.R;

public class SocorrosFragment extends Fragment {
    /* Associa o Fragment ao layout XML e permite sua recriação pelo Android. */
    public SocorrosFragment() {
        super(R.layout.fragment_socorros);
    }

    /* Vincula os componentes após a criação da View e carrega os textos dos resources. */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        TextView titulo = view.findViewById(R.id.text_title);
        TextView descricao = view.findViewById(R.id.text_description);
        titulo.setText(R.string.socorros_title);
        descricao.setText(R.string.socorros_description);
    }
}
```

### Criar: app/src/main/res/layout/fragment_socorros.xml

```xml
<?xml version="1.0" encoding="utf-8"?>
<androidx.core.widget.NestedScrollView xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:fillViewport="true">
    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="vertical"
        android:padding="@dimen/screen_padding">
        <TextView
            android:id="@+id/text_title"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:accessibilityHeading="true"
            android:textColor="@color/text_primary"
            android:textSize="@dimen/title_size"
            android:textStyle="bold"
            tools:text="@string/socorros_title" />
        <TextView
            android:id="@+id/text_description"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:layout_marginTop="@dimen/spacing_medium"
            android:textColor="@color/text_secondary"
            android:textSize="@dimen/body_size"
            tools:text="@string/socorros_description" />
    </LinearLayout>
</androidx.core.widget.NestedScrollView>
```

### Criar: app/src/main/java/com/example/n2dmii/fragments/SobreFragment.java

```java
package com.example.n2dmii.fragments;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.n2dmii.R;

public class SobreFragment extends Fragment {
    /* Associa o Fragment ao layout XML e permite sua recriação pelo Android. */
    public SobreFragment() {
        super(R.layout.fragment_sobre);
    }

    /* Vincula os componentes após a criação da View e carrega os textos dos resources. */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        TextView titulo = view.findViewById(R.id.text_title);
        TextView descricao = view.findViewById(R.id.text_description);
        titulo.setText(R.string.sobre_title);
        descricao.setText(R.string.sobre_description);
    }
}
```

### Criar: app/src/main/res/layout/fragment_sobre.xml

```xml
<?xml version="1.0" encoding="utf-8"?>
<androidx.core.widget.NestedScrollView xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:fillViewport="true">
    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="vertical"
        android:padding="@dimen/screen_padding">
        <TextView
            android:id="@+id/text_title"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:accessibilityHeading="true"
            android:textColor="@color/text_primary"
            android:textSize="@dimen/title_size"
            android:textStyle="bold"
            tools:text="@string/sobre_title" />
        <TextView
            android:id="@+id/text_description"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:layout_marginTop="@dimen/spacing_medium"
            android:textColor="@color/text_secondary"
            android:textSize="@dimen/body_size"
            tools:text="@string/sobre_description" />
        <TextView
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:layout_marginTop="@dimen/spacing_large"
            android:background="@color/warning_background"
            android:padding="@dimen/spacing_medium"
            android:text="@string/medical_disclaimer"
            android:textColor="@color/warning_text"
            android:textSize="@dimen/body_size" />
    </LinearLayout>
</androidx.core.widget.NestedScrollView>
```

### Criar: app/src/main/res/drawable/ic_home.xml

```xml
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="@dimen/icon_size"
    android:height="@dimen/icon_size"
    android:viewportWidth="24"
    android:viewportHeight="24">
    <path android:fillColor="@color/text_primary" android:pathData="M10,20v-6h4v6h5v-9h3L12,2 2,11h3v9z" />
</vector>
```

### Criar: app/src/main/res/drawable/ic_hospital.xml

```xml
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="@dimen/icon_size"
    android:height="@dimen/icon_size"
    android:viewportWidth="24"
    android:viewportHeight="24">
    <path android:fillColor="@color/text_primary" android:pathData="M19,3H5c-1.1,0 -2,0.9 -2,2v14c0,1.1 0.9,2 2,2h14c1.1,0 2,-0.9 2,-2V5c0,-1.1 -0.9,-2 -2,-2zM18,14h-4v4h-4v-4H6v-4h4V6h4v4h4z" />
</vector>
```

### Criar: app/src/main/res/drawable/ic_map.xml

```xml
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="@dimen/icon_size"
    android:height="@dimen/icon_size"
    android:viewportWidth="24"
    android:viewportHeight="24">
    <path android:fillColor="@color/text_primary" android:pathData="M20.5,3l-0.16,0.03L15,5 9,3 3.36,4.9C3.15,4.97 3,5.16 3,5.39V20.5c0,0.28 0.22,0.5 0.5,0.5l0.16,-0.03L9,19l6,2 5.64,-1.9c0.21,-0.07 0.36,-0.26 0.36,-0.49V3.5c0,-0.28 -0.22,-0.5 -0.5,-0.5zM10,5.47l4,1.33v11.73l-4,-1.33z" />
</vector>
```

### Criar: app/src/main/res/drawable/ic_aid.xml

```xml
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="@dimen/icon_size"
    android:height="@dimen/icon_size"
    android:viewportWidth="24"
    android:viewportHeight="24">
    <path android:fillColor="@color/text_primary" android:pathData="M20,6h-4V4c0,-1.1 -0.9,-2 -2,-2h-4c-1.1,0 -2,0.9 -2,2v2H4c-1.1,0 -2,0.9 -2,2v12c0,1.1 0.9,2 2,2h16c1.1,0 2,-0.9 2,-2V8c0,-1.1 -0.9,-2 -2,-2zM10,4h4v2h-4zM16,15h-3v3h-2v-3H8v-2h3v-3h2v3h3z" />
</vector>
```

### Criar: app/src/main/res/drawable/ic_info.xml

```xml
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="@dimen/icon_size"
    android:height="@dimen/icon_size"
    android:viewportWidth="24"
    android:viewportHeight="24">
    <path android:fillColor="@color/text_primary" android:pathData="M11,17h2v-6h-2zM12,2a10,10 0,1 0,0 20a10,10 0,0 0,0 -20zM12,20a8,8 0,1 1,0 -16a8,8 0,0 1,0 16zM11,9h2V7h-2z" />
</vector>
```

## Verificação desta entrega

Executado com o JDK incorporado do Android Studio:

`gradlew.bat :app:assembleDebug :app:lintDebug --console=plain`

Resultado final: BUILD SUCCESSFUL. APK em `app/build/outputs/apk/debug/app-debug.apk`. O Lint terminou sem erros e com 14 avisos: 7 sobre versões mais recentes de dependências/plugin, 5 sobre accessibilityHeading em versões anteriores à API 28, 1 sobre peso com dimensão zero referenciada por resource e 1 sobre desenho duplicado do fundo. Em API 24–27 o atributo XML accessibilityHeading é ignorado; o texto permanece legível. O aviso de descontinuação de android.builtInKotlin=false também está descrito acima.

Não havia aparelho ou emulador conectado no adb. Portanto, navegação, rotação e aparência ainda precisam do seu teste no dispositivo. A próxima etapa permanece aguardando sua confirmação.
