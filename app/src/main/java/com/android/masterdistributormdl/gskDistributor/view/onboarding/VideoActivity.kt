package com.android.masterdistributormdl.gskDistributor.view.onboarding

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.os.Bundle
import android.util.Log
import android.view.Surface
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.core.VideoCapture
import androidx.camera.lifecycle.ProcessCameraProvider

import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.android.masterdistributormdl.databinding.ActivityVideoBinding
import java.io.File
import java.io.IOException
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class VideoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityVideoBinding
    private lateinit var cameraExecutor: ExecutorService
    private var mediaRecorder: MediaRecorder? = null
    private var recording: Boolean = false
    private lateinit var videoFile: File
    private var cameraSelector: CameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
    @SuppressLint("RestrictedApi")
    private var videoCapture: VideoCapture? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVideoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        cameraExecutor = Executors.newSingleThreadExecutor()

        // Request permissions
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
            && ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO), 101)
        }

        // Start/Stop recording functionality
        binding.recordButton.setOnClickListener {
            if (recording) {
                stopRecording()
            } else {
                startRecording()
            }
        }

        // Stop recording
        binding.stopButton.setOnClickListener {
            stopRecording()
        }

        // Rotate camera
        binding.rotateButton.setOnClickListener {
            rotateCamera()
        }
    }

    @SuppressLint("RestrictedApi")
    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            // Preview Use Case
            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(binding.viewFinder.surfaceProvider)
            }

            // Video Capture Use Case
            videoCapture = VideoCapture.Builder()
                .setTargetRotation(Surface.ROTATION_0)
                .build()

            // MediaRecorder setup
            setupMediaRecorder()

            try {
                // Unbind all previous use cases
                cameraProvider.unbindAll()

                // Bind the preview and video capture use cases to camera
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, videoCapture)

            } catch (e: Exception) {
                e.printStackTrace()
                Log.e("CameraX", "Error binding use cases", e)
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun setupMediaRecorder() {
        try {
            if (mediaRecorder == null) {
                mediaRecorder = MediaRecorder()
            }

            // Reset media recorder before setup
            mediaRecorder?.reset()

            mediaRecorder?.apply {
                // Set audio and video sources
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setVideoSource(MediaRecorder.VideoSource.CAMERA)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setVideoEncoder(MediaRecorder.VideoEncoder.H264)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)

                // Set output file
                videoFile = File(getExternalFilesDir(null), "test_video.mp4")
                Log.d("MediaRecorder", "Video file path: ${videoFile.absolutePath}")
                setOutputFile(videoFile.absolutePath)

                // Set video size and frame rate
                setVideoSize(1920, 1080)
                setVideoFrameRate(30)

                // Set the orientation
                setOrientationHint(90)

                // Prepare recorder
                prepare()
                Log.d("MediaRecorder", "MediaRecorder prepared successfully")
            }
        } catch (e: IOException) {
            e.printStackTrace()
            Log.e("MediaRecorder", "Error preparing MediaRecorder", e)
        }
    }

    private fun startRecording() {
        try {
            // Start the video capture using MediaRecorder
            mediaRecorder?.start()
            recording = true
            Log.d("MediaRecorder", "Recording started")
            Toast.makeText(this, "Recording Started", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("MediaRecorder", "Error starting recording", e)
            Toast.makeText(this, "Error starting recording", Toast.LENGTH_SHORT).show()
        }
    }

    private fun stopRecording() {
        try {
            // Stop the recording
            mediaRecorder?.stop()
            mediaRecorder?.reset()
            recording = false
            Toast.makeText(this, "Recording Stopped", Toast.LENGTH_SHORT).show()

            // Play the video in VideoView
            binding.videoView.setVideoPath(videoFile.absolutePath)
            binding.videoView.start()
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("MediaRecorder", "Error stopping recording", e)
            Toast.makeText(this, "Error stopping recording", Toast.LENGTH_SHORT).show()
        } finally {
            mediaRecorder?.release()  // Release MediaRecorder
        }
    }

    private fun rotateCamera() {
        cameraSelector = if (cameraSelector == CameraSelector.DEFAULT_BACK_CAMERA) {
            CameraSelector.DEFAULT_FRONT_CAMERA
        } else {
            CameraSelector.DEFAULT_BACK_CAMERA
        }

        // Restart the camera with the rotated selector
        startCamera()
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaRecorder?.release() // Release the recorder when activity is destroyed
        cameraExecutor.shutdown()
    }
}
