package shcm.shsupercm.mods.gamepadboat;

import me.sargunvohra.mcmods.autoconfig1u.ConfigData;
import me.sargunvohra.mcmods.autoconfig1u.annotation.Config;
import me.sargunvohra.mcmods.autoconfig1u.annotation.ConfigEntry;
import org.lwjgl.glfw.GLFW;

@Config(name = "gamepadboat")
public class GamepadBoatConfig implements ConfigData {
    @ConfigEntry.Gui.Tooltip
    public int controllerID = GLFW.GLFW_JOYSTICK_1;
    @ConfigEntry.Gui.Tooltip
    public long pollRate = 125;
    @ConfigEntry.Gui.Tooltip
    public long pwmCycle = 20;
    @ConfigEntry.Gui.Tooltip
    public float deadzone = 0.1f;
}
