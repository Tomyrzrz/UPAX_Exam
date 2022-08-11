package com.softim.moviesapi.ui.gallery

import android.app.Activity.RESULT_OK
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.OnProgressListener
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import com.google.firebase.storage.ktx.storage
import com.softim.moviesapi.R
import com.softim.moviesapi.data.models.ModelUserImages
import com.softim.moviesapi.databinding.FragmentGalleryBinding
import com.softim.moviesapi.utilities.ExceptionDialogFragment
import com.softim.moviesapi.utilities.GalleryAdapter
import java.io.IOException
import java.util.*


class GalleryFragment : Fragment() {

    private var _binding: FragmentGalleryBinding? = null
    private lateinit var adapter: GalleryAdapter
    private val galleryImages = mutableListOf<ModelUserImages>()
    private val IMAGEN = 5
    private var filePath: Uri? = null
    private val storageReference = Firebase.storage
    private var bd = Firebase.firestore
    private lateinit var ref: DocumentReference
    val uniqueID = UUID.randomUUID().toString()


    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val galleryViewModel =
            ViewModelProvider(this).get(GalleryViewModel::class.java)

        _binding = FragmentGalleryBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val textView: TextView = binding.txtUploadYourPhoto
        galleryViewModel.text.observe(viewLifecycleOwner) {
            textView.text = it
        }
        binding.imgUploadPhoto.setOnClickListener {
            selectImage()
        }
        initRecyclerView()
        binding.btnUploadPhoto.setOnClickListener {
            if (filePath != null)
                uploadImage()
            else {
                val message = "Firts Select An Image."
                ExceptionDialogFragment(message).show(
                    parentFragmentManager,
                    ExceptionDialogFragment.TAG
                )
            }
        }
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        queryGallery()
    }

    private fun queryGallery() {
        val sharedPreferences = activity?.getSharedPreferences("user_gallery", AppCompatActivity.MODE_PRIVATE)
        val user_local = sharedPreferences?.getString("user", "")
        if (user_local != "") {
            bd.collection("user_gallery").document(user_local!!).collection("images").get()
                .addOnSuccessListener {
                    if (it != null) {
                        galleryImages.clear()
                        galleryImages.addAll(it.toObjects(ModelUserImages::class.java))
                        adapter.notifyDataSetChanged()
                    }
                }
        }
    }

    private fun initRecyclerView() {
        adapter = GalleryAdapter(galleryImages)
        binding.rcvGallery.layoutManager = GridLayoutManager(requireContext(), 3, RecyclerView.VERTICAL, false)
            .apply {
                binding.rcvGallery.layoutManager = this
            }
        binding.rcvGallery.adapter = adapter
    }

    private fun selectImage() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
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
        if (requestCode === this.IMAGEN && resultCode === RESULT_OK && data?.data != null ) {
            filePath = data.data
            try {
                binding.imgUploadPhoto.setImageURI(filePath)
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    private fun uploadImage() {
        if (filePath != null) {
            val sharedPreferences = activity?.getSharedPreferences("user_gallery", AppCompatActivity.MODE_PRIVATE)
            val user_local = sharedPreferences?.getString("user", "")

            if (user_local == "") {
                val editor = sharedPreferences.edit()
                editor?.putString("user", uniqueID)
                editor?.apply()
            }
            val progressDialog = ProgressDialog(requireContext())
            progressDialog.setTitle("Uploading...")
            progressDialog.show()

            val ref: StorageReference = storageReference.reference
                .child("images/"+uniqueID)


            val uploadTask = ref.putFile(filePath!!)
            uploadTask.addOnFailureListener {
                progressDialog.dismiss()
                val message = "Failed: ${it.message}"
                ExceptionDialogFragment(message).show(parentFragmentManager, ExceptionDialogFragment.TAG)

            }.addOnSuccessListener {tarea->
                if (tarea.task.isComplete) {
                    val uriTask: Task<Uri> = tarea.storage.downloadUrl
                    while ((!uriTask.isComplete))
                        Log.e("error","Aun no")
                    val uploadedImageUri = "" + uriTask.result
                    saveImgFirestore(uploadedImageUri)
                    progressDialog.dismiss()
                }
            }

                .addOnProgressListener(
                    object : OnProgressListener<UploadTask.TaskSnapshot?> {
                        override fun onProgress(
                            taskSnapshot: UploadTask.TaskSnapshot
                        ) {
                            val progress = ((100.0
                                    * taskSnapshot.bytesTransferred
                                    / taskSnapshot.totalByteCount))
                            progressDialog.setMessage(
                                ("Uploaded "+ progress.toInt() + "%")
                            )
                        }
                    })
        }
    }

    private fun saveImgFirestore(downloadUri: String) {
        val sharedPreferences = activity?.getSharedPreferences("user_gallery", AppCompatActivity.MODE_PRIVATE)
        val user_local = sharedPreferences?.getString("user", "")

        ref = bd.collection("user_gallery").document(user_local!!)
            .collection("images").document()

        val user = ModelUserImages(downloadUri)
        ref.set(user)
            .addOnSuccessListener {
                val message = "Image Uploaded"
                ExceptionDialogFragment(message).show(parentFragmentManager, ExceptionDialogFragment.TAG)
                queryGallery()
                binding.imgUploadPhoto.setImageResource(R.drawable.tomarfoto)
                filePath = null
            }.addOnFailureListener {
                val message = "Failed Uploaded: ${it.message}"
                ExceptionDialogFragment(message).show(parentFragmentManager, ExceptionDialogFragment.TAG)
            }
    }
    //com.google.android.gms.tasks.zzw@26fa92c
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}