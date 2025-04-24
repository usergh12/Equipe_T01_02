package com.example.segurapp // Pacote correto

import android.net.Uri // Necessário para a URI da imagem
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment // HERDA DE FRAGMENT
import androidx.navigation.fragment.findNavController
import com.example.segurapp.databinding.FragmentTelaRegistroBinding // Usa View Binding

class TelaRegistro : Fragment() { // É UM FRAGMENT

    private var _binding: FragmentTelaRegistroBinding? = null
    private val binding get() = _binding!!
    private var selectedImageUri: Uri? = null

    // ActivityResultLauncher para buscar imagens
    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            selectedImageUri = uri
            binding.txtLegenda.text = "Imagem selecionada!" // Atualiza a UI
            Toast.makeText(requireContext(), "Imagem selecionada: $uri", Toast.LENGTH_SHORT).show()
        }
    }

    // Usa onCreateView para inflar o layout
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTelaRegistroBinding.inflate(inflater, container, false)
        return binding.root
    }

    // Usa onViewCreated para configurar as views
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupSpinner()
        setupImagePicker()
        setupSendButton()
    }

    private fun setupSpinner() {
        // Use as categorias corretas aqui
        val categorias = arrayOf("Selecione...", "Risco Elétrico", "Risco de Queda", "Obstrução", "Outro")
        // Usa requireContext()
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, categorias)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        // Acessa o spinner via View Binding
        binding.spnRegistro.adapter = adapter
    }

    private fun setupImagePicker() {
        // Acessa o FrameLayout via View Binding
        binding.frmSelecionarImagem.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }
    }

    private fun setupSendButton() {
        // Acessa o botão via View Binding
        binding.btnEnviar.setOnClickListener {
            val categoria = binding.spnRegistro.selectedItem.toString()
            val descricao = binding.edtDescricao.text.toString().trim()

            // Validações básicas
            if (categoria == "Selecione...") {
                Toast.makeText(requireContext(), "Selecione uma categoria", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (descricao.isEmpty()) {
                Toast.makeText(requireContext(), "Descreva a ocorrência", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (selectedImageUri == null) {
                Toast.makeText(requireContext(), "Selecione uma imagem", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Lógica de UPLOAD para Firebase vai aqui...
            Toast.makeText(requireContext(), "Enviando dados...", Toast.LENGTH_LONG).show()
            // uploadOccurrenceToFirebase(categoria, descricao, selectedImageUri)
        }
    }

    // Limpa o binding
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}