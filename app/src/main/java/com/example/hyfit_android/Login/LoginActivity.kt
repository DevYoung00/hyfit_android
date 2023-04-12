package com.example.hyfit_android.Login

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.widget.Toast
import com.example.hyfit_android.Join.JoinActivity1
import com.example.hyfit_android.MainActivity
import com.example.hyfit_android.UserInfo.ValidExpiredActivity
import com.example.hyfit_android.UserInfo.ValidView
import com.example.hyfit_android.UserRetrofitService
import com.example.hyfit_android.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity(), LoginView, ValidView {

    lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        var ljwt:String = intent.getStringExtra("logoutjwt").toString()
        var invalid=intent.getBooleanExtra("invalid", false)
        Log.d("logoutjwt", ljwt)

        if(invalid){
            deleteJwt()
            Toast.makeText(this, "Token expired", Toast.LENGTH_LONG).show()
        }


        binding.join.setOnClickListener {
            init()
            val intent=Intent(this, JoinActivity1::class.java)
            startActivity(intent)
        }

        binding.loginNext.setOnClickListener{
            login()
//            val intent=Intent(this, MainActivity::class.java)
//            startActivity(intent)
        }
        binding.forgot.setOnClickListener {
            init()
            val intent=Intent(this, FindPasswordActivity1::class.java)
            startActivity(intent)
        }


    }

    private fun login(){
        if (binding.textEmail.text.toString().isEmpty() || binding.textPassword.text.toString().isEmpty()){
            Toast.makeText(this,"Fill in all the blanks", Toast.LENGTH_LONG).show()
            Log.d("test", "fill in all the blanks")
            return
        }

        val email:String=binding.textEmail.text.toString()
        val password:String=binding.textPassword.text.toString()
        val usService = UserRetrofitService()
        usService.setLoginView(this)
        usService.login(LoginReq(email, password))

    }
    private fun saveJwt(jwt:String?){
        val spf=getSharedPreferences("auth", MODE_PRIVATE)
        val editor=spf.edit()
        editor.putString("jwt", jwt)
        editor.apply()
        editor.commit()
    }

    private fun getJwt():String?{
        val spf = getSharedPreferences("auth", AppCompatActivity.MODE_PRIVATE)
        return spf!!.getString("jwt","0")
    }

    private fun init(){
        binding.textEmail.text = null
        binding.textPassword.text = null
    }


    override fun onLoginSuccess(code: Int, jwt: String) {
        when(code){
            1000->{
                Log.d("Success", code.toString())
                saveJwt(jwt)
                val intent=Intent(this, MainActivity::class.java)
                init()
                startActivity(intent)
                if (intent.component?.className == "com.example.hyfit_android.MainActivity") {
                    // MainActivity로 전환된 경우에만 타이머 시작
                    startTimer()
                }
            }
            else->{
                Log.d("error", code.toString())
                if(code.toString()=="2003"){
                    Toast.makeText(this, "Incorrect password", Toast.LENGTH_LONG).show()
                    init()
                }
                else if(code.toString()=="2002")
                {
                    Toast.makeText(this, "Email does not exist", Toast.LENGTH_LONG).show()
                    init()
                }
            }
        }
    }


    override fun onLoginFailure(code: Int, msg: String) {
            Log.d("failure", code.toString())
            if(code.toString()=="2003"){
                Toast.makeText(this, "Incorrect password", Toast.LENGTH_LONG).show()
                init()
            }
            else if(code.toString()=="2002")
            {
                Toast.makeText(this, "Email does not exist", Toast.LENGTH_LONG).show()
                init()
            }
    }

    private fun startTimer() {
        val timer = object : CountDownTimer(50*1000, 100) {
            override fun onTick(millisUntilFinished: Long) {
            }

            override fun onFinish() {
                valid()
                //startTimer()
            }
        }

        timer.start()
    }

    private fun valid(){
        val spf = getSharedPreferences("auth", AppCompatActivity.MODE_PRIVATE)
        val jwt:String?=spf!!.getString("jwt","0")
        val usService=UserRetrofitService()
        usService.setvalidView(this)
        usService.valid(jwt)
    }
    override fun onValidSuccess(code: Int, result: String) {
        when(code){
            1000->{
                if(result=="invalid"){
                    Log.d("jwt invalid", result)
                    val intent=Intent(this, ValidExpiredActivity::class.java)
                    intent.putExtra("invalid", true)
                    startActivity(intent)

                }
                else{
                    Log.d("oldone", getJwt().toString())
                    saveJwt(result)
                    Log.d("newone", getJwt().toString())
                    startTimer()
                }
            }
            else->Log.d("validsad", "sadsad")
        }
    }

    override fun onValidFailure(code: Int, msg: String) {
        Log.d("Can't valid", "sadasd")

    }
    private fun deleteJwt(){
        val spf=getSharedPreferences("auth", MODE_PRIVATE)
        val editor=spf.edit()
        editor.clear()
        editor.apply()
        editor.commit()
    }

}