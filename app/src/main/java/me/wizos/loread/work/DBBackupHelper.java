package me.wizos.loread.work;

import android.app.backup.BackupAgentHelper;
import android.app.backup.FileBackupHelper;

import me.wizos.loread.db.CoreDB;

public class DBBackupHelper extends BackupAgentHelper {
    @Override
    public void onCreate() {
        super.onCreate();
        // Allocate a helper and add it to the backup agent void onCreate(){
        FileBackupHelper helper = new FileBackupHelper (this, "../databases/"+ CoreDB.DATABASE_NAME);
        addHelper ("me.wizos.loread", helper) ;
    }
}
