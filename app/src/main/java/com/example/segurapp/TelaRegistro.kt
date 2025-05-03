package com.example.segurapp

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import com.example.segurapp.databinding.FragmentTelaRegistroBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.io.File
import java.util.Date

class TelaRegistro : Fragment() {
    private var _binding: FragmentTelaRegistroBinding? = null
    private val binding get() = _binding!!
    private var selectedImageUri: Uri? = null
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var currentLocation: String? = null
    private var userEmail: String? = null
    private val db = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance().reference

    private fun requestPermissions() {
        val permissions = mutableListOf<String>()
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.CAMERA)
        }
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.ACCESS_FINE_LOCATION)
        }
        if (permissions.isNotEmpty()) {
            permissionsLauncher.launch(permissions.toTypedArray())
        } else {
            getLocation()
        }
    }

    private val permissionsLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true) {
            getLocation()
        }
        if(permissions[Manifest.permission.CAMERA] != true){
            binding.frmSelecionarImagem.isEnabled = false
        }
    }

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            selectedImageUri = uri
            binding.txtLegenda.text = "Imagem selecionada!"
        }
    }

    private val takePictureLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success: Boolean ->
        if (success) {
            selectedImageUri?.let {
                binding.txtLegenda.text = "Foto tirada!"
            }
        } else {
            selectedImageUri = null
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTelaRegistroBinding.inflate(inflater, container, false)
        arguments?.let {
            userEmail = it.getString("userEmail")
        }
        if (userEmail == null) {
            Log.e("TelaRegistro", "Erro: E-mail do usuário não recebido via argumentos.")
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        requestPermissions()
        setupSpinner()
        setupImagePicker()
        setupSendButton()
    }

    private fun getLocation() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            currentLocation = "Permissão Negada"
            return
        }

        fusedLocationClient.lastLocation
            .addOnSuccessListener { location ->
                if (location != null) {
                    currentLocation = "Lat: ${location.latitude}, Lon: ${location.longitude}"
                } else {
                    currentLocation = "Não disponível"
                }
            }
            .addOnFailureListener { e ->
                currentLocation = "Erro ao obter"
            }
    }

    private fun setupSpinner() {
        val categorias = arrayOf("Selecione...", "Risco Elétrico", "Risco de Queda", "Obstrução", "Outro")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, categorias)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spnRegistro.adapter = adapter
    }

    private fun setupImagePicker() {
        binding.frmSelecionarImagem.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("Selecionar imagem")
                .setMessage("Deseja tirar uma foto ou escolher da galeria?")
                .setPositiveButton("Tirar Foto") { _, _ ->
                    if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                        val photoFile: File? = try {
                            File.createTempFile("photo_${System.currentTimeMillis()}", ".jpg", requireContext().cacheDir)
                        } catch (ex: java.io.IOException) {
                            Log.e("TelaRegistro", "Erro ao criar arquivo de imagem temporário", ex)
                            null
                        }

                        photoFile?.also {
                            selectedImageUri = FileProvider.getUriForFile(
                                requireContext(),
                                "${requireContext().packageName}.provider",
                                it
                            )
                            takePictureLauncher.launch(selectedImageUri)
                        }
                    } else {
                        requestPermissions()
                    }
                }
                .setNegativeButton("Escolher da Galeria") { _, _ ->
                    pickImageLauncher.launch("image/*")
                }
                .setNeutralButton("Cancelar", null)
                .show()
        }
    }

    private fun setupSendButton() {
        binding.btnEnviar.setOnClickListener {
            val categoria = binding.spnRegistro.selectedItem.toString()
            val descricao = binding.edtDescricao.text.toString().trim()

            if (userEmail.isNullOrEmpty()) {
                Log.e("TelaRegistro", "Tentativa de envio sem userEmail.")
                return@setOnClickListener
            }
            if (categoria == "Selecione...") {
                binding.spnRegistro.requestFocus()
                return@setOnClickListener
            }
            if (descricao.isEmpty()) {
                binding.edtDescricao.error = "Campo obrigatório"
                binding.edtDescricao.requestFocus()
                return@setOnClickListener
            }
            if (selectedImageUri == null) {
                return@setOnClickListener
            }

            binding.btnEnviar.isEnabled = false
            binding.btnEnviar.alpha = 0.5f
            uploadOccurrenceToFirebase(categoria, descricao, selectedImageUri!!, userEmail!!)
        }
    }

    private fun uploadOccurrenceToFirebase(categoria: String, descricao: String, imageUri: Uri, email: String) {
        val imageFileName = "images/${System.currentTimeMillis()}_${email.replace(".", "_")}.jpg"
        val imageRef = storage.child(imageFileName)

        imageRef.putFile(imageUri)
            .addOnSuccessListener {
                imageRef.downloadUrl.addOnSuccessListener { uri ->
                    val occurrenceData = hashMapOf(
                        "categoria" to categoria,
                        "descricao" to descricao,
                        "imagem" to uri.toString(),
                        "localizacao" to currentLocation,
                        "email" to email,
                        "timestamp" to Date()
                    )

                    db.collection("Ocorrencias")
                        .add(occurrenceData)
                        .addOnSuccessListener {
                            clearForm()
                            binding.btnEnviar.isEnabled = true
                            binding.btnEnviar.alpha = 1.0f
                        }
                        .addOnFailureListener { e ->
                            binding.btnEnviar.isEnabled = true
                            binding.btnEnviar.alpha = 1.0f
                        }
                }.addOnFailureListener { e ->
                    binding.btnEnviar.isEnabled = true
                    binding.btnEnviar.alpha = 1.0f
                }
            }
            .addOnFailureListener { e ->
                binding.btnEnviar.isEnabled = true
                binding.btnEnviar.alpha = 1.0f
            }
    }
    private fun clearForm() {
        binding.spnRegistro.setSelection(0)
        binding.edtDescricao.text.clear()
        binding.edtDescricao.error = null
        selectedImageUri = null
        binding.txtLegenda.text = ""
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null

    }
}