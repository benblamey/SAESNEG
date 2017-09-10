package com.benblamey.saesneg.phaseB;

public enum FestibusFeatures {

    /////////////
    // Friends
    /////////////
/////////////
    // Friends
    /////////////
    Friends_InCommon,

    /////////////
    // Events (well-known)
    /////////////
    Events,

    /////////////
    // Spatial
    /////////////
    Spatial_SameLocation,

    /////////////
    // Temporal
    /////////////
    Temporal, // Used for either the legacy similarity measure, or the alternative distributed measure.

    /////////////
    // User Help
    /////////////
    User_PhotosInSameAlbum,

    /////////////
    // Scene
    /////////////
    Scene_ColorLayout,
    Scene_scColor,
    Scene_edgeHistogram,
    //Scene_identicalPhoto,

    /////////////
    // Kind
    /////////////
    // These are referenced by reflection - don't rename them!
    Kind_Album_Album,
    Kind_Album_Checkin,
    Kind_Album_Event,
    Kind_Album_Link,
    Kind_Album_Photo,
    Kind_Album_StatusMessage,
    Kind_Checkin_Checkin,
    Kind_Checkin_Event,
    Kind_Checkin_Link,
    Kind_Checkin_Photo,
    Kind_Checkin_StatusMessage,
    Kind_Event_Event,
    Kind_Event_Link,
    Kind_Event_Photo,
    Kind_Event_StatusMessage,
    Kind_Link_Link,
    Kind_Link_Photo,
    Kind_Link_StatusMessage,
    Kind_Photo_Photo,
    Kind_Photo_StatusMessage,
    Kind_StatusMessage_StatusMessage,
    LAST
}
