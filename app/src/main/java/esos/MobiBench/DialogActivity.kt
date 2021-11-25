package esos.MobiBench

import android.annotation.TargetApi
import android.app.Activity
import android.content.SharedPreferences
import android.net.ConnectivityManager
import android.os.Build
import android.os.Bundle
import android.view.KeyEvent
import android.view.ViewGroup
import android.view.Window
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
        window.setBackgroundDrawableResource(R.color.white)
        val cm = getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        var ni = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI)
        isWifiAvail = ni!!.isAvailable
        isWifiConn = ni.isConnected
        ni = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE)
        isMobileAvail = ni!!.isAvailable
        isMobileConn = ni.isConnected
        if (check_using_db == 1) {
            if (isWifiConn || isMobileConn) {
                // Alert
            }
        }
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