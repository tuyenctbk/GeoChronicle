package com.example.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.location.HistoricalPresets
import com.example.location.HistoricalTourLocation

@Composable
fun LocationPresetDialog(
    currentLocationName: String,
    onDismiss: () -> Unit,
    onSelectPreset: (HistoricalTourLocation) -> Unit,
    onUseCurrentGps: () -> Unit,
    onCustomCoordinates: (Double, Double, String) -> Unit
) {
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    var customLatText by remember { mutableStateOf("") }
    var customLonText by remember { mutableStateOf("") }
    var customLabelText by remember { mutableStateOf("") }
    var customError by remember { mutableStateOf<String?>(null) }

    val errorInvalidCoords = stringResource(R.string.error_invalid_coordinates)
    val errorBounds = stringResource(R.string.error_coordinate_bounds)
    val defaultCustomName = stringResource(R.string.custom_coordinates)

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss, modifier = Modifier.testTag("dismiss_location_dialog_button")) {
                Text(stringResource(R.string.close))
            }
        },
        title = {
            Text(stringResource(R.string.choose_location_title), fontWeight = FontWeight.Bold)
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                TabRow(selectedTabIndex = selectedTabIndex) {
                    Tab(
                        selected = selectedTabIndex == 0,
                        onClick = { selectedTabIndex = 0 },
                        text = { Text(stringResource(R.string.tab_walk_hubs)) },
                        modifier = Modifier.testTag("tab_presets")
                    )
                    Tab(
                        selected = selectedTabIndex == 1,
                        onClick = { selectedTabIndex = 1 },
                        text = { Text(stringResource(R.string.tab_custom_coords)) },
                        modifier = Modifier.testTag("tab_custom_coords")
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (selectedTabIndex == 0) {
                    // Option 1: Use Current GPS Device Location
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onUseCurrentGps()
                                onDismiss()
                            }
                            .testTag("use_gps_location_item")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(14.dp)
                        ) {
                            Icon(
                                Icons.Default.GpsFixed,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    stringResource(R.string.use_gps_title),
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    stringResource(R.string.current_reading_format, currentLocationName),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        stringResource(R.string.iconic_cities_title),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 280.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(HistoricalPresets.PRESETS) { hub ->
                            val hubName = if (hub.nameRes != null) stringResource(hub.nameRes) else hub.name
                            val hubDesc = if (hub.descRes != null) stringResource(hub.descRes) else hub.description

                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        onSelectPreset(hub.copy(name = hubName, description = hubDesc))
                                        onDismiss()
                                    }
                                    .testTag("preset_${hub.id}")
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(12.dp)
                                ) {
                                    Text(hub.iconEmoji, style = MaterialTheme.typography.titleLarge)
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            hubName,
                                            fontWeight = FontWeight.SemiBold,
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                        Text(
                                            hubDesc,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 2
                                        )
                                    }
                                }
                            }
                        }
                    }
                } else {
                    // Custom Latitude / Longitude
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    ) {
                        Text(
                            stringResource(R.string.custom_coords_prompt),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = customLabelText,
                            onValueChange = { customLabelText = it },
                            label = { Text(stringResource(R.string.location_name_label)) },
                            placeholder = { Text(stringResource(R.string.location_name_placeholder)) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("custom_label_input")
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = customLatText,
                                onValueChange = { customLatText = it },
                                label = { Text(stringResource(R.string.latitude_label)) },
                                placeholder = { Text(stringResource(R.string.lat_placeholder)) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("custom_lat_input")
                            )

                            OutlinedTextField(
                                value = customLonText,
                                onValueChange = { customLonText = it },
                                label = { Text(stringResource(R.string.longitude_label)) },
                                placeholder = { Text(stringResource(R.string.lon_placeholder)) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("custom_lon_input")
                            )
                        }

                        if (customError != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = customError ?: "",
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = {
                                val lat = customLatText.toDoubleOrNull()
                                val lon = customLonText.toDoubleOrNull()
                                if (lat == null || lon == null) {
                                    customError = errorInvalidCoords
                                } else if (lat < -90.0 || lat > 90.0 || lon < -180.0 || lon > 180.0) {
                                    customError = errorBounds
                                } else {
                                    val name = customLabelText.ifBlank { defaultCustomName }
                                    onCustomCoordinates(lat, lon, name)
                                    onDismiss()
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("apply_custom_coords_button")
                        ) {
                            Icon(Icons.Default.Place, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(stringResource(R.string.explore_location_button))
                        }
                    }
                }
            }
        }
    )
}
