package com.duyvv.permission.ui

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context.LOCATION_SERVICE
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import android.text.Spannable
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.text.buildSpannedString
import androidx.fragment.app.Fragment
import com.duyvv.permission.R
import com.duyvv.permission.databinding.FragmentMainBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices

class MainFragment : Fragment() {

    private lateinit var binding: FragmentMainBinding

    private var fusedLocationClient: FusedLocationProviderClient? = null

    private lateinit var locationCallback: LocationCallback

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMainBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setup()

        requestLocationPermission()
    }

    private fun setup() {
        binding.apply {
            btnStart.setOnClickListener {
                getLocationByGoogleServices()
            }
            btnStart.isEnabled = false
            tvPermissionStatus.text = buildSpannedString {
                append("Location permission: ")
                append(
                    "Not granted",
                    ForegroundColorSpan(Color.RED),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }
        }
    }

    @SuppressLint("MissingPermission", "SetTextI18n")
    private fun getLocationByGoogleServices() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(p0: LocationResult) {
                for (location in p0.locations) {
                    location?.let {
                        Log.d("TAG", "onLocationResult: $it")
                        binding.tvLat.text = "Lat: ${it.latitude}"
                        binding.tvLng.text = "Lng: ${it.longitude}"
                    }
                }
            }
        }
        if (hasPermission(Manifest.permission.ACCESS_FINE_LOCATION) &&
            hasPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
        ) {
            fusedLocationClient?.requestLocationUpdates(
                LocationRequest.Builder(5000)
                    .build(),
                locationCallback,
                Looper.getMainLooper()
            )
        }
    }

    override fun onPause() {
        super.onPause()
        fusedLocationClient?.removeLocationUpdates(locationCallback)
    }

    @SuppressLint("MissingPermission", "SetTextI18n")
    private fun getLocationByGPS() {
        val locationManager =
            requireActivity().getSystemService(LOCATION_SERVICE) as LocationManager
        locationManager.requestLocationUpdates(
            LocationManager.GPS_PROVIDER,
            1000,
            1f
        ) {
            binding.tvLat.text = "Lat: ${it.latitude}"
            binding.tvLng.text = "Lng: ${it.longitude}"
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            updateUI()
        } else {
            Toast.makeText(requireContext(), "Permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateUI() {
        binding.apply {
            btnStart.isEnabled = true
            btnStart.setBackgroundColor(requireContext().getColor(R.color.green))
            tvLat.visibility = View.VISIBLE
            tvLng.visibility = View.VISIBLE
            tvPermissionStatus.text = buildSpannedString {
                append("Location permission: ")
                append(
                    "Granted",
                    ForegroundColorSpan(requireContext().getColor(R.color.green)),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }
        }
    }

    private fun requestLocationPermission() {
        if (!hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)) {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        } else {
            updateUI()
        }
    }

    private fun hasPermission(permission: String): Boolean {
        return ActivityCompat.checkSelfPermission(
            requireContext(),
            permission
        ) == PackageManager.PERMISSION_GRANTED
    }
}