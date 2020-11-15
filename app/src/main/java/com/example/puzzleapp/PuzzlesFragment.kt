package com.example.puzzleapp

import android.annotation.SuppressLint
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.puzzleapp.databinding.FragmentPuzzlesBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class PuzzlesFragment : Fragment() {

    private lateinit var binding: FragmentPuzzlesBinding

    private lateinit var settings: Settings

    private lateinit var currentPhotoPath: String

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentPuzzlesBinding.inflate(
            inflater, container, false
        ).apply {
            lifecycleOwner = viewLifecycleOwner
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        settings = Settings(requireContext())

        val puzzles = listOf(
            R.drawable.scarlett_johansson,
            R.drawable.puzzle_image,
            R.drawable.lawrence,
            R.drawable.lopez,
            R.drawable.hassan,
            R.drawable.mahmoud,
            R.drawable.vahid_moradi,
            R.drawable.ic_add_photo
        )
        showPuzzles(puzzles)
    }

    private fun showPuzzles(puzzles: List<Int>) {
        binding.recyclerPuzzles.withModels {
            puzzles.forEachIndexed { index, imageSource ->
                itemPuzzle {
                    id(index)
                    imageSource(imageSource)
                    onPuzzleClick { _ ->
                        if (index == puzzles.lastIndex) {
                            dispatchTakePictureIntent()
                        } else {
                            viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
                                settings.storePuzzleSrcType(
                                    Settings.TYPE_DEFAULT
                                )
                                settings.storePuzzleSrcDrawable(
                                    imageSource
                                )
                            }
                            findNavController().navigate(
                                PuzzlesFragmentDirections.actionPuzzlesFragmentToLevelFragment()
                            )
                        }
                    }
                }
            }
        }
    }

    @SuppressLint("QueryPermissionsNeeded")
    private fun dispatchTakePictureIntent() {
        try {
            val imageFile = createImageFile()
            val callCameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            if (callCameraIntent.resolveActivity(requireActivity().packageManager) != null) {
                val authorities = "com.example.puzzleapp.fileprovider"
                val imageUri = FileProvider.getUriForFile(requireContext(), authorities, imageFile)
                callCameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
                startActivityForResult(callCameraIntent, CAPTURE_IMAGE_REQUEST)
            }
        } catch (e: IOException) {
            Toast.makeText(requireContext(), "Could not create file!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getPictureFromGallery() {
        val pickPhoto = Intent(
            Intent.ACTION_PICK,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        )
        pickPhoto.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        startActivityForResult(pickPhoto, GALLERY_IMAGE_REQUEST)
    }

    @SuppressLint("SimpleDateFormat")
    @Throws(IOException::class)
    fun createImageFile(): File {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val imageFileName: String = "JPEG_" + timeStamp + "_"
        val storageDir: File =
            requireActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES)!!
        if (!storageDir.exists()) storageDir.mkdirs()
        val imageFile = File.createTempFile(imageFileName, ".jpg", storageDir)
        currentPhotoPath = imageFile.absolutePath
        return imageFile
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
                settings.storePuzzleSrcType(
                    Settings.TYPE_CUSTOM
                )
                settings.storePuzzleSrcPath(
                    currentPhotoPath
                )
            }
            findNavController().navigate(
                PuzzlesFragmentDirections.actionPuzzlesFragmentToLevelFragment()
            )
        } else if (requestCode == GALLERY_IMAGE_REQUEST && resultCode == RESULT_OK) {

            val selectedImage = data!!.data
            val filePathColumn = arrayOf(MediaStore.Images.Media.DATA)

            val cursor: Cursor? = requireActivity().contentResolver.query(
                selectedImage!!,
                filePathColumn, null, null, null
            )
            cursor!!.moveToFirst()

            cursor.close()
        }
    }

    private fun getURLForResource(resourceId: Int): String? {
        return Uri.parse("android.resource://" + BuildConfig.APPLICATION_ID + "/" + resourceId)
            .toString()
    }

    companion object {
        private const val CAPTURE_IMAGE_REQUEST = 1
        private const val GALLERY_IMAGE_REQUEST = 2
        private const val REQUEST_IMAGE_CAPTURE = 1
    }

}