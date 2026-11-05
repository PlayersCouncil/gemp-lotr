package com.gempukku.lotro.cards.unofficial.hobbit.set31;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_31_029_Tests
{

	// The Hobbit table: the Hobbit path, whose site 5 is Mirkwood 30_53 ("When the fellowship moves to Mirkwood,
	// discard Gandalf": no Gandalf is laid), and Bilbo, Reliable Companion 30_46 as the Ring-bearer.  Turn 1's
	// skirmishes are at The Troll Hoard 30_50 (site 2).
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("feuds", "31_29");

					put("thorin", "30_19");     // Thorin, Oakenshield: [dwarven] companion, strength 7 (8 beside Dain), vitality 4; a Fellowship ability not used
					put("balin", "30_2");       // Balin, Brother of Dwalin: [dwarven] companion, strength 7; a Fellowship ability not used
					put("dwalin", "30_9");      // Dwalin, Brother of Balin: [dwarven] companion, damage +1; a Maneuver ability not used
					put("dain", "32_2");        // Dain Ironfoot: a [dwarven] ally, strength 7; Thorin is strength +1
					put("orcrist", "30_17");    // Orcrist: a [dwarven] artifact hand weapon, on Thorin
					put("oakenshield", "31_6"); // Oakenshield: a [dwarven] possession, a shield and no weapon, on Thorin
					put("ironforged", "32_18"); // Iron-Forged Weapon: an [esgaroth] possession hand weapon, on a [dwarven] companion
					put("axe", "31_1");         // Dwarven Axe: a [dwarven] possession hand weapon, on a [dwarven] character
					put("spear", "32_4");       // Dwarven Spear: a [dwarven] possession hand weapon, on a [dwarven] character
					put("sting", "30_47");      // Sting: a [shire] artifact hand weapon, on Bilbo

					put("elrond", "30_21");     // Elrond, Elven Lord: [elven] ally; a start-of-turn heal and a Regroup ability, not used
					put("hadafang", "33_13");   // Hadafang: an [elven] artifact, on Elrond; no ally
					put("tauriel", "31_10");    // Tauriel, Staunch Defender: [elven] ally; a Maneuver ability not used
					put("legolas", "31_9");     // Legolas, Prince of Mirkwood: [elven] ally; an Archery ability not used
					put("lindir", "33_14");     // Lindir, Elven Minstrel: [elven] ally; heals an [elven] ally on a move (none is wounded)
					put("bard", "30_22");       // Bard, The Bowman: [esgaroth] Man ally; a Regroup ability not used

					put("marauder", "30_38");   // Orkish Marauder: [gundabad] Orc, strength 8, vitality 2; abilities not used
					put("watchful", "30_40");   // Watchful Orc: [gundabad] Orc, strength 8, vitality 3; abilities not used
				}},
				null, null, null,
				VirtualTableScenario.HobbitDraft
		);
	}

	// The same, with Old Forest Road 30_62 as site 5 in place of Mirkwood: a site 5 of another title.
	protected VirtualTableScenario GetOldForestRoadScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("feuds", "31_29");
					put("dwalin", "30_9");      // Dwalin, Brother of Balin: [dwarven] companion; a Maneuver ability not used
					put("axe", "31_1");         // Dwarven Axe: a [dwarven] possession hand weapon
				}},
				new HashMap<>()
				{{
					putAll(VirtualTableScenario.HobbitSites);
					// Old Forest Road: at the start of the Shadow phase, may discard Gandalf (none is laid) to play a minion.
					put("site5", "30_62");
				}},
				null,
				null,
				VirtualTableScenario.HobbitDraft
		);
	}

	@Test
	public void AncestralFeudsStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 31
		 * Name: Ancestral Feuds
		 * Unique: True
		 * Side: Shadow
		 * Culture: Mirkwood
		 * Twilight Cost: 2
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: Each time the fellowship moves to site 5, discard each weapon borne by a [dwarven] companion.<br><b>Skirmish:</b> Remove (3) to make a [dwarven] character strength -X (limit -3), where X is the number of [elven] allies you spot.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("feuds");

		assertEquals("Ancestral Feuds", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.MIRKWOOD, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void MovingToSite5DiscardsEachWeaponBorneByADwarvenCompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var thorin = scn.GetFreepsCard("thorin");
		var balin = scn.GetFreepsCard("balin");
		var dwalin = scn.GetFreepsCard("dwalin");
		var dain = scn.GetFreepsCard("dain");
		var orcrist = scn.GetFreepsCard("orcrist");
		var oakenshield = scn.GetFreepsCard("oakenshield");
		var ironforged = scn.GetFreepsCard("ironforged");
		var axe = scn.GetFreepsCard("axe");
		var spear = scn.GetFreepsCard("spear");
		var sting = scn.GetFreepsCard("sting");
		var bilbo = scn.GetRingBearer();
		var feuds = scn.GetShadowCard("feuds");

		scn.StartGame();
		scn.SkipToSite(4);
		scn.MoveCompanionsToTable(thorin, balin, dwalin);
		scn.MoveAlliesToTable(dain);
		scn.AttachCardsTo(thorin, orcrist, oakenshield);
		scn.AttachCardsTo(balin, ironforged);
		scn.AttachCardsTo(dwalin, axe);
		scn.AttachCardsTo(dain, spear);
		scn.AttachCardsTo(bilbo, sting);
		scn.MoveCardsToSupportArea(feuds);
		assertAttachedTo(orcrist, thorin);
		assertAttachedTo(oakenshield, thorin);
		assertAttachedTo(ironforged, balin);
		assertAttachedTo(axe, dwalin);
		assertAttachedTo(spear, dain);
		assertAttachedTo(sting, bilbo);
		assertInZone(Zone.SUPPORT, feuds);

		scn.FreepsPass();
		assertEquals(5, scn.GetCurrentSiteNumber());
		// Mirkwood's own "discard Gandalf" answers the same move: the Free Peoples player orders the two.
		scn.FreepsResolveActionOrder("Ancestral Feuds");

		// Weapons of two cultures and two item types, on three [dwarven] companions.
		assertInDiscard(orcrist, ironforged, axe);
		// A shield, which is no weapon, on a [dwarven] companion; a weapon on a [dwarven] ally; a weapon on a
		// companion of another culture.
		assertAttachedTo(oakenshield, thorin);
		assertAttachedTo(spear, dain);
		assertAttachedTo(sting, bilbo);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void MovingToASite5OfAnotherTitleDiscardsTheWeaponAsWell() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetOldForestRoadScenario();

		var dwalin = scn.GetFreepsCard("dwalin");
		var axe = scn.GetFreepsCard("axe");
		var feuds = scn.GetShadowCard("feuds");

		scn.StartGame();
		scn.SkipToSite(4);
		scn.MoveCompanionsToTable(dwalin);
		scn.AttachCardsTo(dwalin, axe);
		scn.MoveCardsToSupportArea(feuds);
		assertAttachedTo(axe, dwalin);
		assertInZone(Zone.SUPPORT, feuds);

		scn.FreepsPass();
		assertEquals("30_62", scn.GetCurrentSite().getBlueprintId());
		assertInDiscard(axe);
		assertInZone(Zone.FREE_CHARACTERS, dwalin);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void MovingToSite4DiscardsNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var thorin = scn.GetFreepsCard("thorin");
		var dwalin = scn.GetFreepsCard("dwalin");
		var orcrist = scn.GetFreepsCard("orcrist");
		var axe = scn.GetFreepsCard("axe");
		var feuds = scn.GetShadowCard("feuds");

		scn.StartGame();
		scn.SkipToSite(3);
		scn.MoveCompanionsToTable(thorin, dwalin);
		scn.AttachCardsTo(thorin, orcrist);
		scn.AttachCardsTo(dwalin, axe);
		scn.MoveCardsToSupportArea(feuds);
		assertAttachedTo(orcrist, thorin);
		assertAttachedTo(axe, dwalin);
		assertInZone(Zone.SUPPORT, feuds);

		scn.FreepsPass();
		assertEquals(4, scn.GetCurrentSiteNumber());
		assertAttachedTo(orcrist, thorin);
		assertAttachedTo(axe, dwalin);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void SkirmishActionRemoves3ToMakeADwarvenCharacterStrengthMinusTheElvenAlliesSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var thorin = scn.GetFreepsCard("thorin");
		var balin = scn.GetFreepsCard("balin");
		var dain = scn.GetFreepsCard("dain");
		var elrond = scn.GetFreepsCard("elrond");
		var hadafang = scn.GetFreepsCard("hadafang");
		var tauriel = scn.GetFreepsCard("tauriel");
		var bard = scn.GetFreepsCard("bard");
		var bilbo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(thorin, balin);
		scn.MoveAlliesToTable(dain, elrond, tauriel, bard);
		scn.AttachCardsTo(elrond, hadafang);

		var feuds = scn.GetShadowCard("feuds");
		var marauder = scn.GetShadowCard("marauder");
		var watchful = scn.GetShadowCard("watchful");
		scn.MoveCardsToSupportArea(feuds);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, feuds);
		assertAttachedTo(hadafang, elrond);
		scn.MoveMinionsToTable(marauder, watchful);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{thorin, marauder}, new PhysicalCardImpl[]{balin, watchful});
		scn.FreepsResolveSkirmish(thorin);
		scn.SetTwilight(5);
		scn.FreepsPass();

		assertEquals(5, scn.GetTwilight());
		// 7 + 1 for Dain.
		assertEquals(8, scn.GetStrength(thorin));
		assertEquals(7, scn.GetStrength(balin));
		assertEquals(7, scn.GetStrength(dain));
		assertTrue(scn.ShadowActionAvailable(feuds));
		scn.ShadowUseCardAction(feuds);
		assertEquals(2, scn.GetTwilight());

		// Elrond and Tauriel; not Hadafang, an [elven] card that is no ally, nor Bard, an ally of another culture.
		// The number comes filled in with all there are (F-0561).
		assertEquals(0, scn.ShadowGetChoiceMin());
		assertEquals(2, scn.ShadowGetChoiceMax());
		assertEquals(Integer.valueOf(2), scn.ShadowGetChoiceDefault());
		scn.ShadowChoose("2");

		// [dwarven] companions in this skirmish and in one still to come, and a [dwarven] ally; not Bilbo, a
		// companion of another culture, nor a minion.
		assertTrue(scn.ShadowHasCardChoiceAvailable(thorin, balin, dain));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(bilbo, marauder));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(thorin);

		assertEquals(6, scn.GetStrength(thorin));
		assertEquals(7, scn.GetStrength(balin));
		assertEquals(7, scn.GetStrength(dain));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void TheCardGivesNoMoreThanMinus3InEachSkirmishPhaseAndTheMinusIsGoneInTheNext() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var thorin = scn.GetFreepsCard("thorin");
		var balin = scn.GetFreepsCard("balin");
		var dain = scn.GetFreepsCard("dain");
		var elrond = scn.GetFreepsCard("elrond");
		var tauriel = scn.GetFreepsCard("tauriel");
		var legolas = scn.GetFreepsCard("legolas");
		var lindir = scn.GetFreepsCard("lindir");
		scn.MoveCompanionsToTable(thorin, balin);
		scn.MoveAlliesToTable(dain, elrond, tauriel, legolas, lindir);

		var feuds = scn.GetShadowCard("feuds");
		var marauder = scn.GetShadowCard("marauder");
		var watchful = scn.GetShadowCard("watchful");
		scn.MoveCardsToSupportArea(feuds);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, feuds);
		scn.MoveMinionsToTable(marauder, watchful);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{thorin, marauder}, new PhysicalCardImpl[]{balin, watchful});
		scn.FreepsResolveSkirmish(thorin);
		scn.SetTwilight(9);
		scn.FreepsPass();

		assertEquals(8, scn.GetStrength(thorin));
		assertEquals(7, scn.GetStrength(balin));
		scn.ShadowUseCardAction(feuds);
		// Four [elven] allies, all spotted: -3, not -4.
		assertEquals(4, scn.ShadowGetChoiceMax());
		scn.ShadowChoose("4");
		scn.ShadowChooseCard(thorin);
		assertEquals(5, scn.GetStrength(thorin));
		assertEquals(6, scn.GetTwilight());

		// The limit is the card's for the phase, not each target's: offered again, paid, and Balin gets nothing.
		scn.FreepsPass();
		assertTrue(scn.ShadowActionAvailable(feuds));
		scn.ShadowUseCardAction(feuds);
		scn.ShadowChoose("4");
		scn.ShadowChooseCard(balin);
		assertEquals(3, scn.GetTwilight());
		assertEquals(7, scn.GetStrength(balin));
		assertEquals(5, scn.GetStrength(thorin));

		// Balin's skirmish is a phase of its own: Thorin is himself again, and the limit is fresh.
		scn.PassSkirmishActions();
		scn.FreepsResolveSkirmish(balin);
		assertEquals(8, scn.GetStrength(thorin));
		scn.FreepsPass();
		scn.ShadowUseCardAction(feuds);
		// One of the four spotted, fewer than there are.
		scn.ShadowChoose("1");
		scn.ShadowChooseCard(balin);
		assertEquals(0, scn.GetTwilight());
		assertEquals(6, scn.GetStrength(balin));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void WithNoElvenAllyTheSkirmishActionIsPaidForAndChangesNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var thorin = scn.GetFreepsCard("thorin");
		var balin = scn.GetFreepsCard("balin");
		var bard = scn.GetFreepsCard("bard");
		scn.MoveCompanionsToTable(thorin, balin);
		scn.MoveAlliesToTable(bard);

		var feuds = scn.GetShadowCard("feuds");
		var marauder = scn.GetShadowCard("marauder");
		scn.MoveCardsToSupportArea(feuds);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, feuds);
		scn.MoveMinionsToTable(marauder);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(thorin, marauder);
		scn.FreepsResolveSkirmish(thorin);
		scn.SetTwilight(5);
		scn.FreepsPass();

		assertEquals(5, scn.GetTwilight());
		assertEquals(7, scn.GetStrength(thorin));
		// The action has a cost, and so is offered with no [elven] ally to spot (Q-0248).
		assertTrue(scn.ShadowActionAvailable(feuds));
		scn.ShadowUseCardAction(feuds);
		scn.ShadowChooseCard(thorin);

		assertEquals(2, scn.GetTwilight());
		assertEquals(7, scn.GetStrength(thorin));
		assertEquals(7, scn.GetStrength(balin));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void SkirmishActionIsNotOfferedInTheAssignmentPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var thorin = scn.GetFreepsCard("thorin");
		var elrond = scn.GetFreepsCard("elrond");
		scn.MoveCompanionsToTable(thorin);
		scn.MoveAlliesToTable(elrond);

		var feuds = scn.GetShadowCard("feuds");
		var marauder = scn.GetShadowCard("marauder");
		scn.MoveCardsToSupportArea(feuds);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, feuds);
		scn.MoveMinionsToTable(marauder);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.SetTwilight(5);
		scn.FreepsPass();

		assertEquals(5, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
		assertFalse(scn.ShadowActionAvailable(feuds));
	}

	@Test
	public void SkirmishActionIsNotOfferedWithOnly2Twilight() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var thorin = scn.GetFreepsCard("thorin");
		var elrond = scn.GetFreepsCard("elrond");
		scn.MoveCompanionsToTable(thorin);
		scn.MoveAlliesToTable(elrond);

		var feuds = scn.GetShadowCard("feuds");
		var marauder = scn.GetShadowCard("marauder");
		scn.MoveCardsToSupportArea(feuds);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, feuds);
		scn.MoveMinionsToTable(marauder);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(thorin, marauder);
		scn.FreepsResolveSkirmish(thorin);
		scn.SetTwilight(2);
		scn.FreepsPass();

		assertEquals(2, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertFalse(scn.ShadowActionAvailable(feuds));
	}
}
