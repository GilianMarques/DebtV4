package gmarques.debtv4.domain.modulos_hilt

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import gmarques.debtv4.data.json_serializador.JacksonJsonSerializador
import gmarques.debtv4.domain._interfaces.JsonSerializador
import javax.inject.Singleton

/*
* mais detalhes sobre injeção de dependencias com hilt na alura e documentação oficial
*  Alura: https://www.alura.com.br/artigos/injecao-de-dependencia-do-android-com-o-hilt
*  Docs: https://dagger.dev/hilt/components.html#:~:text=components%20used%20for%20injection
*/

@Module
@InstallIn(SingletonComponent::class) // dependencias geradas nesse modulo ficam disponiveis para toda a aplicaçao e vinculadas ao ciclo de vida do app
class ModuleJson {

    @Provides
    @Singleton
    fun provideJacksonJsonSerializer(): JsonSerializador {
        return JacksonJsonSerializador()
    }

}