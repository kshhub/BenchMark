package esos.MobiBench

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.view.Window
import android.widget.ImageButton


class First : Activity() {
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.activity_first)
        val btn = findViewById<View>(R.id.start_button) as ImageButton
        btn.setOnClickListener(object : View.OnClickListener {
            var intent: Intent? = null
            override fun onClick(v: View) {
                // val vibrator = getSystemService(VIBRATOR_SERVICE) as Vibrator
                // vibrator.vibrate(100)
                val prefs = getSharedPreferences("Setting", MODE_PRIVATE)
                val editor = prefs.edit()
                editor.putBoolean("init_flag", false)
                editor.commit()
                intent = Intent(this@First, TabMain::class.java)
                intent!!.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                startActivity(intent)
            }
        })
    }

    // BACK키를 눌렀을 경우에 activity를 종료하게 한다.
    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            setResult(RESULT_CANCELED)
            finish()
        }
        return super.onKeyDown(keyCode, event)
    }
}