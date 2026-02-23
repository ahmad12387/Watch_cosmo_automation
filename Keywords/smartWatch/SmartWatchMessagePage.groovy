package smartWatch

import static com.kms.katalon.core.checkpoint.CheckpointFactory.findCheckpoint
import static com.kms.katalon.core.testcase.TestCaseFactory.findTestCase
import static com.kms.katalon.core.testdata.TestDataFactory.findTestData
import static com.kms.katalon.core.testobject.ObjectRepository.findTestObject
import static com.kms.katalon.core.testobject.ObjectRepository.findWindowsObject
import com.kms.katalon.core.util.KeywordUtil

import com.kms.katalon.core.annotation.Keyword
import com.kms.katalon.core.checkpoint.Checkpoint
import com.kms.katalon.core.cucumber.keyword.CucumberBuiltinKeywords as CucumberKW
import com.kms.katalon.core.mobile.keyword.MobileBuiltInKeywords as Mobile
import com.kms.katalon.core.model.FailureHandling
import com.kms.katalon.core.testcase.TestCase
import com.kms.katalon.core.testdata.TestData
import com.kms.katalon.core.testobject.TestObject
import com.kms.katalon.core.webservice.keyword.WSBuiltInKeywords as WS
import com.kms.katalon.core.webui.keyword.WebUiBuiltInKeywords as WebUI
import com.kms.katalon.core.windows.keyword.WindowsBuiltinKeywords as Windows

import internal.GlobalVariable

public class SmartWatchMessagePage {
	/**
	 * Collect ONLY incoming text messages from chat screen
	 * @param xml parsed XML
	 * @return Set<String> incoming messages (text|time)
	 */
	static List<String> extractIncomingText(def xml) {

		List<String> incomingMessages = []

		xml.depthFirst().each { node ->

			// Step 1: Identify message text node
			if (
			node.@class == 'android.widget.TextView' &&
					node.@'resource-id' == 'io.senlab.cosmo:id/msg_text'
					) {

				String text = node.@text.toString().trim()
				if (!text) return

					// Step 2: Detect LEFT-aligned (incoming) message
					String bounds = node.@bounds.toString()
				def matcher = bounds =~ /\[(\d+),(\d+)\]\[(\d+),(\d+)\]/
				if (!matcher.find()) return

					int x1 = matcher.group(1) as int
				int x2 = matcher.group(3) as int
				int centerX = (x1 + x2) / 2

				if (centerX < 120) {

					// 🔹 Step 3: Extract timestamp from same message container
					String time = ""

					def timeNode = node.parent().depthFirst().find {
						it.@'resource-id' == 'io.senlab.cosmo:id/msg_time'
					}

					if (timeNode) {
						time = timeNode.@text.toString().trim()
					}

					// 🔹 Step 4: Log + store unique message
					KeywordUtil.logInfo("Incoming msg → ${text} @ ${time}")

					int y1 = matcher.group(2) as int
					incomingMessages.add("${text} @ ${time} | y=${y1}")
				}
			}
		}

		return incomingMessages
	}
}
