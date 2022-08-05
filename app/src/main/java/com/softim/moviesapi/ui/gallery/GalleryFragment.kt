package com.softim.moviesapi.ui.gallery

import android.app.Activity.RESULT_OK
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.OnProgressListener
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import com.google.firebase.storage.ktx.storage
import com.softim.moviesapi.databinding.FragmentGalleryBinding
import com.softim.moviesapi.utilities.ExceptionDialogFragment
import java.io.IOException


class GalleryFragment : Fragment() {

    private var _binding: FragmentGalleryBinding? = null
    private val IMAGEN = 5
    private var filePath: Uri? = null
    private val storageReference = Firebase.storage

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
        binding.btnUploadPhoto.setOnClickListener {
            if (filePath != null)
                uploadImage()
            else
                Toast.makeText(requireContext(), "First Select An Image", Toast.LENGTH_SHORT).show()
        }
        return root
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
            val sharedPreferences = activity?.getSharedPreferences("user_movies", AppCompatActivity.MODE_PRIVATE)
            val user_local = sharedPreferences?.getString("user", "")
            val progressDialog = ProgressDialog(requireContext())
            progressDialog.setTitle("Uploading...")
            progressDialog.show()

            // Defining the child of storageReference
            val ref: StorageReference = storageReference.reference
                .child("images/"+user_local)

            // adding listeners on upload
            // or failure of image
            ref.putFile(filePath!!)
                .addOnSuccessListener { // Image uploaded successfully
                    // Dismiss dialog
                    progressDialog.dismiss()
                    Toast
                        .makeText(
                            requireContext(),
                            "Image Uploaded Correctly.",
                            Toast.LENGTH_SHORT
                        )
                        .show()
                }
                .addOnFailureListener { e -> // Error, Image not uploaded
                    progressDialog.dismiss()
                    Toast
                        .makeText(
                            requireContext(),
                            "Failed " + e.message,
                            Toast.LENGTH_SHORT
                        )
                        .show()
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}