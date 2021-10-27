package esos.MobiBench

import android.app.AlertDialog
import android.app.TabActivity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.Color
import android.graphics.drawable.AnimationDrawable
import android.net.ConnectivityManager
import android.net.Uri
import android.os.*
import android.util.Log
import android.view.*
import android.widget.*
import android.widget.AdapterView.OnItemClickListener
import java.io.BufferedReader
import java.io.InputStreamReader
import java.math.BigInteger
import java.net.HttpURLConnection
import java.net.URL
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.*


class TabMain : TabActivity() {
    // for update version check
    private var mResult: String? = "1"
    private var prefs: SharedPreferences? = null
    private var editor: SharedPreferences.Editor? = null
    private var db_index = 0
    private val set: Setting = Setting()

    //private EditText et_io_size = null;
    private var m_exe: MobiBenchExe? = null
    private var tv_progress_txt: TextView? = null
    private var tv_progress_per: TextView? = null
    private var mFlag = false // using App stop button
    private var result: Cursor? = null
    private var db_date: String? = null
    private val arr = ArrayList<String>()
    private var aa: ArrayAdapter<String>? = null
    private val tmpExpName = arrayOf(
        "Seq.Write",
        "Seq.read",
        "Rand.Write",
        "Rand.Read",
        "SQLite.Insert",
        "SQLite.Update",
        "SQLite.Delete"
    )

    //   ProgressTrd thread = null;
    var mb_thread: MobiBenchExe? = null
    private var con: Context? = null

    /* For Animation*/
    var anidrawable: AnimationDrawable? = null
    var image: ImageView? = null
    var mHandler: Handler = object : Handler() {
        override fun handleMessage(msg: Message) {
            if (msg.what <= 100) {
                prBar!!.progress = msg.what
                tv_progress_per!!.text = "" + msg.what + "%"
            } else if (msg.what == 999) {
                tv_progress_txt!!.text = msg.obj as String
            } else if (msg.what == 666) {
                mFlag = false
            } else if (msg.what == 444) {
                //while(image.getId() != R.drawable.bg_mea_middle06 ){
                anidrawable!!.stop()
                //}
                image!!.setImageResource(R.drawable.bg_mea)
                if (msg.arg1 == 1) {
                    print_error(1)
                }
                Log.d(DEBUG_TAG, "[JWGOM] join start")
                try {
                    mb_thread!!.join()
                } catch (e: InterruptedException) {
                    // TODO Auto-generated catch block
                    e.printStackTrace()
                }
                Log.d(DEBUG_TAG, "[JWGOM] join end")
                btn_clk_check = true
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(DEBUG_TAG, "**********onCreate")
        /* Mobibench 초기화 */
        if (m_exe == null) {
            m_exe = MobiBenchExe()
            m_exe!!.LoadEngine()
            m_exe!!.SetStoragePath(this.filesDir.toString())
        }
        // For Database
        dbAdapter = NotesDbAdapter(this)
        dbAdapter!!.open()

        /* For tab layout setting */
        val tabHost = tabHost
        LayoutInflater.from(this).inflate(R.layout.tabmain, tabHost.tabContentView, true)
        tabHost.addTab(
            tabHost.newTabSpec("measure")
                .setIndicator("", resources.getDrawable(R.drawable.tab_mea))
                .setContent(R.id.measure)
        )
        tabHost.addTab(
            tabHost.newTabSpec("history")
                .setIndicator("", resources.getDrawable(R.drawable.tab_history))
                .setContent(R.id.history)
        )
        for(tab in 0 until tabHost.tabWidget.childCount){
            tabHost.tabWidget.getChildAt(tab).setBackgroundColor(Color.parseColor("#B5E61D"))
        }
        tabHost.tabWidget.getChildAt(0).setBackgroundColor(Color.parseColor("#CDDF94"))
        tabHost.setOnTabChangedListener {
            if(tabHost.currentTab == 0){
                tabHost.tabWidget.getChildAt(0).setBackgroundColor(Color.parseColor("#CDDF94"))
                tabHost.tabWidget.getChildAt(1).setBackgroundColor(Color.parseColor("#B5E61D"))
            }else{
                tabHost.tabWidget.getChildAt(1).setBackgroundColor(Color.parseColor("#CDDF94"))
                tabHost.tabWidget.getChildAt(0).setBackgroundColor(Color.parseColor("#B5E61D"))
            }
        }

        /* For Animation*/
        image = findViewById<View>(R.id.position_mea) as ImageView
        image!!.setBackgroundResource(R.drawable.aniimage)
        anidrawable = image!!.background as AnimationDrawable

        //image.post(new StartAni());


        /* Preference Control */
        prefs = getSharedPreferences("Setting", MODE_PRIVATE)
        editor = prefs!!.edit()

        /*jwgom
		 *
		 * ***************************************************************************************************************************************************
		 * */
        var pi: PackageInfo? = null
        try {
            pi = packageManager.getPackageInfo(packageName, 0)
        } catch (e: PackageManager.NameNotFoundException) {

            // TODO Auto-generated catch block
            e.printStackTrace()
        }
        val verSion = pi!!.versionName
        val verCode = pi.versionCode
        Log.d(DEBUG_TAG, "versionName is $verSion")
        Log.d(DEBUG_TAG, "versionCode is $verCode")

        /*jwgom
		 *
		 * ***************************************************************************************************************************************************
		 * */db_index = prefs!!.getInt("database_index", 0) // data base indexing
        result = null
        var db_data: String? = null
        val arr = ArrayList<String>()
        Log.d(DEBUG_TAG, "**********onCreate before jwgom  1")
        if (db_index != 0) {
            for (i in 0 until db_index) {
                result = dbAdapter!!.fetchNote(i.toLong()) // 횟수 제한
                result!!.moveToFirst()
                db_data = "  " + result!!.getString(2) + "(" + result!!.getString(1) + ")"
                arr.add(db_data)
            }
            result!!.close()
        }
        val list = findViewById<View>(R.id.ListView01) as ListView
        aa = ArrayAdapter(this, R.layout.history_listitem, arr)
        list.adapter = aa // ListView에 ArrayAdapter 설정
        list.onItemClickListener =
            OnItemClickListener { a, v, position, id ->
                result = dbAdapter!!.fetchNote(position.toLong())
                result!!.moveToFirst()
                //for(int i=0; i < 7; i++) {
                Log.d(DEBUG_TAG, "Start cursor position " + result!!.position)


                // clear values
                result_start = 0
                DialogActivity.ResultDate = null
                for (i in 0..6) {
                    DialogActivity.bHasResult[i] = 0
                    DialogActivity.ResultCPU_act[i] = null
                    DialogActivity.ResultCPU_iow[i] = null
                    DialogActivity.ResultCPU_idl[i] = null
                    DialogActivity.ResultCS_tot[i] = null
                    DialogActivity.ResultCS_vol[i] = null
                    DialogActivity.ResultThrp[i] = null
                    DialogActivity.ResultExpName[i] = null
                    DialogActivity.ResultType[i] = null
                }
                DialogActivity.ResultDate = result!!.getString(1)
                while (!result!!.isAfterLast) {
                    Log.d(
                        DEBUG_TAG,
                        "Create DialogActivity (position/result_start/expname) " + result!!.position + " " + result_start + " " + result!!.getString(
                            10
                        )
                    )
                    if (result!!.getString(10) == tmpExpName[0]) { // seq write
                        result_start = 0
                    } else if (result!!.getString(10) == tmpExpName[1]) {
                        result_start = 1
                    } else if (result!!.getString(10) == tmpExpName[2]) {
                        result_start = 2
                    } else if (result!!.getString(10) == tmpExpName[3]) {
                        result_start = 3
                    } else if (result!!.getString(10) == tmpExpName[4]) {
                        result_start = 4
                    } else if (result!!.getString(10) == tmpExpName[5]) {
                        result_start = 5
                    } else if (result!!.getString(10) == tmpExpName[6]) {
                        result_start = 6
                    }
                    DialogActivity.bHasResult[result_start] = result!!.getInt(3)
                    DialogActivity.ResultCPU_act[result_start] = result!!.getString(4)
                    DialogActivity.ResultCPU_iow[result_start] = result!!.getString(5)
                    DialogActivity.ResultCPU_idl[result_start] = result!!.getString(6)
                    DialogActivity.ResultCS_tot[result_start] = result!!.getString(7)
                    DialogActivity.ResultCS_vol[result_start] = result!!.getString(8)
                    DialogActivity.ResultThrp[result_start] = result!!.getString(9)
                    DialogActivity.ResultExpName[result_start] = result!!.getString(10)
                    DialogActivity.ResultType[result_start] = result!!.getString(2)
                    result!!.moveToNext()
                }
                val intent = Intent(this@TabMain, DialogActivity::class.java)
                DialogActivity.check_using_db = 0
                startActivity(intent)
            }

        /*jwgom
		 *
		 * ***************************************************************************************************************************************************
		 * */Log.d(DEBUG_TAG, "**********onCreate after jwgom")

        /* spinner define (total 5 spinner) */
        val ad_partition: ArrayAdapter<*>
        ad_partition = if (StorageOptions.b_2nd_sdcard === true) {
            ArrayAdapter.createFromResource(this, R.array.partition, android.R.layout.simple_spinner_item)
        } else {
            ArrayAdapter.createFromResource(this, R.array.partition2, android.R.layout.simple_spinner_item)
        }
        val ad_file_sync: ArrayAdapter<*> =
            ArrayAdapter.createFromResource(this, R.array.filesyncmode, R.layout.spinner_item)
        val ad_io_size: ArrayAdapter<*> =
            ArrayAdapter.createFromResource(this, R.array.iosize, R.layout.spinner_item)
        val ad_sql_sync: ArrayAdapter<*> =
            ArrayAdapter.createFromResource(this, R.array.sqlsyncmode, R.layout.spinner_item)
        val ad_journal: ArrayAdapter<*> =
            ArrayAdapter.createFromResource(this, R.array.journalmode, R.layout.spinner_item)
        //
        ad_partition.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        ad_file_sync.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        ad_io_size.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        ad_sql_sync.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        ad_journal.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        tv_progress_txt = findViewById<View>(R.id.progress_text) as TextView
        tv_progress_per = findViewById<View>(R.id.progress_per) as TextView
        prBar = findViewById<View>(R.id.progress) as ProgressBar
        prBar!!.progress = 0


        /* First Warning message control */
        load_init()


        // Activity가 실행 중인 동안 화면을 밝게 유지합니다.
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        /* Image button listener (Benchmark - al 버튼) */
        findViewById<View>(R.id.btn_all).setOnClickListener{
            startMobibenchExe()
        }

        //        mb_thread.setDaemon(true);
        con = this
        Log.d(DEBUG_TAG, "**********onCreate complete")
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        if (requestCode == 0) {
            if (resultCode == RESULT_CANCELED) {
                finish()
            }
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (!mFlag) {
                Toast.makeText(
                    this@TabMain,
                    "Press the \"Back button\" again to exit",
                    Toast.LENGTH_SHORT
                ).show()
                mFlag = true
                mHandler.sendEmptyMessageDelayed(666, 2000)
                return false
            } else {
                setResult(RESULT_CANCELED)
                finish()
            }
        }
        return super.onKeyDown(keyCode, event)
    }

    /* (stage 1) */
    private fun startMobibenchExe() {
        if (btn_clk_check == false) {
            print_error(0)
        } else {
            image!!.post(StartAni())
            btn_clk_check = false
            Log.d(DEBUG_TAG, "[TM] BTN_CLICK:TRUE" + "[" + btn_clk_check + "]")
            DialogActivity.ClearResult(dbAdapter)
            Toast.makeText(this, "Start Benchmark : File, SQlite", Toast.LENGTH_SHORT).show()
            mb_thread = MobiBenchExe(con, mHandler)
            mb_thread!!.start()
        }
    }

    /* Load : stored in preferences */ /* Load preferenced values */
    private fun load_init() {
        if (StorageOptions.b_2nd_sdcard === false && prefs!!.getInt("p_target_partition", 0) == 2) {
            set.set_target_partition(0)
        } else {
            set.set_target_partition(prefs!!.getInt("p_target_partition", 0))
        }
        set.set_thread_num(prefs!!.getInt("p_threadnum", 1))
        set.set_filesize_write(prefs!!.getInt("p_filesize_w", 10))
        set.set_filesize_read(prefs!!.getInt("p_filesize_r", 32))
        set.set_io_size(prefs!!.getInt("p_io_size", 0))
        set.set_file_sync_mode(prefs!!.getInt("p_file_sync_mode", 3))
        set.set_transaction_num(prefs!!.getInt("p_transaction", 1))
        set.set_sql_sync_mode(prefs!!.getInt("p_sql_sync_mode", 0))
        set.set_journal_mode(prefs!!.getInt("p_journal_mode", 0))

        Log.d(DEBUG_TAG, "[JWGOM] start")
        Log.d(DEBUG_TAG, "[JWGOM] end1")
        //Toast.makeText(this, "load init cb count "+ set.get_cb_count() , Toast.LENGTH_SHORT).show();
        Log.d(DEBUG_TAG, "[JWGOM] end")
        //print_values();
    }

//    fun set_default() {
//        editor!!.putInt("p_target_partition", 0)
//        set.set_target_partition(0)
//        editor!!.putInt("p_threadnum", 1)
//        set.set_thread_num(1)
//        editor!!.putInt("p_filesize_w", 10)
//        set.set_filesize_write(10)
//        editor!!.putInt("p_filesize_r", 32)
//        set.set_filesize_read(32)
//        editor!!.putInt("p_io_size", 0)
//        set.set_io_size(0)
//        editor!!.putInt("p_file_sync_mode", 3)
//        set.set_file_sync_mode(0)
//        editor!!.putInt("p_transaction", 100)
//        set.set_transaction_num(100)
//        editor!!.putInt("p_sql_sync_mode", 2)
//        set.set_sql_sync_mode(1)
//        editor!!.putInt("p_journal_mode", 1)
//        set.set_journal_mode(1)
//        editor!!.putInt("p_cb_count", 0)
//
//        editor!!.putBoolean("p_cb_sw", false)
//        editor!!.putBoolean("p_cb_sr", false)
//        editor!!.putBoolean("p_cb_rw", false)
//        editor!!.putBoolean("p_cb_rr", false)
//        editor!!.putBoolean("p_cb_insert", false)
//        editor!!.putBoolean("p_cb_update", false)
//        editor!!.putBoolean("p_cb_delete", false)
//        editor!!.commit()
//    }

    fun print_error(type: Int) {
        when (type) {
            0 -> Toast.makeText(this, "MobiBench working..", Toast.LENGTH_SHORT).show()
            1 -> Toast.makeText(this, "Benchmark engin exited with error", Toast.LENGTH_LONG).show()
            2 -> Toast.makeText(
                this,
                "The file size must be less than the free space.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun onResume() {
        super.onResume()
        db_index = prefs!!.getInt("database_index", 0) // data base indexing
        arr.clear()
        for (i in 0 until db_index) {
            result = dbAdapter!!.fetchNote(i.toLong()) // 횟수 제한
            result!!.moveToFirst()
            db_date = " " + result!!.getString(2) + "  ( " + result!!.getString(1) + " )"
            arr.add(db_date!!)
            //aa.notifyDataSetChanged();
        }
        val list = findViewById<View>(R.id.ListView01) as ListView
        aa = ArrayAdapter( // ListView에 할당할 ArrayAdapter 생성
            this, R.layout.history_listitem, arr
        ) // 여기에 앞에서 만든 ArrayList를 사용한다
        list.adapter = aa // ListView에 ArrayAdapter 설정
    }

    internal inner class StartAni : Runnable {
        override fun run() {
            anidrawable!!.start()
        }
    }

    companion object {
        private var result_start = 0

        /*jwgom*/
        private const val DEBUG_TAG = "progress bar"
        var prBar: ProgressBar? = null
        private var btn_clk_check = true


        // For Database
        var dbAdapter: NotesDbAdapter? = null
    }
}