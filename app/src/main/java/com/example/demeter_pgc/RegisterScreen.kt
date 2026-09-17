package com.example.demeter_pgc

import android.app.Activity
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.modifier.modifierLocalConsumer

import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.auth

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun RegisterScreen (onClickBack :() -> Unit = {},onSuccessfullRegister:()-> Unit={}){
    val context=LocalContext.current
    //val  auth = Firebase.auth
    //val activity= LocalView.current.context as Activity

// ESTADO DE LOS INPUT
    var inputName by remember { mutableStateOf("") }
    var inputEmail by remember { mutableStateOf("") }
    var inputPassword by remember { mutableStateOf("") }
    var inputPasswordConfirmation by remember { mutableStateOf("") }

    var nameError by remember { mutableStateOf("") }
    var emailError by remember { mutableStateOf("") }
    var passwordError by remember { mutableStateOf("") }
    var passwordConfirmationError by remember { mutableStateOf("") }
    var registerError by remember { mutableStateOf("") }


    // icono de poder regresar
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                },
                navigationIcon = {
                    IconButton(onClick = onClickBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "icon register"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(horizontal = 32.dp)
                .imePadding()
                .verticalScroll(rememberScrollState()),

            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = R.drawable.demeter),
                contentDescription = "Logo Demeter",
                modifier = Modifier.size(200.dp)
            )
            // TEXTO DE REGISTRO
            Spacer(modifier = Modifier.height(32.dp))

            Text("Registrarse", fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0C7211)
            )
            //  NOMBRE
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = inputName,
                onValueChange = {inputName=it},
                label = { Text("Nombre") },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon ={
                    Icon(
                        imageVector = Icons.Filled.Person,
                        contentDescription = "icon name"
                    )
                },supportingText = {
                    if (nameError.isNotEmpty()) {
                        Text(
                            text = nameError,
                            color = Color.Red
                        )
                    }
                }
            )
            //  NOMBRE USUARIO
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = inputEmail,
                onValueChange = {inputEmail=it},
                label = { Text("Correo") },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon ={
                    Icon(
                        imageVector = Icons.Filled.Email,
                        contentDescription = "icon gmail"
                    )
                },supportingText = {
                    if (emailError.isNotEmpty()) {
                        Text(
                            text = emailError,
                            color = Color.Red
                        )
                    }
                }
            )
            //  CONTRASEÑA
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = inputPassword,
                onValueChange = {inputPassword=it},
                label = { Text("Contraseña") },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon ={
                    Icon(
                        imageVector = Icons.Filled.Lock,
                        contentDescription = "icon password"
                    )
                },supportingText = {
                    if (passwordError .isNotEmpty()) {
                        Text(
                            text = passwordError ,
                            color = Color.Red
                        )
                    }
                }
            )
            //  CONFIRMAR CONTRASEÑA
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = inputPasswordConfirmation,
                onValueChange = {inputPasswordConfirmation=it},
                label = { Text("Confirmar contraseña") },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon ={
                    Icon(
                        imageVector = Icons.Filled.Lock,
                        contentDescription = "icon pass"
                    )
                },supportingText = {
                    if (passwordConfirmationError .isNotEmpty()) {
                        Text(
                            text = passwordConfirmationError,
                            color = Color.Red
                        )
                    }
                }
            )
            if(registerError.isNotEmpty()){
                Text(registerError,color=Color.Red)
            }
            // BOTON REGISTRO
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    val auth= Firebase.auth
                    val activity=context as Activity
                    val isValidName = validateName(inputName).first
                    val isValidEmail = validateEmail(inputEmail).first
                    val isValidPassword = validatePassword(inputPassword).first
                    val isValidConfirmPassword =
                        validateConfirmPassword(inputPassword,inputPasswordConfirmation).first

                    nameError = validateName(inputName).second
                    emailError = validateEmail(inputEmail).second
                    passwordError = validatePassword(inputPassword).second
                    passwordConfirmationError =
                        validateConfirmPassword(inputPassword, inputPasswordConfirmation).second

                    if (isValidName && isValidEmail && isValidPassword && isValidConfirmPassword) {
                        auth.createUserWithEmailAndPassword(inputEmail,inputPassword).
                                addOnCompleteListener (activity) { task ->
                                    if(task.isSuccessful){
                                        onSuccessfullRegister()
                                    }else{
                                        registerError = when (task.exception) {
                                            is FirebaseAuthInvalidCredentialsException -> "Correo inválido"
                                            is FirebaseAuthUserCollisionException -> "Ese correo ya está registrado"
                                            else -> "Error al registrarse, intenta de nuevo"
                                        }
                                    }
                                }
                    } else {
                        registerError="Hubo un error en el registro"
                    }
                }, modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF0C7211),
                    contentColor = Color.White
                )
            ) {
                Text("Registrarse")
            }

        }
    }
}