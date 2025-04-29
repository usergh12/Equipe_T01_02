package com.example.segurapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.segurapp.databinding.ActivityTelaCadastroBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore

class TelaCadastro : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private var _binding: ActivityTelaCadastroBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityTelaCadastroBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        setupButtonClickListener()
    }

    private fun setupButtonClickListener() {
        binding.btnCadastro.setOnClickListener {
            val name = binding.edtNome.text.toString().trim()
            val setor = binding.edtSetor.text.toString().trim()
            val email = binding.edtEmail.text.toString().trim()
            val password = binding.edtSenha.text.toString().trim()

            registerUser(name, setor, email, password)
        }
    }

    private fun registerUser(name: String, setor: String, email: String, password: String) {
        if (name.isEmpty() || setor.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Preencha todos os campos", Toast.LENGTH_SHORT).show()
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
                                saveUserDataToFirestore(user, name, setor, email)
                            } else {
                                Log.e("TelaCadastro", "Falha ao atualizar perfil", profileTask.exception)
                                Toast.makeText(this, "Falha ao atualizar perfil", Toast.LENGTH_SHORT).show()
                            }
                        }
                } else {
                    Log.e("TelaCadastro", "Falha ao registrar usuário", task.exception)
                    Toast.makeText(this, "Falha ao registrar usuário", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun saveUserDataToFirestore(user: FirebaseUser, name: String, setor: String, email: String) {
        val db = FirebaseFirestore.getInstance()

        val userData = mapOf(
            "nome" to name,
            "email" to email,
            "setor" to setor
        )

        db.collection("Usuarios")
            .document(user.uid)
            .set(userData)
            .addOnSuccessListener {
                Toast.makeText(this, "Usuário registrado com sucesso", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, TelaCadastroConcluido::class.java))
            }
            .addOnFailureListener { e ->
                Log.e("FirestoreError", "Erro ao salvar dados no Firestore", e)
                Toast.makeText(this, "Erro ao salvar dados no Firestore", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}
