package gmarques.debtv4.data.sincronismo.api

/**
 * Interface que sera usada por [SincAdapter] para executar as açoes necessarias do sincronismo
 * @see SincAdapter
 */
interface Callback {
    /**
     * Deve incluir todos os objetos do tipo a ser sincronizado mesmo os removidos
     *
     * @return x
     */
    suspend fun getDadosLocal(): ArrayList<Sincronizavel>

    /**
     * Deve incluir todos os objetos do tipo a ser sincronizado mesmo os removidos
     *
     * @return x
     */
    suspend fun getDadosNuvem(): ArrayList<Sincronizavel>

    /**
     * Os objetos passados por esse metodo devem ser removidos definitivamente do banco de dados
     * pois foram removidos e ja passaram da data de validade no armazenamento
     * @param obj obj
     */
    suspend fun removerDefinitivamenteLocal(obj: Sincronizavel)

    /**
     * Os objetos passados por esse metodo devem ser removidos definitivamente do banco de dados
     * pois foram removidos e ja passaram da data de validade no armazenamento
     * @param obj obj
     */
    suspend fun removerDefinitivamenteNuvem(obj: Sincronizavel)

    /**
     * Serve para atualizar os objetos locais usando os objetos da nuvem
     * que sao mais recentes
     * @param nuvemObj o
     */
    suspend fun atualizarObjetoLocal(nuvemObj: Sincronizavel)

    /**
     * Serve para atualizar os objetos da nuvem usando os objetos locais
     * que sao mais recentes
     * @param localObj o
     */
    suspend fun atualizarObjetoNuvem(localObj: Sincronizavel)

    suspend fun addNovoObjetoLocal(nuvemObj: Sincronizavel)

    suspend fun addNovoObjetoNuvem(localObj: Sincronizavel)

    /**
     * Quando esse metodo for chamado significa que todas as operaçoes de atualizaçao
     * foram executadas, e só. cabe a classe que implementa esta interface verificar
     * se todas as operaçoes feitas em nuvem ja estao conluidas e se tiveram exito,
     * antes finalizar o sincronismo.
     */
    suspend fun sincronismoConluido()
}