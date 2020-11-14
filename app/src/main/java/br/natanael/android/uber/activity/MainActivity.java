package br.natanael.android.uber.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import br.natanael.android.uber.R;
import br.natanael.android.uber.helper.UsuarioFirebase;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Esconde toolbar
        getSupportActionBar().hide();
    }

    public void abrirTelaDeLogin(View view ){
        startActivity(new Intent(this, LoginActivity.class));
    }

    public void abrirTelaDeCadastro(View view){
        startActivity(new Intent(this, CadastrarActivity.class));
    }

    @Override
    protected void onStart() {
        super.onStart();
        UsuarioFirebase.redirecionarUsuarioLogado(this);
    }
}
