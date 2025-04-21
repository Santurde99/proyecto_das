package com.example.proyecto1;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

public class Map_Activity extends AppCompatActivity {
    private static final int PERMISSION_REQUEST_CODE = 100;

    private MapView map;
    private FusedLocationProviderClient locationClient;
    private GeoPoint posicionActual;
    private Marker marcadorObjetivo;
    private GeoPoint puntoObjetivo;
    private MyLocationNewOverlay myLocationOverlay;
    private Button btnGenerar, btnEliminar;
    private ImageButton back_button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Configuración inicial de OSMDroid
        Configuration.getInstance().load(getApplicationContext(),
                PreferenceManager.getDefaultSharedPreferences(getApplicationContext()));

        setContentView(R.layout.activity_map);

        // Inicializar vistas
        map = findViewById(R.id.map);
        btnGenerar = findViewById(R.id.btn_generar);
        btnEliminar = findViewById(R.id.btn_eliminar);
        back_button = findViewById(R.id.back_button);

        // Configurar mapa
        configurarMapa();

        // Configurar botones
        btnGenerar.setOnClickListener(v -> generarPunto());
        btnEliminar.setOnClickListener(v -> eliminarPunto());

        // Inicializar cliente de ubicación
        locationClient = LocationServices.getFusedLocationProviderClient(this);

        // Verificar permisos
        verificarPermisos();


        back_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void configurarMapa() {
        map.setTileSource(TileSourceFactory.MAPNIK);
        map.setMultiTouchControls(true);

        IMapController mapController = map.getController();
        mapController.setZoom(18.0);

        myLocationOverlay = new MyLocationNewOverlay(new GpsMyLocationProvider(this), map);
        myLocationOverlay.enableMyLocation();
        map.getOverlays().add(myLocationOverlay);
    }

    private void verificarPermisos() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                    },
                    PERMISSION_REQUEST_CODE);
        } else {
            obtenerUbicacionActual();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                obtenerUbicacionActual();
            } else {
                Toast.makeText(this, "Permisos de ubicación son necesarios", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void obtenerUbicacionActual() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        locationClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null) {
                posicionActual = new GeoPoint(location.getLatitude(), location.getLongitude());
                map.getController().setCenter(posicionActual);
                myLocationOverlay.enableFollowLocation();

                // Cargar punto guardado (si existe)
                cargarPuntoGuardado();
            } else {
                Toast.makeText(this, "No se pudo obtener la ubicación actual", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void generarPunto() {
        if (posicionActual == null) {
            Toast.makeText(this, "Esperando obtener tu ubicación...", Toast.LENGTH_SHORT).show();
            return;
        }

        if (marcadorObjetivo != null) {
            map.getOverlays().remove(marcadorObjetivo);
        }

        puntoObjetivo = generarPuntoAleatorio(posicionActual, 400, 500);

        marcadorObjetivo = new Marker(map);
        marcadorObjetivo.setPosition(puntoObjetivo);
        marcadorObjetivo.setTitle("¡Objetivo!");
        marcadorObjetivo.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);

        try {
            Drawable icon = ContextCompat.getDrawable(this, org.osmdroid.library.R.drawable.marker_default);
            marcadorObjetivo.setIcon(icon);
        } catch (Exception e) {
            e.printStackTrace();
        }

        map.getOverlays().add(marcadorObjetivo);
        map.invalidate();

        guardarPuntoObjetivo(puntoObjetivo);
        iniciarServicio();

        Toast.makeText(this, "Nuevo punto generado", Toast.LENGTH_SHORT).show();
    }

    private void eliminarPunto() {
        if (marcadorObjetivo != null) {
            map.getOverlays().remove(marcadorObjetivo);
            map.invalidate();
            marcadorObjetivo = null;
            puntoObjetivo = null;
        }

        getSharedPreferences("punto_objetivo", MODE_PRIVATE).edit().clear().apply();
        stopService(new Intent(this, Location_Service.class));
        Toast.makeText(this, "Punto eliminado", Toast.LENGTH_SHORT).show();
    }

    private void iniciarServicio() {
        if (puntoObjetivo == null) return;

        Intent servicio = new Intent(this, Location_Service.class);
        servicio.putExtra("lat", puntoObjetivo.getLatitude());
        servicio.putExtra("lng", puntoObjetivo.getLongitude());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(servicio);
        } else {
            startService(servicio);
        }
    }

    private GeoPoint generarPuntoAleatorio(GeoPoint centro, int minDistancia, int maxDistancia) {
        double distancia = minDistancia + Math.random() * (maxDistancia - minDistancia);
        double radioEnGrados = distancia / 111000f;

        double u = Math.random();
        double v = Math.random();
        double w = radioEnGrados * Math.sqrt(u);
        double t = 2 * Math.PI * v;

        double x = w * Math.cos(t);
        double y = w * Math.sin(t);

        double newX = x / Math.cos(Math.toRadians(centro.getLatitude()));

        return new GeoPoint(centro.getLatitude() + y, centro.getLongitude() + newX);
    }

    private void guardarPuntoObjetivo(GeoPoint punto) {
        getSharedPreferences("punto_objetivo", MODE_PRIVATE)
                .edit()
                .putFloat("lat", (float) punto.getLatitude())
                .putFloat("lng", (float) punto.getLongitude())
                .apply();
    }

    private void cargarPuntoGuardado() {
        var prefs = getSharedPreferences("punto_objetivo", MODE_PRIVATE);
        if (prefs.contains("lat") && prefs.contains("lng")) {
            double lat = prefs.getFloat("lat", 0);
            double lng = prefs.getFloat("lng", 0);
            puntoObjetivo = new GeoPoint(lat, lng);

            marcadorObjetivo = new Marker(map);
            marcadorObjetivo.setPosition(puntoObjetivo);
            marcadorObjetivo.setTitle("¡Objetivo!");
            marcadorObjetivo.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);

            try {
                Drawable icon = ContextCompat.getDrawable(this, org.osmdroid.library.R.drawable.marker_default);
                marcadorObjetivo.setIcon(icon);
            } catch (Exception e) {
                e.printStackTrace();
            }

            map.getOverlays().add(marcadorObjetivo);
            map.invalidate();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        map.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        map.onPause();
    }
}
