package org.technoserve.farmcollector.ui.screens

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import org.technoserve.farmcollector.R
import org.technoserve.farmcollector.hasLocationPermission
import org.technoserve.farmcollector.map.LocationHelper
import org.technoserve.farmcollector.map.MapScreen
import org.technoserve.farmcollector.map.MapViewModel
import org.technoserve.farmcollector.ui.composes.AreaDialog
import org.technoserve.farmcollector.ui.composes.ConfirmDialog
import org.technoserve.farmcollector.utils.convertSize

/**
 * This screen helps you to capture and visualize farm polygon.
 * When capturing, You are able to start, add point, clear map or remove a point on the map
 */

const val CALCULATED_AREA_OPTION = "CALCULATED_AREA"
const val ENTERED_AREA_OPTION = "ENTERED_AREA"

@OptIn(ExperimentalLayoutApi::class)
@SuppressLint("MissingPermission")
@Composable
fun SetPolygon(
    navController: NavController,
    viewModel: MapViewModel,
) {
    val context = LocalContext.current as Activity
    var coordinates by remember { mutableStateOf(listOf<Pair<Double, Double>>()) }
    var accuracyArray by remember { mutableStateOf(listOf<Float>()) }
    var isCapturingCoordinates by remember { mutableStateOf(false) }
    var hasPointsOnMap by remember { mutableStateOf(false) }
    var showConfirmDialog = remember { mutableStateOf(false) }
    val showClearMapDialog = remember { mutableStateOf(false) }
    //  Getting farm details such as polygon or single pair of lat and long if shared from farm list
    val farmData =
        navController.previousBackStackEntry?.arguments?.getParcelable<ParcelableFarmData>("farmData")
    // cast farmData string to Farm object
    val farmInfo = farmData?.farm
    var accuracy by remember { mutableStateOf("") }
    var viewSelectFarm by remember { mutableStateOf(false) }
    val sharedPref = context.getSharedPreferences("FarmCollector", Context.MODE_PRIVATE)
    val locationHelper = LocationHelper(context)
    val showAlertDialog = remember { mutableStateOf(false) }
    val mapViewModel: MapViewModel = viewModel()
    val showLocationDialog = remember { mutableStateOf(false) }


    LaunchedEffect(Unit) {
        mapViewModel.clearCoordinates()

        //  Get the accuracyArrayData from savedStateHandle
        val accuracyArrayData =
            navController.currentBackStackEntry?.savedStateHandle?.get<List<Float?>>("accuracyArray")

        // If the accuracyArrayData exists, clear it
        accuracyArrayData?.let {
            navController.currentBackStackEntry?.savedStateHandle?.set(
                "accuracyArray",
                emptyList<Float?>()
            )
        }
        if (!isLocationEnabled(context)) {
            showLocationDialog.value = true
        }
    }

    // Define string constants
    val titleText = stringResource(id = R.string.enable_location_services)
    val messageText = stringResource(id = R.string.location_services_required_message)
    val enableButtonText = stringResource(id = R.string.enable)

    // Dialog to prompt user to enable location services
    if (showLocationDialog.value) {
        AlertDialog(
            onDismissRequest = { showLocationDialog.value = false },
            title = { Text(titleText) },
            text = { Text(messageText) },
            confirmButton = {
                Button(onClick = {
                    showLocationDialog.value = false
                    promptEnableLocation(context)
                }) {
                    Text(enableButtonText)
                }
            },
            dismissButton = {
                Button(onClick = {
                    showLocationDialog.value = false
                    Toast
                        .makeText(
                            context,
                            R.string.location_permission_denied_message,
                            Toast.LENGTH_SHORT,
                        ).show()
                }) {
                    Text(stringResource(id = R.string.cancel))
                }
            },
            containerColor = MaterialTheme.colorScheme.background,
            tonalElevation = 6.dp
        )
    }

    // First, get initial location if needed
    if (!isCapturingCoordinates && farmInfo == null) {
        locationHelper.getCurrentLocation { location ->
            location?.let {
                accuracy = it.accuracy.toString()
                if (viewModel.state.value.clusterItems.isEmpty()) {
                    viewModel.addCoordinate(it.latitude, it.longitude)
                }
            } ?: run {
                Toast.makeText(
                    context,
                    context.getString(R.string.can_not_get_location),
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

// Then start continuous location updates
    locationHelper.requestLocationUpdates( onLocationUpdate = { location ->
        location?.let {
            accuracy = it.accuracy.toString()
        } ?: run {
            Toast.makeText(
                context,
                context.getString(R.string.location_update_failed),
                Toast.LENGTH_SHORT
            ).show()
        }
    }
    )
    // Display coordinates of a farm on map
    if (farmInfo != null && !isCapturingCoordinates && !viewSelectFarm) {
        viewModel.clearCoordinates()
        if (farmInfo.coordinates?.isNotEmpty() == true) {
            viewModel.addCoordinates(farmInfo.coordinates!!)
        } else if (farmInfo.latitude.isNotEmpty() && farmInfo.longitude.isNotEmpty()) {
            viewModel.addMarker(Pair(farmInfo.latitude.toDouble(), farmInfo.longitude.toDouble()))
        }
        viewSelectFarm = true
    }
    val enteredArea = sharedPref.getString("plot_size", "0.0")?.toDoubleOrNull() ?: 0.0
    val selectedUnit = sharedPref.getString("selectedUnit", "Ha") ?: "Ha"
    val enteredAreaConverted = convertSize(enteredArea, selectedUnit)
    val calculatedArea = mapViewModel.calculateArea(coordinates)
    var showSaveButton by remember { mutableStateOf(false) } // To track if save button should be shown
    val showInvalidPolygonDialog = remember { mutableStateOf(false) }

    // Confirm dialog to finalize the polygon
    if (showConfirmDialog.value) {
        ConfirmDialog(
            title = stringResource(id = R.string.set_polygon),
            message = stringResource(id = R.string.confirm_set_polygon),
            showConfirmDialog,
            onProceedFn = {
                if (coordinates.size >= 3) {
                    mapViewModel.clearCoordinates()
                    mapViewModel.addCoordinates(coordinates)

                    if (coordinates.isNotEmpty()) {
                        // update map camera position
                        val coordinate = coordinates.first()
                        coordinates = coordinates + coordinate
                        viewModel.addMarker(coordinate)
                        // add camera position
                        viewModel.addCoordinate(
                            coordinates.first().first,
                            coordinates.first().second,
                        )
                    }
                    showSaveButton = true
                } else {
                    showAlertDialog.value = true
                }
                showConfirmDialog.value = false
            },
            onCancelFn = {
                isCapturingCoordinates = true
                showConfirmDialog.value = false
            }
        )
    }


    // Alert dialog for insufficient coordinates
    if (showAlertDialog.value) {
        AlertDialog(
            onDismissRequest = {
                showAlertDialog.value = false
            },
            title = {
                Text(text = stringResource(id = R.string.insufficient_coordinates_title))
            },
            text = {
                Text(text = stringResource(id = R.string.insufficient_coordinates_message))
            },
            confirmButton = {
                Button(
                    onClick = {
                        showAlertDialog.value = false
                    },
                ) {
                    Text(text = stringResource(id = R.string.ok))
                    isCapturingCoordinates = true
                    showConfirmDialog.value = false
                    mapViewModel.clearCoordinates()
                }
            },
            containerColor = MaterialTheme.colorScheme.background,
            tonalElevation = 6.dp
        )
    }

    // Display AreaDialog
    AreaDialog(
        showDialog = mapViewModel.showDialog.collectAsState().value,
        onDismiss = { mapViewModel.dismissDialog() },
        onConfirm = { chosenArea ->
            val chosenSize =
                when (chosenArea) {
                    CALCULATED_AREA_OPTION -> calculatedArea.toString()
                    ENTERED_AREA_OPTION -> enteredAreaConverted.toString()
                    else -> throw IllegalArgumentException("Unknown area option: $chosenArea")
                }
            val truncatedSize = truncateToDecimalPlaces(formatInput(chosenSize), 9)
            sharedPref.edit().putString("plot_size", truncatedSize).apply()
            if (sharedPref.contains("selectedUnit")) {
                sharedPref.edit().remove("selectedUnit").apply()
            }
            coordinates = listOf()
            mapViewModel.clearCoordinates()
            navController.navigateUp()
        },
        calculatedArea = calculatedArea,
        enteredArea = enteredAreaConverted
    )

    // Confirm clear map
    if (showClearMapDialog.value) {
        ConfirmDialog(
            stringResource(id = R.string.set_polygon),
            stringResource(id = R.string.clear_map),
            showClearMapDialog,
            fun() {
                coordinates = listOf()
                accuracy = ""
                viewModel.clearCoordinates()
                showClearMapDialog.value = false
            },
            onCancelFn = { showClearMapDialog.value = false }
        )
    }

    val isDarkTheme = isSystemInDarkTheme()
    val textColor = MaterialTheme.colorScheme.onBackground

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
    ) {
        Column(
            modifier =
            Modifier
                .fillMaxWidth()
                .fillMaxHeight(
                    if (viewSelectFarm) {
                        0.65f
                    } else if (accuracy.isNotEmpty()) {
                        .87f
                    } else {
                        .93f
                    },
                ),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Google map
            MapScreen(
                state = viewModel.state.value,
                setupClusterManager = viewModel::setupClusterManager,
                calculateZoneViewCenter = viewModel::calculateZoneLatLngBounds,
                onMapTypeChange = viewModel::onMapTypeChange,
            )
        }
        Column(
            modifier =
            Modifier
                .background(MaterialTheme.colorScheme.background)
                .fillMaxWidth()
                .fillMaxHeight(),
        ) {
            if (!viewSelectFarm && accuracy.isNotEmpty()) {
                Column(
                    modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(40.dp)
                        .padding(horizontal = 14.dp),
                ) {
                    Text(
                        modifier = Modifier.padding(horizontal = 2.dp),
                        color = Color.Black,
                        text = stringResource(id = R.string.accuracy) + ": $accuracy m",
                    )
                }
            }

            FlowRow(
                modifier =
                Modifier
                    .fillMaxWidth()
                    .fillMaxHeight()
                    .padding(bottom = 10.dp),
                horizontalArrangement = if (viewSelectFarm) Arrangement.Center else Arrangement.Start,
            ) {
                // Hiding some buttons depending on page usage. Viewing verse setting farm polygon
                if (viewSelectFarm) {
                    Row {
                        if (farmInfo != null) {
                            Column(
                                modifier =
                                Modifier
                                    .background(MaterialTheme.colorScheme.background)
                                    .padding(5.dp),
                            ) {
                                Text(
                                    text = stringResource(id = R.string.farm_info),
                                    style =
                                    MaterialTheme.typography.bodySmall.copy(
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                    ),
                                    modifier = Modifier.padding(5.dp),
                                )
                                Column(
                                    content = { },
                                    modifier =
                                    Modifier
                                        .width(200.dp)
                                        .background(Color.Black)
                                        .height(2.dp),
                                )
                                Text(
                                    text = "${stringResource(id = R.string.farm_name)}: ${farmInfo.farmerName}",
                                    style = MaterialTheme.typography.bodyMedium.copy(color = textColor),
                                    modifier = Modifier.padding(top = 5.dp),
                                )
                                Text(
                                    text = "${stringResource(id = R.string.member_id)}: ${farmInfo.memberId.ifEmpty { "N/A" }}",
                                    style = MaterialTheme.typography.bodyMedium.copy(color = textColor),
                                )
                                Text(
                                    text = "${stringResource(id = R.string.village)}: ${farmInfo.village}",
                                    style = MaterialTheme.typography.bodyMedium.copy(color = textColor),
                                )
                                Text(
                                    text = "${stringResource(id = R.string.district)}: ${farmInfo.district}",
                                    style = MaterialTheme.typography.bodyMedium.copy(color = textColor),
                                )
                                Text(
                                    text = "${stringResource(id = R.string.latitude)}: ${farmInfo.latitude}",
                                    style = MaterialTheme.typography.bodyMedium.copy(color = textColor)
                                )
                                Text(
                                    text = "${stringResource(id = R.string.longitude)}: ${farmInfo.longitude}",
                                    style = MaterialTheme.typography.bodyMedium.copy(color = textColor)
                                )
                                Text(
                                    text = "${stringResource(id = R.string.size)}: ${
                                        truncateToDecimalPlaces(
                                            formatInput(farmInfo.size.toString()),
                                            9
                                        )
                                    } ${
                                        stringResource(
                                            id = R.string.ha,
                                        )
                                    }",
                                    style = MaterialTheme.typography.bodyMedium.copy(color = textColor),
                                )
                            }
                        }
                    }
                    Row {
                        Button(
                            shape = RoundedCornerShape(10.dp),
                            modifier =
                            Modifier
                                .width(120.dp)
                                .fillMaxWidth(0.23f),
                            onClick = {
                                viewModel.clearCoordinates()
                                navController.navigateUp()
                            },
                        ) {
                            Text(text = stringResource(id = R.string.close))
                        }
                        Button(
                            shape = RoundedCornerShape(10.dp),
                            modifier =
                            Modifier
                                .width(150.dp)
                                .fillMaxWidth(0.23f)
                                .padding(start = 10.dp),
                            onClick = {
                                navController.navigate("updateFarm/${farmInfo?.id}")
                            },
                        ) {
                            Text(text = stringResource(id = R.string.update))
                        }
                    }
                } else {

                    // "Start" button - visible only when not capturing coordinates and the "Finish" button hasn't been clicked
                    if (!isCapturingCoordinates && !showConfirmDialog.value && !showSaveButton) {
                        ElevatedButton(
                            modifier = Modifier
                                .fillMaxWidth(0.25f)
                                .size(width = 80.dp, height = 80.dp),
                            shape = RoundedCornerShape(0.dp),
                            colors = ButtonDefaults.buttonColors(Color.White),
                            onClick = {
                                if (!isLocationEnabled(context)) {
                                    showLocationDialog.value = true
                                } else {
                                    //  Get the accuracyArrayData from savedStateHandle
                                    val accuracyArrayData =
                                        navController.currentBackStackEntry?.savedStateHandle?.get<List<Float?>>(
                                            "accuracyArray"
                                        )

                                    // If the accuracyArrayData exists, clear it
                                    accuracyArrayData?.let {
                                        navController.currentBackStackEntry?.savedStateHandle?.set(
                                            "accuracyArray",
                                            emptyList<Float?>()
                                        )
                                    }
                                    coordinates = listOf() // Clear coordinates array when starting
                                    viewModel.clearCoordinates()
                                    // mapViewModel.clearPolygon()
                                    isCapturingCoordinates = true
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "Start",
                                tint = Color.Black,
                                modifier = Modifier.padding(4.dp)
                            )
                        }
                    }

                    // "Finish" button - visible when capturing coordinates but not yet finished or saved
                    if (isCapturingCoordinates && !showSaveButton) {
                        ElevatedButton(
                            modifier = Modifier
                                .fillMaxWidth(0.25f)
                                .size(width = 80.dp, height = 80.dp),
                            shape = RoundedCornerShape(0.dp),
                            colors = ButtonDefaults.buttonColors(Color.White),
                            onClick = {
                                if (coordinates.isNotEmpty()) {
                                    showConfirmDialog.value =
                                        true
                                    isCapturingCoordinates =
                                        false
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Done,
                                contentDescription = "Finish",
                                tint = Color.Black,
                                modifier = Modifier.padding(4.dp)
                            )
                        }
                    }

                    // "Save" button - visible after finishing the polygon and previewing it, but only when there are at least 3 points
                    if (showSaveButton && coordinates.size > 3) {
                        ElevatedButton(
                            modifier = Modifier
                                .fillMaxWidth(0.25f)
                                .size(width = 80.dp, height = 80.dp),
                            shape = RoundedCornerShape(0.dp),
                            colors = ButtonDefaults.buttonColors(Color.White),
                            onClick = {
                                mapViewModel.addCoordinates(coordinates)
                                // Show the polygon on the map for review
                                val parcelableCoordinates =
                                    coordinates.map { ParcelablePair(it.first, it.second) }
                                navController.previousBackStackEntry?.savedStateHandle?.set(
                                    "coordinates",
                                    parcelableCoordinates
                                )
                                navController.previousBackStackEntry?.savedStateHandle?.set(
                                    "accuracyArray",
                                    accuracyArray
                                )
                                if (calculatedArea > 0.000000001) {
                                    mapViewModel.showAreaDialog(
                                        calculatedArea.toString(),
                                        enteredAreaConverted.toString()
                                    )
                                } else {
                                    // Show the dialog for invalid points
                                    showInvalidPolygonDialog.value = true
                                }
                            },
                            enabled = hasPointsOnMap
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.save_polygon),
                                contentDescription = "Save Polygon",
                                tint = Color.Black,
                                modifier = Modifier.padding(4.dp)
                            )
                        }
                    }
                    // Invalid Polygon Dialog
                    InvalidPolygonDialog(
                        showDialog = showInvalidPolygonDialog,
                        onDismiss = { showInvalidPolygonDialog.value = false }
                    )

                    // Logic to handle when a point is deleted
                    LaunchedEffect(coordinates.size) {
                        if (coordinates.size < 3) {
                            showSaveButton = false
                            if (coordinates.size > 1) {
                                isCapturingCoordinates =
                                    true // Show "Finish" button again when points are deleted
                            }
                        }
                    }

                    ElevatedButton(
                        modifier =
                        Modifier
                            .fillMaxWidth(0.25f)
                            .size(width = 80.dp, height = 80.dp),
                        shape = RoundedCornerShape(0.dp),
                        colors = ButtonDefaults.buttonColors(Color.White),
                        onClick = {
                            if (!locationHelper.isLocationEnabled()) {
                                showLocationDialog.value = true
                            } else if (context.hasLocationPermission() && isCapturingCoordinates) {
                                locationHelper.requestLocationPermissionAndUpdatePolygon { latitude, longitude, accuracy ->
                                    if (latitude != "0.0" && longitude != "0.0") {
                                        viewModel.addMarker(
                                            Pair(
                                                latitude.toDouble(),
                                                longitude.toDouble()
                                            )
                                        )
                                        viewModel.addCoordinate(
                                            latitude.toDouble(),
                                            longitude.toDouble()
                                        )
                                        val coordinate =
                                            Pair(latitude.toDouble(), longitude.toDouble())
                                        coordinates = coordinates + coordinate

                                        hasPointsOnMap =
                                            coordinates.isNotEmpty()  // Enable Drop/Reset buttons

                                        accuracyArray = accuracyArray + accuracy.toFloat()
                                        val meanAccuracy = if (accuracyArray.isNotEmpty()) {
                                            accuracyArray.average().toFloat()
                                        } else 0f

                                        Log.d(
                                            "Location Results",
                                            "Latitude: $latitude, Longitude: $longitude, Accuracy: $accuracy"
                                        )
                                        Log.d("Mean Accuracy", "Mean accuracy is: $meanAccuracy")
                                    } else if (latitude == "0.0" && longitude == "0.0") {
                                        Toast
                                            .makeText(
                                                context,
                                                context.getString(R.string.can_not_get_location),
                                                Toast.LENGTH_LONG,
                                            ).show()
                                    } else {
                                        if (latitude
                                                .split(".")[1]
                                                .length < 6 ||
                                            longitude
                                                .split(".")[1]
                                                .length < 6
                                        ) {
                                            Toast
                                                .makeText(
                                                    context,
                                                    context.getString(R.string.can_not_get_location),
                                                    Toast.LENGTH_LONG,
                                                ).show()
                                        }
                                    }
                                }

                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = stringResource(id = R.string.add_point),
                            tint = Color.Black,
                            modifier = Modifier.padding(4.dp),
                        )
                    }
                    ElevatedButton(
                        modifier = Modifier
                            .fillMaxWidth(0.25f)
                            .size(width = 80.dp, height = 80.dp),
                        colors = ButtonDefaults.buttonColors(Color.White),
                        shape = RoundedCornerShape(0.dp),
                        onClick = {
                            coordinates = coordinates.dropLast(1)
                            viewModel.removeLastCoordinate()
                        },
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.drop),
                            contentDescription = stringResource(id = R.string.drop_point),
                            tint = Color.Black,
                            modifier = Modifier.padding(4.dp),
                        )
                    }
                    ElevatedButton(
                        modifier =
                        Modifier
                            .fillMaxWidth(0.25f)
                            .size(width = 80.dp, height = 80.dp),
                        shape = RoundedCornerShape(0.dp),
                        colors = ButtonDefaults.buttonColors(Color.White),
                        onClick = {
                            showClearMapDialog.value = true
                        },
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = stringResource(id = R.string.reset),
                            tint = Color.Red,
                            modifier = Modifier.padding(4.dp),
                        )
                    }
                }
            }
        }
    }
}

/**
 *  This function is used to display the message when the user captures the invalid polygon
 */

@Composable
fun InvalidPolygonDialog(
    showDialog: MutableState<Boolean>,
    onDismiss: () -> Unit
) {
    if (showDialog.value) {
        AlertDialog(
            onDismissRequest = { showDialog.value = false },
            title = { Text(text = stringResource(id = R.string.invalid_polygon_title)) },
            text = { Text(text = stringResource(id = R.string.invalid_polygon_message)) },
            confirmButton = {
                TextButton(onClick = {
                    onDismiss()
                }) {
                    Text(text = stringResource(id = R.string.ok))
                }
            },
            containerColor = MaterialTheme.colorScheme.background,
            tonalElevation = 6.dp
        )
    }
}


