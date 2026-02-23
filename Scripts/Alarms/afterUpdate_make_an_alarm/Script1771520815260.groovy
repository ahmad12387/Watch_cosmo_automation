import com.kms.katalon.core.util.KeywordUtil
import groovy.json.JsonOutput
import groovy.xml.XmlSlurper
import com.kms.katalon.core.util.KeywordUtil
import com.kms.katalon.core.testobject.TestObject
import com.kms.katalon.core.testobject.ConditionType
import cosmo.fota.FotaAlarmHelper
import groovy.xml.XmlSlurper
import cosmo.fota.FotaAlarmHelper

// ===================================================================
// HELPER — Dump UI XML from watch and parse it (STABLE VERSION)
// ===================================================================
def dumpAndParseXML() {

    // Ensure folder exists
    File dir = new File("C:\\cosmo_xml")
    if (!dir.exists()) {
        dir.mkdirs()
    }

    // Dump UI hierarchy on device
    "adb shell uiautomator dump /sdcard/ui.xml".execute().waitFor()

    // Pull XML to local fixed path
    "adb pull /sdcard/ui.xml C:\\cosmo_xml\\ui.xml".execute().waitFor()

    KeywordUtil.logInfo("📄 UI XML dumped & pulled successfully")

    File xmlFile = new File("C:\\cosmo_xml\\ui.xml")
    return new XmlSlurper().parse(xmlFile)
}

// ========== STEP 1: Wake up the smartwatch ==========
def wakeCommand = 'adb shell am start -n io.senlab.cosmo/io.senlab.cosmo.MainActivity'
def process1 = wakeCommand.execute()
process1.waitFor()
KeywordUtil.logInfo("✅ Screen awakened successfully.")

// Small delay to allow UI to load
Thread.sleep(2000)

// ========== STEP 2: Swipe left Till it reaches Alarm App==========
def swipeCommand = 'adb shell input swipe 200 120 40 120'
def process2 = swipeCommand.execute()
Thread.sleep(2000)
swipeCommand.execute()
Thread.sleep(2000)
swipeCommand.execute()
Thread.sleep(2000)
process2.waitFor()
KeywordUtil.logInfo("✅ Swipe left performed successfully.")

// ========== STEP 3: Tap Alarm App (Top-Left) ==========
def tapAlarmCommand = 'adb shell input tap 62 72'
tapAlarmCommand.execute()
Thread.sleep(2000)

KeywordUtil.logInfo("✅ Alarm app opened successfully.")


// =======================================================
// FUNCTION: Extract Alarms From Screen
// =======================================================

def extractAlarms(parsedXml) {

	def alarms = []

	// Each alarm row container
	parsedXml.'**'.findAll {
		it.@'resource-id' == 'io.senlab.cosmo:id/alarms'
	}.each { listView ->

		listView.children().each { row ->

			def messageNode = row.depthFirst().find {
				it.@'resource-id' == 'io.senlab.cosmo:id/message'
			}

			def repeatNode = row.depthFirst().find {
				it.@'resource-id' == 'io.senlab.cosmo:id/repeat'
			}

			def timeNode = row.depthFirst().find {
				it.@'resource-id' == 'io.senlab.cosmo:id/time'
			}

			def activeNode = row.depthFirst().find {
				it.@'resource-id' == 'io.senlab.cosmo:id/active'
			}

			if (messageNode && timeNode) {

				alarms << [
					label  : messageNode.@text.toString(),
					time   : timeNode.@text.toString(),
					repeat : repeatNode?.@text?.toString(),
					active : activeNode?.@enabled?.toString()   // true / false
				]
			}
		}
	}

	return alarms
}



// =======================================================
// MAIN LOGIC
// =======================================================

def allAlarms = []
def previousCount = -1

while (true) {

	def parsedXml = dumpAndParseXML()
	def currentAlarms = extractAlarms(parsedXml)

	currentAlarms.each { alarm ->
		if (!allAlarms.contains(alarm)) {
			allAlarms << alarm
		}
	}

	if (allAlarms.size() == previousCount) {
		break
	}

	previousCount = allAlarms.size()

	"adb shell input swipe 120 200 120 60".execute().waitFor()
	Thread.sleep(1500)
}

// =======================================================
// LOAD & COMPARE
// =======================================================

import cosmo.fota.FotaAlarmHelper

List beforeAlarms = FotaAlarmHelper.loadPreFotaAlarms()

KeywordUtil.logInfo("📂 Loaded ${beforeAlarms.size()} alarms from Pre-FOTA JSON")

FotaAlarmHelper.compareAlarms(beforeAlarms, allAlarms)









// ===============================
// PRINT BEFORE FOTA DATA
// ===============================
KeywordUtil.logInfo("========== BEFORE FOTA ALARMS ==========")

beforeAlarms.eachWithIndex { alarm, index ->
	KeywordUtil.logInfo("BEFORE [${index+1}] -> Label: ${alarm.label}, Time: ${alarm.time}, Repeat: ${alarm.repeat}, Active: ${alarm.active}")
}

// ===============================
// PRINT AFTER FOTA DATA
// ===============================
KeywordUtil.logInfo("========== AFTER FOTA ALARMS ==========")

allAlarms.eachWithIndex { alarm, index ->
	KeywordUtil.logInfo("AFTER  [${index+1}] -> Label: ${alarm.label}, Time: ${alarm.time}, Repeat: ${alarm.repeat}, Active: ${alarm.active}")
}