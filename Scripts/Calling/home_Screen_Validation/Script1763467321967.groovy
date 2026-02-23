import com.kms.katalon.core.util.KeywordUtil
import java.nio.file.*

//// STEP 1: Wake the smartwatch
//def wakeCommand = 'adb shell am start -n io.senlab.cosmo/io.senlab.cosmo.MainActivity'
//def process = wakeCommand.execute()
//process.waitFor()
//KeywordUtil.logInfo("✅ Screen wake command executed.")
//Thread.sleep(2000)

// STEP 2: Dump UI hierarchy on watch
KeywordUtil.logInfo("📸 Capturing current UI hierarchy...")
def dumpCmd = 'adb shell uiautomator dump /sdcard/window_dump.xml'
def dumpProcess = dumpCmd.execute()
dumpProcess.waitFor()
Thread.sleep(1000)

// STEP 3: Try to pull XML to D drive first
def xmlPath = Paths.get("D:\\window_dump.xml")
def pullCmd = 'adb pull /sdcard/window_dump.xml D:\\window_dump.xml'
def pullProcess = pullCmd.execute()
pullProcess.waitFor()
Thread.sleep(1000)

// STEP 4: Retry to C drive if D pull failed
if (!Files.exists(xmlPath)) {
    KeywordUtil.logInfo("⚠️ Retrying pull to C drive...")
    def pullCmdAlt = 'adb pull /sdcard/window_dump.xml "C:\\Users\\ahmad\\Katalon Studio\\window_dump.xml"'
    def pullProcessAlt = pullCmdAlt.execute()
    pullProcessAlt.waitFor()
    xmlPath = Paths.get("C:\\Users\\ahmad\\Katalon Studio\\window_dump.xml")
    Thread.sleep(1000)
}

// STEP 5: Final check
if (!Files.exists(xmlPath)) {
    KeywordUtil.markFailedAndStop("❌ XML dump not found — both pulls failed.")
}

// STEP 6: Read and validate
def xmlContent = Files.readString(xmlPath)

if (xmlContent.contains('io.senlab.cosmo:id/clock') && xmlContent.contains('io.senlab.cosmo:id/calendar')) {
    KeywordUtil.logInfo("✅ Home screen detected — clock and date visible.")
} else {
    KeywordUtil.markFailedAndStop("❌ Home screen not detected — device may still be locked or on another screen.")
}

KeywordUtil.logInfo("🎯 Screen validation completed successfully.")
