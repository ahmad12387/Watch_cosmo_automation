package smartWatch

import com.kms.katalon.core.annotation.Keyword
import com.kms.katalon.core.util.KeywordUtil

public class SmartWatchCallingPage extends SmartWatchBase {

	// Call by contact index (dynamic)
	@Keyword
	def callContactByIndex(int index) {
		int startY = 60
		int gap = 55
		int finalY = startY + ((index - 1) * gap)

		KeywordUtil.logInfo("Calling contact at index $index (Y = $finalY)")
		tap(120, finalY)
		Thread.sleep(1500)
	}

	@Keyword
	def callContactByName(String name) {

		KeywordUtil.logInfo("Searching contact: $name")

		// Always wait for contacts screen first
		waitForContactsToLoad()

		runAdb("uiautomator dump /sdcard/window_dump.xml")
		runAdb("pull /sdcard/window_dump.xml window_dump.xml")

		String xml = new File("window_dump.xml").text
		String cleanXml = xml.replaceAll("\\s+", " ")

		// Very flexible match
		def matcher = cleanXml =~ /text="(?i)$name"[^>]*?bounds="\[(\d+),(\d+)\]\[(\d+),(\d+)\]"/

		if (!matcher.find()) {
			KeywordUtil.logInfo(cleanXml)  // debug help
			KeywordUtil.markFailed("❌ '$name' NOT found in XML.")
			return
		}

		int x = ((matcher[0][1] as int) + (matcher[0][3] as int)) / 2
		int y = ((matcher[0][2] as int) + (matcher[0][4] as int)) / 2

		KeywordUtil.logInfo("📍 Found '$name' at center = ($x,$y)")
		tap(x, y)
		Thread.sleep(1500)
	}
	@Keyword
	def dumpContactsXML() {

		KeywordUtil.logInfo("📄 Dumping UI XML...")

		// MUST run inside shell
		runAdb("uiautomator dump /sdcard/window_dump.xml", true)

		// MUST run outside shell
		runAdb("pull /sdcard/window_dump.xml window_dump.xml", false)

		Thread.sleep(800)  // allow file write + copy

		return new File("window_dump.xml").text
	}


	@Keyword
	def waitForContactsToLoad(int timeoutSec = 8) {

		KeywordUtil.logInfo("⏳ Waiting for Contacts screen to load...")

		long end = System.currentTimeMillis() + timeoutSec * 1000

		while (System.currentTimeMillis() < end) {

			String xml = dumpContactsXML()

			// Debug print
			KeywordUtil.logInfo("🔍 XML Preview: " + xml.substring(0, Math.min(300, xml.length())))

			if (xml.contains("Contacts") || xml.contains("contactName")) {
				KeywordUtil.logInfo("✅ Contacts loaded.")
				return
			}

			Thread.sleep(400)
		}

		KeywordUtil.markFailedAndStop("❌ Contacts did NOT load in time!")
	}



	// Validate call connection
	@Keyword
	def validateCallConnected() {
		KeywordUtil.logInfo("Validating call connection...")
		// Add logic here
	}


	// End call
	@Keyword
	def endCall() {
		KeywordUtil.logInfo("Ending call...")
		tap(120, 200)
	}

	// Wait for call to end logic
	@Keyword
	def waitForCallToEnd(int timeoutSec = 10) {

		KeywordUtil.logInfo("⏳ Waiting for call to end...")

		long end = System.currentTimeMillis() + timeoutSec * 1000

		while (System.currentTimeMillis() < end) {

			String xml = dumpContactsXML()

			// Debug snippet
			KeywordUtil.logInfo("🔍 Call-End XML Preview: " +
					xml.substring(0, Math.min(200, xml.length()))
					)

			boolean isBackToContacts =
					xml.contains('resource-id="io.senlab.cosmo:id/title"') &&
					xml.contains('Contacts') &&
					xml.contains('resource-id="io.senlab.cosmo:id/contactName"')

			if (isBackToContacts) {
				KeywordUtil.logInfo("✅ Call ended — Contacts screen detected.")
				return
			}

			Thread.sleep(500)
		}

		KeywordUtil.markFailedAndStop("❌ Call did NOT end — Contacts screen not detected within timeout.")
	}
}
