package com.itheamc.waypointgenerator

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import com.itheamc.waypointgenerator.ui.theme.WayPointGeneratorTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!Python.isStarted()) {
            Python.start(AndroidPlatform(this))
        }

        enableEdgeToEdge()
        setContent {
            WayPointGeneratorTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Calculate(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun Calculate(modifier: Modifier = Modifier) {

    val scope = rememberCoroutineScope()

    var area by remember { mutableDoubleStateOf(0.0) }
    var distance by remember { mutableDoubleStateOf(0.0) }
    var contains by remember { mutableStateOf(false) }
    var error: String? by remember { mutableStateOf(null) }


    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        if (error != null) {
            Text(text = error!!)
        } else {
            Text(text = "Area: $area")
            Text(text = "Distance: $distance")
            Text(text = "Contains: $contains")
        }

        Button(
            onClick = {
                error = null
                area = 0.0
                distance = 0.0
                contains = false

                scope.launch {
                    shapelyExample(
                        onArea = { area = it },
                        onDistance = { distance = it },
                        onContains = { contains = it },
                        onError = { error = it }
                    )
                }
            }
        ) {
            Text(text = "Calculate")
        }

    }
}

/**
 * Demonstrates basic usage of the Shapely library for geometric operations using JPython.
 *
 * This function performs the following operations:
 * 1. Creates a Point object.
 * 2. Creates a Polygon object.
 * 3. Calculates the distance between the Point and the Polygon.
 * 4. Calculates the area of the Polygon.
 * 5. Checks if the Point is contained within the Polygon.
 *
 * The results of these operations are passed to the provided callback functions.
 *
 * @param onArea Callback function to receive the calculated area of the polygon.
 *               Takes a Double representing the area.
 * @param onDistance Callback function to receive the calculated distance between the point and polygon.
 *                   Takes a Double representing the distance.
 * @param onContains Callback function to receive a boolean indicating if the point is within the polygon.
 *                   Takes a Boolean, true if the point is within the polygon, false otherwise.
 *
 * @throws Exception if any error happens during the python execution.
 */
private fun shapelyExample(
    onArea: (Double) -> Unit,
    onDistance: (Double) -> Unit,
    onContains: (Boolean) -> Unit,
    onError: (String) -> Unit
) {
    try {
        val python = Python.getInstance()
        val shapelyGeometry = python.getModule("shapely.geometry")

        // Create Point
        val point = shapelyGeometry.callAttr("Point", 10, 8)

        // Create Polygon
        val polygonPoints = arrayOf(
            arrayOf(0, 0),
            arrayOf(0, 2),
            arrayOf(2, 2),
            arrayOf(12, 20)
        )
        val polygon = shapelyGeometry.callAttr("Polygon", polygonPoints)

        // Calculate and print distance
        val distance = point.callAttr("distance", polygon).toJava(Double::class.java)
        onDistance(distance)

        // Calculate and print area
        val area = polygon["area"]?.toJava(Double::class.java)
        area?.let { onArea(it) }

        // Check and print if point is within polygon
        val contains = polygon.callAttr("contains", point).toJava(Boolean::class.java)
        onContains(contains)
    } catch (e: Exception) {
        onError(e.message ?: "Unknown error")
    }
}