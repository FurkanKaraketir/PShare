package com.furkankrktr.pshare

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.os.Environment
import android.view.ContextMenu
import android.view.MenuItem
import android.view.View
import android.view.Window
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.theartofdev.edmodo.cropper.CropImage
import kotlinx.android.synthetic.main.activity_gorsel.*
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream


class GorselActivity : AppCompatActivity() {

    lateinit var outputStream: OutputStream

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.activity_gorsel)
        hideSystemUI()
        val resimUri = intent.getStringExtra("resim")
        Glide.with(this).load(resimUri).into(resimTamEkran)

        resimTamEkran.setOnClickListener {
            showSystemUI()
        }

        registerForContextMenu(resimTamEkran)


    }


    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) hideSystemUI()
    }

    override fun onCreateContextMenu(
        menu: ContextMenu?,
        v: View?,
        menuInfo: ContextMenu.ContextMenuInfo?
    ) {
        super.onCreateContextMenu(menu, v, menuInfo)
        menuInflater.inflate(R.menu.long_click_menu, menu)

    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.indirBtn) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.CAMERA
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                //İzin Verilmedi, iste
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA),
                    1
                )


            } else {
                //İzin Var
                val drawable = resimTamEkran.drawable as BitmapDrawable

                val bitmap = drawable.bitmap

                val filepath = Environment.getExternalStorageDirectory()
                if (filepath != null) {

                    val dir = File(filepath.absolutePath + "/PShare/")
                    dir.mkdir()
                    val file = File(dir, System.currentTimeMillis().toString() + ".jpg")
                    try {
                        outputStream = FileOutputStream(file)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }


                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
                    Toast.makeText(this, "Resim İndiriliyor", Toast.LENGTH_LONG).show()
                    try {
                        outputStream.flush()

                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    try {
                        outputStream.close()

                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    println("iş tamam")

                } else {
                    println("Naber")
                }

            }


        }

        return super.onContextItemSelected(item)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        //İzin Yeni Verildi
        if (requestCode == 1) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                val drawable = resimTamEkran.drawable as BitmapDrawable

                val bitmap = drawable.bitmap

                val filepath = Environment.getExternalStorageDirectory()
                if (filepath != null) {

                    val dir = File(filepath.absolutePath + "/PShare/")
                    dir.mkdir()
                    val file = File(dir, System.currentTimeMillis().toString() + ".jpg")
                    try {
                        outputStream = FileOutputStream(file)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }


                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
                    Toast.makeText(this, "Resim İndiriliyor", Toast.LENGTH_LONG).show()
                    try {
                        outputStream.flush()

                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    try {
                        outputStream.close()

                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    println("iş tamam")

                } else {
                    println("Naber")
                }


            }
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    private fun hideSystemUI() {
        // Enables regular immersive mode.
        // For "lean back" mode, remove SYSTEM_UI_FLAG_IMMERSIVE.
        // Or for "sticky immersive," replace it with SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_IMMERSIVE
                // Set the content to appear under the system bars so that the
                // content doesn't resize when the system bars hide and show.
                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                // Hide the nav bar and status bar
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_FULLSCREEN)
    }

    // Shows the system bars by removing all the flags
// except for the ones that make the content appear under the system bars.
    private fun showSystemUI() {
        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN)
    }


}