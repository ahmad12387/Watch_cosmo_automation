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


// ===================================================================
// STEP 3: Tap Photos Button (middle bottom icon)
// ===================================================================
KeywordUtil.logInfo("=== STEP 3: Tapping Photos Button ===")

def xmlPhotos = dumpAndParseXML()

tapNodeCenter(xmlPhotos, "resource-id", "io.senlab.cosmo:id/sendImage")

KeywordUtil.logInfo("📸 Photos button tapped successfully!")


// ===================================================================
// STEP 4 — Tap Video Button (btnToVideo)
// ===================================================================
KeywordUtil.logInfo("=== Tapping Video Button ===")

def xmlVideo = dumpAndParseXML()

tapNodeCenter(xmlVideo, "resource-id", "io.senlab.cosmo:id/btnToVideo")

KeywordUtil.logInfo("🎥 Video button tapped successfully!")

Thread.sleep(4000)


// ===================================================================
// STEP 5— START VIDEO RECORDING
// ===================================================================
KeywordUtil.logInfo("=== STEP: Starting Video Recording ===")

def xmlStart = dumpAndParseXML()

// Try locating by resource-id first
def startNode = xmlStart.depthFirst().find {
    it.@'resource-id'.toString() == "io.senlab.cosmo:id/btnCam"
}

if (startNode) {
    tapNodeCenter(xmlStart, "resource-id", "io.senlab.cosmo:id/btnCam")
    KeywordUtil.logInfo("⏺ Video recording STARTED successfully!")
} else {
    // Fallback to bounds if resource-id missing
    KeywordUtil.logInfo("⚠️ Resource-id not found, using fallback bounds...")

    tapNodeCenter(xmlStart, "bounds", "[89,174][151,236]")
    KeywordUtil.logInfo("⏺ Video recording STARTED using fallback bounds!")
}

Thread.sleep(20000)


// ===================================================================
// STEP 6— TAP UPLOAD VIDEO BUTTON (io.senlab.cosmo:id/video_send)
// ===================================================================
KeywordUtil.logInfo("=== STEP: Uploading Video ===")

def xmlUpload = dumpAndParseXML()

// Try by resource-id FIRST (most reliable)
def uploadNode = xmlUpload.depthFirst().find {
    it.@'resource-id'.toString() == "io.senlab.cosmo:id/video_send"
}

if (uploadNode) {
    tapNodeCenter(xmlUpload, "resource-id", "io.senlab.cosmo:id/video_send")
    KeywordUtil.logInfo("📤 Video upload button tapped successfully!")
} else {
    KeywordUtil.logInfo("⚠️ video_send not found, using fallback bounds...")

    tapNodeCenter(xmlUpload, "bounds", "[89,163][151,225]")
    KeywordUtil.logInfo("📤 Video upload triggered using bounds fallback!")
}

Thread.sleep(10000)

// ===================================================================
// HELPER: Check if timestamp belongs to the video (vertical proximity)
// ===================================================================
boolean isTimeNearVideo(String timeBounds, String videoBounds) {

	def t = (timeBounds =~ /\[(\d+),(\d+)\]\[(\d+),(\d+)\]/)
	def v = (videoBounds =~ /\[(\d+),(\d+)\]\[(\d+),(\d+)\]/)

	if (!t.find() || !v.find()) return false

	int timeY = t.group(2) as int
	int videoBottomY = v.group(4) as int

	// Timestamp appears just BELOW the message bubble
	return Math.abs(timeY - videoBottomY) < 50
}

// ===================================================================
// STEP 7— VERIFY LATEST VIDEO MESSAGE
// ===================================================================
KeywordUtil.logInfo("=== STEP: Verifying Latest Uploaded Video ===")

def xmlVerify = dumpAndParseXML()

// Find all video thumbnails
def videoMessages = xmlVerify.depthFirst().findAll {
    it.@'resource-id'.toString() == "io.senlab.cosmo:id/msg_video"
}

if (videoMessages.isEmpty()) {
    KeywordUtil.markFailed("❌ No video messages found on screen!")
    return
}

KeywordUtil.logInfo("🎥 Total video messages found: ${videoMessages.size()}")

// Sort by Y2 (bottom-most message is the latest)
def sortedVideos = videoMessages.sort { node ->
    def bounds = node.@bounds.toString()
    def m = (bounds =~ /\[(\d+),(\d+)\]\[(\d+),(\d+)\]/)
    m.find()
    return m.group(4) as int   // Y2 coordinate
}

def latestVideo = sortedVideos.last()
KeywordUtil.logInfo("🎬 Latest video bounds: ${latestVideo.@bounds}")

// Find associated timestamp
def timeNode = xmlVerify.depthFirst().find { t ->
    t.@'resource-id'.toString() == "io.senlab.cosmo:id/msg_time" &&
    isTimeNearVideo(t.@bounds.toString(), latestVideo.@bounds.toString())
}

if (timeNode) {
    KeywordUtil.logInfo("⏰ Latest video timestamp: ${timeNode.@text}")
} else {
    KeywordUtil.logInfo("⚠️ Timestamp not found but video exists.")
}

KeywordUtil.logInfo("✅ LATEST VIDEO UPLOAD VERIFIED SUCCESSFULLY!")






// ===================================================================
// STEP 8 — SCROLL & COLLECT ALL VIDEO MESSAGES
// ===================================================================
KeywordUtil.logInfo("=== STEP 8: Collecting ALL previous video messages ===")

// Global list for all video messages
def allVideos = []

// Helper: extract video + timestamp from XML
def extractVideoMessages = { xmlObj ->

	def list = []

	xmlObj.depthFirst().findAll { node ->
		node.@'resource-id'.toString() == "io.senlab.cosmo:id/msg_video"
	}.each { videoNode ->

		def parent = videoNode.parent()

		def msgTimeNode = parent.depthFirst().find {
			it.@'resource-id'.toString() == "io.senlab.cosmo:id/msg_time"
		}

		list << [
			bounds: videoNode.@bounds.toString(),
			time  : msgTimeNode?.@text?.toString()
		]
	}
	return list
}

// Add the latest message first
allVideos << [
	bounds: latestVideo.@bounds.toString(),
	time  : timeNode?.@text?.toString()
]

KeywordUtil.logInfo("📌 Added latest video to list")


// ===============================================================
// START SCROLL LOOP
// ===============================================================

int maxScrolls = 12
int previousCount = -1

for (int s = 1; s <= maxScrolls; s++) {

	KeywordUtil.logInfo("🔄 Scroll iteration ${s}")

	// Swipe UP to reveal older messages
	"adb shell input swipe 120 80 120 200".execute().waitFor()
	Thread.sleep(500)

	def xmlScroll = dumpAndParseXML()
	def foundVideos = extractVideoMessages(xmlScroll)

	foundVideos.each { vid ->

		if (!vid.time || vid.time.trim() == "") {
			KeywordUtil.logInfo("⏭ Skipped video without timestamp: ${vid.bounds}")
			return
		}

		// Only add unique messages
		if (!allVideos.contains(vid)) {
			allVideos << vid
			KeywordUtil.logInfo("📥 Found earlier video → Bounds=${vid.bounds} | Time=${vid.time}")
		}
	}

	// Stop when we have no new messages
	if (allVideos.size() == previousCount) {
		KeywordUtil.logInfo("📌 No new messages detected — reached top.")
		break
	}

	previousCount = allVideos.size()
}




// ===================================================================
// STEP 9 — DISPLAY ALL VIDEO MESSAGES
// ===================================================================

if (allVideos.isEmpty()) {
	KeywordUtil.markFailed("❌ No video messages found in chat!")
	return
}

KeywordUtil.logInfo("===============================================")
KeywordUtil.logInfo("🎥 TOTAL VIDEO MESSAGES FOUND: ${allVideos.size()}")
KeywordUtil.logInfo("===============================================")

allVideos.eachWithIndex { v, i ->
	KeywordUtil.logInfo("(${i+1}) ▶ Bounds: ${v.bounds} | Time: ${v.time}")
}

KeywordUtil.logInfo("✅ Video history extraction completed successfully!")

