

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Camera
import android.hardware.camera2.*
import android.media.MediaRecorder
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.SystemClock
import android.util.Log
import android.util.Size
import android.view.LayoutInflater
import android.view.Surface
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.View
import android.view.ViewGroup
import android.widget.Toast

import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.android.masterdistributormdl.R
import com.android.masterdistributormdl.databinding.FragmentRecordVideoBinding
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class RecordVideoFragment : Fragment() {

    private lateinit var binding: FragmentRecordVideoBinding
    private lateinit var mediaRecorder: MediaRecorder
    private lateinit var surfaceView: SurfaceView

    private var isRecording = false
    private var camera: android.hardware.Camera? = null
    private val videoFile = File(requireContext().getExternalFilesDir(null), "video.mp4")


    private lateinit var surfaceHolder: SurfaceHolder

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_record_video, container, false)
        binding.lifecycleOwner = this
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }




    private fun formatTime(milliseconds: Long): String {
        val seconds = (milliseconds / 1000) % 60
        val minutes = (milliseconds / 60000) % 60
        val hours = (milliseconds / 3600000)
        return String.format("%02d:%02d:%02d", hours, minutes, seconds)
    }



    private fun allPermissionsGranted() =
        REQUIRED_PERMISSIONS.all {
            ContextCompat.checkSelfPermission(requireContext(), it) == PackageManager.PERMISSION_GRANTED
        }

    companion object {
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO)
        private const val REQUEST_CODE_PERMISSIONS = 10
    }
}
