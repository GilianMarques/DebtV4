package gmarques.debtv4.presenter

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.*
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import gmarques.debtv4.R
import gmarques.debtv4.databinding.FragLoginBinding
import gmarques.debtv4.presenter.outros.AnimatedClickListener
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.MessageFormat


class FragLogin : Fragment() {

    private var mGoogleSignInClient: GoogleSignInClient? = null
    private var mAuth: FirebaseAuth? = null
    private val resultadoLoginCallback = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { coletarCredenciais(it) }

    private lateinit var binding: FragLoginBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragLoginBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initBotaoTentarNovamente()
        initAuth()
        logar()
    }

    private fun initBotaoTentarNovamente() {
        binding.btnTentarNovamente.setOnClickListener(object : AnimatedClickListener() {
            override fun onClick(view: View) {
                super.onClick(view)

                binding.progressBar.visibility = View.VISIBLE
                binding.btnTentarNovamente.visibility = View.GONE
                binding.tvInfo.text = ""

                logar()
            }
        })


    }

    private fun initAuth() {

        val signInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id)).requestEmail().build()

        mAuth = FirebaseAuth.getInstance()
        mGoogleSignInClient = GoogleSignIn.getClient(requireActivity(), signInOptions)

    }

    /**
     * chama o dialogo de seleçao de contas do google
     * */
    private fun logar() {
        val signInIntent = mGoogleSignInClient!!.signInIntent
        resultadoLoginCallback.launch(signInIntent)
    }

    /**
     * Extrai as credenciais do bundle recebido do dialogo de seleçao de contas do google
     * e usa pra autenticar com o firebase
     * */
    private fun coletarCredenciais(it: ActivityResult) {
        try {
            val account = GoogleSignIn.getSignedInAccountFromIntent(it.data)
                .getResult(ApiException::class.java)

            firebaseAuthWithGoogle(account)

        } catch (e: ApiException) {
            tratarErrosDeLoginGoogle(e)
        }
    }

    /**
     * Mostra mensagens ao usuario de acordo com o tipo de erro caso tenha ocorrido algum
     * */
    private fun tratarErrosDeLoginGoogle(e: ApiException) {

        binding.progressBar.visibility = View.GONE
        binding.btnTentarNovamente.visibility = View.VISIBLE

        when (e.statusCode) {
            GoogleSignInStatusCodes.SIGN_IN_CANCELLED -> {
                binding.tvInfo.setText(R.string.Voce_cancelou_o_login)
            }
            GoogleSignInStatusCodes.SIGN_IN_CURRENTLY_IN_PROGRESS -> {
                binding.tvInfo.setText(R.string.Ha_mais_de_um_processo_de_login_em_andamento)
            }
            GoogleSignInStatusCodes.SIGN_IN_FAILED -> {
                binding.tvInfo.text =
                        MessageFormat.format(getString(R.string.O_login_falhou), e.statusCode)
            }
            else -> {
                binding.tvInfo.text =
                        MessageFormat.format(getString(R.string.Houve_um_erro_ao_contactar_a_api_do_goole), e.statusCode)
            }
        }
    }

    /**
     * Usa as credenciais da conta selecionada pelo usuario para autenticar no firebase
     * */
    private fun firebaseAuthWithGoogle(acct: GoogleSignInAccount) {
        val credenciais = GoogleAuthProvider.getCredential(acct.idToken, null)
        mAuth!!.signInWithCredential(credenciais).addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    revogarAcessoAoApp()
                    usuarioAutenticado()
                } else {
                    erroDeAutenticacaoFirebase(task)
                }
            }
    }

    /**
     * Essa função garante que o dialogo de seleçao de contas do google sera exibido se o usuario fizer logoff
     * de sua conta. Se o app nao revogar o acesso do usuario e o usuario fizer logoff e tentar fazer login, o google
     * nao mostra o dialogo de seleçao de contas apenas seleciona a conta que ja esta autorizada mesmo
     * se tiver mais de uma conta no dispositivo, isso impede o usuario de alternar entre contas por exemplo*/
    private fun revogarAcessoAoApp() {
        mGoogleSignInClient!!.revokeAccess()
    }

    /**
     * Sauda o usuario e fecha a activity
     * */
    private fun usuarioAutenticado() = lifecycleScope.launch {

        binding.progressBar.visibility = View.GONE

        val user = mAuth!!.currentUser!!
        val nome = if (user.displayName != null) user.displayName else "?"
        binding.tvInfo.text =
                MessageFormat.format(getString(R.string.Bem_vindo_X), nome!!.split(" ")[0])

        delay(2000)
        findNavController().navigate(FragLoginDirections.actionFragMain())
    }

    /**
     * Notifica o usuario sobre o erro de autenticação no firebase caso tenha acontecido algum
     * */
    private fun erroDeAutenticacaoFirebase(task: Task<AuthResult>) {
        binding.progressBar.visibility = View.GONE
        binding.btnTentarNovamente.visibility = View.VISIBLE

        val ex = task.exception
        if (ex != null) binding.tvInfo.text =
                MessageFormat.format(getString(R.string.Houve_um_erro_ao_autenticar_sua_conta), ex.message)

    }

}