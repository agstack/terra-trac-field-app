package org.technoserve.farmcollector.database.helpers.map

import android.content.Context
import com.google.android.gms.maps.GoogleMap
import com.google.maps.android.clustering.ClusterManager
import com.google.maps.android.collections.MarkerManager
import org.technoserve.farmcollector.database.models.map.ZoneClusterItem

class ZoneClusterManager(
    context: Context,
    googleMap: GoogleMap,
) : ClusterManager<ZoneClusterItem>(context, googleMap, MarkerManager(googleMap))