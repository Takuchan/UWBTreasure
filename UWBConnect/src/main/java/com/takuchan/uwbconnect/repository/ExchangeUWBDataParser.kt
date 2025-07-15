package com.takuchan.uwbconnect.repository

import com.takuchan.uwbconnect.data.AnchorData



class ExchangeUWBDataParser {
    private val anchorDataMap = mutableMapOf<Int, AnchorData>()
    private val regex = Regex("""TWR\[(\d+)]\.(\w+)\s*:\s*(-?\d+\.?\d*)""")

    fun parseLine(line: String) {
        val dataPart = line.substringAfter("INFO :").trim()
        regex.find(dataPart)?.let { matchResult ->
            val (idStr, property, valueStr) = matchResult.destructured
            val id = idStr.toIntOrNull() ?: return
            val anchorData = anchorDataMap.getOrPut(id) { AnchorData(id = id) }
            when (property) {
                "nLos" -> anchorData.nLos = valueStr.toIntOrNull()
                "distance" -> anchorData.distance = valueStr.toIntOrNull()
                "aoa_azimuth" -> anchorData.azimuth = valueStr.toFloatOrNull()
                "aoa_elevation" -> anchorData.elevation = valueStr.toFloatOrNull()
            }
        }
    }

    fun getTrilaterationData(requiredIds: List<Int> = listOf(0, 1, 2)): List<AnchorData>? {
        val candidateAnchors = mutableListOf<AnchorData>()
        for (id in requiredIds) {
            val anchor = anchorDataMap[id]
            if (anchor == null || !anchor.isCompleteForTrilateration()) {
                return null
            }
            candidateAnchors.add(anchor)
        }
        requiredIds.forEach { id -> anchorDataMap.remove(id) }
        return candidateAnchors
    }
}