package com.example.laza.fragments.settings
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.example.laza.activites.ShoppingActivity
import com.example.laza.databinding.FragmentLanguageBinding
import com.example.laza.utils.MyPreference
import java.util.*

class LanguageFragment : Fragment() {
    private lateinit var binding: FragmentLanguageBinding
    lateinit var myPreference: MyPreference

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentLanguageBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val currentLanguage = Locale.getDefault().language

        myPreference = MyPreference(requireContext())
        Log.d("test", currentLanguage)
        when (currentLanguage) {
            "en" -> {
                changeToEnglish()
            }

            "ar" -> {
                changeToArabic()
            }
        }

        binding.linearArabic.setOnClickListener {
            changeLanguage("ar")
        }

        binding.linearEnglish.setOnClickListener {
            changeLanguage("en")
        }

        binding.arrow1.setOnClickListener {
            findNavController().navigateUp()
        }
    }


    private fun changeLanguage(language: String) {
        myPreference.setLanguage(language)
        val locale = Locale(language)
        Locale.setDefault(locale)
        val config = requireContext().resources.configuration
        config.setLocale(locale)
        requireContext().resources.updateConfiguration(config, requireContext().resources.displayMetrics)
        val intent = Intent(requireContext(), ShoppingActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
    }

    private fun changeToEnglish() {
        binding.imgEnglish.visibility = View.VISIBLE
        binding.imgArabic.visibility = View.GONE
    }

    private fun changeToArabic() {
        binding.imgEnglish.visibility = View.GONE
        binding.imgArabic.visibility = View.VISIBLE
    }




}