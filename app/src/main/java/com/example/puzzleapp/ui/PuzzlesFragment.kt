package com.example.puzzleapp.ui

import android.Manifest
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
import com.example.puzzleapp.R
import com.example.puzzleapp.databinding.FragmentPuzzlesBinding
import com.example.puzzleapp.itemPuzzle
import com.example.puzzleapp.utils.Settings
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class PuzzlesFragment : Fragment() {

    private lateinit var binding: FragmentPuzzlesBinding

    @Inject
    lateinit var settings: Settings

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

        val puzzles = listOf(
            R.drawable.scarlett_johansson,
            R.drawable.puzzle_image,
            R.drawable.lawrence,
            R.drawable.lopez,
            R.drawable.hassan,
            R.drawable.mahmoud,
            R.drawable.vahid_moradi,
            R.drawable.margot,
            R.drawable.ic_add_photo,
            R.drawable.ic_camera
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
                        when (index) {
                            puzzles.lastIndex -> {
                                dispatchTakePictureIntent()
                            }
                            puzzles.lastIndex - 1 -> {
                                askForGalleryPermission()
                            }
                            else -> {
                                storeTypeAndSrc(Settings.TYPE_DEFAULT, null, imageSource)
                                findNavController().navigate(
                                    PuzzlesFragmentDirections.actionPuzzlesFragmentToLevelFragment()
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    private fun askForGalleryPermission() {
        Dexter.withContext(requireActivity())
            .withPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
            .withListener(object : PermissionListener {
                override fun onPermissionGranted(response: PermissionGrantedResponse) {
                    getPictureFromGallery()
                }

                override fun onPermissionDenied(response: PermissionDeniedResponse) {
                    if (response.isPermanentlyDenied) {
                        val intent =
                            Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                        val uri: Uri = Uri.fromParts("package", requireActivity().packageName, null)
                        intent.data = uri
                        startActivity(intent)
                    }
                }

                override fun onPermissionRationaleShouldBeShown(
                    permission: PermissionRequest?,
                    token: PermissionToken?
                ) {
                    token?.continuePermissionRequest()
                }
            })
            .check()
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
            storeTypeAndSrc(Settings.TYPE_CUSTOM, currentPhotoPath)

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

            val columnIndex: Int = cursor.getColumnIndex(filePathColumn[0])
            val picturePath: String = cursor.getString(columnIndex)
            cursor.close()

            storeTypeAndSrc(Settings.TYPE_CUSTOM, picturePath)
            findNavController().navigate(PuzzlesFragmentDirections.actionPuzzlesFragmentToLevelFragment())
        }
    }

    private fun storeTypeAndSrc(
        srcType: String,
        picturePath: String? = null,
        pictureDrawable: Int? = -1
    ) {
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            settings.storePuzzleSrcType(srcType)

            if (srcType == Settings.TYPE_CUSTOM) {
                picturePath?.let { path ->
                    settings.storePuzzleSrcPath(path)
                }
            } else {
                pictureDrawable?.let { drawable ->
                    settings.storePuzzleSrcDrawable(drawable)
                }
            }
        }
    }

    companion object {
        private const val CAPTURE_IMAGE_REQUEST = 1
        private const val GALLERY_IMAGE_REQUEST = 2
        private const val REQUEST_IMAGE_CAPTURE = 1
    }

}