import com.kms.katalon.core.util.KeywordUtil
import com.kms.katalon.core.testobject.TestObject
import com.kms.katalon.core.testobject.ConditionType
import com.kms.katalon.core.mobile.keyword.MobileBuiltInKeywords as Mobile
import com.kms.katalon.core.util.KeywordUtil



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


// =======================================================
// FUNCTION: Capture ALL image messages by scrolling
// =======================================================
def captureAllImageMessages() {

	def allImages = []
	def lastHash = ""

	while (true) {

		def xml = dumpAndParseXML()

		// Find all image bubbles on the current screen
		def screenImages = []
		def imageNodes = xml.depthFirst().findAll {
			it.@'resource-id'.toString() == "io.senlab.cosmo:id/msg_image"
		}

		imageNodes.each { imgNode ->

			def parent = imgNode.parent()
			def timeNode = parent.depthFirst().find {
				it.@'resource-id'.toString() == "io.senlab.cosmo:id/msg_time"
			}

			def msgTime = timeNode?.@text?.toString()

			// Skip invalid entries
			if (!msgTime || msgTime.trim() == "") return

			screenImages << [
				image: true,
				time: msgTime
			]
		}

		// Merge without duplicates
		screenImages.each { img ->
			def exists = allImages.any { old -> old.time == img.time }
			if (!exists) allImages << img
		}

		// Detect if scrolling reached the top (no new messages)
		def currentHash = allImages.toString().hashCode().toString()
		if (currentHash == lastHash) {
			KeywordUtil.logInfo("📌 No new images after scrolling — reached top.")
			break
		}

		lastHash = currentHash

		// Scroll UP
		KeywordUtil.logInfo("🔼 Scrolling up to load older images...")
		"adb shell input swipe 120 80 120 200".execute().waitFor()
		Thread.sleep(800)
	}

	return allImages
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
	}

	if (!node) {
		KeywordUtil.markFailed("❌ Node not found for ${attributeType}: ${attributeValue}")
		return
	}

	String bounds = node.@bounds.toString()
	KeywordUtil.logInfo("📌 Found bounds for ${attributeValue}: ${bounds}")

	def matcher = bounds =~ /\[(\d+),(\d+)\]\[(\d+),(\d+)\]/
	matcher.find()

	int x1 = matcher.group(1) as int
	int y1 = matcher.group(2) as int
	int x2 = matcher.group(3) as int
	int y2 = matcher.group(4) as int

	int centerX = (x1 + x2) / 2
	int centerY = (y1 + y2) / 2

	KeywordUtil.logInfo("👉 Tapping center at (${centerX}, ${centerY}) for ${attributeValue}")

	"adb shell input tap ${centerX} ${centerY}".execute().waitFor()

	KeywordUtil.logInfo("✅ Successfully tapped ${attributeValue}")
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


// =======================================================
// STEP 3 — Tap the Image Icon (dynamic)
// =======================================================
KeywordUtil.logInfo("=== STEP: Tapping the Image Send Icon ===")

def xmlImage = dumpAndParseXML()

tapNodeCenter(xmlImage, "resource-id", "io.senlab.cosmo:id/sendImage")

Thread.sleep(1000)   // wait 1.5 seconds

// ====================================================
// STEP 4 — Capture Image by tapping btnCam
// ====================================================
KeywordUtil.logInfo("=== Capturing Image ===")

def xmlCamera = dumpAndParseXML()
tapNodeCenter(xmlCamera, "resource-id", "io.senlab.cosmo:id/btnCam")

KeywordUtil.logInfo("📸 Image captured successfully!")

Thread.sleep(1500)   // wait 1.5 seconds
// =========================================
// STEP 5 — Upload Image (tap upload icon)
// =========================================
KeywordUtil.logInfo("=== STEP: Uploading Image to Chat ===")

def xmlUpload = dumpAndParseXML()

tapNodeCenter(xmlUpload, "resource-id", "io.senlab.cosmo:id/image_send")

KeywordUtil.logInfo("📤 Image uploaded successfully!")





Thread.sleep(5000)   // wait 5 seconds


// Dump XML again after upload
def xmlVerify = dumpAndParseXML()

// STEP 6: Get all message containers (image + time pairs) and verify latest message
def messages = []

xmlVerify.depthFirst().findAll {
    it.@'resource-id'.toString() == "io.senlab.cosmo:id/msg_image"
}.each { imgNode ->

    def parent = imgNode.parent()
    def timeNode = parent.depthFirst().find {
        it.@'resource-id'.toString() == "io.senlab.cosmo:id/msg_time"
    }

    messages << [
        image: true,
        time: timeNode?.@text?.toString()
    ]
}

if (messages.isEmpty()) {
    KeywordUtil.markFailed("❌ No image messages found!")
} else {
    def latest = messages.last()
    KeywordUtil.logInfo("✅ Latest image message found at time: ${latest.time}")
}


KeywordUtil.logInfo("🔍 Capturing ALL image messages...")

def allImages = captureAllImageMessages()

KeywordUtil.logInfo("📸 TOTAL images found: ${allImages.size()}")

allImages.each { img ->
	KeywordUtil.logInfo(" → Image @ ${img.time}")
}

