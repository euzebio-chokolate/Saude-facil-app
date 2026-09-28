# Design da etapa 1 — referências visuais

Esta revisão aplica o estilo das seis imagens enviadas à base existente, em Java e XML. A entrega continua limitada à etapa 1. Não é necessário copiar os arquivos: já estão aplicados no projeto.

## O que mudou

- Cabeçalho verde-água com nome e saudação.
- Fundo cinza claro, cartões brancos arredondados, ícones vetoriais e tipografia compacta.
- Cartões de SAMU, Bombeiros, Polícia e CVV. São informativos nesta etapa; não abrem o discador. O título não anuncia interação. Foram usados tons um pouco mais escuros para contraste com o texto branco. CVV aparece como apoio emocional.
- Atalhos para as abas Unidades, Socorros e Mapa. Avaliar sintomas aparece como “Em breve” e não é clicável.
- Aviso médico preservado após os atalhos, acessível por rolagem.
- Barra inferior compacta e cores adaptadas ao modo escuro.
- Estrutura visual de cartões nas outras abas, com mensagens de conteúdo futuro. O campo de pesquisa é somente uma prévia visual nesta etapa.

As imagens de listas, mapa, autoavaliação e resultado serão usadas nas respectivas etapas funcionais. Não foram inseridos hospitais, mapas ilustrativos ou instruções médicas fictícias para simular funcionalidades prontas. Sobre segue a mesma identidade, pois não foi enviada uma imagem dessa tela.

## Verificação e teste

Compilar com `gradlew.bat :app:assembleDebug :app:lintDebug`. A revisão foi instalada e aberta no emulador para conferir o layout.

No Android Studio, execute `app`, confira os cartões e role até o aviso médico. Teste os atalhos para as abas e a barra inferior. Confira também rotação, tema escuro e tamanho de fonte aumentado. Confirme o teste antes da próxima etapa.

## Conteúdo completo dos arquivos desta revisão

Os caminhos são relativos à raiz do projeto. “Criar” significa arquivo novo nesta revisão; “Modificar” significa arquivo existente na etapa 1. Gradle, manifesto e temas continuam com a configuração entregue anteriormente. A configuração de Google Maps e suas permissões em execução permanece reservada à etapa do mapa.


### Modificar: app/src/main/res/values/colors.xml

```xml
<resources>
    <color name="primary">#168F90</color>
    <color name="on_primary">#FFFFFF</color>
    <color name="background">#F2F3F5</color>
    <color name="surface">#FFFFFF</color>
    <color name="text_primary">#263140</color>
    <color name="text_secondary">#626C7D</color>
    <color name="warning_background">#FFF6DF</color>
    <color name="warning_text">#654C16</color>
    <color name="outline">#E5E7EB</color>
    <color name="primary_soft">#E4F3F3</color>
    <color name="header_background">#209697</color>
    <color name="header_text">#FFFFFF</color>
    <color name="emergency_samu">#D93830</color>
    <color name="emergency_fire">#BC5100</color>
    <color name="emergency_police">#2668D7</color>
    <color name="emergency_cvv">#8840CF</color>
    <color name="emergency_text">#FFFFFF</color>
    <color name="icon_overlay">#26FFFFFF</color>
</resources>
```

### Modificar: app/src/main/res/values-night/colors.xml

```xml
<resources>
    <color name="primary">#72D3D0</color>
    <color name="on_primary">#003738</color>
    <color name="background">#141B22</color>
    <color name="surface">#202A34</color>
    <color name="text_primary">#EDF2F7</color>
    <color name="text_secondary">#BDC7D4</color>
    <color name="warning_background">#40351F</color>
    <color name="warning_text">#FFE2A4</color>
    <color name="outline">#36424F</color>
    <color name="primary_soft">#213E42</color>
</resources>
```

### Modificar: app/src/main/res/values/dimens.xml

```xml
<resources>
    <dimen name="zero">0dp</dimen>
    <dimen name="screen_padding">16dp</dimen>
    <dimen name="spacing_small">8dp</dimen>
    <dimen name="spacing_medium">16dp</dimen>
    <dimen name="spacing_large">24dp</dimen>
    <dimen name="title_size">22sp</dimen>
    <dimen name="body_size">14sp</dimen>
    <dimen name="caption_size">12sp</dimen>
    <dimen name="section_size">11sp</dimen>
    <dimen name="card_title_size">16sp</dimen>
    <dimen name="number_size">24sp</dimen>
    <dimen name="icon_size">24dp</dimen>
    <dimen name="icon_box">40dp</dimen>
    <dimen name="card_radius">16dp</dimen>
    <dimen name="card_padding">14dp</dimen>
    <dimen name="card_height">72dp</dimen>
    <dimen name="stroke_width">1dp</dimen>
    <dimen name="navigation_text_size">10sp</dimen>
    <dimen name="navigation_padding">6dp</dimen>
    <dimen name="navigation_height">64dp</dimen>
    <dimen name="map_preview_height">280dp</dimen>
</resources>
```

### Criar: app/src/main/res/values/design_strings.xml

```xml
<resources>
    <string name="home_eyebrow">ATENDIMENTO RÁPIDO</string>
    <string name="home_greeting">Como podemos ajudar?</string>
    <string name="emergency_section">TELEFONES DE EMERGÊNCIA E APOIO</string>
    <string name="actions_section">AÇÕES RÁPIDAS</string>
    <string name="samu_name">SAMU</string>
    <string name="samu_description">Urgência médica</string>
    <string name="samu_number">192</string>
    <string name="fire_name">Bombeiros</string>
    <string name="fire_description">Resgate / Incêndio</string>
    <string name="fire_number">193</string>
    <string name="police_name">Polícia</string>
    <string name="police_description">Segurança</string>
    <string name="police_number">190</string>
    <string name="cvv_name">CVV</string>
    <string name="cvv_description">Apoio emocional</string>
    <string name="cvv_number">188</string>
    <string name="assessment_label">Avaliar sintomas</string>
    <string name="assessment_soon">Em breve</string>
    <string name="units_short">Hospitais e postos próximos</string>
    <string name="aid_short">Informações de emergência</string>
    <string name="map_short">Localização das unidades</string>
    <string name="search_preview">Pesquise por nome ou bairro…</string>
    <string name="units_pending">As unidades de saúde estarão disponíveis aqui em breve.</string>
    <string name="map_pending">O mapa de unidades estará disponível aqui em breve.</string>
    <string name="aid_pending">Os guias de primeiros socorros estarão disponíveis aqui em breve.</string>
    <string name="about_summary">Informação de saúde de um jeito simples, para ajudar você a encontrar orientação e serviços.</string>
    <string name="medical_label">INFORMAÇÃO IMPORTANTE</string>
</resources>
```

### Criar: app/src/main/res/values/styles.xml

```xml
<resources>
    <style name="SectionLabel">
        <item name="android:layout_width">match_parent</item>
        <item name="android:layout_height">wrap_content</item>
        <item name="android:layout_marginTop">@dimen/spacing_large</item>
        <item name="android:layout_marginBottom">@dimen/spacing_small</item>
        <item name="android:textColor">@color/text_secondary</item>
        <item name="android:textSize">@dimen/section_size</item>
        <item name="android:textStyle">bold</item>
        <item name="android:letterSpacing">0.08</item>
    </style>
    <style name="CardTitle">
        <item name="android:layout_width">match_parent</item>
        <item name="android:layout_height">wrap_content</item>
        <item name="android:textColor">@color/text_primary</item>
        <item name="android:textSize">@dimen/card_title_size</item>
        <item name="android:textStyle">bold</item>
    </style>
    <style name="CardCaption">
        <item name="android:layout_width">match_parent</item>
        <item name="android:layout_height">wrap_content</item>
        <item name="android:textColor">@color/text_secondary</item>
        <item name="android:textSize">@dimen/caption_size</item>
    </style>
    <style name="NavigationText" parent="TextAppearance.MaterialComponents.Caption">
        <item name="android:textSize">@dimen/navigation_text_size</item>
    </style>
</resources>
```

### Criar: app/src/main/res/drawable/bg_surface.xml

```xml
<shape xmlns:android="http://schemas.android.com/apk/res/android">
    <solid android:color="@color/surface" />
    <corners android:radius="@dimen/card_radius" />
    <stroke android:width="@dimen/stroke_width" android:color="@color/outline" />
</shape>
```

### Criar: app/src/main/res/drawable/bg_header.xml

```xml
<shape xmlns:android="http://schemas.android.com/apk/res/android">
    <solid android:color="@color/header_background" />
    <corners android:bottomLeftRadius="@dimen/card_radius" android:bottomRightRadius="@dimen/card_radius" />
</shape>
```

### Criar: app/src/main/res/drawable/bg_icon.xml

```xml
<shape xmlns:android="http://schemas.android.com/apk/res/android">
    <solid android:color="@color/primary_soft" />
    <corners android:radius="@dimen/spacing_small" />
</shape>
```

### Criar: app/src/main/res/drawable/bg_emergency_icon.xml

```xml
<shape xmlns:android="http://schemas.android.com/apk/res/android">
    <solid android:color="@color/icon_overlay" />
    <corners android:radius="@dimen/spacing_small" />
</shape>
```

### Criar: app/src/main/res/drawable/bg_notice.xml

```xml
<shape xmlns:android="http://schemas.android.com/apk/res/android">
    <solid android:color="@color/warning_background" />
    <corners android:radius="@dimen/card_radius" />
</shape>
```

### Criar: app/src/main/res/drawable/bg_samu.xml

```xml
<shape xmlns:android="http://schemas.android.com/apk/res/android">
    <solid android:color="@color/emergency_samu" />
    <corners android:radius="@dimen/card_radius" />
</shape>
```

### Criar: app/src/main/res/drawable/bg_fire.xml

```xml
<shape xmlns:android="http://schemas.android.com/apk/res/android">
    <solid android:color="@color/emergency_fire" />
    <corners android:radius="@dimen/card_radius" />
</shape>
```

### Criar: app/src/main/res/drawable/bg_police.xml

```xml
<shape xmlns:android="http://schemas.android.com/apk/res/android">
    <solid android:color="@color/emergency_police" />
    <corners android:radius="@dimen/card_radius" />
</shape>
```

### Criar: app/src/main/res/drawable/bg_cvv.xml

```xml
<shape xmlns:android="http://schemas.android.com/apk/res/android">
    <solid android:color="@color/emergency_cvv" />
    <corners android:radius="@dimen/card_radius" />
</shape>
```

### Criar: app/src/main/res/drawable/ic_phone.xml

```xml
<vector xmlns:android="http://schemas.android.com/apk/res/android" android:width="@dimen/icon_size" android:height="@dimen/icon_size" android:viewportWidth="24" android:viewportHeight="24">
    <path android:fillColor="@color/emergency_text" android:pathData="M6.6,10.8c1.4,2.8 3.8,5.2 6.6,6.6l2.2,-2.2c0.3,-0.3 0.7,-0.4 1.1,-0.2 1.2,0.4 2.5,0.6 3.8,0.6 0.6,0 1,0.4 1,1V20c0,0.6 -0.4,1 -1,1C10.7,21 3,13.3 3,3.7c0,-0.6 0.4,-1 1,-1h3.5c0.6,0 1,0.4 1,1 0,1.3 0.2,2.6 0.6,3.8 0.1,0.4 0,0.8 -0.2,1.1z" />
</vector>
```

### Criar: app/src/main/res/drawable/ic_pin.xml

```xml
<vector xmlns:android="http://schemas.android.com/apk/res/android" android:width="@dimen/icon_size" android:height="@dimen/icon_size" android:viewportWidth="24" android:viewportHeight="24">
    <path android:fillColor="@color/primary" android:pathData="M12,2C8.1,2 5,5.1 5,9c0,5.2 7,13 7,13s7,-7.8 7,-13c0,-3.9 -3.1,-7 -7,-7zM12,11.5a2.5,2.5 0,1 1,0 -5a2.5,2.5 0,0 1,0 5z" />
</vector>
```

### Criar: app/src/main/res/drawable/ic_clipboard.xml

```xml
<vector xmlns:android="http://schemas.android.com/apk/res/android" android:width="@dimen/icon_size" android:height="@dimen/icon_size" android:viewportWidth="24" android:viewportHeight="24">
    <path android:fillColor="@color/primary" android:pathData="M16,3h-2.2a2,2 0,0 0,-3.6 0H8a2,2 0,0 0,-2 2v15h12V5a2,2 0,0 0,-2 -2zM12,3a1,1 0,1 1,0 2a1,1 0,0 1,0 -2zM8,7h8v11H8z" />
</vector>
```

### Criar: app/src/main/res/drawable/ic_chevron.xml

```xml
<vector xmlns:android="http://schemas.android.com/apk/res/android" android:width="@dimen/icon_size" android:height="@dimen/icon_size" android:viewportWidth="24" android:viewportHeight="24">
    <path android:fillColor="@color/primary" android:pathData="M9,6l6,6 -6,6 -1.4,-1.4 4.6,-4.6 -4.6,-4.6z" />
</vector>
```

### Criar: app/src/main/res/drawable/ic_search.xml

```xml
<vector xmlns:android="http://schemas.android.com/apk/res/android" android:width="@dimen/icon_size" android:height="@dimen/icon_size" android:viewportWidth="24" android:viewportHeight="24">
    <path android:fillColor="@color/primary" android:pathData="M9.5,3a6.5,6.5 0,1 0,4.1 11.5L20,21l1,-1 -6.5,-6.4A6.5,6.5 0,0 0,9.5 3zM9.5,5a4.5,4.5 0,1 1,0 9a4.5,4.5 0,0 1,0 -9z" />
</vector>
```

### Criar: app/src/main/res/drawable/ic_book.xml

```xml
<vector xmlns:android="http://schemas.android.com/apk/res/android" android:width="@dimen/icon_size" android:height="@dimen/icon_size" android:viewportWidth="24" android:viewportHeight="24">
    <path android:fillColor="@color/primary" android:pathData="M3,3v16h7l2,2 2,-2h7V3h-7l-2,2 -2,-2zM5,5h4l2,2v11l-1,-1H5zM19,5v12h-5l-1,1V7l2,-2z" />
</vector>
```

### Modificar: app/src/main/res/layout/fragment_home.xml

```xml
<?xml version="1.0" encoding="utf-8"?>
<androidx.core.widget.NestedScrollView xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent" android:layout_height="match_parent" android:fillViewport="true">
    <LinearLayout android:layout_width="match_parent" android:layout_height="wrap_content" android:orientation="vertical">
        <LinearLayout android:layout_width="match_parent" android:layout_height="wrap_content" android:orientation="vertical"
            android:padding="@dimen/screen_padding" android:paddingBottom="@dimen/spacing_large" android:background="@drawable/bg_header">
            <TextView style="@style/CardCaption" android:text="@string/home_eyebrow" android:textStyle="bold" android:letterSpacing="0.1" android:textColor="@color/header_text" />
            <TextView android:id="@+id/text_title" style="@style/CardTitle" android:layout_marginTop="@dimen/spacing_small" android:text="@string/app_name" android:textSize="@dimen/title_size" android:textColor="@color/header_text" />
            <TextView android:id="@+id/text_description" style="@style/CardCaption" android:layout_marginTop="@dimen/spacing_small" android:text="@string/home_greeting" android:textSize="@dimen/body_size" android:textColor="@color/header_text" />
        </LinearLayout>
        <LinearLayout android:layout_width="match_parent" android:layout_height="wrap_content" android:orientation="vertical" android:paddingStart="@dimen/screen_padding" android:paddingEnd="@dimen/screen_padding" android:paddingBottom="@dimen/spacing_large">
            <TextView style="@style/SectionLabel" android:text="@string/emergency_section" />
            <LinearLayout android:layout_width="match_parent" android:layout_height="wrap_content" android:minHeight="@dimen/card_height" android:layout_marginTop="@dimen/spacing_small" android:background="@drawable/bg_samu" android:padding="@dimen/card_padding" android:gravity="center_vertical" android:orientation="horizontal">
                <ImageView android:layout_width="@dimen/icon_box" android:layout_height="@dimen/icon_box" android:background="@drawable/bg_emergency_icon" android:padding="@dimen/spacing_small" android:src="@drawable/ic_phone" android:importantForAccessibility="no" />
                <LinearLayout android:layout_width="@dimen/zero" android:layout_height="wrap_content" android:layout_weight="1" android:layout_marginStart="@dimen/spacing_medium" android:layout_marginEnd="@dimen/spacing_medium" android:orientation="vertical">
                    <TextView style="@style/CardTitle" android:text="@string/samu_name" android:textColor="@color/emergency_text" />
                    <TextView style="@style/CardCaption" android:text="@string/samu_description" android:textColor="@color/emergency_text" />
                </LinearLayout>
                <TextView android:layout_width="wrap_content" android:layout_height="wrap_content" android:text="@string/samu_number" android:textColor="@color/emergency_text" android:textSize="@dimen/number_size" android:textStyle="bold" />
            </LinearLayout>
            <LinearLayout android:layout_width="match_parent" android:layout_height="wrap_content" android:minHeight="@dimen/card_height" android:layout_marginTop="@dimen/spacing_small" android:background="@drawable/bg_fire" android:padding="@dimen/card_padding" android:gravity="center_vertical" android:orientation="horizontal">
                <ImageView android:layout_width="@dimen/icon_box" android:layout_height="@dimen/icon_box" android:background="@drawable/bg_emergency_icon" android:padding="@dimen/spacing_small" android:src="@drawable/ic_phone" android:importantForAccessibility="no" />
                <LinearLayout android:layout_width="@dimen/zero" android:layout_height="wrap_content" android:layout_weight="1" android:layout_marginStart="@dimen/spacing_medium" android:layout_marginEnd="@dimen/spacing_medium" android:orientation="vertical">
                    <TextView style="@style/CardTitle" android:text="@string/fire_name" android:textColor="@color/emergency_text" />
                    <TextView style="@style/CardCaption" android:text="@string/fire_description" android:textColor="@color/emergency_text" />
                </LinearLayout>
                <TextView android:layout_width="wrap_content" android:layout_height="wrap_content" android:text="@string/fire_number" android:textColor="@color/emergency_text" android:textSize="@dimen/number_size" android:textStyle="bold" />
            </LinearLayout>
            <LinearLayout android:layout_width="match_parent" android:layout_height="wrap_content" android:minHeight="@dimen/card_height" android:layout_marginTop="@dimen/spacing_small" android:background="@drawable/bg_police" android:padding="@dimen/card_padding" android:gravity="center_vertical" android:orientation="horizontal">
                <ImageView android:layout_width="@dimen/icon_box" android:layout_height="@dimen/icon_box" android:background="@drawable/bg_emergency_icon" android:padding="@dimen/spacing_small" android:src="@drawable/ic_phone" android:importantForAccessibility="no" />
                <LinearLayout android:layout_width="@dimen/zero" android:layout_height="wrap_content" android:layout_weight="1" android:layout_marginStart="@dimen/spacing_medium" android:layout_marginEnd="@dimen/spacing_medium" android:orientation="vertical">
                    <TextView style="@style/CardTitle" android:text="@string/police_name" android:textColor="@color/emergency_text" />
                    <TextView style="@style/CardCaption" android:text="@string/police_description" android:textColor="@color/emergency_text" />
                </LinearLayout>
                <TextView android:layout_width="wrap_content" android:layout_height="wrap_content" android:text="@string/police_number" android:textColor="@color/emergency_text" android:textSize="@dimen/number_size" android:textStyle="bold" />
            </LinearLayout>
            <LinearLayout android:layout_width="match_parent" android:layout_height="wrap_content" android:minHeight="@dimen/card_height" android:layout_marginTop="@dimen/spacing_small" android:background="@drawable/bg_cvv" android:padding="@dimen/card_padding" android:gravity="center_vertical" android:orientation="horizontal">
                <ImageView android:layout_width="@dimen/icon_box" android:layout_height="@dimen/icon_box" android:background="@drawable/bg_emergency_icon" android:padding="@dimen/spacing_small" android:src="@drawable/ic_phone" android:importantForAccessibility="no" />
                <LinearLayout android:layout_width="@dimen/zero" android:layout_height="wrap_content" android:layout_weight="1" android:layout_marginStart="@dimen/spacing_medium" android:layout_marginEnd="@dimen/spacing_medium" android:orientation="vertical">
                    <TextView style="@style/CardTitle" android:text="@string/cvv_name" android:textColor="@color/emergency_text" />
                    <TextView style="@style/CardCaption" android:text="@string/cvv_description" android:textColor="@color/emergency_text" />
                </LinearLayout>
                <TextView android:layout_width="wrap_content" android:layout_height="wrap_content" android:text="@string/cvv_number" android:textColor="@color/emergency_text" android:textSize="@dimen/number_size" android:textStyle="bold" />
            </LinearLayout>
            <TextView style="@style/SectionLabel" android:text="@string/actions_section" />
            <LinearLayout android:id="@+id/action_assessment" android:layout_width="match_parent" android:layout_height="wrap_content" android:minHeight="@dimen/card_height" android:layout_marginTop="@dimen/spacing_small" android:padding="@dimen/card_padding" android:background="@drawable/bg_surface" android:orientation="horizontal" android:gravity="center_vertical" android:clickable="false" android:focusable="false" >
                <ImageView android:layout_width="@dimen/icon_box" android:layout_height="@dimen/icon_box" android:src="@drawable/ic_clipboard" android:importantForAccessibility="no" android:padding="@dimen/spacing_small" android:background="@drawable/bg_icon"  />
                <LinearLayout android:layout_width="@dimen/zero" android:layout_height="wrap_content" android:layout_weight="1" android:layout_marginStart="@dimen/spacing_medium" android:layout_marginEnd="@dimen/spacing_medium" android:orientation="vertical">
                    <TextView style="@style/CardTitle" android:text="@string/assessment_label" />
                    <TextView style="@style/CardCaption" android:text="@string/assessment_soon" />
                </LinearLayout>

            </LinearLayout>
            <LinearLayout android:id="@+id/action_units" android:layout_width="match_parent" android:layout_height="wrap_content" android:minHeight="@dimen/card_height" android:layout_marginTop="@dimen/spacing_small" android:padding="@dimen/card_padding" android:background="@drawable/bg_surface" android:orientation="horizontal" android:gravity="center_vertical" android:clickable="true" android:focusable="true" android:foreground="?attr/selectableItemBackground">
                <ImageView android:layout_width="@dimen/icon_box" android:layout_height="@dimen/icon_box" android:src="@drawable/ic_pin" android:importantForAccessibility="no" android:padding="@dimen/spacing_small" android:background="@drawable/bg_icon"  />
                <LinearLayout android:layout_width="@dimen/zero" android:layout_height="wrap_content" android:layout_weight="1" android:layout_marginStart="@dimen/spacing_medium" android:layout_marginEnd="@dimen/spacing_medium" android:orientation="vertical">
                    <TextView style="@style/CardTitle" android:text="@string/unidades_title" />
                    <TextView style="@style/CardCaption" android:text="@string/units_short" />
                </LinearLayout>
                <ImageView android:layout_width="@dimen/icon_size" android:layout_height="@dimen/icon_size" android:src="@drawable/ic_chevron" android:importantForAccessibility="no"   />
            </LinearLayout>
            <LinearLayout android:id="@+id/action_aid" android:layout_width="match_parent" android:layout_height="wrap_content" android:minHeight="@dimen/card_height" android:layout_marginTop="@dimen/spacing_small" android:padding="@dimen/card_padding" android:background="@drawable/bg_surface" android:orientation="horizontal" android:gravity="center_vertical" android:clickable="true" android:focusable="true" android:foreground="?attr/selectableItemBackground">
                <ImageView android:layout_width="@dimen/icon_box" android:layout_height="@dimen/icon_box" android:src="@drawable/ic_aid" android:importantForAccessibility="no" android:padding="@dimen/spacing_small" android:background="@drawable/bg_icon"  />
                <LinearLayout android:layout_width="@dimen/zero" android:layout_height="wrap_content" android:layout_weight="1" android:layout_marginStart="@dimen/spacing_medium" android:layout_marginEnd="@dimen/spacing_medium" android:orientation="vertical">
                    <TextView style="@style/CardTitle" android:text="@string/socorros_title" />
                    <TextView style="@style/CardCaption" android:text="@string/aid_short" />
                </LinearLayout>
                <ImageView android:layout_width="@dimen/icon_size" android:layout_height="@dimen/icon_size" android:src="@drawable/ic_chevron" android:importantForAccessibility="no"   />
            </LinearLayout>
            <LinearLayout android:id="@+id/action_map" android:layout_width="match_parent" android:layout_height="wrap_content" android:minHeight="@dimen/card_height" android:layout_marginTop="@dimen/spacing_small" android:padding="@dimen/card_padding" android:background="@drawable/bg_surface" android:orientation="horizontal" android:gravity="center_vertical" android:clickable="true" android:focusable="true" android:foreground="?attr/selectableItemBackground">
                <ImageView android:layout_width="@dimen/icon_box" android:layout_height="@dimen/icon_box" android:src="@drawable/ic_map" android:importantForAccessibility="no" android:padding="@dimen/spacing_small" android:background="@drawable/bg_icon"  />
                <LinearLayout android:layout_width="@dimen/zero" android:layout_height="wrap_content" android:layout_weight="1" android:layout_marginStart="@dimen/spacing_medium" android:layout_marginEnd="@dimen/spacing_medium" android:orientation="vertical">
                    <TextView style="@style/CardTitle" android:text="@string/nav_mapa" />
                    <TextView style="@style/CardCaption" android:text="@string/map_short" />
                </LinearLayout>
                <ImageView android:layout_width="@dimen/icon_size" android:layout_height="@dimen/icon_size" android:src="@drawable/ic_chevron" android:importantForAccessibility="no"   />
            </LinearLayout>
            <TextView style="@style/SectionLabel" android:text="@string/medical_label" />
            <TextView android:layout_width="match_parent" android:layout_height="wrap_content" android:padding="@dimen/spacing_medium" android:background="@drawable/bg_notice" android:text="@string/medical_disclaimer" android:textColor="@color/warning_text" android:textSize="@dimen/body_size" />
        </LinearLayout>
    </LinearLayout>
</androidx.core.widget.NestedScrollView>
```

### Modificar: app/src/main/res/layout/fragment_unidades.xml

```xml
<?xml version="1.0" encoding="utf-8"?>
<androidx.core.widget.NestedScrollView xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent" android:layout_height="match_parent" android:fillViewport="true">
    <LinearLayout android:layout_width="match_parent" android:layout_height="wrap_content" android:orientation="vertical" android:padding="@dimen/screen_padding">
        <TextView android:id="@+id/text_title" style="@style/CardTitle" android:text="@string/unidades_title" android:textSize="@dimen/title_size" />
        <LinearLayout android:layout_width="match_parent" android:layout_height="wrap_content" android:minHeight="@dimen/card_height" android:layout_marginTop="@dimen/spacing_medium" android:padding="@dimen/card_padding" android:background="@drawable/bg_surface" android:gravity="center_vertical" android:orientation="horizontal">
            <ImageView android:layout_width="@dimen/icon_size" android:layout_height="@dimen/icon_size" android:src="@drawable/ic_search" android:importantForAccessibility="no"   />
            <TextView style="@style/CardCaption" android:layout_marginStart="@dimen/spacing_medium" android:text="@string/search_preview" />
        </LinearLayout>

        <LinearLayout android:layout_width="match_parent" android:layout_height="wrap_content"  android:layout_marginTop="@dimen/spacing_medium" android:padding="@dimen/spacing_large" android:background="@drawable/bg_surface" android:orientation="vertical" android:gravity="center">
            <ImageView android:layout_width="@dimen/icon_box" android:layout_height="@dimen/icon_box" android:src="@drawable/ic_hospital" android:importantForAccessibility="no" android:padding="@dimen/spacing_small" android:background="@drawable/bg_icon"  />
            <TextView android:id="@+id/text_description" style="@style/CardCaption" android:text="@string/units_pending" android:textSize="@dimen/body_size" android:gravity="center" android:layout_marginTop="@dimen/spacing_medium" />
        </LinearLayout>

    </LinearLayout>
</androidx.core.widget.NestedScrollView>
```

### Modificar: app/src/main/res/layout/fragment_mapa.xml

```xml
<?xml version="1.0" encoding="utf-8"?>
<androidx.core.widget.NestedScrollView xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent" android:layout_height="match_parent" android:fillViewport="true">
    <LinearLayout android:layout_width="match_parent" android:layout_height="wrap_content" android:orientation="vertical" android:padding="@dimen/screen_padding">
        <TextView android:id="@+id/text_title" style="@style/CardTitle" android:text="@string/mapa_title" android:textSize="@dimen/title_size" />

        <LinearLayout android:layout_width="match_parent" android:layout_height="wrap_content" android:minHeight="@dimen/map_preview_height" android:layout_marginTop="@dimen/spacing_medium" android:padding="@dimen/spacing_large" android:background="@drawable/bg_surface" android:orientation="vertical" android:gravity="center">
            <ImageView android:layout_width="@dimen/icon_box" android:layout_height="@dimen/icon_box" android:src="@drawable/ic_map" android:importantForAccessibility="no" android:padding="@dimen/spacing_small" android:background="@drawable/bg_icon"  />
            <TextView android:id="@+id/text_description" style="@style/CardCaption" android:text="@string/map_pending" android:textSize="@dimen/body_size" android:gravity="center" android:layout_marginTop="@dimen/spacing_medium" />
        </LinearLayout>

    </LinearLayout>
</androidx.core.widget.NestedScrollView>
```

### Modificar: app/src/main/res/layout/fragment_socorros.xml

```xml
<?xml version="1.0" encoding="utf-8"?>
<androidx.core.widget.NestedScrollView xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent" android:layout_height="match_parent" android:fillViewport="true">
    <LinearLayout android:layout_width="match_parent" android:layout_height="wrap_content" android:orientation="vertical" android:padding="@dimen/screen_padding">
        <TextView android:id="@+id/text_title" style="@style/CardTitle" android:text="@string/socorros_title" android:textSize="@dimen/title_size" />

        <LinearLayout android:layout_width="match_parent" android:layout_height="wrap_content"  android:layout_marginTop="@dimen/spacing_medium" android:padding="@dimen/spacing_large" android:background="@drawable/bg_surface" android:orientation="vertical" android:gravity="center">
            <ImageView android:layout_width="@dimen/icon_box" android:layout_height="@dimen/icon_box" android:src="@drawable/ic_book" android:importantForAccessibility="no" android:padding="@dimen/spacing_small" android:background="@drawable/bg_icon"  />
            <TextView android:id="@+id/text_description" style="@style/CardCaption" android:text="@string/aid_pending" android:textSize="@dimen/body_size" android:gravity="center" android:layout_marginTop="@dimen/spacing_medium" />
        </LinearLayout>

    </LinearLayout>
</androidx.core.widget.NestedScrollView>
```

### Modificar: app/src/main/res/layout/fragment_sobre.xml

```xml
<?xml version="1.0" encoding="utf-8"?>
<androidx.core.widget.NestedScrollView xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent" android:layout_height="match_parent" android:fillViewport="true">
    <LinearLayout android:layout_width="match_parent" android:layout_height="wrap_content" android:orientation="vertical" android:padding="@dimen/screen_padding">
        <TextView android:id="@+id/text_title" style="@style/CardTitle" android:text="@string/sobre_title" android:textSize="@dimen/title_size" />

        <LinearLayout android:layout_width="match_parent" android:layout_height="wrap_content"  android:layout_marginTop="@dimen/spacing_medium" android:padding="@dimen/spacing_large" android:background="@drawable/bg_surface" android:orientation="vertical" android:gravity="center">
            <ImageView android:layout_width="@dimen/icon_box" android:layout_height="@dimen/icon_box" android:src="@drawable/ic_info" android:importantForAccessibility="no" android:padding="@dimen/spacing_small" android:background="@drawable/bg_icon"  />
            <TextView android:id="@+id/text_description" style="@style/CardCaption" android:text="@string/about_summary" android:textSize="@dimen/body_size" android:gravity="center" android:layout_marginTop="@dimen/spacing_medium" />
        </LinearLayout>
        <TextView style="@style/SectionLabel" android:text="@string/medical_label" />
        <TextView style="@style/CardCaption" android:text="@string/medical_disclaimer" android:padding="@dimen/spacing_medium" android:textColor="@color/warning_text" android:background="@drawable/bg_notice" />

    </LinearLayout>
</androidx.core.widget.NestedScrollView>
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
        android:layout_height="@dimen/navigation_height"
        android:background="@color/surface"
        app:backgroundTint="@color/surface"
        app:itemIconTint="@color/navigation_item"
        app:itemTextColor="@color/navigation_item"
        app:itemHorizontalTranslationEnabled="false"
        app:itemIconSize="@dimen/icon_size"
        app:itemPaddingTop="@dimen/navigation_padding"
        app:itemPaddingBottom="@dimen/navigation_padding"
        app:itemTextAppearanceActive="@style/NavigationText"
        app:itemTextAppearanceInactive="@style/NavigationText"
        app:labelVisibilityMode="labeled"
        app:menu="@menu/bottom_navigation_menu" />
</LinearLayout>
```

### Modificar: app/src/main/res/menu/bottom_navigation_menu.xml

```xml
<?xml version="1.0" encoding="utf-8"?>
<menu xmlns:android="http://schemas.android.com/apk/res/android">
    <item android:id="@+id/nav_inicio" android:icon="@drawable/ic_home" android:title="@string/nav_inicio" />
    <item android:id="@+id/nav_unidades" android:icon="@drawable/ic_hospital" android:title="@string/nav_unidades" />
    <item android:id="@+id/nav_mapa" android:icon="@drawable/ic_map" android:title="@string/nav_mapa" />
    <item android:id="@+id/nav_socorros" android:icon="@drawable/ic_book" android:title="@string/nav_socorros" />
    <item android:id="@+id/nav_sobre" android:icon="@drawable/ic_info" android:title="@string/nav_sobre" />
</menu>
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

### Modificar: app/src/main/java/com/example/n2dmii/fragments/HomeFragment.java

```java
package com.example.n2dmii.fragments;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.n2dmii.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

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
        descricao.setText(R.string.home_greeting);
        configurarAtalho(view, R.id.action_units, R.id.nav_unidades);
        configurarAtalho(view, R.id.action_aid, R.id.nav_socorros);
        configurarAtalho(view, R.id.action_map, R.id.nav_mapa);
    }

    /* Reutiliza a navegação existente para abrir as abas a partir dos cartões. */
    private void configurarAtalho(View raiz, int cartaoId, int abaId) {
        View cartao = raiz.findViewById(cartaoId);
        cartao.setOnClickListener(view -> {
            BottomNavigationView navegacao = requireActivity().findViewById(R.id.bottom_navigation);
            navegacao.setSelectedItemId(abaId);
        });
    }
}
```

### Modificar: app/src/main/java/com/example/n2dmii/fragments/UnidadesFragment.java

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
        descricao.setText(R.string.units_pending);
    }
}
```

### Modificar: app/src/main/java/com/example/n2dmii/fragments/MapaFragment.java

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
        descricao.setText(R.string.map_pending);
    }
}
```

### Modificar: app/src/main/java/com/example/n2dmii/fragments/SocorrosFragment.java

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
        descricao.setText(R.string.aid_pending);
    }
}
```

### Modificar: app/src/main/java/com/example/n2dmii/fragments/SobreFragment.java

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
        descricao.setText(R.string.about_summary);
    }
}
```
