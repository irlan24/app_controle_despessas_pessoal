package com.irlan.app.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.irlan.app.model.Despesa;

import java.util.ArrayList;
import java.util.List;

/**
 * Classe responsável por gerenciar o banco de dados SQLite
 * Implementa operações CRUD (Create, Read, Update, Delete)
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    // Tag para logs
    private static final String TAG = "DatabaseHelper";

    // Configurações do Banco de Dados
    private static final String DATABASE_NAME = "financeapp.db";
    private static final int DATABASE_VERSION = 1;

    // Nome da tabela
    private static final String TABLE_DESPESAS = "despesas";

    // Colunas da tabela
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_VALOR = "valor";
    private static final String COLUMN_DESCRICAO = "descricao";
    private static final String COLUMN_CARTAO = "cartao";
    private static final String COLUMN_DATA = "data";

    // SQL para criar a tabela
    private static final String CREATE_TABLE_DESPESAS =
            "CREATE TABLE " + TABLE_DESPESAS + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_VALOR + " REAL NOT NULL, " +
                    COLUMN_DESCRICAO + " TEXT NOT NULL, " +
                    COLUMN_CARTAO + " TEXT NOT NULL, " +
                    COLUMN_DATA + " TEXT NOT NULL" +
                    ")";

    // Construtor
    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Cria a tabela quando o banco é criado pela primeira vez
        db.execSQL(CREATE_TABLE_DESPESAS);
        Log.d(TAG, "Tabela criada com sucesso");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Chamado quando a versão do banco muda
        // Por enquanto, apenas recria a tabela (em produção, você faria migração de dados)
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_DESPESAS);
        onCreate(db);
        Log.d(TAG, "Banco de dados atualizado");
    }

    // ==================== OPERAÇÕES CRUD ====================

    /**
     * Insere uma nova despesa no banco de dados
     * @param despesa Objeto Despesa a ser inserido
     * @return ID da despesa inserida, ou -1 em caso de erro
     */
    public long inserirDespesa(Despesa despesa) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(COLUMN_VALOR, despesa.getValor());
        values.put(COLUMN_DESCRICAO, despesa.getDescricao());
        values.put(COLUMN_CARTAO, despesa.getCartao());
        values.put(COLUMN_DATA, despesa.getData());

        long id = db.insert(TABLE_DESPESAS, null, values);
        db.close();

        Log.d(TAG, "Despesa inserida com ID: " + id);
        return id;
    }

    /**
     * Busca uma despesa específica pelo ID
     * @param id ID da despesa
     * @return Objeto Despesa ou null se não encontrado
     */
    public Despesa buscarDespesaPorId(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Despesa despesa = null;

        Cursor cursor = db.query(
                TABLE_DESPESAS,
                null,
                COLUMN_ID + " = ?",
                new String[]{String.valueOf(id)},
                null,
                null,
                null
        );

        if (cursor != null && cursor.moveToFirst()) {
            despesa = cursorParaDespesa(cursor);
            cursor.close();
        }

        db.close();
        return despesa;
    }

    /**
     * Retorna todas as despesas do banco
     * @return Lista de todas as despesas
     */
    public List<Despesa> buscarTodasDespesas() {
        List<Despesa> listaDespesas = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        // Ordena por data decrescente (mais recente primeiro)
        Cursor cursor = db.query(
                TABLE_DESPESAS,
                null,
                null,
                null,
                null,
                null,
                COLUMN_DATA + " DESC, " + COLUMN_ID + " DESC"
        );

        if (cursor.moveToFirst()) {
            do {
                Despesa despesa = cursorParaDespesa(cursor);
                listaDespesas.add(despesa);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();

        Log.d(TAG, "Total de despesas encontradas: " + listaDespesas.size());
        return listaDespesas;
    }

    /**
     * Busca despesas com filtros combinados
     * @param valor Valor parcial (pode ser null ou vazio)
     * @param descricao Descrição parcial (pode ser null ou vazia)
     * @param cartao Cartão específico (pode ser null, vazio ou "Todos")
     * @param dataInicial Data inicial no formato yyyy-MM-dd (pode ser null)
     * @param dataFinal Data final no formato yyyy-MM-dd (pode ser null)
     * @return Lista de despesas filtradas
     */
    public List<Despesa> buscarDespesasComFiltros(
            String valor,
            String descricao,
            String cartao,
            String dataInicial,
            String dataFinal) {

        List<Despesa> listaDespesas = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        // Construção dinâmica da query
        StringBuilder query = new StringBuilder("SELECT * FROM " + TABLE_DESPESAS + " WHERE 1=1");
        List<String> argumentos = new ArrayList<>();

        // Filtro por valor (busca parcial convertendo valor para texto)
        if (valor != null && !valor.trim().isEmpty()) {
            query.append(" AND CAST(" + COLUMN_VALOR + " AS TEXT) LIKE ?");
            argumentos.add("%" + valor.trim() + "%");
        }

        // Filtro por descrição (busca parcial, case-insensitive)
        if (descricao != null && !descricao.trim().isEmpty()) {
            query.append(" AND LOWER(" + COLUMN_DESCRICAO + ") LIKE LOWER(?)");
            argumentos.add("%" + descricao.trim() + "%");
        }

        // Filtro por cartão (exato)
        if (cartao != null && !cartao.trim().isEmpty() && !cartao.equalsIgnoreCase("Todos")) {
            query.append(" AND " + COLUMN_CARTAO + " = ?");
            argumentos.add(cartao);
        }

        // Filtro por data inicial (maior ou igual)
        if (dataInicial != null && !dataInicial.trim().isEmpty()) {
            query.append(" AND " + COLUMN_DATA + " >= ?");
            argumentos.add(dataInicial);
        }

        // Filtro por data final (menor ou igual)
        if (dataFinal != null && !dataFinal.trim().isEmpty()) {
            query.append(" AND " + COLUMN_DATA + " <= ?");
            argumentos.add(dataFinal);
        }

        // Ordenação
        query.append(" ORDER BY " + COLUMN_DATA + " DESC, " + COLUMN_ID + " DESC");

        Log.d(TAG, "Query de busca: " + query.toString());
        Log.d(TAG, "Argumentos: " + argumentos.toString());

        // Executa a query
        Cursor cursor = db.rawQuery(
                query.toString(),
                argumentos.toArray(new String[0])
        );

        if (cursor.moveToFirst()) {
            do {
                Despesa despesa = cursorParaDespesa(cursor);
                listaDespesas.add(despesa);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();

        Log.d(TAG, "Despesas encontradas com filtros: " + listaDespesas.size());
        return listaDespesas;
    }

    /**
     * Atualiza uma despesa existente
     * @param despesa Despesa com dados atualizados
     * @return Número de linhas afetadas
     */
    public int atualizarDespesa(Despesa despesa) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(COLUMN_VALOR, despesa.getValor());
        values.put(COLUMN_DESCRICAO, despesa.getDescricao());
        values.put(COLUMN_CARTAO, despesa.getCartao());
        values.put(COLUMN_DATA, despesa.getData());

        int linhasAfetadas = db.update(
                TABLE_DESPESAS,
                values,
                COLUMN_ID + " = ?",
                new String[]{String.valueOf(despesa.getId())}
        );

        db.close();
        Log.d(TAG, "Despesa atualizada. Linhas afetadas: " + linhasAfetadas);
        return linhasAfetadas;
    }

    /**
     * Deleta uma despesa pelo ID
     * @param id ID da despesa a ser deletada
     * @return Número de linhas deletadas
     */
    public int deletarDespesa(int id) {
        SQLiteDatabase db = this.getWritableDatabase();

        int linhasDeletadas = db.delete(
                TABLE_DESPESAS,
                COLUMN_ID + " = ?",
                new String[]{String.valueOf(id)}
        );

        db.close();
        Log.d(TAG, "Despesa deletada. Linhas afetadas: " + linhasDeletadas);
        return linhasDeletadas;
    }

    /**
     * Calcula o total de despesas (com ou sem filtros)
     * @param despesas Lista de despesas
     * @return Valor total
     */
    public double calcularTotal(List<Despesa> despesas) {
        double total = 0.0;
        for (Despesa despesa : despesas) {
            total += despesa.getValor();
        }
        return total;
    }

    /**
     * Conta o total de despesas no banco
     * @return Quantidade de registros
     */
    public int contarDespesas() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_DESPESAS, null);

        int count = 0;
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }

        cursor.close();
        db.close();
        return count;
    }

    /**
     * Deleta todas as despesas (use com cuidado!)
     */
    public void deletarTodasDespesas() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_DESPESAS, null, null);
        db.close();
        Log.d(TAG, "Todas as despesas foram deletadas");
    }

    // ==================== MÉTODOS AUXILIARES ====================

    /**
     * Converte um Cursor em um objeto Despesa
     * @param cursor Cursor com dados do banco
     * @return Objeto Despesa
     */
    private Despesa cursorParaDespesa(Cursor cursor) {
        int id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID));
        double valor = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_VALOR));
        String descricao = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DESCRICAO));
        String cartao = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CARTAO));
        String data = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DATA));

        return new Despesa(id, valor, descricao, cartao, data);
    }
}