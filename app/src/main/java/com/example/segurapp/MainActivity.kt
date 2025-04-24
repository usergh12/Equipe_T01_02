package com.example.segurapp // Pacote correto

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController

class MainActivity : AppCompatActivity() {

    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // --- CORREÇÃO ESSENCIAL ---
        // Usa o layout da Activity que contém o NavHostFragment
        setContentView(R.layout.activity_main) // <-- Agora isso funciona!

        // --- Configuração da Navegação ---
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        // Opcional: Configurar ActionBar com Navegação
        val appBarConfiguration = AppBarConfiguration(navController.graph)
        // Descomente a linha abaixo se você tiver uma ActionBar e quiser que o título mude
        // setupActionBarWithNavController(navController, appBarConfiguration)
    }

    // Necessário para o botão "Up" (voltar) na ActionBar funcionar
    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }
}