package com.example.postagensredesocial.helper;

import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.postagensredesocial.model.Postagem;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.UUID;

public class DBHelper{ //classe que gerencia o DB.
    private DatabaseReference rootDBRef; //root do Realtime Database
    private DatabaseReference postagensDBRef; //child/tabela de postagens

    private StorageReference rootStorageRef; //root do Storage com as fotos
    private StorageReference fotosStorageRef;
    private String fotoDownloadURL = null; //retornada quando faz upload de fotos

    public DBHelper(){
        rootDBRef = FirebaseDatabase.getInstance().getReference();
        postagensDBRef = rootDBRef.child("postagens");

        rootStorageRef = FirebaseStorage.getInstance().getReference();
        fotosStorageRef = rootStorageRef.child("fotos");
    }

    //upload do post no Storage --> coloca a URL de download em fotoDownloadURL
    //recebe Postagem sem foto -->  faz o upload da foto no Storage, coloca url na Postagem e faz upload dela no DB
    public void escrevePostagem(Postagem p, Uri fotoPath){
        //postagem que será uploadada
        final Postagem post = p;
        //gerando nome random para a imagem
        String randomFileName = UUID.randomUUID().toString() + fotoPath.getLastPathSegment();
        final StorageReference imgRef = fotosStorageRef.child(randomFileName);

        //Adicionando URI ao Storage
        imgRef.putFile(fotoPath)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(final UploadTask.TaskSnapshot taskSnapshot) {
                        Log.i ("AVISO", "Sucesso no Upload");

                        //Listener para recuperar URL de download
                        imgRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                //colocando
                                fotoDownloadURL = uri.toString();
                                if(fotoDownloadURL != null){
                                    post.setFotourl(fotoDownloadURL);
                                    postagensDBRef.push().setValue(post);
                                    Log.i("PostAVISO", "em DBHelper.escrevePostagem() --> upload de Postagem SUCESSO!");
                                }
                                Log.i ("AVISO", "Foto uploadada em: " + fotoDownloadURL);
                            }
                        });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.i ("AVISO", "Falha no Upload");
                        fotoDownloadURL = null;
                    }
                });
    }

    public DatabaseReference getPostagensDBRef() {
        return postagensDBRef;
    }
}
