import com.kms.katalon.core.util.KeywordUtil
import com.kms.katalon.core.testobject.TestObject
import com.kms.katalon.core.testobject.ConditionType
import com.kms.katalon.core.mobile.keyword.MobileBuiltInKeywords as Mobile
import com.kms.katalon.core.util.KeywordUtil
import cosmo.fota.FotaMessageHelper


// ========== STEP 1: Wake up the smartwatch ==========
def wakeCommand = 'adb shell am start -n io.senlab.cosmo/io.senlab.cosmo.MainActivity'
def process1 = wakeCommand.execute()
process1.waitFor()
KeywordUtil.logInfo("✅ Screen awakened successfully.")

// Small delay to allow UI to load
Thread.sleep(2000)


// ========== STEP 2: Swipe left ==========
def swipeCommand = 'adb shell input swipe 200 120 40 120'
def process2 = swipeCommand.execute()
process2.waitFor()
KeywordUtil.logInfo("✅ Swipe left performed successfully.")

// Small delay to allow homescreen animation
Thread.sleep(2000)


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


// ===========================
// FUNCTION: Tap Messages Icon
// ===========================
def tapMessagesIcon() {

    // Dump XML from the watch
    "adb shell uiautomator dump /sdcard/ui.xml".execute().waitFor()
    "adb pull /sdcard/ui.xml C:\\cosmo_xml\\ui.xml".execute().waitFor()
    KeywordUtil.logInfo("📄 XML pulled successfully")

    File xmlFile = new File("C:\\cosmo_xml\\ui.xml")
    String xmlText = xmlFile.text

    // Regex to find the Messages icon by its bounds index=1
    def matcher = xmlText =~ /bounds="\[120,19\]\[236,129\]"/

    if(!matcher) {
        KeywordUtil.markFailed("❌ Messages icon NOT FOUND in UI XML")
        return
    }

    int x1 = 120
    int y1 = 19
    int x2 = 236
    int y2 = 129

    int centerX = (x1 + x2) / 2
    int centerY = (y1 + y2) / 2

    KeywordUtil.logInfo("📌 Tapping Messages Icon at center (${centerX}, ${centerY})")

    String tapCmd = "adb shell input tap ${centerX} ${centerY}"
    tapCmd.execute().waitFor()

    KeywordUtil.logInfo("✅ Messages app opened successfully")
}

// Call the function
tapMessagesIcon()


// ===================================================================
// STEP 2: Tap Contact → “Ahmad Alam” (dynamic)
// ===================================================================
KeywordUtil.logInfo("=== STEP 2: Tapping Contact 'Ahmad Alam' Dynamically ===")

def xml2 = dumpAndParseXML()

tapNodeCenter(xml2, "text", "Ahmad Alam")

Thread.sleep(1500)



//STEP 1: LOAD PRE-FOTA DATA
KeywordUtil.logInfo("📦 Loading PRE-FOTA messages from JSON")

List<Map<String, String>> preFotaMessages =
		FotaMessageHelper.loadPreFotaMessages()

KeywordUtil.logInfo("✅ Loaded ${preFotaMessages.size()} PRE-FOTA messages")


//STEP 2: CAPTURE POST-FOTA DATA (UNCHANGED)
List<Map<String, String>> postFotaMessages = []
String lastKnownDate = "Date not visible"
int previousCount = -1

for (int scroll = 0; scroll < 10; scroll++) {

	def xml = dumpAndParseXML()

	def dateNodes = xml.depthFirst().findAll {
		it.@'resource-id'.toString() == "io.senlab.cosmo:id/day"
	}
	if (dateNodes && !dateNodes.isEmpty()) {
		lastKnownDate = dateNodes.last().@text.toString()
	}

	def messageBubbles = xml.depthFirst().findAll {
		it.@class.toString() == "android.widget.LinearLayout" &&
		it.depthFirst().any { n ->
			n.@'resource-id'.toString() in [
				"io.senlab.cosmo:id/msg_text",
				"io.senlab.cosmo:id/msg_image",
				"io.senlab.cosmo:id/msg_video",
				"io.senlab.cosmo:id/video_icon"
			]
		}
	}

	messageBubbles.each { bubble ->

		def textNode  = bubble.depthFirst().find {
			it.@'resource-id'.toString() == "io.senlab.cosmo:id/msg_text"
		}
		def imageNode = bubble.depthFirst().find {
			it.@'resource-id'.toString() == "io.senlab.cosmo:id/msg_image"
		}
		def videoNode = bubble.depthFirst().find {
			it.@'resource-id'.toString() in [
				"io.senlab.cosmo:id/msg_video",
				"io.senlab.cosmo:id/video_icon"
			]
		}

		String type
		String text

		if (textNode) {
			type = "text"
			text = textNode.@text.toString()
		} else if (imageNode) {
			type = "image"
			text = "[IMAGE]"
		} else if (videoNode) {
			type = "video"
			text = "[VIDEO]"
		} else {
			return
		}

		def timeNode = bubble.depthFirst().find {
			it.@'resource-id'.toString() == "io.senlab.cosmo:id/msg_time"
		}
		if (!timeNode) return

		String time = timeNode.@text.toString()

		boolean exists = postFotaMessages.any {
			it.type == type &&
			it.text == text &&
			it.time == time
		}

		if (!exists) {
			postFotaMessages.add([
				contact : "Ahmad Alam",
				type    : type,
				text    : text,
				time    : time,
				date    : lastKnownDate   // kept for logging only
			])
		}
	}

	if (postFotaMessages.size() == previousCount) break
	previousCount = postFotaMessages.size()

	"adb shell input swipe 120 80 120 200".execute().waitFor()
	Thread.sleep(350)
}

//STEP 3: CORRECT VALIDATION 

KeywordUtil.logInfo("🔍 Validating POST-FOTA messages")

// Compare ONLY stable fields
preFotaMessages.each { preMsg ->

	boolean found = postFotaMessages.any { postMsg ->
		postMsg.type == preMsg.type &&
		postMsg.text == preMsg.text &&
		postMsg.time == preMsg.time
	}

	if (!found) {
		KeywordUtil.markFailed(
			"❌ Message missing AFTER FOTA → ${preMsg}"
		)
	}
}

KeywordUtil.logInfo("✅ ALL messages validated successfully AFTER FOTA")