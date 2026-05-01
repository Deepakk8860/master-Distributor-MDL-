package com.android.masterdistributormdl.login
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.android.masterdistributormdl.R
import com.android.masterdistributormdl.databinding.LoginBinding

import com.android.masterdistributormdl.utils.AlertError
import com.android.masterdistributormdl.utils.InternetError
import com.android.masterdistributormdl.utils.Loading
import com.android.masterdistributormdl.utils.STATUS_COLOR1
import com.android.masterdistributormdl.utils.addFragment
import com.android.masterdistributormdl.utils.clearAllEditTextFocus
import com.android.masterdistributormdl.utils.setEditText
import com.android.masterdistributormdl.utils.shooterFragment
import com.android.masterdistributormdl.utils.showToastShort
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.gson.JsonObject
import kotlinx.coroutines.launch


class Login : Fragment() {
    private lateinit var binding: LoginBinding
    lateinit var model: LoginModel
    private lateinit var googleSignInClient: GoogleSignInClient
    private val RC_SIGN_IN = 100

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding=
            DataBindingUtil.inflate(inflater, R.layout.login, container, false)
        model = ViewModelProvider(this)[LoginModel::class.java]
        binding.model = model
        binding.lifecycleOwner = this
        binding.executePendingBindings()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHeader()
        initView()
        handleRetrofitMessage()
        initListener()
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            handleSignInResult(task)
        }
    }

    private fun handleSignInResult(task: Task<GoogleSignInAccount>) {
        try {
            val account = task.getResult(ApiException::class.java)
            verifyGoogleLogin(account.email,account.photoUrl)
//            Toast.makeText(requireContext(), "Signed in as: ${account.displayName}", Toast.LENGTH_SHORT).show()
            // You can now use account.idToken, account.email, etc.
        } catch (e: ApiException) {
            Toast.makeText(requireContext(), "Sign in failed", Toast.LENGTH_SHORT).show()
        }
    }

    private fun initView() {
        setEditText(binding.emailInput)

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(requireActivity(), gso)
    }


    private fun handleRetrofitMessage() {
        model.retrofitError.observe(viewLifecycleOwner) {
            if (it.status == 0) {
                showToastShort(it.message)
            } else if (it.status == 1) {
                InternetError.show(requireContext())
            }
        }
        model.errorMessage.observe(viewLifecycleOwner) {
            if (!it.isNullOrEmpty()) AlertError.show(requireContext(), it) {}
        }
        model.isLoaderVisible.observe(viewLifecycleOwner) { it ->
            Loading.showHide(requireActivity(), it)
        }
    }

    private fun initListener() {
        binding.relGoogleLogin.setOnClickListener {
            handleGoogleLogin()
        }


        binding.loginButton.setOnClickListener {
            if (validateInputs()) {
                handleLogin()
            }

        }
    }

    private fun verifyGoogleLogin(email: String?, photoUrl: Uri?) {
        val profileImgUrl = photoUrl?.toString() ?: ""
        val param = JsonObject()
        param.addProperty("user_email", email)
        param.addProperty("profile_img", profileImgUrl)
        model.verifyByGoogleLogin(param) {
            if (it.status == 0) {
                val data =it.data
                lifecycleScope.launch {
                    model.save(it.data.userid, data.session_id)
                    model.getUserProfile(requireActivity())
                }
            } else {
                AlertError.show(requireContext(), it.message) {}
            }
        }

    }


    private fun handleGoogleLogin() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    private fun handleLogin() {
        val param = JsonObject()
        param.addProperty("username", binding.emailInput.text.toString())
        model.loginUser(param) {
            if (it.status == 0) {
                addFragment(requireActivity(),OtpVerify(), bundleOf("email" to binding.emailInput.text.toString()))
            }else{
                AlertError.show(requireContext(),it.message){}
            }
        }
    }


    private fun sendOtpToEmail() {

        addFragment(requireActivity(),OtpVerify())
    }

    private fun validateInputs(): Boolean {
        clearAllEditTextFocus(binding.emailInput)
        val email = binding.emailInput.text.toString().trim()

        var isValid = true

        if (email.isEmpty()) {
            isValid = false
            binding.emailInput.setBackgroundResource(R.drawable.edt_error)
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            isValid = false
            binding.emailInput.setBackgroundResource(R.drawable.edt_error)
        }else{
            binding.emailInput.setBackgroundResource(R.drawable.edt_selected)
        }
        return isValid
    }


    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (!hidden) {
            setHeader()
        }
    }


    private fun setHeader() {
        try {
            shooterFragment = this
            (activity as com.android.masterdistributormdl.gskDistributor.view.MainActivity).setHeader("Login", STATUS_COLOR1)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

}


