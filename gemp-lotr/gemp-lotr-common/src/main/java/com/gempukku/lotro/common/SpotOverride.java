package com.gempukku.lotro.common;


import java.util.HashMap;
import java.util.Map;

/**
 * This class defines static SpotOverride maps to use as overrides when determining which inactive
 * cards are visible to the action being performed.
 */
public class SpotOverride {

	public static final Map<InactiveReason, Boolean> NONE = new HashMap<>();

	public static final Map<InactiveReason, Boolean> INCLUDE_ALL = Map.of(
			InactiveReason.OUT_OF_TURN, Boolean.TRUE,
			InactiveReason.ATTACHED_TO_INACTIVE, Boolean.TRUE,
			InactiveReason.STACKED, Boolean.TRUE,
			InactiveReason.HINDERED, Boolean.TRUE);

	public static final Map<InactiveReason, Boolean> INCLUDE_HINDERED = Map.of(
			InactiveReason.HINDERED, Boolean.TRUE);

	public static final Map<InactiveReason, Boolean> INCLUDE_STACKED = Map.of(
			InactiveReason.STACKED, Boolean.TRUE);


}

