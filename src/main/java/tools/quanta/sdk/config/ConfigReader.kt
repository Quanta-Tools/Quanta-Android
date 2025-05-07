package tools.quanta.sdk.config

import android.content.Context
import android.content.res.XmlResourceParser
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import java.io.IOException

class ConfigReader(private val context: Context, private val xmlResourceId: Int) {

    private val configData = mutableMapOf<String, Any>()

    init {
        parseConfigXml()
    }

    private fun parseConfigXml() {
        val parser: XmlResourceParser = context.resources.getXml(xmlResourceId)
        try {
            var eventType = parser.eventType
            while (eventType != XmlPullParser.END_DOCUMENT) {
                when (eventType) {
                    XmlPullParser.START_TAG -> {
                        when (parser.name) {
                            "string" -> {
                                val name = parser.getAttributeValue(null, "name")
                                if (name != null && parser.next() == XmlPullParser.TEXT) {
                                    configData[name] = parser.text
                                }
                            }
                            "integer" -> {
                                val name = parser.getAttributeValue(null, "name")
                                if (name != null && parser.next() == XmlPullParser.TEXT) {
                                    configData[name] = parser.text.toIntOrNull() ?: 0
                                }
                            }
                            "boolean" -> {
                                val name = parser.getAttributeValue(null, "name")
                                if (name != null && parser.next() == XmlPullParser.TEXT) {
                                    configData[name] = parser.text.toBooleanStrictOrNull() ?: false
                                }
                            }
                        }
                    }
                }
                eventType = parser.next()
            }
        } catch (e: XmlPullParserException) {
            e.printStackTrace() // Handle parsing errors
        } catch (e: IOException) {
            e.printStackTrace() // Handle I/O errors
        } finally {
            parser.close()
        }
    }

    fun getString(key: String, defaultValue: String? = null): String? {
        return configData[key] as? String ?: defaultValue
    }

    fun getInt(key: String, defaultValue: Int = 0): Int {
        return configData[key] as? Int ?: defaultValue
    }

    fun getBoolean(key: String, defaultValue: Boolean = false): Boolean {
        return configData[key] as? Boolean ?: defaultValue
    }
}
