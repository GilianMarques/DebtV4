package gmarques.debtv4.data.sincronismo.api


/**
 * Use nas classes que usam [SincAdapter] para atualziar a interface
 * @See SincAdapter
 */
interface UICallback {
    /**
     * chamado ao final da tarefa ou durante caso haja algum erro no meio do caminho
     * @param sucesso s
     * @param msg     sera null se sucesso for true
     */
    fun feito(sucesso: Boolean, msg: String?)

    /**
     * Chamado a cada operação para informar o usuario do progresso da tarefa
     * @param titulo a
     * @param msg    a
     */
    fun status(titulo: String, msg: String)
}