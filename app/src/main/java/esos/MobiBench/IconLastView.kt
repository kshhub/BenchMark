package esos.MobiBench

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.widget.*


@SuppressLint("ViewConstructor")
class IconLastView(var this_context: Context, aItem: IconTextItem) :
    LinearLayout(this_context) {
    /**
     * Icon
     */
    private val mIcon: ImageView

    /**
     * TextView 01
     */
    private val mText01r: TextView
    private val mText02r: TextView
    private val mText03r: TextView
    private val mText04r: TextView
    private val mText05r: TextView
    private val mText06r: TextView
    private val mText07r // experiment
            : TextView
    private var pos: String? = null

    // private TextView line;
    private val btn_webview: ImageButton
    private var clip_str: String? = null
    var mClickListener = OnClickListener { v ->
        when (v.id) {
            R.id.ibtn_webview -> if (DialogActivity.isWifiConn || DialogActivity.isMobileConn) {
                val intent = Intent(this_context, Webview::class.java)
                DialogActivity.G_EXP_CHOICE = pos!!
                this_context.startActivity(intent)
            } else {
                Toast.makeText(this_context, "Not connected to wifi or 3g/4g", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    /**
     * set Text
     *
     * @param index
     * @param data
     */
    fun setText(index: Int, data: String?) {
        if (index == 0) {
            mText01r.text = data
        } else if (index == 1) {
            mText02r.text = data
        } else if (index == 2) {
            mText03r.text = data
        } else if (index == 3) {
            mText04r.text = data
        } else if (index == 4) {
            mText05r.text = data
        } else if (index == 5) {
            mText06r.text = data
        } else if (index == 6) {
            mText07r.text = data
        } else {
            throw IllegalArgumentException()
        }
    }

    fun setClip(clip: String?) {
        clip_str = clip
    }

    /**
     * set Icon
     *
     * @param icon
     */
    fun setIcon(icon: Drawable?) {
        mIcon.setImageDrawable(icon)
    }

    init {
        // Layout Inflation
        val inflater =
            this_context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        inflater.inflate(R.layout.lastlistitem, this, true)

        // Set Icon
        mIcon = findViewById<View>(R.id.iconItemr) as ImageView
        mIcon.setImageDrawable(aItem.icon)

        //  line = (TextView) findViewById(R.id.list_last_line);

        // Set Text 01
        mText01r = findViewById<View>(R.id.dataItem01r) as TextView
        mText01r.text = aItem.getData(0)

        // Set Text 02
        mText02r = findViewById<View>(R.id.dataItem02r) as TextView
        mText02r.text = aItem.getData(1)

        // Set Text 03
        mText03r = findViewById<View>(R.id.dataItem03r) as TextView
        mText03r.text = aItem.getData(2)

        // Set Text 04
        mText04r = findViewById<View>(R.id.dataItem04r) as TextView
        mText04r.text = aItem.getData(3)

        // Set Text 05
        mText05r = findViewById<View>(R.id.dataItem05r) as TextView
        mText05r.text = aItem.getData(4)

        // Set Text 06
        mText06r = findViewById<View>(R.id.dataItem06r) as TextView
        mText06r.text = aItem.getData(5)

        // Set Text 06
        mText07r = findViewById<View>(R.id.dataItem07r) as TextView
        mText07r.text = aItem.getData(6)
        pos = aItem.getData(6)
        btn_webview = findViewById<View>(R.id.ibtn_webview) as ImageButton
        findViewById<View>(R.id.ibtn_webview).setOnClickListener(mClickListener)
    }
}