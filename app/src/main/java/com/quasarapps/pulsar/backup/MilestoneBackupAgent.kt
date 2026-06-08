package com.quasarapps.pulsar.backup

import android.app.backup.BackupAgent
import android.app.backup.BackupDataInput
import android.app.backup.BackupDataOutput
import android.app.backup.FullBackupDataOutput
import android.os.ParcelFileDescriptor
import com.quasarapps.pulsar.data.SettingsRepository
import kotlinx.coroutines.runBlocking

/**
 * Gates Android Auto Backup on the user's "Back up milestones" setting (Settings → Backup & privacy).
 *
 * The app uses full (Auto) Backup, configured declaratively via `android:fullBackupContent` /
 * `android:dataExtractionRules`. There's no runtime API to toggle that per-user, so this agent
 * overrides [onFullBackup]: it runs the default full backup (which honors those XML rules) only when
 * the setting is enabled, and contributes nothing otherwise — so milestone data, including titles,
 * never leaves the device when the user opts out (cloud backup and device-to-device transfer alike).
 *
 * Restore is always honored, so re-enabling — or restoring data that was backed up while enabled —
 * still works. Key/value backup isn't used; [onBackup]/[onRestore] are required overrides and are
 * intentionally no-ops.
 *
 * NOTE: backup can't be exercised in CI (it needs a real device + `adb shell bmgr`); the gating
 * decision lives in [SettingsRepository.backupEnabled] (unit-tested) and this agent is the thin
 * wiring around it.
 */
class MilestoneBackupAgent : BackupAgent() {

    override fun onFullBackup(data: FullBackupDataOutput) {
        if (backupEnabled()) {
            super.onFullBackup(data)
        }
        // else: contribute nothing this pass, so the milestone/settings stores aren't uploaded.
    }

    // Read the setting synchronously here (the agent has no coroutine scope). On a read failure
    // (I/O / corruption) fail *closed* — skip the backup — so an error can never upload milestone
    // data after the user explicitly opted out. A skipped pass is recoverable (the next cycle
    // retries once the read succeeds); an erroneous upload of opted-out data isn't.
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
