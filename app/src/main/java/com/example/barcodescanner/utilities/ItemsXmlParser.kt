package com.example.barcodescanner.utilities

import android.util.Xml
import com.example.barcodescanner.data.Item
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import java.io.IOException
import java.io.InputStream


const val CODE = "code"
const val BARCODE2 = "barcode2"
const val BARCODE1 = "barcode1"
const val NAME = "name"
const val VENDOR = "vendor"
const val COST = "cost"
const val PRICE = "price"

const val ROOT_ENTRY = "Root"
const val ITEM_ENTRY = "item"

class ItemsXmlParser {
    
    //omit namespaces
    private val ns: String? = null

    // parse an xml file and return list of items
    @Throws(XmlPullParserException::class, IOException::class)
    fun parse(inputStream: InputStream): List<Item> {
        inputStream.use {
            val parser: XmlPullParser = Xml.newPullParser()
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
            parser.setInput(it, null)
            parser.nextTag()
            return readFeed(parser)
        }
    }
    //returns a List containing the items it extracted from the xml(feed).
    @Throws(XmlPullParserException::class, IOException::class)
    private fun readFeed(parser: XmlPullParser): List<Item> {
        val items = mutableListOf<Item>()
        parser.require(XmlPullParser.START_TAG, ns, ROOT_ENTRY)
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.eventType != XmlPullParser.START_TAG) {
                continue
            }
            // Starts by looking for the item tag
            if (parser.name == ITEM_ENTRY) {
                items.add(readEntry(parser))
            } else {
                skip(parser)
            }
        }
        return items
    }
    // extract data of an item
    @Throws(XmlPullParserException::class, IOException::class)
    private fun readEntry(parser: XmlPullParser): Item {
        parser.require(XmlPullParser.START_TAG, ns, ITEM_ENTRY)
        var code = ""
        var barcode1 :String? = null
        var barcode2 :String? = null
        var name: String? = null
        var vendor: String? = null
        var cost:Double? = null
        var price: Double? = null
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.eventType != XmlPullParser.START_TAG) {
                continue
            }
            when (parser.name) {
                CODE -> code = readCode(parser)
                BARCODE1 -> barcode1 = readBarcode1(parser)
                BARCODE2 -> barcode2 = readBarcode2(parser)
                NAME -> name = readName(parser)
                VENDOR -> vendor = readVendor(parser)
                COST -> cost = readCost(parser)
                PRICE -> price = readPrice(parser)
                else -> skip(parser)
            }
        }
        return Item(code,barcode1,barcode2, name,vendor,cost,price)
    }


    @Throws(XmlPullParserException::class, IOException::class)
    private fun readCode(parser: XmlPullParser): String {
        parser.require(XmlPullParser.START_TAG, ns, CODE)
        val code = readText(parser)
        parser.require(XmlPullParser.END_TAG, ns, CODE)
        return code
    }

    @Throws(XmlPullParserException::class, IOException::class)
    private fun readBarcode1(parser: XmlPullParser): String {
        parser.require(XmlPullParser.START_TAG, ns, BARCODE1)
        val barcode1 = readText(parser)
        parser.require(XmlPullParser.END_TAG, ns, BARCODE1)
        return barcode1
    }

    @Throws(XmlPullParserException::class, IOException::class)
    private fun readBarcode2(parser: XmlPullParser): String {
        parser.require(XmlPullParser.START_TAG, ns, BARCODE2)
        val barcode2 = readText(parser)
        parser.require(XmlPullParser.END_TAG, ns, BARCODE2)
        return barcode2
    }

    @Throws(XmlPullParserException::class, IOException::class)
    private fun readName(parser: XmlPullParser): String {
        parser.require(XmlPullParser.START_TAG,ns,NAME)
        val name = readText(parser)
        parser.require(XmlPullParser.END_TAG,ns,NAME)
        return name
    }

    @Throws(XmlPullParserException::class, IOException::class)
    private fun readVendor(parser: XmlPullParser): String {
        parser.require(XmlPullParser.START_TAG,ns,VENDOR)
        val vendor = readText(parser)
        parser.require(XmlPullParser.END_TAG,ns,VENDOR)
        return vendor
    }

    @Throws(XmlPullParserException::class, IOException::class)
    private fun readCost(parser: XmlPullParser): Double? {
        parser.require(XmlPullParser.START_TAG,ns,COST)
        val cost = readText(parser).toDoubleOrNull()
        parser.require(XmlPullParser.END_TAG,ns,COST)
        return cost
    }

    @Throws(XmlPullParserException::class, IOException::class)
    private fun readPrice(parser: XmlPullParser): Double? {
        parser.require(XmlPullParser.START_TAG,ns,PRICE)
        val price = readText(parser).toDoubleOrNull()
        parser.require(XmlPullParser.END_TAG,ns,PRICE)
        return price
    }






    // extracts data of required tags
    @Throws(XmlPullParserException::class, IOException::class)
    private fun readText(parser: XmlPullParser): String {
        var result = ""
        if (parser.next() == XmlPullParser.TEXT) {
            result = parser.text
            parser.nextTag()
        }
        return result
    }
    // skips all not required elements tags
    @Throws(XmlPullParserException::class, IOException::class)
    private fun skip(parser: XmlPullParser) {
        if (parser.eventType != XmlPullParser.START_TAG) {
            throw IllegalStateException()
        }
        var depth = 1
        while (depth != 0) {
            when (parser.next()) {
                XmlPullParser.END_TAG -> depth--
                XmlPullParser.START_TAG -> depth++
            }
        }
    }
}