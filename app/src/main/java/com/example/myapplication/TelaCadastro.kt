package com.example.segurapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.segurapp.databinding.ActivityTelaCadastroBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore

class TelaCadastro : Fragment(R.layout.activity_tela_cadastro) {
    private lateinit var auth: FirebaseAuth
    private var _binding: ActivityTelaCadastroBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d("TelaCadastro", "onViewCreated chamado!")
        _binding = ActivityTelaCadastroBinding.bind(view)
        setupButtonClickListener()
        auth = FirebaseAuth.getInstance()
    }

    private fun setupButtonClickListener() {
        Log.d("TelaCadastro", "setupButtonClickListener chamado!")
        binding.btnCadastro.setOnClickListener {
            Log.d("TelaCadastro", "Botão clicado!")
            Toast.makeText(context, "Botão clicado!", Toast.LENGTH_SHORT).show() // Check if the Toast is displayed
            val name = binding.edtNome.text.toString()
            val setor = binding.edtCargo.text.toString()  // Alterado para setor
            val email = binding.edtEmail.text.toString()
            val password = binding.edtSenha.text.toString()

            registerUser(name, setor, email, password)
        }
    }

    private fun registerUser(name: String, setor: String, email: String, password: String) {
        if (email.isEmpty() || password.isEmpty() || name.isEmpty() || setor.isEmpty()) {  // Alterado de cargo para setor
            Toast.makeText(context, "Preencha todos os campos", Toast.LENGTH_SHORT).show()
            return
        }

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    val profileUpdates = UserProfileChangeRequest.Builder()
                        .setDisplayName(name)
                        .build()

                    user?.updateProfile(profileUpdates)
                        ?.addOnCompleteListener { profileTask ->
                            if (profileTask.isSuccessful) {
                                // Salvar dados no Firestore
                                saveUserDataToFirestore(user, name, setor, email)  // Alterado para setor
                            } else {
                                Log.e("RegisterFragment", "Falha ao atualizar o perfil do usuário", profileTask.exception)
                                Toast.makeText(context, "Falha ao atualizar perfil", Toast.LENGTH_SHORT).show()
                            }
                        }
                } else {
                    Log.e("RegisterFragment", "Falha ao registrar usuário", task.exception)
                    Toast.makeText(context, "Falha ao registrar usuário", Toast.LENGTH_SHORT).show()
                }
            }
    }

    // Função para salvar os dados no Firestore
    private fun saveUserDataToFirestore(user: FirebaseUser?, name: String, setor: String, email: String) {
        val db = FirebaseFirestore.getInstance()

        val userData = mapOf(
            "nome" to name,
            "email" to email,
            "setor" to setor
        )

        user?.let {
            // Salvar os dados do usuário no Firestore
            db.collection("Usuarios").document(user.uid)
                .set(userData)
                .addOnSuccessListener {
                    Toast.makeText(context, "Usuário registrado com sucesso", Toast.LENGTH_SHORT).show()

                    val intent = Intent(context, TelaCadastroConcluido::class.java)
                    startActivity(intent)
                }
                .addOnFailureListener {
                    Toast.makeText(context, "Erro ao salvar dados no Firestore", Toast.LENGTH_SHORT).show()
                }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
