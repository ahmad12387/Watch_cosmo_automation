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
// FUNCTION: Tap Camera Icon
// ===========================
def tapCameraIcon() {

	"adb shell uiautomator dump /sdcard/ui.xml".execute().waitFor()
	"adb pull /sdcard/ui.xml C:\\cosmo_xml\\ui.xml".execute().waitFor()

	KeywordUtil.logInfo("📄 XML pulled successfully")

	File xmlFile = new File("C:\\cosmo_xml\\ui.xml")
	String xmlText = xmlFile.text

	def matcher = xmlText =~ /bounds="\[4,129\]\[120,236\]"/

	if(!matcher) {
		KeywordUtil.markFailed("❌ Camera icon NOT FOUND")
		return
	}

	int centerX = 62
	int centerY = 182

	KeywordUtil.logInfo("📌 Tapping Camera Icon at (${centerX}, ${centerY})")

	"adb shell input tap ${centerX} ${centerY}".execute().waitFor()

	KeywordUtil.logInfo("✅ Camera app opened successfully")
}

// Call function
tapCameraIcon()


// ===================================================================
// STEP 2: Open Photos / Gallery
// ===================================================================
KeywordUtil.logInfo("=== STEP: Opening Photos Gallery ===")

def xmlGalleryBefore = dumpAndParseXML()

tapNodeCenter(xmlGalleryBefore, "resource-id", "io.senlab.cosmo:id/btnGallery")

Thread.sleep(1500)

//Step 3 — Function to Get All Gallery Images
def getGalleryImages(xml) {
	
		def images = xml.depthFirst().findAll {
			it.@'resource-id'.toString() == "io.senlab.cosmo:id/image_small"
		}
	
		def imageBounds = []
	
		images.each {
			imageBounds.add(it.@bounds.toString())
		}
	
		return imageBounds
	}
	
//Step 4 — Save Images Before FOTA
	KeywordUtil.logInfo("=== Collecting Gallery Images Before FOTA ===")
	
	def xmlBefore = dumpAndParseXML()
	
	def imagesBefore = getGalleryImages(xmlBefore)
	
	KeywordUtil.logInfo("Images found before FOTA: " + imagesBefore)
	
	File file = new File("C:\\cosmo_xml\\gallery_before.txt")
	
	file.text = imagesBefore.join("\n")
	
////Step 5 — After FOTA Collect Images Again
//	KeywordUtil.logInfo("=== Collecting Gallery Images After FOTA ===")
//	
//	def xmlAfter = dumpAndParseXML()
//	
//	def imagesAfter = getGalleryImages(xmlAfter)
//	
//	KeywordUtil.logInfo("Images found after FOTA: " + imagesAfter)
//// step 6  — Load Saved Images
//	File file = new File("C:\\cosmo_xml\\gallery_before.txt")
//	
//	def savedImages = file.readLines()