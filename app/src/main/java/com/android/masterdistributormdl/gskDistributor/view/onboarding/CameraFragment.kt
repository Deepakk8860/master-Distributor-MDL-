package com.android.masterdistributormdl.gskDistributor.view.onboarding

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.Recorder
import androidx.camera.video.VideoCapture
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.android.masterdistributormdl.R
import com.android.masterdistributormdl.databinding.ActivityCameraBinding
import com.android.masterdistributormdl.gskDistributor.utils.AlertError
import com.android.masterdistributormdl.gskDistributor.utils.addFragment
import com.android.masterdistributormdl.gskDistributor.utils.showToastShort
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class CameraFragment : Fragment() {
    private lateinit var cameraProvider: ProcessCameraProvider
    private lateinit var previewView: PreviewView
    private lateinit var videoCapture: androidx.camera.core.VideoCapture
    lateinit var model: OnboardModel
    private lateinit var executor: ExecutorService
    private lateinit var binding: ActivityCameraBinding
    private var isRecording = false
    private var startTime = 0L
    private var textHindi=""
    private var textEnglish=""
    private var currentCameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA
    private var isFlashOn = false // Track flash state
    private val handler = Handler(Looper.getMainLooper())
    private val timeRunnable = object : Runnable {
        override fun run() {
            val elapsedTime = System.currentTimeMillis() - startTime
            binding.timerTextView.text = formatTime(elapsedTime)
            handler.postDelayed(this, 1000)
        }
    }

    private val requestPermissionsLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            if (permissions[Manifest.permission.CAMERA] == true && permissions[Manifest.permission.RECORD_AUDIO] == true) {
                startCamera()
            } else {
                Log.e("CameraFragment", "Permissions not granted")
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = ActivityCameraBinding.inflate(inflater, container, false)
        model = ViewModelProvider(this)[OnboardModel::class.java]
        binding.model = model
        previewView = binding.previewView
        executor = Executors.newSingleThreadExecutor()
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
            || ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissionsLauncher.launch(
                arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO)
            )
        } else {
            startCamera()

        }
        videoKycText()

        binding.startRecordingButton.setOnClickListener {
            if (!isRecording) {
                startRecording()
            }
        }

        binding.ivCancel.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        binding.languageSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                binding.txtDes.text=textHindi
            } else {
                // Set to English
                binding.txtDes.text=textEnglish
            }
        }

        binding.stopRecordingButton.setOnClickListener {
            if (isRecording) {
                stopRecording()
            }
        }

        binding.flipCameraButton.setOnClickListener {
            flipCamera()
        }

        binding.btnFlashlight.setOnClickListener {
            toggleFlash()
        }

        return binding.root
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
        cameraProviderFuture.addListener({
            cameraProvider = cameraProviderFuture.get()
            bindCameraUseCases()
        }, ContextCompat.getMainExecutor(requireContext()))
    }

    private fun videoKycText(){
        model.videoKycText() {
            if (it.status == 0) {
                textHindi=it.data.hindi
                textEnglish=it.data.english
                binding.txtDes.text=it.data.english
            } else {
                AlertError.show(requireContext(), it.message) {}
            }
        }
    }

    private fun bindCameraUseCases() {
        val preview = Preview.Builder().build()
        videoCapture = androidx.camera.core.VideoCapture.Builder().build()

        try {
            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(viewLifecycleOwner, currentCameraSelector, preview, videoCapture)
        } catch (e: IllegalArgumentException) {
            showToastShort("No suitable camera found")
        }

        preview.setSurfaceProvider(previewView.surfaceProvider)
    }


    //toggle flash light
    private fun toggleFlash() {
        val camera = cameraProvider.bindToLifecycle(
            viewLifecycleOwner, currentCameraSelector
        )
        isFlashOn = !isFlashOn
        camera.cameraControl.enableTorch(isFlashOn) // Enable or disable the torch

        // Update the button text or icon
        if (isFlashOn){
            binding.btnFlashlight.setImageResource(R.drawable.light_on)

        } else{
            binding.btnFlashlight.setImageResource(R.drawable.light_off)

        }
    }


    private fun flipCamera() {
        currentCameraSelector = if (currentCameraSelector == CameraSelector.DEFAULT_FRONT_CAMERA) {
            binding.btnFlashlight.visibility=View.VISIBLE
            CameraSelector.DEFAULT_BACK_CAMERA
        } else {
            binding.btnFlashlight.visibility=View.GONE
            CameraSelector.DEFAULT_FRONT_CAMERA
        }
        bindCameraUseCases()
    }

    private fun startRecording() {
        binding.flipCameraButton.visibility=View.GONE
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val outputFile = File(requireContext().externalMediaDirs.first(), "video_$timestamp.mp4")
        val outputOptions = androidx.camera.core.VideoCapture.OutputFileOptions.Builder(outputFile).build()

        isRecording = true
        startTime = System.currentTimeMillis()
        handler.post(timeRunnable)
        binding.startRecordingButton.isEnabled = false
        binding.stopRecordingButton.visibility = View.VISIBLE
        binding.startRecordingButton.visibility = View.GONE

        videoCapture.startRecording(
            outputOptions,
            executor,
            object : androidx.camera.core.VideoCapture.OnVideoSavedCallback {
                override fun onVideoSaved(outputFileResults: androidx.camera.core.VideoCapture.OutputFileResults) {
                    isRecording = false
                    handler.removeCallbacks(timeRunnable)
                    requireActivity().runOnUiThread {
                        binding.timerTextView.text = "00:00"
                        binding.startRecordingButton.isEnabled = true

                        addFragment(requireActivity(),VideoPlayFragment(), bundleOf("video_path" to outputFile.absolutePath))
                    }
                }

                override fun onError(videoCaptureError: Int, message: String, cause: Throwable?) {
                    isRecording = false
                    handler.removeCallbacks(timeRunnable)
                    requireActivity().runOnUiThread {
                        binding.startRecordingButton.isEnabled = true
                    }
                }
            }
        )
    }

    private fun stopRecording() {
        videoCapture.stopRecording()
        isRecording = false
        binding.stopRecordingButton.visibility = View.GONE
        binding.startRecordingButton.visibility = View.VISIBLE
        binding.startRecordingButton.isEnabled = true
    }

    private fun formatTime(elapsedTime: Long): String {
        val seconds = (elapsedTime / 1000) % 60
        val minutes = (elapsedTime / (1000 * 60)) % 60
        return String.format("%02d:%02d", minutes, seconds)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        cameraProvider.unbindAll()
        executor.shutdown()
        handler.removeCallbacks(timeRunnable)
    }
}
