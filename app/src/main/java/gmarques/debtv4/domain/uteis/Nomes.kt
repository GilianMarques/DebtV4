package gmarques.debtv4.domain.uteis

class Nomes {
    companion object {
        private val espacosMultiplos = Regex("[ ][ ]+")
        private val caracteresEspeciais = Regex("""[\\!@$%¨&*()_+="'°|¬¢£§;:/<>()\[\]{}]""")

        fun aplicarCorrecao(nomeInvalido: String): String {
            var nome: String = nomeInvalido

            nome = nome.replace(caracteresEspeciais, "")
            nome = nome.replace(espacosMultiplos, " ")

            if (nome.startsWith(" ")) nome = nome.drop(1)
            if (nome.endsWith(" ")) nome = nome.dropLast(1)

            return nome
        }

    }

}
