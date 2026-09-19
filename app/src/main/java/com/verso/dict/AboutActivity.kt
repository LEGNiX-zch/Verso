package com.verso.dict

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.verso.dict.databinding.ActivityAboutBinding

class AboutActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAboutBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAboutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.toolbar.setNavigationOnClickListener { finish() }

        val versionName = try {
            val info = packageManager.getPackageInfo(packageName, 0)
            info.versionName ?: "1.0.0"
        } catch (e: android.content.pm.PackageManager.NameNotFoundException) {
            "1.0.0"
        }
        binding.tvVersion.text = "${getString(R.string.about_version)}：v$versionName"

        binding.btnAppreciate.setOnClickListener { showAppreciateDialog() }
    }

    private fun showAppreciateDialog() {
        val view = layoutInflater.inflate(R.layout.dialog_appreciate, null)
        val imageView = view.findViewById<android.widget.ImageView>(R.id.iv_qrcode)
        val fallback = view.findViewById<android.widget.TextView>(R.id.tv_appreciate_fallback)

        // Resolve the appreciation QR code by name so the project builds even before the
        // image asset is added. It is shown automatically once present.
        val resId = resources.getIdentifier("appreciate_qrcode", "drawable", packageName)
        if (resId != 0) {
            imageView.setImageResource(resId)
            imageView.visibility = android.view.View.VISIBLE
            fallback.visibility = android.view.View.GONE
        } else {
            imageView.visibility = android.view.View.GONE
            fallback.text = "赞赏二维码尚未添加，请在资源文件夹中放入对应图片后重新编译。"
            fallback.visibility = android.view.View.VISIBLE
        }

        AlertDialog.Builder(this)
            .setView(view)
            .setPositiveButton(R.string.appreciate_close, null)
            .create()
            .show()
    }
}
