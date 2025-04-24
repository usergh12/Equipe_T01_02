package com.example.myapplication

import ButtonClickHandler
import TextViewClickHandler
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class TelaCadastro : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_tela_cadastro)
        val txtVoltarLogin = findViewById<TextView>(R.id.txtVoltarLogin)
        val txtVoltarLoginClickHandler = TextViewClickHandler(this, MainActivity::class.java)
        txtVoltarLoginClickHandler.irParaTela(txtVoltarLogin)

        val btnCadastro = findViewById<Button>(R.id.btnCadatro)
        val btnClicker = ButtonClickHandler(btnCadastro)
        btnClicker.mudarTela(this, TelaCadastroConcluido::class.java)
    }
}
