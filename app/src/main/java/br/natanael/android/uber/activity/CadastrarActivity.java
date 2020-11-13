package br.natanael.android.uber.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;

import br.natanael.android.uber.R;
import br.natanael.android.uber.helper.ConfiguracaoFirebase;
import br.natanael.android.uber.helper.UsuarioFirebase;
import br.natanael.android.uber.model.Usuario;

public class CadastrarActivity extends AppCompatActivity {
    private FirebaseAuth firebaseAuth;

    private TextView txtNome, txtEmail, txtSenha;
    private Switch switchTipo;
    private ProgressBar progressBarCadastro;
    private DatabaseReference database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastrar);

        inicializarComponentes();

        inicializarVariaveis();

    }

    public void efetuarCadastro(View view) {
        final String nome = txtNome.getText().toString();
        final String email = txtEmail.getText().toString();
        final String senha = txtSenha.getText().toString();
        final String tipo = switchTipo.isChecked() ? "M" : "P";

        if(nome != null && email != null && senha != null)
        {
            if(!nome.isEmpty() && !email.isEmpty() && !senha.isEmpty())
            {
                progressBarCadastro.setVisibility(View.VISIBLE);
                firebaseAuth.createUserWithEmailAndPassword(email, senha).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()) {
                            FirebaseUser user = firebaseAuth.getCurrentUser();

                            Usuario usuario = new Usuario();
                            usuario.setNome(nome);
                            usuario.setEmail(email);
                            usuario.setSenha(senha);
                            usuario.setTipo(tipo);
                            usuario.setId(user.getUid());

                            usuario.salvar();

                            UsuarioFirebase.atualizarNome(usuario.getNome());

                            if (usuario.getTipo() == "P") {
                                startActivity(new Intent(CadastrarActivity.this, MapsActivity.class));
                                finish();
                            } else {
                                startActivity(new Intent(CadastrarActivity.this, RequisicoesActivity.class));
                                finish();
                                Toast.makeText(CadastrarActivity.this, "Cadastro realizado com sucesso: " + user.getEmail(), Toast.LENGTH_SHORT).show();
                            }
                        }
                        else
                        {
                            String erroExcecao = "";
                            try {
                                throw task.getException();
                            }
                            catch (FirebaseAuthWeakPasswordException ex)
                            {
                                erroExcecao = "Digite uma senha mais forte";
                            }
                            catch (FirebaseAuthInvalidCredentialsException ex) {
                                erroExcecao = "Por favor, digite um e-mail válido";
                            }
                            catch (FirebaseAuthUserCollisionException ex){
                                erroExcecao = "Esta conta já foi cadastrada";
                            }
                            catch (Exception ex)
                            {
                                erroExcecao = "ao cadastrar usuario: " + ex.getMessage();
                                ex.printStackTrace();
                            }

                            Toast.makeText(CadastrarActivity.this, "Erro: " + erroExcecao, Toast.LENGTH_SHORT).show();
                        }
                        progressBarCadastro.setVisibility(View.INVISIBLE);
                    }
                });
            }
            else
            {
                Toast.makeText(this, "Nenhum campo pode ficar vazio", Toast.LENGTH_SHORT).show();
            }

            
        }
        else
        {
            Toast.makeText(this, "Todos os campos são obrigatórios!", Toast.LENGTH_SHORT).show();
        }
        




    }

    private void inicializarVariaveis() {
        firebaseAuth = ConfiguracaoFirebase.getFirebaseAuth();
        database = ConfiguracaoFirebase.getDatabaseReference();

        progressBarCadastro.setVisibility(View.INVISIBLE);
    }

    private void inicializarComponentes() {
        txtNome = findViewById(R.id.txtNome);
        txtEmail = findViewById(R.id.txtEmail);
        txtSenha = findViewById(R.id.txtSenha);
        switchTipo = findViewById(R.id.switchTipo);
        progressBarCadastro = findViewById(R.id.progressBarCadastro);
    }
}
