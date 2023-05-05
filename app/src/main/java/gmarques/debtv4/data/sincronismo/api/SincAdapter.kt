package gmarques.debtv4.data.sincronismo.api

import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import kotlin.reflect.KSuspendFunction1

// TODO: testar essas funçoes
/**
 * (importado de debt V3)
 *  Classe criada com o objetivo de generificar a tarefa de sincronizar dados
 *
 *  PARA IMPLEMENTAR SINCRONISMO EM OUTROS APPS BASTA COPIAR ESTA PACOTE (api)
 * PARA O PROJETO EM QUESTAO E INSTANCIAR ESSA CLASSE PASSANDO O CALLBACK E IMPLEMENTANDO
 * OS METODOS NESSESSARIOS NELE                              .
 *
 * OS OBJETOS A SEREM SINCRONIZADOS DEVEM IMPLEMENTAR A INTERFACE Sincronizavel
 * USE A INTERFACE CallbackUI PARA ATUALIZAR A INTERFACE ENQUANTO O SINCRONIMSO É EXECUTRADO
 *
 * * * *  INSTRUÇOES PARA ADD UM NOVO OBJETO SINCRONIZAVEL A ESTE APP (DEBTV3)
 *   #1 "herde o objeto de @Sincronizavel e implemente os metodos dessa interface corretamente, use como exemplo qqer outro objeto como receita ou despesa para ter certeza de que fez tudo certo.
 *   #2 em SincAdapterImpl no metodo getDadosLocal(); escreva o codigo pro realm carregar todos os objetos do tipo do banco de dados para fazer o sincronismo.
 *   #3 em FirebaseImpl no metodo getDados(); adicione o codigo para baixar os dados do objeto da nuvem para o sincronismo. e atualize a variavel 'tiposDeDados' sobre esse metodo
 *      para refletir a quantidade de objetos que sao sincronizaveis
 *   #4 em MyRealm no metodo removerPermanentemente() adicione o codigo para remover permanentemente o objeto
 *
 *   #Dica: No geral é só implementar @Sincronizavel, e dar Crtl+C - Crtl+V nas classes citadas a cima, todas elas ja tem codigo pra sincronizar os objetos, é só copiar o metodo, renomear
 *  seguindo padrao de nomes da classe, mudar o objeto com que o metodo ta trabalhando e pronto. Sempre copie os metodos que sincroniza mas despesas, pois sao os que fazem
 * mais verificaçoes de segurança por conta das despesas dependerem das categorias etc... estes metodos vao garantir que o novo objeto seja sincronizado com sucesso
 *
 * */
class SincAdapter(private val callback: Callback) {

    /**
     * Objetos marcados como removido antes dessa data devem ser removidos do banco de dados permanentemente
     */
    private val dataDeExpiracao: DateTime = DateTime(DateTimeZone.UTC).minusDays(VALIDADE_DADOS_REMOVIDOS)

    private var localData = ArrayList<Sincronizavel>()
    private var nuvemData = ArrayList<Sincronizavel>()

   suspend fun executar() {

        nuvemData = ordenarListas(callback.getDadosNuvem())
        localData = ordenarListas(callback.getDadosLocal())

        nuvemData = removerObjetosExpirados(nuvemData, callback::removerDefinitivamenteNuvem)
        localData = removerObjetosExpirados(localData, callback::removerDefinitivamenteLocal)

        // atualiza o db local com os objetos da nuvem que forem mais recentes
        sincronizar(nuvemData, localData, callback::atualizarObjetoLocal, callback::addNovoObjetoLocal)
        // atualiza o db da nuvem com os objetos locais que forem mais recentes
        sincronizar(localData, nuvemData, callback::atualizarObjetoNuvem, callback::addNovoObjetoNuvem)

        callback.sincronismoConluido()
    }

    /**
     * Se assegura de que os objetos removidos sejam os primeiros da lista
     */
    private fun ordenarListas(lista: ArrayList<Sincronizavel>): ArrayList<Sincronizavel> {
        val comparador = Comparator { o1: Sincronizavel, o2: Sincronizavel -> compareValuesBy(o1.foiRemovido, o2.foiRemovido) }
        lista.sortWith(comparador)
        return lista
    }

    /**
     * Remove do array os objetos que foram removidos ha muito tempo, considerando a [VALIDADE_DADOS_REMOVIDOS].
     *
     * Objetos removidos dos bancos de dados a mais de "X" dias devem ser removidos dos
     * arrays para sincronismo de forma definitiva antes do começo dod sincronismo
     *
     */
    private suspend fun removerObjetosExpirados(listaDeSincronizaveis: ArrayList<Sincronizavel>, removerObjetoDefinitivamente: KSuspendFunction1<Sincronizavel, Unit>): ArrayList<Sincronizavel> {

        // faço uma copia da lista original e a esvazio para receber apenas objetos validos
        val listaAlvo = ArrayList(listaDeSincronizaveis).also { listaDeSincronizaveis.clear() }

        listaAlvo.forEach {
            // se o obj n foi removido, ou se foi, porem ainda esta dentro da data de validade, ele é mantido no array
            if (!it.foiRemovido || dataDeExpiracao.isBefore(it.ultimaAtualizacao)) listaDeSincronizaveis.add(it)
            else removerObjetoDefinitivamente(it)
        }

        return listaDeSincronizaveis
    }

    /**
     * Considere que:
     * @param fonteDadosA é a lista de objetos da nuvem.
     * @param fonteDadosB é a lista de objetos do db local.
     * @param atualizarObjetoB é a funçao que vai atualizar o db local com o objeto da nuvem.
     * @param adicionarObjetoA é a função que vai adicionar o objeto da nuvem no db local.
     *
     * O que essa função faz é iterar sobre a lista de objetos da nuvem, buscando a copia local de cada objeto
     * e atualizando o banco local caso o objeto da nuvem seja mais recente que seu relativo local
     * ou adicionando o objeto da nuvem no banco local caso um relativo local nao exista.
     *
     * Ao inverter a ordem dos parametros e passar as funçoes corretas, essa função executa a mesma
     * logica do exemplo mas dessa vez invertida, atualizando o db da nuvem.
     *
     * Obs: Se um objeto de um db nao existe no outro db é pq nunca existiu ja que os objetos removidos permanecem
     * no db ate que expirem e os que ja expiraram sao removidos dos arrays de sincronismo no começo
     * da operaçao acabando com a possibilidade de inserir um objeto em um db do qual ele foi removido.
     */
    private suspend fun sincronizar(
        fonteDadosA: ArrayList<Sincronizavel>,
        fonteDadosB: ArrayList<Sincronizavel>,
        atualizarObjetoB: KSuspendFunction1<Sincronizavel, Unit>,
        adicionarObjetoA: KSuspendFunction1<Sincronizavel, Unit>,
    ) = fonteDadosA.forEach { objetoA ->

        val objetoB = getRelativo(fonteDadosB, objetoA)

        if (objetoB == null) adicionarObjetoA(objetoA)
        else if (objetoA.ultimaAtualizacao > objetoB.ultimaAtualizacao) atualizarObjetoB(objetoA)


    }

    /**
     * Encontra na lista recebida um objeto de mesma id que o objeto recebido
     *
     * Serve para encontrar na lista de dados locais a versao local de um objeto presente
     * na lista de dados da nuvem e vice-versa
     */
    private fun getRelativo(lista: List<Sincronizavel>, alvo: Sincronizavel): Sincronizavel? {
        lista.forEach { if (alvo.getUid() === it.getUid()) return it }
        return null
    }

    companion object {
        /**
         * Objetos removidos ha mais de x dias devem ser
         * removidos permanentemente do banco de dados para liberar espaço
         *
         *  x = data atual - esse valor
         */
        const val VALIDADE_DADOS_REMOVIDOS = 365 // dias
    }
}