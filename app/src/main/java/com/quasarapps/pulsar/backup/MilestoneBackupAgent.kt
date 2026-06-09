package com.quasarapps.pulsar.backup

import android.app.backup.BackupAgent
import android.app.backup.BackupDataInput
import android.app.backup.BackupDataOutput
import android.app.backup.FullBackupDataOutput
import android.os.ParcelFileDescriptor
import com.quasarapps.pulsar.data.SettingsRepository
import kotlinx.coroutines.runBlocking

/**
 * Gates Android Auto Backup on the user's "Back up milestones" setting. The app uses full (Auto)
 * Backup configured declaratively; there's no runtime toggle for it, so this agent overrides
 * [onFullBackup] to run the default backup only when the setting is enabled — otherwise milestone
 * data (including titles) never leaves the device, for cloud and device-to-device alike. Restore is
 * always honored. Key/value backup is unused, so [onBackup]/[onRestore] are intentional no-ops.
 *
 * Backup can't be exercised in CI (needs a real device + `adb shell bmgr`); the gating decision lives
 * in [SettingsRepository.backupEnabled] (unit-tested) and this agent is the thin wiring around it.
 */
class MilestoneBackupAgent : BackupAgent() {

    override fun onFullBackup(data: FullBackupDataOutput) {
        if (backupEnabled()) {
            super.onFullBackup(data)
        }
        // else: contribute nothing this pass, so the milestone/settings stores aren't uploaded.
    }

    // Read synchronously (the agent has no coroutine scope). On a read failure, fail *closed* — skip
    // the backup — so an error can never upload data after the user opted out. A skipped pass is
    // recoverable on the next cycle; an erroneous upload isn't.
    private fun backupEnabled(): Boolean = runCatching {
        runBlocking { SettingsRepository(applicationContext).snapshot().backupEnabled }
    }.getOrDefault(false)

    override fun onBackup(
        oldState: ParcelFileDescriptor?,
        data: BackupDataOutput,
        newState: ParcelFileDescriptor,
    ) {
        // No key/value backup — this app uses full (Auto) Backup only.
    }

    override fun onRestore(
        data: BackupDataInput,
        appVersionCode: Int,
        newState: ParcelFileDescriptor,
    ) {
        // No key/value restore.
    }
}
