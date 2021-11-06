package su.plo.voice.client.gui.tabs;

import com.google.common.collect.ImmutableList;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import su.plo.voice.client.VoiceClient;
import su.plo.voice.client.config.ClientConfig;
import su.plo.voice.client.gui.VoiceSettingsScreen;
import su.plo.voice.client.gui.widgets.ConfigIntegerSlider;
import su.plo.voice.client.gui.widgets.DropDownWidget;
import su.plo.voice.client.gui.widgets.MicrophoneThresholdWidget;
import su.plo.voice.client.gui.widgets.ToggleButton;
import su.plo.voice.client.socket.SocketClientUDPListener;
import su.plo.voice.client.utils.TextUtils;
import su.plo.voice.rnnoise.Denoiser;

import java.util.ArrayList;
import java.util.List;

public class AdvancedTabWidget extends TabWidget {
    public AdvancedTabWidget(Minecraft client, VoiceSettingsScreen parent) {
        super(client, parent);

        ClientConfig config = VoiceClient.getClientConfig();

        ToggleButton rnNoise = new ToggleButton(0, 0, 97, 20, config.rnNoise,
                toggled -> {
                    VoiceClient.recorder.toggleRnNoise();
                });
        rnNoise.active = Denoiser.platformSupported();

        this.addEntry(new CategoryEntry(new TranslatableComponent("gui.plasmo_voice.advanced.noise_reduction")));
        this.addEntry(new OptionEntry(
                new TranslatableComponent("gui.plasmo_voice.advanced.rnnoise"),
                rnNoise,
                config.rnNoise,
                TextUtils.multiLine("gui.plasmo_voice.advanced.rnnoise.tooltip", 6),
                (button, element) -> {
                    VoiceClient.recorder.toggleRnNoise();
                    ((ToggleButton) element).updateValue();
                })
        );
        this.addEntry(new OptionEntry(
                new TranslatableComponent("gui.plasmo_voice.advanced.microphone_testing"),
                new MicrophoneThresholdWidget(0, 0, 97, parent.getSource() != null, false, parent),
                null,
                null)
        );
        List<String> bitRates = ImmutableList.of(
                "72",
                "96",
                "128",
                "192",
                "256",
                "384",
                "512"
        );
        List<Component> bitRatesMessages = new ArrayList<>();
        for (String bitRate : bitRates) {
            bitRatesMessages.add(new TextComponent(bitRate + " kbps"));
        }
        this.addEntry(new OptionEntry(
                new TranslatableComponent("jopus mode"),
                new DropDownWidget(parent, 0, 0, 97, 20,
                        new TextComponent(config.bitrate.get() + " kbps"),
                        bitRatesMessages,
                        false,
                        i -> {
                            int rate = Integer.parseInt(bitRates.get(i));
                            VoiceClient.recorder.updateBitRate(rate * 1000);
                            config.bitrate.set(rate);
                        }),
                config.bitrate,
                (button, element) -> {
                    VoiceClient.recorder.updateBitRate(config.bitrate.get() * 1000);
                    element.setMessage(new TextComponent(config.bitrate.get() + " kbps"));
                })
        );
//        this.addEntry(new OptionEntry(
//                new TranslatableComponent("jopus mode"),
//                new DropDownWidget(parent, 0, 0, 97, 20,
//                        new TextComponent(config.jopusMode.get()),
//                        ImmutableList.of(new TextComponent("voip"), new TextComponent("audio"), new TextComponent("low-delay")),
//                        false,
//                        i -> {
//                            VoiceClient.recorder.updateJopusMode();
//                            config.jopusMode.set(ImmutableList.of("voip", "audio", "low-delay").get(i));
//                        }),
//                config.jopusMode,
//                (button, element) -> {
//                    VoiceClient.recorder.updateJopusMode();
//                    element.setMessage(new TextComponent(config.jopusMode.get()));
//                })
//        );


        this.addEntry(new CategoryEntry(new TranslatableComponent("gui.plasmo_voice.advanced.compressor")));
        this.addEntry(new OptionEntry(
                new TranslatableComponent("gui.plasmo_voice.advanced.compressor"),
                new ToggleButton(0, 0, 97, 20, config.compressor,
                        toggled -> {}),
                VoiceClient.getClientConfig().compressor,
                TextUtils.multiLine("gui.plasmo_voice.advanced.compressor.tooltip", 4),
                (button, element) -> {
                    ((ToggleButton) element).updateValue();
                })
        );
        this.addEntry(new OptionEntry(
                new TranslatableComponent("gui.plasmo_voice.advanced.compressor.threshold"),
                new ConfigIntegerSlider(0, 0, 97, new TextComponent("dB"), config.compressorThreshold, null),
                VoiceClient.getClientConfig().compressorThreshold,
                TextUtils.multiLine("gui.plasmo_voice.advanced.compressor.threshold.tooltip", 4),
                (button, element) -> {
                    ((ConfigIntegerSlider) element).updateValue();
                })
        );
        this.addEntry(new OptionEntry(
                new TranslatableComponent("gui.plasmo_voice.advanced.limiter.threshold"),
                new ConfigIntegerSlider(0, 0, 97, new TextComponent("dB"), config.limiterThreshold, null),
                VoiceClient.getClientConfig().limiterThreshold,
                TextUtils.multiLine("gui.plasmo_voice.advanced.limiter.threshold.tooltip", 3),
                (button, element) -> {
                    ((ConfigIntegerSlider) element).updateValue();
                })
        );


        ConfigIntegerSlider directionalSourcesAngle = new ConfigIntegerSlider(0, 0, 97, config.directionalSourcesAngle);
        ToggleButton directionalSources = new ToggleButton(0, 0, 97, 20, config.directionalSources,
                toggled -> directionalSourcesAngle.active = toggled);
        directionalSourcesAngle.active = config.directionalSources.get();

        this.addEntry(new CategoryEntry(new TranslatableComponent("gui.plasmo_voice.advanced.engine")));
        this.addEntry(new OptionEntry(
                new TranslatableComponent("gui.plasmo_voice.advanced.hrtf"),
                new ToggleButton(0, 0, 97, 20, config.hrtf,
                        toggled -> VoiceClient.getSoundEngine().restart()),
                config.hrtf,
                TextUtils.multiLine("gui.plasmo_voice.advanced.hrtf.tooltip", 7),
                (button, element) -> {
                    VoiceClient.getSoundEngine().restart();
                    ((ToggleButton) element).updateValue();
                })
        );
        this.addEntry(new OptionEntry(
                new TranslatableComponent("gui.plasmo_voice.advanced.directional_sources"),
                directionalSources,
                config.directionalSources,
                TextUtils.multiLine("gui.plasmo_voice.advanced.directional_sources.tooltip", 5),
                (button, element) -> {
                    // kill all queues to prevent possible problems
                    SocketClientUDPListener.closeAll();

                    directionalSourcesAngle.active = config.directionalSources.get();
                    ((ToggleButton) element).updateValue();
                })
        );
        this.addEntry(new OptionEntry(
                new TranslatableComponent("gui.plasmo_voice.advanced.directional_sources_angle"),
                directionalSourcesAngle,
                config.directionalSourcesAngle,
                TextUtils.multiLine("gui.plasmo_voice.advanced.directional_sources_angle.tooltip", 4),
                (button, element) -> {
                    // kill all queues to prevent possible problems
                    SocketClientUDPListener.closeAll();

                    ((ConfigIntegerSlider) element).updateValue();
                })
        );


        this.addEntry(new CategoryEntry(new TranslatableComponent("gui.plasmo_voice.advanced.visual_ui")));
        this.addEntry(new OptionEntry(
                new TranslatableComponent("gui.plasmo_voice.advanced.visual_ui.distance"),
                new ToggleButton(0, 0, 97, 20, config.visualizeDistance,
                        toggled -> {}),
                config.visualizeDistance,
                (button, element) -> {
                    ((ToggleButton) element).updateValue();
                })
        );
        this.addEntry(new OptionEntry(
                new TranslatableComponent("gui.plasmo_voice.advanced.visual_ui.priority"),
                new ToggleButton(0, 0, 97, 20, config.showPriorityVolume,
                        toggled -> {
                            parent.updateGeneralTab();
                        }),
                config.showPriorityVolume,
                TextUtils.multiLine("gui.plasmo_voice.advanced.visual_ui.priority.tooltip", 2),
                (button, element) -> {
                    ((ToggleButton) element).updateValue();
                    parent.updateGeneralTab();
                })
        );
    }
}
