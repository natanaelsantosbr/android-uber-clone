package br.natanael.android.uber.activity;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Camera;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.print.PrinterId;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;

import br.natanael.android.uber.R;
import br.natanael.android.uber.helper.ConfiguracaoFirebase;
import br.natanael.android.uber.model.Destino;

public class PassageiroActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private FirebaseAuth firebaseAuth;
    private LocationManager locationManager;
    private LocationListener locationListener;

    private EditText editDestino;
    private TextView txtVelocimetro;

    double currentSpeed,kmphSpeed;
    private double speed = 0.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_passageiro);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        inicializarComponentes();

    }

    private void inicializarComponentes() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Iniciar uma viagem");

        firebaseAuth = ConfiguracaoFirebase.getFirebaseAuth();

        editDestino = findViewById(R.id.editDestino);
        txtVelocimetro = findViewById(R.id.txtVelocimetro);

        setSupportActionBar(toolbar);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        recuperarLocalizacaoUsuario();
    }

    public static double round(double unrounded, int precision, int roundingMode) {
        BigDecimal bd = new BigDecimal(unrounded);
        BigDecimal rounded = bd.setScale(precision, roundingMode);
        return rounded.doubleValue();
    }

    private void recuperarLocalizacaoUsuario() {
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {

                speed = location.getSpeed();
                currentSpeed = round(speed,3, BigDecimal.ROUND_HALF_UP);
                kmphSpeed = round((currentSpeed*3.6),3,BigDecimal.ROUND_HALF_UP);

                txtVelocimetro.setText(kmphSpeed+" km/h");

                //recuperar latitude e longitude
                double latitude = location.getLatitude();
                double longitude = location.getLongitude();
                LatLng meuLocal = new LatLng(latitude, longitude);

                mMap.clear();

                mMap.addMarker(new MarkerOptions()
                        .position(meuLocal)
                        .title("Meu local")
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.usuario)));

                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(meuLocal, 18));
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        };

        //Solicitar atualizacoes de localizacao
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    0,
                    0,
                    locationListener
            );
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId())
        {
            case R.id.menuSair:
                firebaseAuth.signOut();
                finish();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    public void  chamarUber(View view)
    {
        String enderecoDestino = editDestino.getText().toString();

        if(enderecoDestino != null)
        {
            if(!enderecoDestino.equals(""))
            {
                Address addressDestino = recuperarDestino(enderecoDestino);

                if(addressDestino != null)
                {
                    Destino destino = new Destino();
                    destino.setCidade(addressDestino.getSubAdminArea());
                    destino.setCep(addressDestino.getPostalCode());
                    destino.setBairro(addressDestino.getSubLocality());
                    destino.setRua(addressDestino.getThoroughfare());
                    destino.setNumero(addressDestino.getFeatureName());
                    destino.setLatitude(String.valueOf(addressDestino.getLatitude()));
                    destino.setLongitude(String.valueOf(addressDestino.getLongitude()));

                    StringBuffer mensagem = new StringBuffer();
                    mensagem.append(" Cidade: " + destino.getCidade());
                    mensagem.append("\nRua: " + destino.getRua());
                    mensagem.append("\nBairro: " + destino.getBairro());
                    mensagem.append("\nNumero: " + destino.getNumero());
                    mensagem.append("\n Cep: " + destino.getCep());

                    AlertDialog.Builder builder = new AlertDialog.Builder(this)
                            .setTitle("Confirme seu endereço")
                            .setMessage(mensagem)
                            .setPositiveButton("Confirmar", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                }
                            }).setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                }
                            });

                    AlertDialog dialog = builder.create();
                    dialog.show();




                }


            }
            else
                Toast.makeText(this, "Informe o endereço do destino", Toast.LENGTH_SHORT).show();
        }
        else
        {
            Toast.makeText(this, "Informe o endereço do destino", Toast.LENGTH_SHORT).show();
        }


    }

    private Address recuperarDestino(String destino) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());

        try {
            List<Address> listaEndereco = geocoder.getFromLocationName(destino, 1);

            if(listaEndereco != null)
            {
                if(listaEndereco.size() > 0){
                    Address address = listaEndereco.get(0);

                    double lat = address.getLatitude();
                    double log = address.getLongitude();


                    return address;


                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return  null;

    }
}
