package com.example.segurapp

import ButtonClickHandler
import TextViewClickHandler
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        val txtCadastro = findViewById<TextView>(R.id.txtCadastro)
        val txtClicker = TextViewClickHandler(this, TelaCadastro::class.java)
        val btnEntrar = findViewById<Button>(R.id.btnLogin)
        val btnHandler = ButtonClickHandler(this, TelaRegistro::class.java)
        val txtErro = findViewById<TextView>(R.id.txtFalhaLogin)

        txtClicker.irParaTela(txtCadastro)
        btnHandler.erroLogin(btnEntrar, txtErro)

    }
}