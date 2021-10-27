package esos.MobiBench

class Setting {
    fun set_filesize_read(value: Int) {
        filesize_read = value
        return
    }

    fun get_filesize_read(): Int {
        return filesize_read
    }

    fun set_filesize_write(value: Int) {
        filesize_write = value
        return
    }

    fun get_filesize_write(): Int {
        return filesize_write
    }

    fun set_io_size(value: Int) {
        io_size = value
        return
    }

    fun get_io_size(): Int {
        var ret = 0
        when (io_size) {
            0 -> ret = 4
            1 -> ret = 8
            2 -> ret = 16
            3 -> ret = 128
            4 -> ret = 512
        }
        return ret
    }

    fun set_target_partition(value: Int) {
        target_partition = value
        return
    }

    fun get_target_partition(): Int {
        return target_partition
    }

    fun set_file_sync_mode(value: Int) {
        file_sync_mode = value
        return
    }

    fun get_file_sync_mode(): Int {
        return file_sync_mode
    }

    fun set_thread_num(value: Int) {
        thread_num = value
        return
    }

    fun get_thread_num(): Int {
        return thread_num
    }

    fun set_transaction_num(value: Int) {
        transaction_num = value
        return
    }

    fun get_transaction_num(): Int {
        return transaction_num
    }

    fun set_journal_mode(value: Int) {
        journal_mode = value
        return
    }

    fun get_journal_mode(): Int {
        return journal_mode
    }

    fun set_sql_sync_mode(value: Int) {
        sql_sync_mode = value
        return
    }

    fun get_sql_sync_mode(): Int {
        return sql_sync_mode
    }

    /* Check box variable control function */

    companion object {
        private var target_partition = 0
        private var thread_num = 0
        private var filesize_read = 0
        private var filesize_write = 0
        private var io_size = 0
        private var file_sync_mode = 0
        private var transaction_num = 0
        private var sql_sync_mode = 0
        private var journal_mode = 0
    }
}