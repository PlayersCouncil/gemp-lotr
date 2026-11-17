package com.gempukku.lotro.cards.official.set05;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_05_051_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("grima", "5_51");

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: unbound [gondor] Man; a maneuver ability only ever used on purpose
					put("legolas", "1_50");     // Legolas, Greenleaf: unbound [elven] Elf; an archery ability only ever used on purpose
					put("sam", "1_311");        // Sam, Son of Hamfast: [shire], Ring-bound by rule, not the Ring-bearer
					put("bounder", "1_286");    // Bounder: [shire] ally, a skirmish ability only ever used on purpose
					put("stairs", "1_24");      // Stairs of Khazad-dum: [dwarven] support-area condition, the only [dwarven] card
					put("gollum", "5_24");      // Gollum, Nasty Treacherous Creature: a [gollum] Shadow card; a regroup ability only

					put("sword", "1_112");      // Ranger's Sword: [gondor] possession on Aragorn, damage +1
					put("saga", "1_114");       // The Saga of Elendil: [gondor] condition on a [gondor] companion
					put("legacy", "1_282");     // The Weight of a Legacy: [sauron] condition on Aragorn, strength -1
					put("poison", "8_28");      // Spider Poison: [gollum] Shadow possession, cheated onto a companion
					put("weary", "4_212");      // Weary: [isengard] condition on a companion; exerts bearer only when assigned to an [isengard] tracker
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void GrimaStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 5
		 * Name: Gríma, Chief Counselor
		 * Unique: True
		 * Side: Shadow
		 * Culture: Isengard
		 * Twilight Cost: 2
		 * Type: Minion
		 * Subtype: Man
		 * Strength: 4
		 * Vitality: 3
		 * Site Number: 3
		 * Game Text: <b>Maneuver:</b> Spot 4 Free Peoples cultures and exert Gríma to return an unbound companion, and all cards on him or her, to their owners' hands.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("grima");

		assertEquals("Gríma", card.getBlueprint().getTitle());
		assertEquals("Chief Counselor", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ISENGARD, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(4, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(3, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void WithFourCulturesExertsToReturnAnUnboundCompanionAndEveryCardOnHimToTheirOwnersHands() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var grima = scn.GetShadowCard("grima");
		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var legolas = scn.GetFreepsCard("legolas");
		var sam = scn.GetFreepsCard("sam");
		var bounder = scn.GetFreepsCard("bounder");
		var stairs = scn.GetFreepsCard("stairs");
		var sword = scn.GetFreepsCard("sword");
		var saga = scn.GetFreepsCard("saga");
		var legacy = scn.GetShadowCard("legacy");
		var poison = scn.GetShadowCard("poison");
		var weary = scn.GetShadowCard("weary");
		scn.MoveCompanionsToTable(aragorn, legolas, sam);
		scn.MoveAlliesToTable(bounder);
		scn.MoveCardsToSupportArea(stairs);
		// On Aragorn: a Free Peoples possession and condition, and a Shadow condition and possession.
		scn.AttachCardsTo(aragorn, sword, saga, legacy, poison);
		// On Legolas, who is not chosen: a Shadow condition.
		scn.AttachCardsTo(legolas, weary);

		scn.StartGame();
		scn.MoveMinionsToTable(grima);
		assertAttachedTo(sword, aragorn);
		assertAttachedTo(saga, aragorn);
		assertAttachedTo(legacy, aragorn);
		assertAttachedTo(poison, aragorn);
		assertAttachedTo(weary, legolas);
		assertInZone(Zone.SUPPORT, stairs);

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		// Exactly 4: [shire] Frodo and Sam, [elven] Legolas, [gondor] Aragorn, and [dwarven] by a condition alone.
		assertEquals(4, scn.GetFreepsCultureCount());
		assertEquals(0, scn.GetFreepsHandCount());
		assertEquals(0, scn.GetShadowHandCount());
		assertEquals(0, scn.GetWoundsOn(grima));

		assertTrue(scn.ShadowActionAvailable(grima));
		scn.ShadowUseCardAction(grima);
		assertEquals(1, scn.GetWoundsOn(grima));

		// Unbound companions of two cultures and races; not the Ring-bearer, the Ring-bound Sam or an ally.
		assertTrue(scn.ShadowHasCardChoiceAvailable(aragorn, legolas));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(frodo, sam, bounder, grima));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(aragorn);

		// Aragorn and his own cards to the Free Peoples player's hand, the Shadow cards on him to the Shadow player's.
		assertEquals(3, scn.GetFreepsHandCount());
		assertTrue(scn.GetFreepsHand().contains(aragorn));
		assertTrue(scn.GetFreepsHand().contains(sword));
		assertTrue(scn.GetFreepsHand().contains(saga));
		assertEquals(2, scn.GetShadowHandCount());
		assertTrue(scn.GetShadowHand().contains(legacy));
		assertTrue(scn.GetShadowHand().contains(poison));
		// Nothing else.
		assertInPlay(legolas, sam, frodo, bounder, stairs);
		assertAttachedTo(weary, legolas);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void NotOfferedWithThreeFreePeoplesCulturesBesideAGollumCardAndAnInactiveFreePeoplesCard() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var grima = scn.GetShadowCard("grima");
		var aragorn = scn.GetFreepsCard("aragorn");
		var legolas = scn.GetFreepsCard("legolas");
		var sam = scn.GetFreepsCard("sam");
		var gollum = scn.GetShadowCard("gollum");
		// The Shadow player's own [dwarven] condition is inactive on the Free Peoples player's turn.
		var shadowStairs = scn.GetShadowCard("stairs");
		scn.MoveCompanionsToTable(aragorn, legolas, sam);
		scn.MoveCardsToSupportArea(shadowStairs);

		scn.StartGame();
		scn.MoveMinionsToTable(grima, gollum);
		assertInZone(Zone.SUPPORT, shadowStairs);

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		// [shire], [elven] and [gondor]; a [gollum] Shadow card spots no Free Peoples culture (CR 4.2 "spot").
		assertEquals(3, scn.GetFreepsCultureCount());
		assertInZone(Zone.SUPPORT, shadowStairs);
		assertEquals(0, scn.GetWoundsOn(grima));
		assertFalse(scn.ShadowActionAvailable(grima));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertInPlay(aragorn, legolas, sam);
	}

	@Test
	public void NotOfferedWhenGrimaIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var grima = scn.GetShadowCard("grima");
		var aragorn = scn.GetFreepsCard("aragorn");
		var legolas = scn.GetFreepsCard("legolas");
		var sam = scn.GetFreepsCard("sam");
		var stairs = scn.GetFreepsCard("stairs");
		scn.MoveCompanionsToTable(aragorn, legolas, sam);
		scn.MoveCardsToSupportArea(stairs);

		scn.StartGame();
		scn.MoveMinionsToTable(grima);
		scn.AddWoundsToChar(grima, 2);
		assertEquals(2, scn.GetWoundsOn(grima));

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertEquals(4, scn.GetFreepsCultureCount());
		// Vitality 3 with 2 wounds: exhausted.
		assertEquals(2, scn.GetWoundsOn(grima));
		assertFalse(scn.ShadowActionAvailable(grima));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertInPlay(aragorn, legolas);
	}
}
