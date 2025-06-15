package com.gempukku.lotro.framework;

import com.gempukku.lotro.game.DefaultUserFeedback;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.game.state.GameState;
import com.gempukku.lotro.logic.timing.DefaultLotroGame;

import java.util.HashMap;

/**
 * This interface holds all the static definitions used throughout the test rig.  It is not a true interface, but
 * since Java does not support partial classes this will have to do.
 */
public interface TestBase {
	/**
	 * The first player, which the vast majority of the time is the Free Peoples player by convention.
	 */
	String P1 = "Free Peoples Player";
	/**
	 * The second player, which the vast majority of the time is the Shadow player by convention
	 */
	String P2 = "Shadow Player";

	/**
	 * A constant used for performing floating-point numeric comparisons in test assertions.  In effect, any decimal
	 * difference smaller than this amount will be completely ignored when determining if two floating point numbers
	 * are "equal".
	 */
	double epsilon = 0.001;

	/*
	 * The default sites to be included in both player's adventure decks.
	 *
	 * When choosing default sites, be sure to pick ones that include optional special abilities rather than
	 * modifiers or triggers, as these will need to be skipped past by each test and/or be a nasty surprise when
	 * adding up numbers.
	 */

	/**
	 * A set of benign default sites from Fellowship Block.
	 */
	HashMap<String, String> FellowshipSites = new HashMap<>() {{
		put("site1", "1_319");
		put("site2", "1_327");
		put("site3", "1_341");
		put("site4", "1_343");
		put("site5", "1_349");
		put("site6", "1_351");
		put("site7", "1_353");
		put("site8", "1_356");
		put("site9", "1_360");
	}};

	/**
	 * A set of benign default sites from King Block.
	 */
	HashMap<String, String> KingSites = new HashMap<>() {{
		put("site1", "7_330");
		put("site2", "7_335");
		put("site3", "8_117");
		put("site4", "7_342");
		put("site5", "7_345");
		put("site6", "7_350");
		put("site7", "8_120");
		put("site8", "10_120");
		put("site9", "7_360");
	}};

	/**
	 * A set of benign default sites from Shadows Block.
	 */
	HashMap<String, String> ShadowsSites = new HashMap<>() {{
		put("site1", "11_239");
		put("site2", "13_185");
		put("site3", "11_234");
		put("site4", "17_148");
		put("site5", "18_138");
		put("site6", "11_230");
		put("site7", "12_187");
		put("site8", "12_185");
		put("site9", "17_146");
	}};

	/*
	 * Some useful Ring-bearer and One Ring references.  This list can be expanded as needed.
	 */

	/**
	 * Frodo, Son of Drogo.
	 * 3 strength
	 * 4 vitality
	 * 10 resistance
	 * Fellowship: Exert another companion who has the Frodo signet to heal Frodo.
	 */
	String FOTRFrodo = "1_290";
	/**
	 * Gimli, Bearer of Grudges
	 * 6 strength
	 * 3 vitality
	 * 5 resistance
	 * Damage +1.
	 * While Gimli is the Ring-bearer, at the start of each skirmish involving him, add 2 burdens or 2 threats.
	 * While Gimli is damage +X, he is resistance +X.
	 */
	String GimliRB = "9_4";
	/**
	 * Galadriel, Bearer of Wisdom
	 * 3 strength
	 * 3 vitality
	 * 4 resistance
	 * While Galadriel bears an artifact or The One Ring, she is resistance +1 for each [Elven] companion you can spot.
	 */
	String GaladrielRB = "9_14";

	/**
	 * The One Ring, The Ruling Ring
	 * +1 strength
	 * Response: If bearer is about to take a wound in a skirmish, he wears The One Ring until the regroup phase.
	 * While wearing The One Ring, each time the Ring-bearer is about to take a wound during a skirmish, add a burden instead.
	 */
	String RulingRing = "1_2";
	/**
	 * The One Ring, Isildur's Bane
	 * +1 strength
	 * +1 vitality
	 * Response: If bearer is about to take a wound, he wears The One Ring until the regroup phase.
	 * While wearing The One Ring, each time the Ring-bearer is about to take a wound, add two burdens instead.
	 */
	String IsildursBaneRing = "1_1";
	/**
	 * The One Ring, Answer To All Riddles
	 * +2 vitality
	 * While wearing The One Ring, the Ring-bearer is strength +2, and each time he is about to take a wound in a skirmish, add a burden instead.
	 * Skirmish: Add a burden to wear The One Ring until the regroup phase.
	 */
	String ATARRing = "4_1";
	/**
	 * The One Ring, The Great Ring
	 * While wearing The One Ring, each time bearer is about to take a wound, add a burden instead.
	 * Maneuver: Wear The One Ring until the regroup phase. Skirmish: Add a burden to make the Ring-bearer strength +3.
	 */
	String GreatRing = "19_1";


	/*
	 * Formats that a game table might start up in.  These control deck legality (although this is ignored throughout
	 * the test rig) and each contain a few rules nuances.
	 */

	/**
	 * Fellowship Block format.
	 * Allowed sets: 1-3
	 * Sites: Fellowship Path
	 * Ring-bearer skirmish can be canceled: yes
	 */
	String Fellowship = "fotr_block";
	/**
	 * Pre-Shadows Multipath format.
	 * Allowed sets: 1-10 with an X-list
	 * Sites: Any one site path from Movie block
	 * Ring-bearer skirmish can be canceled: no
	 */
	String Multipath = "multipath";
	/**
	 * Expanded format.
	 * Allowed sets: 1-19 with an X-list
	 * Sites: Shadows Path
	 * Ring-bearer skirmish can be canceled: no
	 */
	String Shadows = "expanded";



	/*
	 * These three functions are used in the base interfaces but are unnecessary in the actual implementation, where the
	 * underlying fields can be used instead.
	 *
	 */

	/**
	 * @return Gets the virtual game table used by the test scenario.
	 */
	DefaultLotroGame game();

	/**
	 * @return Gets the game state of the virtual table used by the test scenario.
	 */
	GameState gameState();

	/**
	 * @return Gets the user decision manager.  This contains information relating to the decision that Gemp is
	 * currently waiting on.
	 */
	DefaultUserFeedback userFeedback();


	PhysicalCardImpl GetFreepsCard(String cardName);
	PhysicalCardImpl GetShadowCard(String cardName);
	PhysicalCardImpl GetCard(String player, String cardName);
}
