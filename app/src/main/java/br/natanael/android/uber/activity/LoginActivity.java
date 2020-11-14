package br.natanael.android.uber.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.EmailAuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;

import br.natanael.android.uber.R;
import br.natanael.android.uber.helper.ConfiguracaoFirebase;
import br.natanael.android.uber.helper.UsuarioFirebase;
import br.natanael.android.uber.model.Usuario;

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText txtLoginEmail, txtLoginSenha;
    private ProgressBar progressBarLogin;
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        inicializarComponentes();

        inicializarVariaveis();

    }

    private void inicializarVariaveis() {
        firebaseAuth = ConfiguracaoFirebase.getFirebaseAuth();
        progressBarLogin.setVisibility(View.INVISIBLE);
    }

    private void inicializarComponentes() {
        txtLoginEmail= findViewById(R.id.txtLoginEmail);
        txtLoginSenha = findViewById(R.id.txtLoginSenha);
        progressBarLogin =findViewById(R.id.progressBarLogin);

    }

    public void efetuarLogin(View view){
        String email = txtLoginEmail.getText().toString();
        String senha = txtLoginSenha.getText().toString();

        if(email != null && senha != null)
        {
            if(!email.isEmpty() && !senha.isEmpty())
            {
                progressBarLogin.setVisibility(View.VISIBLE);
                firebaseAuth.signInWithEmailAndPassword(email, senha).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful())
                        {
                            UsuarioFirebase.redirecionarUsuarioLogado(LoginActivity.this);
                        }
                        else
                        {
                            String excecao = "";

                            try {
                                throw  task.getException();
                            }
                            catch (FirebaseAuthInvalidUserException e)
                            {
                                excecao = "Usuario nao encontrado";
                            }
                            catch (FirebaseAuthInvalidCredentialsException e )
                            {
                                excecao = "Email e senha nao correspondem a um usuario cadastrado";

                            }
                            catch (Exception e)
                            {
                                excecao = e.getMessage();
                                e.printStackTrace();
                            }
                            Toast.makeText(LoginActivity.this, excecao, Toast.LENGTH_SHORT).show();
                        }
                        progressBarLogin.setVisibility(View.INVISIBLE);

                    }
                });
                
            }
            else
            {
                Toast.makeText(this, "Todos os campos são obrigatórios", Toast.LENGTH_SHORT).show();
            }

        }
        else
        {
            Toast.makeText(this, "Todos os campos são obrigatórios", Toast.LENGTH_SHORT).show();
        }
    }
}
