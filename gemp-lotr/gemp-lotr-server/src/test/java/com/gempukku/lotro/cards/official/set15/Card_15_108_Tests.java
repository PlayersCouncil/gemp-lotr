package com.gempukku.lotro.cards.official.set15;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_15_108_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("destructive", "15_108");

					put("goblin", "11_137");     // Scurrying Goblin: [orc] Orc, cost 1; its trigger needs an underground site
					put("spear", "12_96");       // Orc Spear: an [orc] possession borne by an [orc] minion; its trigger answers its own play only
					put("grond", "18_82");       // Grond, Forged With Black Steel: an [orc] support-area possession; a regroup-move trigger only
					put("beckoning", "12_83");   // The Beckoning Shadow: an [orc] condition, nothing below 5 burdens
					put("troop", "1_177");       // Goblin Patrol Troop: a [moria] Orc minion, no text
					put("scimitar", "1_180");    // Goblin Scimitar: a [moria] possession for a [moria] Orc; its trigger answers its own play only

					put("firstlevel", "7_92");   // First Level: a [gondor] fortification condition; a maneuver ability only
					put("slits", "5_80");        // Arrow-slits: a [rohan] fortification condition; a maneuver ability only
					put("stronghold", "102_48"); // Stronghold of My Fathers: a [rohan] fortification POSSESSION; a Response to a wound only
					put("stairs", "1_24");       // Stairs of Khazad-dum: a [dwarven] condition that is no fortification
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetFortificationScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("destructive", "15_108");
					// Parapet: a [rohan] fortification in the support area; its Archery ability needs 2 [rohan] Men
					put("parapet", "5_87");
					put("goblin", "11_137");    // Scurrying Goblin: an [orc] Orc of strength 5, to bear the mount
					// Threatening Warg: an [orc] possession (a mount) on an [orc] Orc of strength 10 or less; its own
					// offer comes only when it is played
					put("warg", "17_91");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void DestructiveOrcStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 15
		 * Name: Destructive Orc
		 * Unique: True
		 * Side: Shadow
		 * Culture: Orc
		 * Twilight Cost: 6
		 * Type: Minion
		 * Subtype: Orc
		 * Strength: 14
		 * Vitality: 3
		 * Site Number: 4
		 * Game Text: When you play Destructive Orc, you may discard a fortification for each [orc] possession and each [orc] artifact you can spot.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("destructive");

		assertEquals("Destructive Orc", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ORC, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.ORC, card.getBlueprint().getRace());
		assertEquals(6, card.getBlueprint().getTwilightCost());
		assertEquals(14, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	// Ketura on Q-0034: an optional action with no cost that would do nothing is not offered.  "A fortification
	// for each [orc] possession and each [orc] artifact" is no fortification where there is neither.
	@Test
	public void TheDiscardIsNotOfferedWithNoOrcPossessionOrArtifact() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFortificationScenario();

		var parapet = scn.GetFreepsCard("parapet");
		scn.MoveCardsToSupportArea(parapet);

		var destructive = scn.GetShadowCard("destructive");
		var goblin = scn.GetShadowCard("goblin");
		scn.MoveCardsToHand(destructive);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, parapet);
		// An [orc] minion that bears nothing
		scn.MoveMinionsToTable(goblin);
		scn.SetTwilight(20);
		scn.FreepsPass();

		// A fortification is there to discard.
		scn.ShadowPlayCard(destructive);
		assertInZone(Zone.SHADOW_CHARACTERS, destructive);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(destructive));
		assertInZone(Zone.SUPPORT, parapet);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void TheDiscardIsOfferedWithAnOrcPossession() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFortificationScenario();

		var parapet = scn.GetFreepsCard("parapet");
		scn.MoveCardsToSupportArea(parapet);

		var destructive = scn.GetShadowCard("destructive");
		var goblin = scn.GetShadowCard("goblin");
		var warg = scn.GetShadowCard("warg");
		scn.MoveCardsToHand(destructive);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, parapet);
		scn.MoveMinionsToTable(goblin);
		scn.AttachCardsTo(goblin, warg);
		scn.SetTwilight(20);
		scn.FreepsPass();

		scn.ShadowPlayCard(destructive);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(destructive));
		scn.ShadowAcceptOptionalTrigger();

		// Ketura's ruling (Q-0399): after the yes, 0 up to the count, so the one Parapet is still asked.
		assertTrue(scn.ShadowHasCardChoiceAvailable(parapet));
		assertEquals(0, scn.ShadowGetChoiceMin());
		assertEquals(1, scn.ShadowGetChoiceMax());
		scn.ShadowChooseCard(parapet);
		assertInDiscard(parapet);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	// Ketura's ruling (Q-0399): "you may discard a fortification for each ..." is a choice of 0 up to the count
	// after the yes (as Q-0248, Q-0339 and Q-0397 read a "may").
	@Test
	public void AfterTheYesTheShadowPlayerMayDiscardNoFortification() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var firstlevel = scn.GetFreepsCard("firstlevel");
		var slits = scn.GetFreepsCard("slits");
		var stronghold = scn.GetFreepsCard("stronghold");
		scn.MoveCardsToSupportArea(firstlevel, slits, stronghold);

		var destructive = scn.GetShadowCard("destructive");
		var goblin = scn.GetShadowCard("goblin");
		var spear = scn.GetShadowCard("spear");
		var grond = scn.GetShadowCard("grond");
		scn.MoveCardsToSupportArea(grond);
		scn.MoveCardsToHand(destructive);

		scn.StartGame();
		scn.MoveMinionsToTable(goblin);
		scn.AttachCardsTo(goblin, spear);
		assertInZone(Zone.SUPPORT, firstlevel, slits, stronghold, grond);
		assertAttachedTo(spear, goblin);
		scn.SetTwilight(20);
		scn.FreepsPass();

		scn.ShadowPlayCard(destructive);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(destructive));
		scn.ShadowAcceptOptionalTrigger();

		// Two [orc] possessions: 0 to 2 of the three fortifications.
		assertTrue(scn.ShadowHasCardChoiceAvailable(firstlevel, slits, stronghold));
		assertEquals(0, scn.ShadowGetChoiceMin());
		assertEquals(2, scn.ShadowGetChoiceMax());
		scn.ShadowDeclineChoosing();

		assertInZone(Zone.SUPPORT, firstlevel, slits, stronghold, grond);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithTwoOrcPossessionsTwoFortificationsOfAnyKindMayBeDiscarded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var firstlevel = scn.GetFreepsCard("firstlevel");
		var slits = scn.GetFreepsCard("slits");
		var stronghold = scn.GetFreepsCard("stronghold");
		var stairs = scn.GetFreepsCard("stairs");
		scn.MoveCardsToSupportArea(firstlevel, slits, stronghold, stairs);

		var destructive = scn.GetShadowCard("destructive");
		var goblin = scn.GetShadowCard("goblin");
		var spear = scn.GetShadowCard("spear");
		var grond = scn.GetShadowCard("grond");
		var beckoning = scn.GetShadowCard("beckoning");
		var troop = scn.GetShadowCard("troop");
		var scimitar = scn.GetShadowCard("scimitar");
		scn.MoveCardsToSupportArea(grond, beckoning);
		scn.MoveCardsToHand(destructive);

		scn.StartGame();
		scn.MoveMinionsToTable(goblin, troop);
		scn.AttachCardsTo(goblin, spear);
		scn.AttachCardsTo(troop, scimitar);
		assertInZone(Zone.SUPPORT, firstlevel, slits, stronghold, stairs, grond, beckoning);
		assertAttachedTo(spear, goblin);
		assertAttachedTo(scimitar, troop);
		assertInHand(destructive);
		scn.SetTwilight(20);
		scn.FreepsPass();

		scn.ShadowPlayCard(destructive);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(destructive));
		scn.ShadowAcceptOptionalTrigger();

		// Two [orc] possessions, the spear borne and Grond in the support area; the [orc] condition and the [moria]
		// possession are not counted, or three would be asked.
		assertTrue(scn.ShadowHasCardChoiceAvailable(firstlevel, slits, stronghold));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(stairs, beckoning));
		assertEquals(0, scn.ShadowGetChoiceMin());
		assertEquals(2, scn.ShadowGetChoiceMax());
		scn.ShadowChooseCards(firstlevel, stronghold);

		assertInDiscard(firstlevel, stronghold);
		assertInZone(Zone.SUPPORT, slits, stairs, grond, beckoning);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void DecliningLeavesEveryFortificationAndAnotherOrcPlayedAfterItIsNotAnswered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var slits = scn.GetFreepsCard("slits");
		scn.MoveCardsToSupportArea(slits);

		var destructive = scn.GetShadowCard("destructive");
		var goblin = scn.GetShadowCard("goblin");
		var grond = scn.GetShadowCard("grond");
		scn.MoveCardsToSupportArea(grond);
		scn.MoveCardsToHand(destructive, goblin);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, slits, grond);
		assertInHand(destructive, goblin);
		scn.SetTwilight(20);
		scn.FreepsPass();

		scn.ShadowPlayCard(destructive);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(destructive));
		scn.ShadowDeclineOptionalTrigger();
		assertInZone(Zone.SUPPORT, slits, grond);

		scn.ShadowPlayCard(goblin);
		assertInZone(Zone.SHADOW_CHARACTERS, goblin);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(destructive));
		assertInZone(Zone.SUPPORT, slits);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithNoFortificationInPlayNothingIsOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var stairs = scn.GetFreepsCard("stairs");
		scn.MoveCardsToSupportArea(stairs);

		var destructive = scn.GetShadowCard("destructive");
		var grond = scn.GetShadowCard("grond");
		scn.MoveCardsToSupportArea(grond);
		scn.MoveCardsToHand(destructive);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, stairs, grond);
		assertInHand(destructive);
		scn.SetTwilight(20);
		scn.FreepsPass();

		scn.ShadowPlayCard(destructive);
		assertInZone(Zone.SHADOW_CHARACTERS, destructive);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(destructive));
		assertInZone(Zone.SUPPORT, stairs);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
