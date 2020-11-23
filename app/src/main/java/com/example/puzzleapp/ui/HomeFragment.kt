package com.example.puzzleapp.ui

import android.Manifest
import android.animation.Animator
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
import androidx.activity.addCallback
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.puzzleapp.R
import com.example.puzzleapp.databinding.FragmentHomeBinding
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
class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding

    @Inject
    lateinit var settings: Settings

    private lateinit var currentPhotoPath: String
    private var isFabOpen = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requireActivity().onBackPressedDispatcher.addCallback(this) {
            if (isFabOpen) {
                closeFabMenu()
            } else {
                requireActivity().finish()
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentHomeBinding.inflate(
            inflater, container, false
        ).apply {
            lifecycleOwner = viewLifecycleOwner
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setOnViewRootListener()
        setOnFabListeners()

        val puzzles = listOf(
            R.drawable.puzzle01,
            R.drawable.puzzle02,
            R.drawable.puzzle03,
            R.drawable.puzzle04,
            R.drawable.puzzle05,
            R.drawable.puzzle06,
            R.drawable.puzzle07,
            R.drawable.puzzle08,
            R.drawable.puzzle09,
            R.drawable.puzzle10,
            R.drawable.puzzle11,
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
                        storeTypeAndSrc(Settings.TYPE_DEFAULT, null, imageSource)
                        try {
                            closeFabMenu()
                            findNavController().navigate(
                                HomeFragmentDirections.actionPuzzlesFragmentToLevelFragment()
                            )
                        } catch (e: Exception) {
                            e.printStackTrace()
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
                HomeFragmentDirections.actionPuzzlesFragmentToLevelFragment()
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
            findNavController().navigate(HomeFragmentDirections.actionPuzzlesFragmentToLevelFragment())
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

    private fun setOnFabListeners() {
        binding.fabState.setOnClickListener {
            if (isFabOpen) {
                closeFabMenu()
            } else {
                showFabMenu()
            }
        }

        binding.fabGallery.setOnClickListener {
            askForGalleryPermission()
        }

        binding.fabCamera.setOnClickListener {
            dispatchTakePictureIntent()
        }
    }

    private fun showFabMenu() {
        isFabOpen = true
        binding.fabLayoutGallery.visibility = View.VISIBLE
        binding.fabLayoutCamera.visibility = View.VISIBLE
        binding.shadowRoot.visibility = View.VISIBLE

        binding.tvGallery.visibility = View.VISIBLE
        binding.tvCamera.visibility = View.VISIBLE

        binding.fabState.setImageResource(R.drawable.ic_close)
        binding.fabState.animate().rotationBy(180f)

        binding.fabLayoutGallery.animate()
            .translationY(-resources.getDimension(R.dimen.standard_68))
        binding.fabLayoutCamera.animate()
            .translationY(-resources.getDimension(R.dimen.standard_133))
    }

    private fun closeFabMenu() {
        isFabOpen = false
        binding.fabState.setImageResource(R.drawable.ic_add)
        binding.fabState.animate().rotationBy(180f)

        binding.tvGallery.visibility = View.GONE
        binding.tvCamera.visibility = View.GONE
        binding.shadowRoot.visibility = View.GONE

        binding.fabLayoutGallery.animate().translationY(0f)
        binding.fabLayoutCamera.animate().translationY(0f)
        binding.fabLayoutCamera.animate().translationY(0f)
            .setListener(object : Animator.AnimatorListener {
                override fun onAnimationStart(animator: Animator?) {}
                override fun onAnimationEnd(animator: Animator?) {
                    if (!isFabOpen) {
                        binding.fabLayoutGallery.visibility = View.GONE
                        binding.fabLayoutCamera.visibility = View.GONE
                        binding.shadowRoot.visibility = View.GONE
                    }
                }

                override fun onAnimationCancel(animator: Animator?) {}
                override fun onAnimationRepeat(animator: Animator?) {}
            })
    }

    private fun setOnViewRootListener() {
        binding.shadowRoot.setOnClickListener {
            binding.shadowRoot.visibility = View.GONE
            closeFabMenu()
        }
    }

    companion object {
        private const val CAPTURE_IMAGE_REQUEST = 1
        private const val GALLERY_IMAGE_REQUEST = 2
        private const val REQUEST_IMAGE_CAPTURE = 1
    }

}