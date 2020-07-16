package com.flore.instagramclone

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {
    var auth: FirebaseAuth? = null // 파이어베이스 Auth
    var googleSignInClient: GoogleSignInClient? = null
    var GOOGLE_LOGIN_CODE = 9001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        auth = FirebaseAuth.getInstance()

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build() // 구글 로그인 기능 초기화

        googleSignInClient = GoogleSignIn.getClient(this, gso);

        btn_login_email.setOnClickListener { // 이메일 로그인 버튼
            signinAndSignup()
        }

        btn_login_google.setOnClickListener { // 구글 로그인 버튼
            googleLogin()
        }
    }

    fun googleLogin() {
        var signInIntent = googleSignInClient?.signInIntent
        startActivityForResult(signInIntent, GOOGLE_LOGIN_CODE) // 구글 로그인 화면 인텐트 이동
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == GOOGLE_LOGIN_CODE) { // 구글 로그인 후 결과가 완료되면 파이어베이스 토큰으로 가져옴
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                var account = task.getResult(ApiException::class.java)!!
                Log.d(TAG, "firebaseAuthWithGoogle:" + account.id) // account.id 에는 구글 로그인 결과값
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                Log.w(TAG, "Google sign in failed", e)
            }

        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null) // auth 값 가져옴
        auth?.signInWithCredential(credential) // 파이어베이스 auth 설정
            ?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // 계정이 있으면 로그인 함
                    intentMainPage(task.result?.user)
                } else {
                    // 계정이 없을 경우 (이메일, 페스워드 틀릴 경우) 에러메세지 표시
                    Toast.makeText(this, task.exception?.message, Toast.LENGTH_LONG).show()
                }
            }

    }

    fun signinAndSignup() { // 첫 사용자인 경우 회원가입을 하고 로그인을 함
        auth?.createUserWithEmailAndPassword(et_email.text.toString(), et_password.text.toString())
            ?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // 계정이 생성 되었을 경우
                    intentMainPage(task.result?.user)
                } else if (!task.exception?.message.isNullOrEmpty()) {
                    // 로그인 에러가 나왔을 경우 에러 메세지 표시
                    Toast.makeText(this, task.exception?.message, Toast.LENGTH_LONG).show()
                } else {
                    signinEmail()
                }
            }
    }

    fun signinEmail() { // 기존 사용자인 경우 로그인을 함
        auth?.signInWithEmailAndPassword(et_email.text.toString(), et_password.text.toString())
            ?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // 계정이 있으면 로그인 함
                    intentMainPage(task.result?.user)
                } else {
                    // 계정이 없을 경우 (이메일, 페스워드 틀릴 경우) 에러메세지 표시
                    Toast.makeText(this, task.exception?.message, Toast.LENGTH_LONG).show()
                }
            }
    }

    fun intentMainPage(user: FirebaseUser?) {
        if (user != null) { // user 인자가 존재할 경우
            startActivity(Intent(this, MainActivity::class.java))
        }
    }

    companion object {
        const val TAG = "LoginActivity"
    }
}