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
    fun set_seq_write(value: Boolean) {
        seq_write = value
        return
    }

    fun get_seq_write(): Boolean {
        return seq_write
    }

    fun set_seq_read(value: Boolean) {
        seq_read = value
        return
    }

    fun get_seq_read(): Boolean {
        return seq_read
    }

    fun set_ran_write(value: Boolean) {
        ran_write = value
        return
    }

    fun get_ran_write(): Boolean {
        return ran_write
    }

    fun set_ran_read(value: Boolean) {
        ran_read = value
        return
    }

    fun get_ran_read(): Boolean {
        return ran_read
    }

    fun set_insert(value: Boolean) {
        insert = value
        return
    }

    fun get_insert(): Boolean {
        return insert
    }

    fun set_update(value: Boolean) {
        update = value
        return
    }

    fun get_update(): Boolean {
        return update
    }

    fun set_delete(value: Boolean) {
        delete = value
        return
    }

    fun get_delete(): Boolean {
        return delete
    }

    fun set_cb_count(count: Int) {
        cb_count = count
        return
    }

    fun get_cb_count(): Int {
        return cb_count
    }

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
        private var seq_write = false
        private var seq_read = false
        private var ran_write = false
        private var ran_read = false
        private var insert = false
        private var update = false
        private var delete = false
        private var cb_count = 0
    }
}