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
