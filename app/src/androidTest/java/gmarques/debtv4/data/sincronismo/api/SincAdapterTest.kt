package gmarques.debtv4.data.sincronismo.api

import gmarques.debtv4.data.Mapper
import gmarques.debtv4.data.json_serializador.JacksonJsonSerializador
import gmarques.debtv4.data.sincronismo.api.SincAdapter.Companion.VALIDADE_DADOS_REMOVIDOS
import gmarques.debtv4.domain.entidades.Despesa
import junit.framework.TestCase
import kotlinx.coroutines.runBlocking
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.junit.Test

internal class SincAdapterTest {

    private val callbackVazio = object : Callback {
        override suspend fun getDadosLocal(): ArrayList<Sincronizavel> {
            return arrayListOf(Despesa())
        }

        override suspend fun getDadosNuvem(): ArrayList<Sincronizavel> {
            return arrayListOf(Despesa())
        }

        override suspend fun removerDefinitivamenteLocal(obj: Sincronizavel) {
        }

        override suspend fun removerDefinitivamenteNuvem(obj: Sincronizavel) {
        }

        override suspend fun atualizarObjetoLocal(nuvemObj: Sincronizavel) {
        }

        override suspend fun atualizarObjetoNuvem(localObj: Sincronizavel) {
        }

        override suspend fun addNovoObjetoLocal(nuvemObj: Sincronizavel) {
        }

        override suspend fun addNovoObjetoNuvem(localObj: Sincronizavel) {
        }

        override suspend fun sincronismoConluido() {
        }
    }

    @Test
    fun ordenarListas() {
        val lista = arrayListOf<Sincronizavel>(Despesa(), Despesa(), Despesa(), Despesa().apply { foiRemovida = true }, Despesa(), Despesa().apply { foiRemovida = true })
        val listaOrdenada = SincAdapter(callbackVazio).ordenarListas(lista)
        TestCase.assertTrue(listaOrdenada[0].foiRemovida)
        TestCase.assertTrue(listaOrdenada[1].foiRemovida)

    }

    @Test
    fun removerObjetosExpirados() {
        val foraDaValidade = DateTime(DateTimeZone.UTC).minusDays(VALIDADE_DADOS_REMOVIDOS * 2).millis
        val dentroDaValidade = DateTime(DateTimeZone.UTC).minusDays((VALIDADE_DADOS_REMOVIDOS / 2)).millis

        val lista = arrayListOf<Sincronizavel>(Despesa().apply {  // nao foi removido
            nome = "desp_1"
        }, Despesa().apply {// nao foi removido
            nome = "desp_2"
        }, Despesa().apply {// nao deve ser removido permanentemente pois nao expirou
            nome = "desp_3"
            foiRemovida = true
            (this as Sincronizavel).ultimaAtualizacao = dentroDaValidade
        }, Despesa().apply {//  deve ser removido permanentemente pois ja expirou
            nome = "desp_4"
            foiRemovida = true
            (this as Sincronizavel).ultimaAtualizacao = foraDaValidade
        })

        var despesaRemovidaEForaDaValidade: Despesa? = null
        var chamadasParaRemocao = 0

        runBlocking {
            SincAdapter(object : Callback {
                override suspend fun getDadosLocal(): ArrayList<Sincronizavel> {
                    return lista
                }

                override suspend fun getDadosNuvem(): ArrayList<Sincronizavel> {
                    return arrayListOf(Despesa())
                }

                override suspend fun removerDefinitivamenteLocal(obj: Sincronizavel) {
                    chamadasParaRemocao++
                    despesaRemovidaEForaDaValidade = obj as Despesa
                }

                override suspend fun removerDefinitivamenteNuvem(obj: Sincronizavel) {
                }

                override suspend fun atualizarObjetoLocal(nuvemObj: Sincronizavel) {
                }

                override suspend fun atualizarObjetoNuvem(localObj: Sincronizavel) {
                }

                override suspend fun addNovoObjetoLocal(nuvemObj: Sincronizavel) {
                }

                override suspend fun addNovoObjetoNuvem(localObj: Sincronizavel) {
                }

                override suspend fun sincronismoConluido() {
                }
            }).executar()
        }

        TestCase.assertEquals("desp_4", despesaRemovidaEForaDaValidade?.nome)
        TestCase.assertEquals(1, chamadasParaRemocao)

    }

    @Test
            /**
             *
             * Simula um sincronismo entre os bancos local e da nuvem com todos os possiveis cenarios aplicaveis:
             *
             * - desp_1_nuvem deve substituir desp_1_local
             *  Pois é mais recente com base em seu timestamp de ultima atualizacao
             *
             * - nada deve acontecer com desp_2_nuvem e desp_2_local
             *  Embora tenham nomes diferentes, a data da utima atualizacao é igual. Esse algoritimo nao trata desses conmflitos
             *  pois a chance disso acontecer é ridiculamente baixa, mas talvez esssa função seja implementada no futuro.
             *
             * - nada deve acontecer com desp_3_nuvem e desp_3_local
             *  pois embora removidas pelo usuario ainda esta dentro da data de validade estipulada para dados
             *
             * - desp_4_nuvem e desp_4_local devem ser removidos
             *  pois foram removidos e ja expiraram
             *
             * - desp_5_local deve substituir desp_5_nuvem
             *  Pois é mais recente com base em seu timestamp de ultima atualizacao
             *
             * - desp_6_local deve ser adicionada na nuvem
             *  Pois é uma nova despesa que nunca existiu no db de destino. Obs: Ess afirmação é baseada
             *  na uid da despesa e nao no nome
             *
             * - desp_6_nuvem deve ser adicionada no db local
             *  Pois é uma nova despesa que nunca existiu no db de destino. Obs: Ess afirmação é baseada
             *  na uid da despesa e nao no nome
             */
    fun sincronizar() {
        val foraDaValidade = DateTime(DateTimeZone.UTC).minusDays(VALIDADE_DADOS_REMOVIDOS * 2).millis
        val dentroDaValidade = DateTime(DateTimeZone.UTC).minusDays((VALIDADE_DADOS_REMOVIDOS / 2)).millis


        val listaLocal = carregarListaLocal(dentroDaValidade, foraDaValidade)
        val listaNuvem = carregarListaNuvem(listaLocal, dentroDaValidade, foraDaValidade)

        // valor final dessas variaveis deve ser 1
        var atualizarObjetoLocal = 0
        var removerDefinitivamenteLocal = 0
        var removerDefinitivamenteNuvem = 0
        var atualizarObjetoNuvem = 0
        var addNovoObjetoLocal = 0
        var addNovoObjetoNuvem = 0
        var getDadosLocal = 0
        var getDadosNuvem = 0


        runBlocking {
            SincAdapter(object : Callback {

                override suspend fun getDadosLocal(): java.util.ArrayList<Sincronizavel> {
                    getDadosLocal++
                    return listaLocal
                }

                override suspend fun getDadosNuvem(): ArrayList<Sincronizavel> {
                    getDadosNuvem++
                    return listaNuvem
                }

                override suspend fun removerDefinitivamenteLocal(obj: Sincronizavel) {
                    val despesa = obj as Despesa
                    TestCase.assertEquals("desp_4_local", despesa.nome)
                    removerDefinitivamenteLocal++
                }

                override suspend fun removerDefinitivamenteNuvem(obj: Sincronizavel) {
                    val despesa = obj as Despesa
                    TestCase.assertEquals("desp_4_nuvem", despesa.nome)
                    removerDefinitivamenteNuvem++
                }

                override suspend fun atualizarObjetoLocal(nuvemObj: Sincronizavel) {/*Essa função deve ser chamada 1 vez para atualizar o db local com desp_1_nuvem no lugar de desp_1_local */
                    val despesa = nuvemObj as Despesa
                    TestCase.assertEquals("desp_1_nuvem", despesa.nome)
                    atualizarObjetoLocal++
                }

                override suspend fun atualizarObjetoNuvem(localObj: Sincronizavel) {/*Essa função deve ser chamada 1 vez para atualizar o db local com desp_5_local no lugar de desp_5_nuvem */
                    val despesa = localObj as Despesa
                    TestCase.assertEquals("desp_5_local", despesa.nome)
                    atualizarObjetoNuvem++
                }

                override suspend fun addNovoObjetoLocal(nuvemObj: Sincronizavel) {/*Essa função deve ser chamada 1 vez para add desp_6_nuvem*/
                    val despesa = nuvemObj as Despesa
                    TestCase.assertEquals("desp_6_nuvem", despesa.nome)
                    addNovoObjetoLocal++
                }

                override suspend fun addNovoObjetoNuvem(localObj: Sincronizavel) {/*Essa função deve ser chamada 1 vez para add desp_6_local*/
                    val despesa = localObj as Despesa
                    TestCase.assertEquals("desp_6_local", despesa.nome)
                    addNovoObjetoNuvem++
                }

                override suspend fun sincronismoConluido() {
                }
            }).executar()
        }



        TestCase.assertEquals(1, atualizarObjetoLocal)
        TestCase.assertEquals(1, removerDefinitivamenteLocal)
        TestCase.assertEquals(1, removerDefinitivamenteNuvem)
        TestCase.assertEquals(1, atualizarObjetoNuvem)
        TestCase.assertEquals(1, addNovoObjetoLocal)
        TestCase.assertEquals(1, addNovoObjetoNuvem)
        TestCase.assertEquals(1, getDadosLocal)
        TestCase.assertEquals(1, getDadosNuvem)

    }

    /**
     * copia os itens da lista local e altera suas propriedades mantendo a uid pra simular diferenças
     * de dados e data de atualização de maneira previsivel
     */
    private fun carregarListaNuvem(listaLocal: java.util.ArrayList<Sincronizavel>, dentroDaValidade: Long, foraDaValidade: Long): java.util.ArrayList<Sincronizavel> {
        return arrayListOf(
            copia(listaLocal[0]).apply { // deve substituir a desp_1_local por ser mais recente
                nome = "desp_1_nuvem"
                (this as Sincronizavel).ultimaAtualizacao = 2_000
            },
            copia(listaLocal[1]).apply { // nao deve acontecer nada com essa despesa pois (embora o nome seja diferente) tem o mesmo timestamp que sua relativa
                nome = "desp_2_nuvem"
                (this as Sincronizavel).ultimaAtualizacao = 1_000
            },
            copia(listaLocal[2]).apply {// nao deve ser removido permanentemente pois nao expirou
                nome = "desp_3_nuvem"
                foiRemovida = true
                (this as Sincronizavel).ultimaAtualizacao = dentroDaValidade
            },
            copia(listaLocal[3]).apply {//  deve ser removido permanentemente pois ja expirou
                nome = "desp_4_nuvem"
                foiRemovida = true
                (this as Sincronizavel).ultimaAtualizacao = foraDaValidade
            },
            copia(listaLocal[4]).apply {
                nome = "desp_5_nuvem"
                (this as Sincronizavel).ultimaAtualizacao = 1_000 // deve ser substituido por desp_5_nuvem por que ela é mais recente
            },
            Despesa().apply {
                nome = "desp_6_nuvem" // deve ser adicionada no db local pois essa é um nova despesa
            },
        )
    }

    private fun carregarListaLocal(dentroDaValidade: Long, foraDaValidade: Long): java.util.ArrayList<Sincronizavel> {
        return arrayListOf(
            Despesa().apply {
                nome = "desp_1_local"
                (this as Sincronizavel).ultimaAtualizacao = 1_000 // deve ser substituido por desp_1_nuvem por que ela é mais recente
            },
            Despesa().apply {  // nao deve acontecer nada com essa despesa pois (embora o nome seja diferente) tem o mesmo timestamp que sua relativa
                nome = "desp_2_local"
                (this as Sincronizavel).ultimaAtualizacao = 1_000
            },
            Despesa().apply {// nao deve ser removido permanentemente pois nao expirou
                nome = "desp_3_local"
                foiRemovida = true
                (this as Sincronizavel).ultimaAtualizacao = dentroDaValidade
            },
            Despesa().apply {//  deve ser removido permanentemente pois ja expirou
                nome = "desp_4_local"
                foiRemovida = true
                (this as Sincronizavel).ultimaAtualizacao = foraDaValidade
            },
            Despesa().apply {
                nome = "desp_5_local"
                (this as Sincronizavel).ultimaAtualizacao = 2_000 // deve substituir a desp_5_nuvem por ser mais recente
            },
            Despesa().apply {
                nome = "desp_6_local"  // deve ser adicionada na nuvem pois essa é um nova despesa
            },
        )
    }

    /**
     * faz uma copia com a mesma uid mas sem ligação com o objeto original permitindo que as
     * propriedades sejam alteradas sem se refletir no outro objeto
     */
    private fun copia(sincronizavel: Sincronizavel): Despesa {

        val mapper = Mapper(JacksonJsonSerializador())

        val original = sincronizavel as Despesa

        val entidade = mapper.getDespesaEntidade(original)
        val copia = mapper.getDespesa(entidade)

        TestCase.assertEquals(original.nome, copia.nome)
        TestCase.assertEquals(original.uid, copia.uid)

        return copia
    }
}