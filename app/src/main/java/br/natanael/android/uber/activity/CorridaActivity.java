package br.natanael.android.uber.activity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.math.BigDecimal;

import br.natanael.android.uber.R;
import br.natanael.android.uber.helper.ConfiguracaoFirebase;
import br.natanael.android.uber.model.Requisicao;
import br.natanael.android.uber.model.Usuario;

public class CorridaActivity extends AppCompatActivity implements OnMapReadyCallback {
    private GoogleMap mMap;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private LatLng localMotorista;
    private LatLng localPassageiro;
    private Usuario motorista;
    private Usuario passageiro;
    private String idRequisicao;
    private Requisicao requisicao;

    private DatabaseReference firebaseRef;
    private Button buttonAceitarCorrida;

    private Marker marcadorMotorista;
    private Marker marcadorPassageiro;
    private String statusRequisicao;
    private boolean requisicaoAtiva;

    private FloatingActionButton fabRota;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_corrida);

        inicializarComponentes();

        if(getIntent().getExtras().containsKey("idRequisicao") && getIntent().getExtras().containsKey("motorista")){

            Bundle extras = getIntent().getExtras();

            motorista = (Usuario) extras.getSerializable("motorista");
            localMotorista = new LatLng(Double.parseDouble(motorista.getLatitude()),Double.parseDouble(motorista.getLongitude()));
            idRequisicao = extras.getString("idRequisicao");
            requisicaoAtiva = extras.getBoolean("requisicaoAtiva");
            verificaStatusRequisicao();
        }
    }

    private void verificaStatusRequisicao() {
        final DatabaseReference requisicoes = firebaseRef.child("requisicoes").child(idRequisicao);

        requisicoes.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                requisicao = snapshot.getValue(Requisicao.class);
                if(requisicao != null)
                {
                    passageiro = requisicao.getPassageiro();

                    localPassageiro = new LatLng(Double.parseDouble(passageiro.getLatitude()) , Double.parseDouble(passageiro.getLongitude()));

                    statusRequisicao = requisicao.getStatus();
                    alterarInterfaceRequisicao(statusRequisicao);
                }


//                mMap.clear();
//
//                mMap.addMarker(new MarkerOptions()
//                        .position(localMotorista)
//                        .title("Meu local")
//                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.carro)));
//
//                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(localMotorista, 18));

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void alterarInterfaceRequisicao(String status ) {
        switch (status)
        {
            case Requisicao.STATUS_AGUARDANDO:
                requisicaoAguardando();
                break;
            case Requisicao.STATUS_A_CAMINHO:
                requisicaoACaminho();
                break;
        }
    }

    private void requisicaoACaminho() {
        buttonAceitarCorrida.setText("A Caminho do passageiro");
        fabRota.setVisibility(View.VISIBLE);
        
        adicionarMarcadorMotorista(localMotorista,  motorista.getNome());

        adicionarMarcadorPassageiro(localPassageiro, passageiro.getNome());

        centralizarDoisMarcadores(marcadorMotorista, marcadorPassageiro);
    }

    private void centralizarDoisMarcadores(Marker marcador1, Marker marcador2) {
        LatLngBounds.Builder builder = new LatLngBounds.Builder();

        builder.include(marcador1.getPosition());
        builder.include(marcador2.getPosition());

        //Limite entre os marcadores
        LatLngBounds bounds = builder.build();


        int largura = getResources().getDisplayMetrics().widthPixels;
        int altura = getResources().getDisplayMetrics().heightPixels;
        int espacoInterno = (int) (largura * 0.20);

        mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, largura,altura,espacoInterno));
    }

    private void adicionarMarcadorMotorista(LatLng localizacao, String titulo)
    {
        if(marcadorMotorista != null)
            marcadorMotorista.remove();

        marcadorMotorista =  mMap.addMarker(new MarkerOptions()
                .position(localizacao)
                .title(titulo)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.carro)));
    }

    private void adicionarMarcadorPassageiro(LatLng localizacao, String titulo)
    {
        if(marcadorPassageiro != null)
            marcadorPassageiro.remove();

        marcadorPassageiro =  mMap.addMarker(new MarkerOptions()
                .position(localizacao)
                .title(titulo)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.usuario)));
    }

    private void requisicaoAguardando() {
        buttonAceitarCorrida.setText("Aceitar Corrida");
        fabRota.setVisibility(View.GONE);
    }

    private void inicializarComponentes() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Iniciar corrida");
        setSupportActionBar(toolbar);


        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        firebaseRef = ConfiguracaoFirebase.getDatabaseReference();
        buttonAceitarCorrida = findViewById(R.id.buttonAceitarCorrida);

        fabRota = findViewById(R.id.fabRota);
        fabRota.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String status = statusRequisicao;

                if(status != null)
                {
                    if(!status.isEmpty())
                    {
                        String lat = "";
                        String lon = "";

                        switch (status)
                        {
                            case Requisicao.STATUS_A_CAMINHO:
                                lat = String.valueOf(localPassageiro.latitude);
                                lon = String.valueOf(localMotorista.longitude);
                                break;
                            case Requisicao.STATUS_VIAGEM:
                                /*lat = String.valueOf(destino);
                                lon = String.valueOf(localMotorista.longitude);*/
                                break;

                        }

                        //Abrir rota
                        String latLong = lat + "," + lon;
                        Uri uri = Uri.parse("google.navigation:q=" + latLong + "&mode=d");
                        Intent mapIntent = new Intent(Intent.ACTION_VIEW, uri);
                        mapIntent.setPackage("com.google.android.apps.maps");
                        startActivity(mapIntent);



                    }
                }
            }
        });


    }

    public  void aceitarCorrida(View view ){
        requisicao = new Requisicao();
        requisicao.setId(idRequisicao);
        requisicao.setMotorista(motorista);
        requisicao.setStatus(Requisicao.STATUS_A_CAMINHO);
        requisicao.atualizar();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        recuperarLocalizacaoUsuario();

    }

    private void recuperarLocalizacaoUsuario() {
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {

                //recuperar latitude e longitude
                double latitude = location.getLatitude();
                double longitude = location.getLongitude();
                localMotorista = new LatLng(latitude, longitude);
                alterarInterfaceRequisicao(statusRequisicao);
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
                    10000,
                    10,
                    locationListener
            );
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        if(requisicaoAtiva)
        {
            Toast.makeText(this, "Necessario encerrar a requisicao atual!", Toast.LENGTH_SHORT).show();
        }
        else
        {
            Intent i = new Intent(CorridaActivity.this, RequisicoesActivity.class);
            startActivity(i);
        }

        return false;
    }
}
