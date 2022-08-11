package com.softim.moviesapi.ui

import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.textfield.TextInputLayout
import com.softim.moviesapi.R
import com.softim.moviesapi.data.models.ModelUser
import com.softim.moviesapi.databinding.FragmentUserFormBinding
import com.softim.moviesapi.utilities.BusinessAdapter
import com.softim.moviesapi.utilities.BusinessLocalBD
import com.softim.moviesapi.utilities.ExceptionDialogFragment
import com.softim.moviesapi.utilities.UserAdapter
import java.io.IOException
import java.util.regex.Pattern


class UserFormFragment : Fragment() {

    private lateinit var binding: FragmentUserFormBinding
    private val IMAGEN = 8
    private var filePath: Uri? = null
    private var adapter: UserAdapter ?= null
    private var listaUsers = mutableListOf<ModelUser>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentUserFormBinding.inflate(inflater, container, false)
        val root: View = binding.root
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnSaveUserForm.setOnClickListener {
            validaForm()
            guardarUser()
        }
        initrecycler()
        consultar()

        binding.btnSelectPhotoUserForm.setOnClickListener {
            selectImage()
        }

    }

    private fun initrecycler() {
        adapter = UserAdapter(listaUsers)
        binding.rcvUsers.layoutManager = LinearLayoutManager(requireContext())
        binding.rcvUsers.adapter = adapter
    }

    private fun consultar() {
        val admin = BusinessLocalBD(requireContext(), "business_bd", null, 1)
        val bd = admin.writableDatabase
        val fila = bd.rawQuery("select * from users", null)
        if (fila.moveToFirst()) {
            listaUsers.clear()
            do {
                val uid: Int = fila.getInt(0)
                val nombre: String = fila.getString(1)
                val telefono: String = fila.getString(2)
                val correo: String = fila.getString(3)
                val direcion: String = fila.getString(4)
                val imagen: String = fila.getString(5)
                val user = ModelUser(uid, nombre, telefono,correo,direcion,imagen)
                listaUsers.add(user)
                adapter?.notifyDataSetChanged()

            } while (fila.moveToNext())
        }
        bd.close()

    }

    private fun guardarUser() {
        if (filePath != null ){
            val admin = BusinessLocalBD(requireContext(), "business_bd", null, 1)
            val bd = admin.writableDatabase
            val registro = ContentValues()
            registro.put("imagen", filePath.toString())
            registro.put("nombre", binding.etName.text.toString())
            registro.put("direccion", binding.etDireccion.text.toString())
            registro.put("telefono", binding.etPhoneNumber.text.toString())
            registro.put("correo", binding.etEmail.text.toString())

            bd.insert("users", null, registro)
            bd.close()
            val message = "User Insertado"
            ExceptionDialogFragment(message).show(parentFragmentManager, ExceptionDialogFragment.TAG)
            limpiar()
            consultar()
        }else{
            val message = "Selecciona una IMAGEN"
            ExceptionDialogFragment(message).show(parentFragmentManager, ExceptionDialogFragment.TAG)
        }
    }

    private fun limpiar() {
        binding.imgFotoUser.setImageResource(R.drawable.tomarfoto)
        binding.etEmail.setText("")
        binding.etDireccion.setText("")
        binding.etPhoneNumber.setText("")
        binding.etName.setText("")
        filePath = null
    }

    private fun selectImage() {
        val intent = Intent()
        intent.type = "image/*"
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
        intent.action = Intent.ACTION_OPEN_DOCUMENT
        startActivityForResult(
            Intent.createChooser(
                intent,
                "Select Image from here..."
            ),
            IMAGEN
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode === this.IMAGEN && resultCode === Activity.RESULT_OK && data?.data != null ) {
            filePath = data.data
            try {
                binding.imgFotoUser.setImageURI(filePath)
                requireActivity().contentResolver.takePersistableUriPermission(
                    filePath!!,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    private fun validaForm(){
        if (!validarEdit(binding.etName, binding.tilName, R.string.error_nombre_user_form))
            return
        else
            binding.tilName.isErrorEnabled = false
        if (!validarTelefono(binding.etPhoneNumber.text.toString())) {
            binding.tilPhoneNumber.error = resources.getString(R.string.error_telefono_user_form)
            return
        }else
            binding.tilPhoneNumber.isErrorEnabled = false
        if (!validarEmail(binding.etEmail.text.toString())){
            binding.tilEmail.error = resources.getString(R.string.error_correo_user_form)
            return
        }else
            binding.tilEmail.isErrorEnabled = false
        if (!validarEdit(binding.etDireccion, binding.tilDireccion, R.string.error_direccion_user_form))
            return
        else
            binding.tilDireccion.isErrorEnabled = false


    }
    private fun validarEmail(email: String): Boolean {
        val pattern: Pattern = Patterns.EMAIL_ADDRESS
        return pattern.matcher(email).matches()
    }

    private fun validarTelefono(telefono: String): Boolean {
        if (telefono.length < 10)
            return false
        val pattern: Pattern = Patterns.PHONE
        return pattern.matcher(telefono).matches()
    }

    private fun validarEdit(ed: EditText, tiLa: TextInputLayout, err: Int): Boolean{
        if (ed.text.toString() == "" || ed.text.length < 3){
            tiLa.error = getString(err)
            tiLa.boxStrokeColor = resources.getColor(R.color.red)
            return false
        }else
            tiLa.isErrorEnabled = false
        return true
    }


}