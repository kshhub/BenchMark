package esos.MobiBench

import android.content.Context
import android.content.Intent
import android.os.Environment
import android.os.Handler
import android.os.Message
import android.util.Log
import esos.MobiBench.StorageOptions.determineStorageOptions
import java.io.File

class MobiBenchExe : Thread {
    private var exp_id = 0
    private var mHandler: Handler? = null
    private var msg: Message? = null
    private var con: Context? = null
    private val db: NotesDbAdapter? = null

    constructor(context: Context?, handler: Handler?) {
        mHandler = handler
        con = context
    }

    override fun run() {
        var is_error = 0
        select_flag.apply {
            when (select_flag) {
                0 -> {
                    RunFileIO()
                    is_error = if (mobibenchState == 4) 1 else 0
                    if (is_error != 0) {
                        return@apply
                    }
                    RunSqlite()
                    intent = Intent(con, DialogActivity::class.java)
                    DialogActivity.check_using_db = 1
                    con!!.startActivity(intent)
                }
                1 -> {
                    RunFileIO()
                    is_error = if (mobibenchState == 4) 1 else 0
                    if (is_error != 0) {
                        return@apply
                    }
                    intent = Intent(con, DialogActivity::class.java)
                    DialogActivity.check_using_db = 1
                    con!!.startActivity(intent)
                }
                2 -> {
                    RunSqlite()
                    is_error = if (mobibenchState == 4) 1 else 0
                    if (is_error != 0) {
                        return@apply
                    }
                    intent = Intent(con, DialogActivity::class.java)
                    DialogActivity.check_using_db = 1
                    con!!.startActivity(intent)
                }
                3 -> {
                    Log.d(DEBUG_TAG, "[RunSQL] - select_flag" + select_flag)
                    RunCustom()
                    is_error = if (mobibenchState == 4) 1 else 0
                    if (is_error != 0) {
                        return@apply
                    }
                    Log.d(DEBUG_TAG, "[RunSQL] - work done")
                    intent = Intent(con, DialogActivity::class.java)
                    DialogActivity.check_using_db = 1
                    con!!.startActivity(intent)
                }
            }
        }
        DeleteDir(exe_path)
        msg = Message.obtain(mHandler, 444, is_error, 0, null)
        mHandler!!.sendMessage(msg!!)
    }

    internal constructor() {}

    enum class eAccessMode {
        WRITE, RANDOM_WRITE, READ, RANDOM_READ
    }

    enum class eDbMode {
        INSERT, UPDATE, DELETE
    }

    enum class eDbEnable {
        DB_DISABLE, DB_ENABLE
    }

    var cpu_active = 0f
    var cpu_idle = 0f
    var cpu_iowait = 0f
    var cs_total = 0
    var cs_voluntary = 0
    var throughput = 0f
    var tps = 0f
    private var exe_path: String? = null
    fun SetStoragePath(path: String?) {
        data_path = path
        sdcard_2nd_path = determineStorageOptions()
    }

    private fun DeleteDir(path: String?) {
        println("DeleteDir : $path")
        val file = File(path)
        val childFileList = file.listFiles()
        for (childFile in childFileList) {
            if (childFile.isDirectory) {
                DeleteDir(childFile.absolutePath) //하위 디렉토리 루프
            } else {
                childFile.delete() //하위 파일삭제
            }
        }
        file.delete() //root 삭제
    }

    private fun RunMobibench(access_mode: eAccessMode, db_enable: eDbEnable, db_mode: eDbMode) {
        val set = Setting()
        val part = set.get_target_partition()
        var exp_id = 0
        if (db_enable == eDbEnable.DB_DISABLE) {
            if (access_mode == eAccessMode.WRITE) exp_id =
                0 else if (access_mode == eAccessMode.READ) exp_id =
                1 else if (access_mode == eAccessMode.RANDOM_WRITE) exp_id =
                2 else if (access_mode == eAccessMode.RANDOM_READ) exp_id = 3
        } else {
            exp_id =
                if (db_mode == eDbMode.INSERT) 4 else if (db_mode == eDbMode.UPDATE) 5 else 6
        }
        StartThread(exp_id)
        val partition: String?
        partition = if (part == 0) {
            data_path
        } else if (part == 1) {
            Environment.getExternalStorageDirectory().path
        } else {
            sdcard_2nd_path
        }
        var command = "mobibench"
        exe_path = "$partition/mobibench"
        command += " -p $exe_path"
        if (db_enable == eDbEnable.DB_DISABLE) {
            command += if (access_mode == eAccessMode.WRITE || access_mode == eAccessMode.RANDOM_WRITE) {
                " -f " + set.get_filesize_write() * 1024
            } else {
                " -f " + set.get_filesize_read() * 1024
            }
            command += " -r " + set.get_io_size()
            command += " -a " + access_mode.ordinal
            command += " -y " + set.get_file_sync_mode()
            command += " -t " + set.get_thread_num()
        } else {
            command += " -d " + db_mode.ordinal
            command += " -n " + set.get_transaction_num()
            //command += " -n "+1000;
            command += " -j " + set.get_journal_mode()
            command += " -s " + set.get_sql_sync_mode()
        }
        println("mobibench command : $command")
        mobibench_run(command)
        JoinThread()
        SendResult(exp_id)
    }

    external fun mobibench_run(str: String?)
    val mobibenchProgress: Int
        external get
    val mobibenchState: Int
        external get

    fun LoadEngine() {
        System.loadLibrary("mobibench")
    }

    fun printResult() {
        println("mobibench cpu_active : $cpu_active")
        println("mobibench cpu_idle : $cpu_idle")
        println("mobibench cpu_iowait : $cpu_iowait")
        println("mobibench cs_total : $cs_total")
        println("mobibench cs_voluntary : $cs_voluntary")
        println("mobibench throughput : $throughput")
        println("mobibench tps : $tps")
    }

    fun SendResult(result_id: Int) {
        printResult()
        when (select_flag) {
            0 -> DialogActivity.ResultType[result_id] = " ▣ Test: All        "
            1 -> DialogActivity.ResultType[result_id] = " ▣ Test: File IO"
            2 -> DialogActivity.ResultType[result_id] = " ▣ Test: SQLite"
            3 -> DialogActivity.ResultType[result_id] = " ▣ Test: My test"
        }
        DialogActivity.ResultCPU_act[result_id] = String.format("%.0f", cpu_active)
        DialogActivity.ResultCPU_iow[result_id] = String.format("%.0f", cpu_iowait)
        DialogActivity.ResultCPU_idl[result_id] = String.format("%.0f", cpu_idle)
        DialogActivity.ResultCS_tot[result_id] = "" + cs_total
        DialogActivity.ResultCS_vol[result_id] = "" + cs_voluntary
        if (result_id < 4) {    // File IO
            if (result_id < 2) // Sequential
            {
                DialogActivity.ResultThrp[result_id] = String.format("%.0f KB/s", throughput)
            } else  // Random
            {
                val set = Setting()
                DialogActivity.ResultThrp[result_id] =
                    String.format("%.0f IOPS(%dKB)", throughput, set.get_io_size())
            }
        } else {    // SQLite
            DialogActivity.ResultThrp[result_id] = String.format("%.0f TPS", tps)
        }
        DialogActivity.ResultExpName[result_id] = ExpName[result_id]
        DialogActivity.bHasResult[result_id] = 1
    }

    fun RunFileIO() {
        var is_error = 0
        RunMobibench(eAccessMode.WRITE, eDbEnable.DB_DISABLE, eDbMode.INSERT)
        is_error = if (mobibenchState == 4) 1 else 0
        if (is_error != 0) {
            return
        }
        RunMobibench(eAccessMode.READ, eDbEnable.DB_DISABLE, eDbMode.INSERT)
        is_error = if (mobibenchState == 4) 1 else 0
        if (is_error != 0) {
            return
        }
        RunMobibench(eAccessMode.RANDOM_WRITE, eDbEnable.DB_DISABLE, eDbMode.INSERT)
        is_error = if (mobibenchState == 4) 1 else 0
        if (is_error != 0) {
            return
        }
        RunMobibench(eAccessMode.RANDOM_READ, eDbEnable.DB_DISABLE, eDbMode.INSERT)
    }

    fun RunSqlite() {
        var is_error = 0
        RunMobibench(eAccessMode.WRITE, eDbEnable.DB_ENABLE, eDbMode.INSERT)
        is_error = if (mobibenchState == 4) 1 else 0
        if (is_error != 0) {
            return
        }
        RunMobibench(eAccessMode.WRITE, eDbEnable.DB_ENABLE, eDbMode.UPDATE)
        is_error = if (mobibenchState == 4) 1 else 0
        if (is_error != 0) {
            return
        }
        RunMobibench(eAccessMode.WRITE, eDbEnable.DB_ENABLE, eDbMode.DELETE)
    }

    fun RunCustom() {
        var is_error = 0
        val set = Setting()
        if (set.get_seq_write() == true) {
            RunMobibench(eAccessMode.WRITE, eDbEnable.DB_DISABLE, eDbMode.INSERT)
            is_error = if (mobibenchState == 4) 1 else 0
            if (is_error != 0) {
                return
            }
        }
        if (set.get_seq_read() == true) {
            RunMobibench(eAccessMode.READ, eDbEnable.DB_DISABLE, eDbMode.INSERT)
            is_error = if (mobibenchState == 4) 1 else 0
            if (is_error != 0) {
                return
            }
        }
        if (set.get_ran_write() == true) {
            RunMobibench(eAccessMode.RANDOM_WRITE, eDbEnable.DB_DISABLE, eDbMode.INSERT)
            is_error = if (mobibenchState == 4) 1 else 0
            if (is_error != 0) {
                return
            }
        }
        if (set.get_ran_read() == true) {
            RunMobibench(eAccessMode.RANDOM_READ, eDbEnable.DB_DISABLE, eDbMode.INSERT)
            is_error = if (mobibenchState == 4) 1 else 0
            if (is_error != 0) {
                return
            }
        }
        if (set.get_insert() == true) {
            RunMobibench(eAccessMode.WRITE, eDbEnable.DB_ENABLE, eDbMode.INSERT)
            is_error = if (mobibenchState == 4) 1 else 0
            if (is_error != 0) {
                return
            }
        }
        if (set.get_update() == true) {
            RunMobibench(eAccessMode.WRITE, eDbEnable.DB_ENABLE, eDbMode.UPDATE)
            is_error = if (mobibenchState == 4) 1 else 0
            if (is_error != 0) {
                return
            }
        }
        if (set.get_delete() == true) {
            RunMobibench(eAccessMode.WRITE, eDbEnable.DB_ENABLE, eDbMode.DELETE)
        }
    }

    fun setMobiBenchExe(flag: Int) {
        select_flag = flag
        Log.d(DEBUG_TAG, "MBE - select flag is " + select_flag)
    }

    var runflag = false

    inner class ProgThread : Thread() {
        override fun run() {
            var prog = 0
            var stat = 0
            var old_prog = 0
            var old_stat = -1
            msg = Message.obtain(mHandler, 0)
            mHandler!!.sendMessage(msg!!)
            runflag = true
            while (runflag) {
                prog = mobibenchProgress
                stat = mobibenchState
                /*
				 * state
				 * 0 : NONE
				 * 1 : READY
				 * 2 : EXE
				 * 3 : END
				 */if (prog > old_prog || prog == 0 || old_stat != stat) {
                    msg = Message.obtain(mHandler, prog)
                    mHandler!!.sendMessage(msg!!)
                    old_prog = prog
                }
                msg = if (stat < 2) {
                    Message.obtain(mHandler, 999, 0, 0, "Initializing for " + ExpName[exp_id])
                } else {
                    Message.obtain(mHandler, 999, 0, 0, "Executing " + ExpName[exp_id])
                }
                mHandler!!.sendMessage(msg!!)
                old_stat = stat
                try {
                    sleep(10)
                } catch (e: InterruptedException) {
                    // TODO Auto-generated catch block
                    e.printStackTrace()
                }
            }
            if (stat == 4) {
                msg = Message.obtain(mHandler, 0)
                mHandler!!.sendMessage(msg!!)
                msg = Message.obtain(mHandler, 999, 0, 0, ExpName[exp_id] + " exited with error")
                mHandler!!.sendMessage(msg!!)
            } else {
                msg = Message.obtain(mHandler, 100)
                mHandler!!.sendMessage(msg!!)
                msg = Message.obtain(mHandler, 999, 0, 0, ExpName[exp_id] + " done")
                mHandler!!.sendMessage(msg!!)
            }
        }
    }

    var thread: Thread? = null
    fun StartThread(id: Int) {
        exp_id = id
        thread = ProgThread()
        thread!!.start()
    }

    fun JoinThread() {
        runflag = false
        try {
            thread!!.join()
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
    }

    fun stopThread() {
        runflag = false
        // synchronized(this) { this.notify() }
    }

    companion object {
        private var select_flag = 0
        private const val DEBUG_TAG = "progress bar"
        private var intent: Intent? = null
        var data_path: String? = null
        var sdcard_2nd_path: String? = null
        var ExpName = arrayOf(
            "Seq.Write",
            "Seq.read",
            "Rand.Write",
            "Rand.Read",
            "SQLite.Insert",
            "SQLite.Update",
            "SQLite.Delete"
        )
    }

    fun rt_sd():String?{
        return sdcard_2nd_path
    }

}