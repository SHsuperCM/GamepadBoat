package shcm.shsupercm.mods.gamepadboat;

import io.github.prospector.modmenu.api.ConfigScreenFactory;
import io.github.prospector.modmenu.api.ModMenuApi;
import me.sargunvohra.mcmods.autoconfig1u.AutoConfig;
import me.sargunvohra.mcmods.autoconfig1u.serializer.GsonConfigSerializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.java.games.input.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.toast.SystemToast;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.text.LiteralText;
import org.lwjgl.glfw.GLFWGamepadState;


import static org.lwjgl.glfw.GLFW.*;

public class GamepadBoat implements ModInitializer, ModMenuApi {
    public GamepadBoatConfig config;

    private GLFWGamepadState controller = null;

    private float acceleration = 0f, breaks = 0f, direction = 0f;

    private long t = 0;

    @Override
    public void onInitialize() {
        AutoConfig.register(GamepadBoatConfig.class, GsonConfigSerializer::new);
        config = AutoConfig.getConfigHolder(GamepadBoatConfig.class).getConfig();

        ClientTickEvents.START_CLIENT_TICK.register(minecraft -> {
            if (controller == null) {
                if (glfwJoystickPresent(config.controllerID) && glfwJoystickIsGamepad(config.controllerID)) {
                    controller = GLFWGamepadState.create();
                    if (MinecraftClient.getInstance().player != null)
                        MinecraftClient.getInstance().getToastManager().add(new SystemToast(SystemToast.Type.NARRATOR_TOGGLE, new LiteralText("Connected Boat Gamepad"), new LiteralText(glfwGetJoystickName(config.controllerID))));
                }
            } else {
                if (!glfwJoystickPresent(config.controllerID) || !glfwJoystickIsGamepad(config.controllerID)) {
                    controller = null;
                    if (MinecraftClient.getInstance().player != null)
                        MinecraftClient.getInstance().getToastManager().add(new SystemToast(SystemToast.Type.NARRATOR_TOGGLE, new LiteralText("Disconnected Boat Gamepad"), null));
                }
            }
        });

        new Thread(() -> {
            try {
                while (!Thread.interrupted()) {
                    if (controller != null) {
                        try {
                            glfwGetGamepadState(config.controllerID, controller);
                            onTick();
                        } catch (Exception ignored) {}
                        Thread.sleep(1000 / config.pollRate);
                    }
                }
            } catch (InterruptedException ignored) {}
        }).start();
    }

    private void onTick() {
        if (MinecraftClient.getInstance().player == null || !(MinecraftClient.getInstance().player.getVehicle() instanceof BoatEntity)) return;

        float leftX = controller.axes().get(GLFW_GAMEPAD_AXIS_LEFT_X);
        float rightTrigger = (controller.axes().get(GLFW_GAMEPAD_AXIS_RIGHT_TRIGGER) + 1f) / 2f;
        float leftTrigger = (controller.axes().get(GLFW_GAMEPAD_AXIS_LEFT_TRIGGER) + 1f) / 2f;

        if (leftX > -config.deadzone && leftX < config.deadzone)
            direction = 0f;
        else
            direction = leftX * config.pwmCycle;

        if (rightTrigger < config.deadzone)
            acceleration = 0f;
        else
            acceleration = rightTrigger * config.pwmCycle;

        if (leftTrigger < config.deadzone)
            breaks = 0f;
        else
            breaks = leftTrigger * config.pwmCycle;


        t++;

        MinecraftClient.getInstance().options.keyLeft.setPressed(direction < 0f && (t % config.pwmCycle) < -direction);
        MinecraftClient.getInstance().options.keyRight.setPressed(direction > 0f && (t % config.pwmCycle) < direction);
        MinecraftClient.getInstance().options.keyForward.setPressed(acceleration > 0f && (t % config.pwmCycle) < acceleration);
        MinecraftClient.getInstance().options.keyBack.setPressed(breaks > 0f && (t % config.pwmCycle) < breaks);
    }

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return (ConfigScreenFactory<Screen>) screen -> AutoConfig.getConfigScreen(GamepadBoatConfig.class, screen).get();
    }
}
