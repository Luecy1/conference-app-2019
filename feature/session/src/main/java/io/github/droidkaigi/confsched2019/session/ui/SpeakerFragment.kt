package io.github.droidkaigi.confsched2019.session.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.navigation.NavController
import dagger.Module
import dagger.Provides
import dagger.android.support.AndroidSupportInjection
import io.github.droidkaigi.confsched2019.di.PageScope
import io.github.droidkaigi.confsched2019.ext.android.changedNonNull
import io.github.droidkaigi.confsched2019.model.defaultLang
import io.github.droidkaigi.confsched2019.session.R
import io.github.droidkaigi.confsched2019.session.databinding.FragmentSpeakerBinding
import io.github.droidkaigi.confsched2019.session.ui.actioncreator.SpeakerActionCreator
import io.github.droidkaigi.confsched2019.session.ui.store.SessionsStore
import io.github.droidkaigi.confsched2019.session.ui.store.SpeakerStore
import io.github.droidkaigi.confsched2019.session.ui.widget.DaggerFragment
import me.tatarka.injectedvmprovider.ktx.injectedViewModelProvider
import javax.inject.Inject

class SpeakerFragment : DaggerFragment() {
    lateinit var binding: FragmentSpeakerBinding

    @Inject lateinit var speakerActionCreator: SpeakerActionCreator
    @Inject lateinit var speakerStoreFactory: SpeakerStore.Factory
    @Inject lateinit var sessionsStore: SessionsStore
    @Inject lateinit var navController: NavController

    private val speakerStore: SpeakerStore by lazy {
        injectedViewModelProvider
            .get(SpeakerStore::class.java.name) {
                speakerStoreFactory.create(speakerFragmentArgs.speaker)
            }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_speaker, container,
            false
        )
        return binding.root
    }

    private lateinit var speakerFragmentArgs: SpeakerFragmentArgs

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        AndroidSupportInjection.inject(this)

        speakerFragmentArgs = SpeakerFragmentArgs.fromBundle(arguments)

        val speakerId = speakerFragmentArgs.speaker
        speakerActionCreator.load(speakerId)
        speakerStore.speaker.changedNonNull(
            this
        ) { speaker ->
            binding.speaker = speaker
        }

        val sessionLiveData = sessionsStore.speakerSessionBySpeakerId(speakerId)
        sessionLiveData
            .changedNonNull(viewLifecycleOwner) { session ->
                binding.session = session
                binding.lang = defaultLang()
            }
        binding.speakerSessionBackground.setOnClickListener {
            navController.navigate(
                SpeakerFragmentDirections.actionSpeakerToSessionDetail(
                    sessionLiveData.value?.id ?: return@setOnClickListener
                )
            )
        }
    }
}

@Module
abstract class SpeakerFragmentModule {

    @Module
    companion object {
        @JvmStatic @Provides
        @PageScope
        fun providesLifecycle(sessionsFragment: SpeakerFragment): Lifecycle {
            return sessionsFragment.viewLifecycleOwner.lifecycle
        }

        @JvmStatic @Provides fun provideActivity(
            sessionsFragment: SpeakerFragment
        ): FragmentActivity {
            return sessionsFragment.requireActivity()
        }
    }
}
