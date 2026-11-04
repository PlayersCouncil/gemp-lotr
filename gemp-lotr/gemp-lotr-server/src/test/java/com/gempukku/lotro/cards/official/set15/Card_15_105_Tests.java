package com.gempukku.lotro.cards.official.set15;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.StrengthModifier;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_15_105_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("spy", "15_105");
					put("spy2", "15_105");

					put("mocking", "11_127");    // Mocking Goblin: [orc] Orc, strength 12; a trigger on its own win only
					put("goblin", "11_137");     // Scurrying Goblin: [orc] Orc, cost 1; its trigger needs an underground site
					put("spear", "12_96");       // Orc Spear: an [orc] possession borne by an [orc] minion; its trigger answers its own play only
					put("grond", "18_82");       // Grond, Forged With Black Steel: an [orc] support-area possession; a regroup-move trigger only
					put("beckoning", "12_83");   // The Beckoning Shadow: an [orc] condition, nothing below 5 burdens
					put("troop", "1_177");       // Goblin Patrol Troop: a [moria] Orc minion of strength 13, no text
					put("scimitar", "1_180");    // Goblin Scimitar: a [moria] possession for a [moria] Orc; its trigger answers its own play only

					put("aragorn", "1_89");      // Aragorn, Ranger of the North: [gondor] Man, strength 8; a maneuver ability only
					put("sword", "1_112");       // Ranger's Sword: strength +2, bearer must be Aragorn, cost 1
					put("gimli", "1_13");        // Gimli, Son of Gloin: [dwarven] Dwarf, strength 6; a skirmish ability only
					put("bounder", "1_286");     // Bounder: a Hobbit ally, strength 2; a skirmish ability only
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void BlackLandSpyStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 15
		 * Name: Black Land Spy
		 * Unique: False
		 * Side: Shadow
		 * Culture: Orc
		 * Twilight Cost: 3
		 * Type: Minion
		 * Subtype: Orc
		 * Strength: 8
		 * Vitality: 2
		 * Site Number: 4
		 * Game Text: When you play this minion, you may spot an [orc] possession to add a threat.
		 * While you can spot a companion of strength 13 or more, this minion is strength +7.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("spy");

		assertEquals("Black Land Spy", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ORC, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.ORC, card.getBlueprint().getRace());
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(8, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void WithABorneOrcPossessionItMayAddAThreatAndAnotherOrcPlayedAfterItIsNotAnswered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		scn.MoveCompanionsToTable(scn.GetFreepsCard("aragorn"));

		var spy = scn.GetShadowCard("spy");
		var mocking = scn.GetShadowCard("mocking");
		var spear = scn.GetShadowCard("spear");
		var goblin = scn.GetShadowCard("goblin");
		scn.MoveCardsToHand(spy, goblin);

		scn.StartGame();
		scn.MoveMinionsToTable(mocking);
		scn.AttachCardsTo(mocking, spear);
		assertAttachedTo(spear, mocking);
		assertInHand(spy, goblin);
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertEquals(0, scn.GetThreats());
		scn.ShadowPlayCard(spy);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(spy));
		scn.ShadowAcceptOptionalTrigger();
		assertEquals(1, scn.GetThreats());

		// Another [orc] Orc played after it, with the spear still in play and room for a second threat (two companions).
		scn.ShadowPlayCard(goblin);
		assertInZone(Zone.SHADOW_CHARACTERS, goblin);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(spy));
		assertEquals(1, scn.GetThreats());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithAnOrcPossessionInTheSupportAreaTheOfferMayBeDeclined() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var spy = scn.GetShadowCard("spy");
		var grond = scn.GetShadowCard("grond");
		scn.MoveCardsToSupportArea(grond);
		scn.MoveCardsToHand(spy);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, grond);
		assertInHand(spy);
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertEquals(0, scn.GetThreats());
		scn.ShadowPlayCard(spy);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(spy));
		scn.ShadowDeclineOptionalTrigger();
		assertEquals(0, scn.GetThreats());
		assertInZone(Zone.SHADOW_CHARACTERS, spy);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AnOrcConditionAndAPossessionOfAnotherCultureOfferNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		scn.MoveCompanionsToTable(scn.GetFreepsCard("aragorn"));

		var spy = scn.GetShadowCard("spy");
		var beckoning = scn.GetShadowCard("beckoning");
		var troop = scn.GetShadowCard("troop");
		var scimitar = scn.GetShadowCard("scimitar");
		scn.MoveCardsToSupportArea(beckoning);
		scn.MoveCardsToHand(spy);

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.AttachCardsTo(troop, scimitar);
		assertInZone(Zone.SUPPORT, beckoning);
		assertAttachedTo(scimitar, troop);
		assertInHand(spy);
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertEquals(0, scn.GetThreats());
		scn.ShadowPlayCard(spy);
		assertInZone(Zone.SHADOW_CHARACTERS, spy);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(spy));
		assertEquals(0, scn.GetThreats());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithThreatsAlreadyAtTheNumberOfCompanionsNothingIsOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var spy = scn.GetShadowCard("spy");
		var mocking = scn.GetShadowCard("mocking");
		var spear = scn.GetShadowCard("spear");
		scn.MoveCardsToHand(spy);

		scn.StartGame();
		// Frodo alone: one threat is all there can be.
		scn.AddThreats(1);
		scn.MoveMinionsToTable(mocking);
		scn.AttachCardsTo(mocking, spear);
		assertAttachedTo(spear, mocking);
		assertInHand(spy);
		assertEquals(1, scn.GetThreats());
		scn.SetTwilight(10);
		scn.FreepsPass();

		scn.ShadowPlayCard(spy);
		assertInZone(Zone.SHADOW_CHARACTERS, spy);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(spy));
		assertEquals(1, scn.GetThreats());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void StrengthPlusSevenOnEachCopyOnceAragornReachesStrength13ByASwordAndNotForAMinionOrAnAllyOf13() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var sword = scn.GetFreepsCard("sword");
		var bounder = scn.GetFreepsCard("bounder");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveAlliesToTable(bounder);
		scn.MoveCardsToHand(sword);
		// Aragorn 8 + 3 = 11; the Bounder 2 + 11 = 13.
		scn.ApplyAdHocModifier(new StrengthModifier(null, aragorn, null, 3));
		scn.ApplyAdHocModifier(new StrengthModifier(null, bounder, null, 11));

		var spy = scn.GetShadowCard("spy");
		var spy2 = scn.GetShadowCard("spy2");
		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.MoveMinionsToTable(spy, spy2, troop);
		scn.FreepsRefreshDecision();

		assertEquals(11, scn.GetStrength(aragorn));
		assertEquals(13, scn.GetStrength(bounder));
		assertEquals(13, scn.GetStrength(troop));
		assertEquals(8, scn.GetStrength(spy));
		assertEquals(8, scn.GetStrength(spy2));

		scn.FreepsPlayCard(sword);
		assertAttachedTo(sword, aragorn);
		// 11 + 2 for the sword
		assertEquals(13, scn.GetStrength(aragorn));
		// 8 + 7 on each copy, once
		assertEquals(15, scn.GetStrength(spy));
		assertEquals(15, scn.GetStrength(spy2));
		assertEquals(13, scn.GetStrength(troop));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ACompanionAtStrength12IsNotEnoughAndAt13ItIs() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(gimli);
		// Gimli 6 + 6 = 12.
		scn.ApplyAdHocModifier(new StrengthModifier(null, gimli, null, 6));

		var spy = scn.GetShadowCard("spy");

		scn.StartGame();
		scn.MoveMinionsToTable(spy);

		assertEquals(12, scn.GetStrength(gimli));
		assertEquals(8, scn.GetStrength(spy));

		scn.ApplyAdHocModifier(new StrengthModifier(null, gimli, null, 1));
		assertEquals(13, scn.GetStrength(gimli));
		assertEquals(15, scn.GetStrength(spy));
	}
}
