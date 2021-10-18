package esos.MobiBench

import android.annotation.TargetApi
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Toast
import esos.MobiBench.StorageOptions.GetFileSystemName
import java.io.*
import java.text.SimpleDateFormat
import java.util.*


@TargetApi(11)
class DialogActivity : Activity() {
    var list: DataListView? = null
    var adapter: IconTextListAdapter? = null
    private var db_prefs: SharedPreferences? = null
    private var pref_editor: SharedPreferences.Editor? = null
    private val calendar = Calendar.getInstance()
    private val dateFormat = SimpleDateFormat("yyyyMMdd_HH_mm_ss")
    private val dateFormat_file = SimpleDateFormat("yyyyMMdd_HHmm")
    private val bm: Bitmap? = null
    private var u_data: UpdateData? = null
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE)
        db_prefs = getSharedPreferences("Setting", MODE_PRIVATE)
        pref_editor = db_prefs!!.edit()

        // window feature for no title - must be set prior to calling setContentView.
        //requestWindowFeature(Window.FEATURE_NO_TITLE);

        // create a DataGridView instance
        val params = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.FILL_PARENT,
            ViewGroup.LayoutParams.FILL_PARENT
        )
        list = DataListView(this)


        // create an DataAdapter and a MTable
        adapter = IconTextListAdapter(this)

        // add items
        val res = resources
        val resID = IntArray(7)
        resID[0] = R.drawable.icon_sw
        resID[1] = R.drawable.icon_sr
        resID[2] = R.drawable.icon_rw
        resID[3] = R.drawable.icon_rr
        resID[4] = R.drawable.icon_insert
        resID[5] = R.drawable.icon_update
        resID[6] = R.drawable.icon_delete
        if (ResultDate == null) { // ResultDate가 0인 경우는 새로운 실험을 시작했을 때만.
            ResultDate = dateFormat.format(calendar.time) // for data base date
        }
        db_index = db_prefs!!.getInt("database_index", 0) // data base indexing
        clip_title = ""
        clip_text = clip_title
        clip_text = """* ${Build.MANUFACTURER.toUpperCase()} ${Build.MODEL}
* $ResultDate

"""
        clip_title = dateFormat_file.format(calendar.time)
        for (idx in 0..6) {
            if (bHasResult[idx] != 0) {
                adapter!!.addItem(
                    IconTextItem(
                        res.getDrawable(resID[idx]),
                        ResultCPU_act[idx],
                        ResultCPU_iow[idx],
                        ResultCPU_idl[idx],
                        ResultCS_tot[idx],
                        ResultCS_vol[idx],
                        ResultThrp[idx], ResultExpName[idx]
                    )
                )
                clip_text += """- ${ResultExpName[idx]}: ${ResultThrp[idx]}
 ▪ CPU: ${ResultCPU_act[idx]},${ResultCPU_iow[idx]},${ResultCPU_idl[idx]}
 ▪ CTX_SW: ${ResultCS_tot[idx]}(${ResultCS_vol[idx]})

"""
                if (check_using_db == 1) {
                    //Log.d(DEBUG_TAG, "addItem / checkusing is 1 : idx/expname " + idx + " " + ResultExpName[idx]);
                    db!!.insert_DB(
                        db_index, ResultDate,
                        ResultType[idx], 1,
                        ResultCPU_act[idx],
                        ResultCPU_iow[idx],
                        ResultCPU_idl[idx],
                        ResultCS_tot[idx],
                        ResultCS_vol[idx],
                        ResultThrp[idx],
                        ResultExpName[idx]
                    )
                }
            }
        }
        if (check_using_db == 1) {
            db_index++
            pref_editor!!.putInt("database_index", db_index)
            pref_editor!!.commit()
        }
        list!!.setAdapter(adapter)
        setContentView(list, params)
        window.setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.custome_title)
        findViewById<View>(R.id.ibtn_share).setOnClickListener(myButtonClick)
        findViewById<View>(R.id.ibtn_save).setOnClickListener(myButtonClick)
        val cm = getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        var ni = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI)
        isWifiAvail = ni!!.isAvailable
        isWifiConn = ni.isConnected
        ni = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE)
        isMobileAvail = ni!!.isAvailable
        isMobileConn = ni.isConnected
        if (check_using_db == 1) {
            if (isWifiConn || isMobileConn) {
                val alert = AlertDialog.Builder(this)
                    .setTitle("Send Results")
                    .setMessage(
                        """
                        Submit the performance result to the ranking server for research purposes.
                        (No personally identifiable information is collected.)
                        """.trimIndent()
                    )
                    .setCancelable(true)
                    .setPositiveButton(
                        "Accept"
                    ) { dialog, whichButton ->
                        u_data = UpdateData()
                        val tmp_string = arrayOfNulls<String>(7)
                        for (k in 0..6) {
                            if (bHasResult[k] == 1) {
                                tmp_string[k] = ResultThrp[k]
                            } else {
                                tmp_string[k] = "-1"
                            }
                        }
                        u_data!!.HttpPostData(
                            tmp_string[0],
                            tmp_string[1],
                            tmp_string[2],
                            tmp_string[3],
                            tmp_string[4],
                            tmp_string[5],
                            tmp_string[6],
                            dev_num,
                            g_partition!!.substring(1),
                            g_thread,
                            g_file_size_w,
                            g_file_size_r,
                            g_io_size,
                            g_file_mode,
                            g_transaction_mode,
                            g_sqlite_mode,
                            g_sqlite_journal,
                            GetFileSystemName(),
                            g_def
                        )
                        Log.d(
                            DEBUG_TAG,
                            "DEFAULT : " + g_def
                        )
                        Toast.makeText(
                            this@DialogActivity,
                            "send result to server",
                            Toast.LENGTH_SHORT
                        )
                            .show()
                    }
                    .setNegativeButton(
                        "Decline"
                    ) { dialog, whichButton -> }
                    .show()
            }
        }
    }

    var myButtonClick = View.OnClickListener { v ->
        when (v.id) {
            R.id.ibtn_share -> try {
                screenshot()
            } catch (e1: Exception) {
                // TODO Auto-generated catch block
                e1.printStackTrace()
            }
            R.id.ibtn_save -> {
                val txtFilename = ResultDate + ".txt"
                var txtPath: String
                val ext = Environment.getExternalStorageState()
                txtPath = if (ext == Environment.MEDIA_MOUNTED) {
                    Environment.getExternalStorageDirectory().absolutePath + "/"
                } else {
                    Environment.MEDIA_UNMOUNTED + "/"
                }
                val txtDir = File("$txtPath/MobiBench/")
                if (!txtDir.exists()) {
                    txtPath = ""
                    txtDir.mkdirs()
                }
                val tmp = clip_text
                // 파일 생성
                try {
                    val txtFile = File(
                        Environment.getExternalStorageDirectory().toString() + "/MobiBench/",
                        txtFilename
                    )
                    txtFile.createNewFile()
                    val out = BufferedWriter(FileWriter(txtFile))
                    out.write(tmp)
                    out.newLine()
                    out.close()
                } catch (e: IOException) {
                }
                val TotalPath = txtPath + "MobiBench/" + txtFilename
                Toast.makeText(applicationContext, "File saved : $TotalPath", Toast.LENGTH_LONG).show()
            }
        }
    }

    @Throws(Exception::class)
    fun screenshot() {
        val view = this.window.decorView // 전체 화면의 view를 가져온다
        view.isDrawingCacheEnabled = true
        val screenshot = view.drawingCache
        val filename = ResultDate + ".jpg"
        var Path: String
        val ext = Environment.getExternalStorageState()
        Path = if (ext == Environment.MEDIA_MOUNTED) {
            Environment.getExternalStorageDirectory().absolutePath + "/"
        } else {
            Environment.MEDIA_UNMOUNTED + "/"
        }
        val dir = File("$Path/MobiBenchImage/")
        if (!dir.exists()) {
            Path = ""
            dir.mkdirs()
        }
        try {
            val f = File(
                Environment.getExternalStorageDirectory().toString() + "/MobiBenchImage/",
                filename
            )
            f.createNewFile()
            val outStream: OutputStream = FileOutputStream(f)
            screenshot.compress(Bitmap.CompressFormat.PNG, 100, outStream)
            outStream.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        view.isDrawingCacheEnabled = false
        val sTotalPath =
            Environment.getExternalStorageDirectory().toString() + "/MobiBenchImage/" + filename
        val uri = Uri.fromFile(File(sTotalPath))
        val shareIntent = Intent()
        shareIntent.action = Intent.ACTION_SEND
        shareIntent.putExtra(Intent.EXTRA_STREAM, uri)
        shareIntent.type = "image/jpeg"
        startActivity(Intent.createChooser(shareIntent, "공유하기"))
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            ResultDate = null
        }
        return super.onKeyDown(keyCode, event)
    }

    companion object {
        var clip_text: String? = null
        var clip_title: String? = null
        private var db_index = 0
        private const val TAG_DA = "datedebug"
        private var db: NotesDbAdapter? = null
        var bHasResult = IntArray(7)
        private const val DEBUG_TAG = "dialogactivity"
        fun ClearResult(database: NotesDbAdapter?) {
            db = database
            for (i in 0..6) {
                bHasResult[i] = 0
                ResultCPU_act[i] = null
                ResultCPU_iow[i] = null
                ResultCPU_idl[i] = null
                ResultCS_tot[i] = null
                ResultCS_vol[i] = null
                ResultThrp[i] = null
                ResultExpName[i] = null
                ResultType[i] = null
            }
        }

        var index_db = 0
        var ResultCPU_act = arrayOfNulls<String>(7)
        var ResultCPU_iow = arrayOfNulls<String>(7)
        var ResultCPU_idl = arrayOfNulls<String>(7)
        var ResultCS_tot = arrayOfNulls<String>(7)
        var ResultCS_vol = arrayOfNulls<String>(7)
        var ResultThrp = arrayOfNulls<String>(7)
        var ResultExpName = arrayOfNulls<String>(7)
        var ResultType = arrayOfNulls<String>(7)
        var ResultDate: String? = null

        /* Global value for save : testing configuration */
        var g_partition: String? = null
        var g_thread: String? = null
        var g_file_size_w: String? = null
        var g_file_size_r: String? = null
        var g_io_size: String? = null
        var g_file_mode: String? = null
        var g_transaction_mode: String? = null
        var g_sqlite_mode: String? = null
        var g_sqlite_journal: String? = null
        var g_def: String? = null
        var G_EXP_CHOICE = "default_g_exp_choice"
        var check_using_db = 0
        var isWifiAvail = false
        var isWifiConn = false
        var isMobileAvail = false
        var isMobileConn = false
        var dev_num: String? = null
    }
}