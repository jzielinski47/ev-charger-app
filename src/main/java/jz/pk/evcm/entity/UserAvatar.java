package jz.pk.evcm.entity;

/**
 * Preset avatars a user can choose from. Only the key is stored; the frontend maps each key
 * to a fixed image, so no user data is ever sent to an image service.
 * ref. api: https://www.dicebear.com/
 */
public enum UserAvatar {
    AURORA,
    BOLT,
    COMET,
    NOVA,
    VOLT
}
