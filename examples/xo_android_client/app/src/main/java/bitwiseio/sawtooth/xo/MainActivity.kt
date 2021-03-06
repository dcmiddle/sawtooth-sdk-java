package bitwiseio.sawtooth.xo

import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.design.widget.TabLayout
import android.support.v4.view.ViewPager
import android.support.v7.preference.PreferenceManager
import android.view.Menu
import android.view.MenuItem
import android.view.View
import com.google.gson.Gson
import bitwiseio.sawtooth.xo.adapters.PagerAdapter
import bitwiseio.sawtooth.xo.models.Game
import bitwiseio.sawtooth.xo.viewmodels.GameViewModel
import bitwiseio.sawtooth.xo.viewmodels.ViewModelFactory

class MainActivity : AppCompatActivity(), GameListFragment.OnListFragmentInteractionListener {

    private val gson = Gson()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById(R.id.list_view_toolbar))
        val viewPager = findViewById<ViewPager>(R.id.pager)
        setupViewPager(viewPager)
        val tabs = findViewById<View>(R.id.tabs) as TabLayout
        tabs.setupWithViewPager(viewPager)
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false)

        val fab: FloatingActionButton = findViewById(R.id.newGameFloatingButton)
        fab.setOnClickListener {
            val intent = Intent(this, CreateGameActivity::class.java)
            intent.putExtra("privateKey", gson.toJson(getPrivateKey(this)))
            startActivity(intent)
        }
    }

    private fun setupViewPager(viewPager: ViewPager) {
        val adapter = PagerAdapter(supportFragmentManager)
        val privateKey = getPrivateKey(this)
        val publicKey = getPublicKey(this, privateKey)
        adapter.addFragment(setUpFragment(getString(R.string.play_tab), publicKey), getString(R.string.play_tab))
        adapter.addFragment(setUpFragment(getString(R.string.watch_tab), publicKey), getString(R.string.watch_tab))
        adapter.addFragment(setUpFragment(getString(R.string.history_tab), publicKey), getString(R.string.history_tab))
        viewPager.adapter = adapter
    }

    private fun setUpFragment(tabName: String, publicKey: String): GameListFragment {
        val args = Bundle()
        args.putString("listFilter", tabName)
        args.putString("publicKey", publicKey)
        var frag = GameListFragment()
        frag.arguments = args
        return frag
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.list_view_menu, menu)
        return true
    }

    override fun onListFragmentInteraction(item: Game?) {
        val intent = Intent(this, GameBoardActivity::class.java)
        intent.putExtra("selectedGame", gson.toJson(item))
        intent.putExtra("privateKey", gson.toJson(getPrivateKey(this)))
        startActivity(intent)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.refresh_list -> {
            val model = ViewModelProviders.of(this, ViewModelFactory(getRestApiUrl(this,
                getString(R.string.rest_api_settings_key),
                getString(R.string.default_rest_api_address)))).get(GameViewModel::class.java)
            model.loadGames(true, getRestApiUrl(this,
                getString(R.string.rest_api_settings_key),
                getString(R.string.default_rest_api_address)))
            true
        }
        R.id.settings -> {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
            true
        }
        else -> {
            // If we got here, the user's action was not recognized.
            // Invoke the superclass to handle it.
            super.onOptionsItemSelected(item)
        }
    }
}
