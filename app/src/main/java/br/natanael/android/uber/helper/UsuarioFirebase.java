package br.natanael.android.uber.helper;

import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.Exclude;

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
}
