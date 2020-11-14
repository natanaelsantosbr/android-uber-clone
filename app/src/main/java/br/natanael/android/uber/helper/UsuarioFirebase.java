package br.natanael.android.uber.helper;

import android.app.Activity;
import android.content.Intent;
import android.os.CpuUsageInfo;
import android.provider.ContactsContract;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Exclude;
import com.google.firebase.database.ValueEventListener;

import br.natanael.android.uber.activity.MapsActivity;
import br.natanael.android.uber.activity.RequisicoesActivity;
import br.natanael.android.uber.model.Usuario;

public class UsuarioFirebase {
    public static FirebaseUser getUsuarioAtual() {
        return ConfiguracaoFirebase.getFirebaseAuth().getCurrentUser();
    }
    public static boolean atualizarNome(String nome) {
        try
        {
            FirebaseUser user = getUsuarioAtual();

            UserProfileChangeRequest profileChangeRequest = new UserProfileChangeRequest.Builder()
                    .setDisplayName(nome)
                    .build();

            user.updateProfile(profileChangeRequest).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(!task.isSuccessful())
                    {
                        Log.d("Perfil", "Erro ao atualizar nome do perfil");
                    }
                }
            });

            return  true;
        }
        catch(Exception e)
        {
            e.printStackTrace();
            return  false;
        }
    }

    public static String getIdentificadorUsuario() {
        return getUsuarioAtual().getUid();
    }

    public static void redirecionarUsuarioLogado(final Activity activity) {

        FirebaseUser user =getUsuarioAtual();

        if(user != null)
        {
            DatabaseReference usuarioRef = ConfiguracaoFirebase.getDatabaseReference()
                    .child("usuarios")
                    .child(getIdentificadorUsuario());

            usuarioRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    Usuario usuario = snapshot.getValue(Usuario.class);

                    if(usuario.getTipo() == "M")
                        activity.startActivity(new Intent(activity, RequisicoesActivity.class));
                    else
                        activity.startActivity(new Intent(activity, MapsActivity.class));
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
    }
}
