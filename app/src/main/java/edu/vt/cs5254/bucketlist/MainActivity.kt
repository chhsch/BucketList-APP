package edu.vt.cs5254.bucketlist

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import edu.vt.cs5254.bucketlist.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    // Name: Chih-Hsing Chen
    // VT Username: hathawayc080119
    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        // Check if there's no saved state, then add the GoalDetailFragment
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
//                .replace(R.id.fragmentContainerView, GoalListFragment())
                .commit()
        }

    }}
