package io.github.muntashirakon.setedit.adapters;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class KnownKeys {
    public static final List<String> KNOWN_KEYS = Arrays.asList(
        // System
        "screen_brightness", "screen_brightness_mode", "screen_off_timeout", "accelerometer_rotation",
        "haptic_feedback_enabled", "sound_effects_enabled", "vibrate_when_ringing",
        "font_scale", "system_locales", "time_12_24", "date_format", "volume_ring",
        "volume_music", "volume_alarm", "volume_notification", "volume_system", "volume_voice",

        // Secure
        "android_id", "bluetooth_on", "bluetooth_name", "data_roaming", "default_input_method",
        "enabled_input_methods", "location_mode", "location_providers_allowed", "mock_location",
        "adb_enabled", "development_settings_enabled", "usb_mass_storage_enabled",
        "wifi_on", "wifi_networks_available_notification_on", "wifi_watchdog_on",
        "allowed_geolocation_origins", "install_non_market_apps", "user_setup_complete",

        // Global
        "airplane_mode_on", "airplane_mode_radios", "animator_duration_scale",
        "transition_animation_scale", "window_animation_scale", "auto_time", "auto_time_zone",
        "bluetooth_on", "development_settings_enabled", "device_name", "mobile_data",
        "network_preference", "usb_mass_storage_enabled", "wifi_sleep_policy"
    );
}
