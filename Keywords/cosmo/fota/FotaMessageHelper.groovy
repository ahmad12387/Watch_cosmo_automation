package cosmo.fota
import static com.kms.katalon.core.checkpoint.CheckpointFactory.findCheckpoint
import static com.kms.katalon.core.testcase.TestCaseFactory.findTestCase
import static com.kms.katalon.core.testdata.TestDataFactory.findTestData
import static com.kms.katalon.core.testobject.ObjectRepository.findTestObject
import static com.kms.katalon.core.testobject.ObjectRepository.findWindowsObject
import com.kms.katalon.core.util.KeywordUtil
import groovy.json.JsonOutput
import groovy.json.JsonSlurper
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

public class FotaMessageHelper {
	static String FILE_PATH = "C:\\cosmo_fota_data\\messages_pre_fota.json"

	// 🧠 SAVE messages BEFORE FOTA
	static void savePreFotaMessages(List messages) {

		File folder = new File("C:\\cosmo_fota_data")
		if (!folder.exists()) {
			folder.mkdirs()
		}

		File file = new File(FILE_PATH)
		file.text = JsonOutput.prettyPrint(JsonOutput.toJson(messages))

		KeywordUtil.logInfo("🧸 Saved ${messages.size()} messages BEFORE FOTA")
	}

	// 🧠 LOAD messages AFTER FOTA
	static List loadPreFotaMessages() {

		File file = new File(FILE_PATH)

		if (!file.exists()) {
			KeywordUtil.markFailed("❌ Pre-FOTA file not found")
			return []
		}

		return new JsonSlurper().parse(file)
	}
}
