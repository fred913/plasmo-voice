package su.plo.voice.client.gui.widgets;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import lombok.Setter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import su.plo.voice.client.config.entries.BooleanConfigEntry;
import su.plo.voice.client.gui.tabs.TabWidget;

import java.util.List;

public class ToggleButton extends AbstractWidget {
    private final TabWidget parent;
    private final PressAction action;
    private final BooleanConfigEntry toggled;
    private final Boolean forceValue;
    @Setter
    private List<Component> tooltip;

    public ToggleButton(TabWidget parent, int x, int y, int width, int height, BooleanConfigEntry toggled, PressAction action) {
        this(parent, x, y, width, height, toggled, null, action);
    }

    public ToggleButton(TabWidget parent, int x, int y, int width, int height, BooleanConfigEntry toggled, Boolean forceValue, PressAction action) {
        super(x, y, width, height, toggled.get() || forceValue != null && forceValue ? new TranslatableComponent("gui.plasmo_voice.on") : new TranslatableComponent("gui.plasmo_voice.off"));
        this.parent = parent;
        this.forceValue = forceValue;
        this.toggled = toggled;
        this.action = action;
    }

    public void updateValue() {
        this.setMessage(getText());
    }

    private Component getText() {
        return toggled.get() || forceValue != null && forceValue
                ? new TranslatableComponent("gui.plasmo_voice.on")
                : new TranslatableComponent("gui.plasmo_voice.off");
    }

    public void invertToggle() {
        this.toggled.invert();
        this.setMessage(getText());
        if (action != null) {
            action.onToggle(this.toggled.get());
        }
    }

    protected int getYImage(boolean hovered) {
        return 0;
    }

    @Override
    public void updateNarration(NarrationElementOutput narrationElementOutput) {
        this.defaultButtonNarrationText(narrationElementOutput);
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        this.invertToggle();
    }

    protected void renderBg(PoseStack matrices, Minecraft client, int mouseX, int mouseY) {
        RenderSystem.setShaderTexture(0, WIDGETS_LOCATION);
        int i = (this.isHovered() && this.active ? 2 : 1) * 20;
        if (this.toggled.get() || forceValue != null && forceValue) {
            blit(matrices, this.x + (int)((double)(this.width - 9)), this.y, 0, 46 + i, 4, 20);
            blit(matrices, this.x + (int)((double)(this.width - 9)) + 4, this.y, 196, 46 + i, 4, 20);
        } else {
            blit(matrices, this.x, this.y, 0, 46 + i, 4, 20);
            blit(matrices, this.x + 4, this.y, 196, 46 + i, 4, 20);
        }

        if (this.isHovered() && tooltip != null) {
            parent.setTooltip(tooltip);
        }
    }

    public interface PressAction {
        void onToggle(boolean toggled);
    }
}
