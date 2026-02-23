import com.kms.katalon.core.util.KeywordUtil
import com.kms.katalon.core.testobject.TestObject
import com.kms.katalon.core.testobject.ConditionType
import groovy.xml.XmlSlurper

// ===================================================================
// HELPER — Dump UI XML from watch and parse it (STABLE VERSION)
// ===================================================================
// ===================================================================
// FUNCTION: Dump XML + Return Parsed Object
// ===================================================================
def dumpAndParseXML() {
	"adb shell uiautomator dump /sdcard/uidump.xml".execute().waitFor()
	"adb pull /sdcard/uidump.xml C:\\cosmo_xml\\uidump.xml".execute().waitFor()

	KeywordUtil.logInfo("📄 XML dumped & pulled successfully")
	File xmlFile = new File("C:\\cosmo_xml\\uidump.xml")
	return new XmlSlurper().parse(xmlFile)
}

//GET BOUNDS IN REAL TIME//
def getAppIconBoundsByIndex(xml, int index) {
	
		def icons = xml.depthFirst().findAll {
			it.name() == 'node' &&
			it.@class.toString() == 'android.widget.RelativeLayout' &&
			it.@clickable.toString() == 'true'
		}
	
		if (icons.size() <= index) {
			KeywordUtil.markFailed("❌ App icon at index ${index} not found")
			return null
		}
	
		return icons[index].@bounds.toString()
	}
	
	
// ===================================================================
// FUNCTION: Tap center of a node by resource-id or text match
// ===================================================================
def tapNodeCenter(xml, String attributeType, String attributeValue) {
    def node

    if (attributeType == "resource-id") {
        node = xml.depthFirst().find { it.@'resource-id'.toString() == attributeValue }
    } else if (attributeType == "text") {
        node = xml.depthFirst().find { it.@text.toString() == attributeValue }
    } else if (attributeType == "bounds") {
        node = xml.depthFirst().find { it.@bounds.toString() == attributeValue }
    }

    if (!node) {
        KeywordUtil.markFailed("❌ Node not found for ${attributeType}: ${attributeValue}")
        return
    }

    String bounds = node.@bounds.toString()
    def matcher = bounds =~ /\[(\d+),(\d+)\]\[(\d+),(\d+)\]/
    matcher.find()

    int x1 = matcher.group(1) as int
    int y1 = matcher.group(2) as int
    int x2 = matcher.group(3) as int
    int y2 = matcher.group(4) as int

    int centerX = (x1 + x2) / 2
    int centerY = (y1 + y2) / 2

    "adb shell input tap ${centerX} ${centerY}".execute().waitFor()
    KeywordUtil.logInfo("✅ Successfully tapped bounds element at (${centerX}, ${centerY})")
}

def getSettingsMenuBoundsByIndex(xml, int index) {
	
		def items = xml.depthFirst().findAll {
			it.name() == 'node' &&
			it.@class.toString() == 'android.widget.LinearLayout' &&
			it.@clickable.toString() == 'true' &&
			it.@'resource-id'.toString().startsWith('io.senlab.cosmo:id/pref')
		}
	
		if (items.size() <= index) {
			KeywordUtil.markFailed("❌ Settings item at index ${index} not found")
			return null
		}
	
		return items[index].@bounds.toString()
	}
	
	// ===================================================================
	// FUNCTION: Get bounds of an item on Device Settings screen by index
	// Screen order:
	// 0 → Display
	// 1 → Sounds and vibration
	// 2 → Time
	// ===================================================================
	def getDeviceSettingsBoundsByIndex(xml, int index) {
	
		// Find all clickable LinearLayout items under Device Settings
		def items = xml.depthFirst().findAll {
			it.name() == 'node' &&
			it.@class.toString() == 'android.widget.LinearLayout' &&
			it.@clickable.toString() == 'true' &&
			it.@'resource-id'.toString().startsWith('io.senlab.cosmo:id/prefs')
		}
	
		// Fail test if requested index does not exist
		if (items.size() <= index) {
			KeywordUtil.markFailed("❌ Device settings item at index ${index} not found")
			return null
		}
	
		// Return bounds of the requested item
		return items[index].@bounds.toString()
	}
	
		
// ========== STEP 1: Wake up the smartwatch ==========
def wakeCommand = 'adb shell am start -n io.senlab.cosmo/io.senlab.cosmo.MainActivity'
def process1 = wakeCommand.execute()
process1.waitFor()
KeywordUtil.logInfo("✅ Screen awakened successfully.")

// Small delay to allow UI to load
Thread.sleep(2000)

// ========== STEP 2: Swipe left Till it reaches Settings App==========
def swipeCommand = 'adb shell input swipe 200 120 40 120'
def process2 = swipeCommand.execute()
Thread.sleep(3000)
swipeCommand.execute()
Thread.sleep(3000)
swipeCommand.execute()
Thread.sleep(3000)
swipeCommand.execute()
Thread.sleep(3000)
process2.waitFor()
KeywordUtil.logInfo("✅ Swipe left performed successfully.")

// ========== STEP 3: Dump XML ==========
def xml = dumpAndParseXML()

// ========== STEP 4: Click on Settings ==========
String settingsBounds = getAppIconBoundsByIndex(xml, 3)
tapNodeCenter(xml, "bounds", settingsBounds)

// Wait for Settings screen to load
Thread.sleep(2000)

// ========== STEP 5: Refresh XML (NO def here) ==========
xml = dumpAndParseXML()

// ========== STEP 6: Click on Device ==========
String deviceBounds = getSettingsMenuBoundsByIndex(xml, 3)
tapNodeCenter(xml, "bounds", deviceBounds)

// ========== STEP 7: Refresh XML on Device Settings screen ==========
xml = dumpAndParseXML()

// ========== STEP 8: Click on Time ==========
String timeBounds = getDeviceSettingsBoundsByIndex(xml, 2)
tapNodeCenter(xml, "bounds", timeBounds)

//// ========== STEP 9: Refresh XML on Time screen ==========
//xml = dumpAndParseXML()
//
//// ========== STEP 10: Click on Time zone ==========
//tapNodeCenter(xml, "resource-id", "io.senlab.cosmo:id/prefsTimezone")

//// ========== STEP 11: Refresh XML on Time Zone screen ==========
//xml = dumpAndParseXML()
//
//// ========== STEP 12: Disable Auto-select (switch to manual time zone) ==========
//tapNodeCenter(xml, "resource-id", "io.senlab.cosmo:id/auto_select")
//
//// ========== STEP 13: Refresh XML after disabling Auto-select ==========
//xml = dumpAndParseXML()
//
//def autoSelectNode = xml.depthFirst().find {
//	it.@'resource-id'.toString() == 'io.senlab.cosmo:id/auto_select'
//}
//
//if (!autoSelectNode) {
//	KeywordUtil.markFailed("❌ Auto-select toggle not found")
//} else {
//	// OPTIONAL: log full node state for debugging
//	KeywordUtil.logInfo("ℹ️ Auto-select node attributes: " + autoSelectNode.attributes().toString())
//
//	// Since toggle is OFF, manual timezone screen should be enabled next
//	KeywordUtil.logInfo("✅ Auto-select toggle tapped successfully (manual mode expected)")
//}
//
//// ========== STEP 14: Refresh XML on Time Zone selection screen ==========
//xml = dumpAndParseXML()
//
//// ========== STEP 15: Select first time zone (GMT-12:00) ==========
//tapNodeCenter(xml, "text", "(GMT-12:00)")
//
//// ========== STEP 16: Go back to previous screen ==========
//CustomKeywords.'smartWatch.SmartWatchNavigation.swipeBackToPreviousScreen'()

// ========== STEP 17: Verify selected Time Zone on Time screen ==========
xml = dumpAndParseXML()

def selectedTimezoneNode = xml.depthFirst().find {
	it.@'resource-id'.toString() == 'io.senlab.cosmo:id/settingTimezone'
}

if (!selectedTimezoneNode) {
	KeywordUtil.markFailed("❌ Time Zone value not found on Time screen")
} else if (selectedTimezoneNode.@text.toString() == 'GMT-12:00') {
	KeywordUtil.logInfo("✅ Time Zone successfully set to GMT-12:00")
} else {
	KeywordUtil.markFailed(
		"❌ Incorrect Time Zone selected. Found: " +
		selectedTimezoneNode.@text.toString()
	)
}
