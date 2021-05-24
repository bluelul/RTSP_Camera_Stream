package com.pedro.sample

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.view.SurfaceHolder
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.pedro.encoder.input.video.CameraOpenException
import com.pedro.rtsp.utils.ConnectCheckerRtsp
import com.pedro.rtspserver.RtspServerCamera1
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity(), ConnectCheckerRtsp, View.OnClickListener,
        SurfaceHolder.Callback {

  private var rtspServerCamera1: RtspServerCamera1? = null
  private var button: Button? = null
  private var bRecord: Button? = null

  private var currentDateAndTime = ""
  private val folder =
          File(Environment.getExternalStorageDirectory().absolutePath + "/rtmp-rtsp-stream-client-java")


  private val PERMISSIONS = arrayOf(Manifest.permission.RECORD_AUDIO, Manifest.permission.CAMERA,
          Manifest.permission.WRITE_EXTERNAL_STORAGE)

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    setContentView(R.layout.activity_main)

    if (!hasPermissions(this, *PERMISSIONS)) {
      ActivityCompat.requestPermissions(this, PERMISSIONS, 1)
    }

    button = findViewById(R.id.b_start_stop)
    button?.setOnClickListener(this)
    bRecord = findViewById(R.id.b_record)
    bRecord?.setOnClickListener(this)
    switch_camera.setOnClickListener(this)
    rtspServerCamera1 = RtspServerCamera1(surfaceView, this, 1935)
    surfaceView.holder.addCallback(this)
  }

  private fun hasPermissions(context: Context?, vararg permissions: String): Boolean {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && context != null) {
      for (permission in permissions) {
        if (ActivityCompat.checkSelfPermission(context,
                        permission) != PackageManager.PERMISSION_GRANTED) {
          return false
        }
      }
    }
    return true
  }

  override fun onNewBitrateRtsp(bitrate: Long) {

  }

  override fun onConnectionSuccessRtsp() {
    runOnUiThread {
      Toast.makeText(this@MainActivity, "Connection success", Toast.LENGTH_SHORT).show()
    }
  }

  override fun onConnectionFailedRtsp(reason: String) {
    runOnUiThread {
      Toast.makeText(this@MainActivity, "Connection failed. $reason", Toast.LENGTH_SHORT)
              .show()
      rtspServerCamera1!!.stopStream()
      button?.setText(R.string.start_button)
    }
  }

  override fun onDisconnectRtsp() {
    runOnUiThread {
      Toast.makeText(this@MainActivity, "Disconnected", Toast.LENGTH_SHORT).show()
    }
  }

  override fun onAuthErrorRtsp() {
    runOnUiThread {
      Toast.makeText(this@MainActivity, "Auth error", Toast.LENGTH_SHORT).show()
      rtspServerCamera1?.stopStream()
      button!!.setText(R.string.start_button)
      tv_url.text = ""
    }
  }

  override fun onAuthSuccessRtsp() {
    runOnUiThread {
      Toast.makeText(this@MainActivity, "Auth success", Toast.LENGTH_SHORT).show()
    }
  }

  @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
  override fun onClick(view: View) {
    when (view.id) {
      R.id.b_start_stop -> if (!rtspServerCamera1!!.isStreaming) {
        if (rtspServerCamera1!!.isRecording || rtspServerCamera1!!.prepareAudio() && rtspServerCamera1!!.prepareVideo()) {
          button!!.setText(R.string.stop_button)
          rtspServerCamera1!!.startStream()
          tv_url.text = rtspServerCamera1?.getEndPointConnection()
        } else {
          Toast.makeText(this, "Error preparing stream, This device cant do it", Toast.LENGTH_SHORT)
                  .show()
        }
      } else {
        button!!.setText(R.string.start_button)
        rtspServerCamera1!!.stopStream()
        tv_url.text = ""
      }
      R.id.switch_camera -> try {
        rtspServerCamera1!!.switchCamera()
      } catch (e: CameraOpenException) {
        Toast.makeText(this, e.message, Toast.LENGTH_SHORT).show()
      }

      R.id.b_record -> if (!rtspServerCamera1!!.isRecording) {
        try {
          if (!folder.exists()) {
            folder.mkdir()
          }
          val sdf = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
          currentDateAndTime = sdf.format(Date())
          if (!rtspServerCamera1!!.isStreaming) {
            if (rtspServerCamera1!!.prepareAudio() && rtspServerCamera1!!.prepareVideo()) {
              rtspServerCamera1!!.startRecord(
                      folder.absolutePath + "/" + currentDateAndTime + ".mp4")
              bRecord!!.setText(R.string.stop_record)
              Toast.makeText(this, "Recording... ", Toast.LENGTH_SHORT).show()
            } else {
              Toast.makeText(this, "Error preparing stream, This device cant do it",
                      Toast.LENGTH_SHORT).show()
            }
          } else {
            rtspServerCamera1!!.startRecord(folder.absolutePath + "/" + currentDateAndTime + ".mp4")
            bRecord!!.setText(R.string.stop_record)
            Toast.makeText(this, "Recording... ", Toast.LENGTH_SHORT).show()
          }
        } catch (e: IOException) {
          rtspServerCamera1!!.stopRecord()
          bRecord!!.setText(R.string.start_record)
          Toast.makeText(this, e.message, Toast.LENGTH_SHORT).show()
        }
      } else {
        rtspServerCamera1!!.stopRecord()
        bRecord!!.setText(R.string.start_record)
        Toast.makeText(this, "file " + currentDateAndTime + ".mp4 saved in " + folder.absolutePath,
                Toast.LENGTH_SHORT).show()
      }
      else -> {
      }
    }
  }

  override fun surfaceCreated(surfaceHolder: SurfaceHolder) {
  }

  override fun surfaceChanged(surfaceHolder: SurfaceHolder, i: Int, i1: Int, i2: Int) {
    rtspServerCamera1!!.startPreview()
  }

  @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
  override fun surfaceDestroyed(surfaceHolder: SurfaceHolder) {
    if (rtspServerCamera1!!.isRecording) {
      rtspServerCamera1!!.stopRecord()
      bRecord!!.setText(R.string.start_record)
      Toast.makeText(this, "file " + currentDateAndTime + ".mp4 saved in " + folder.absolutePath,
              Toast.LENGTH_SHORT).show()
      currentDateAndTime = ""
    }
    if (rtspServerCamera1!!.isStreaming) {
      rtspServerCamera1!!.stopStream()
      button!!.text = resources.getString(R.string.start_button)
      tv_url.text = ""
    }
    rtspServerCamera1!!.stopPreview()
  }
}
