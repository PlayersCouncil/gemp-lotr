package com.gempukku.lotro.cards.official.set14;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_14_001_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("dain", "14_1");
					put("gimli", "1_13");       // Gimli, Son of Glóin: Dwarf companion, cost 2
					put("grimir", "1_17");      // Grimir, Dwarven Elder: Dwarf ally, cost 1
					put("dasron", "13_29");     // Dasron, Merchant From Dorwinion: [gandalf] Man companion, cost 2
					put("erland", "2_21");      // Erland, Advisor to Brand: [gandalf] Man ally, cost 2
					put("gandalf", "1_72");     // Gandalf, Friend of the Shirefolk: [gandalf] Wizard companion, cost 4
					put("boromir", "1_97");     // Boromir, Son of Denethor: [gondor] Man companion, cost 3
					put("lordofmoria", "1_21"); // Lord of Moria: [dwarven] support condition, cost 2; each Dwarf is damage +1
					put("watch", "12_35");      // Watch and Wait: [gandalf] condition borne by a companion; answers a skirmish won
					put("armor", "1_8");        // Dwarven Armor: [dwarven] possession on a Dwarf
					put("foes", "1_105");       // Foes of Mordor: [gondor] support condition; a maneuver ability
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc minion with no text
					put("brigand", "4_10");     // Dunlending Brigand: [dunland] Man minion with no text
					put("poison", "8_28");      // Spider Poison: Shadow possession in the support area, no minion
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void DainIronfootStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 14
		 * Name: Dáin Ironfoot, King Under the Mountain
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Dwarven
		 * Twilight Cost: 3
		 * Type: Companion
		 * Subtype: Dwarf
		 * Strength: 7
		 * Vitality: 3
		 * Resistance: 6
		 * Game Text: Each of your other Dwarves and [gandalf] Men is twilight cost -1.
		 * At the start of the regroup phase, you may discard a [dwarven] or [gandalf] condition from play to discard a minion from play.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("dain");

		assertEquals("Dáin Ironfoot", card.getBlueprint().getTitle());
		assertEquals("King Under the Mountain", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.DWARVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.DWARF, card.getBlueprint().getRace());
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(7, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getResistance());
	}

	@Test
	public void OnceDainIsInPlayADwarfCompanionCostsOneLess() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var dain = scn.GetFreepsCard("dain");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCardsToHand(dain, gimli);

		scn.StartGame();

		assertEquals(2, scn.GetTwilightCost(gimli));
		scn.FreepsPlayCard(dain);
		assertInZone(Zone.FREE_CHARACTERS, dain);
		assertEquals(1, scn.GetTwilightCost(gimli));

		int pool = scn.GetTwilight();
		scn.FreepsPlayCard(gimli);

		// 2 printed - 1
		assertEquals(pool + 1, scn.GetTwilight());
		assertInZone(Zone.FREE_CHARACTERS, gimli);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void DiscountsHisPlayersDwarvesAndGandalfMenOfEitherTypeAndNoOthers() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var dain = scn.GetFreepsCard("dain");
		var gimli = scn.GetFreepsCard("gimli");
		var grimir = scn.GetFreepsCard("grimir");
		var dasron = scn.GetFreepsCard("dasron");
		var erland = scn.GetFreepsCard("erland");
		var gandalf = scn.GetFreepsCard("gandalf");
		var boromir = scn.GetFreepsCard("boromir");
		var lordofmoria = scn.GetFreepsCard("lordofmoria");
		scn.MoveCompanionsToTable(dain);
		scn.MoveCardsToHand(gimli, grimir, dasron, erland, gandalf, boromir, lordofmoria);

		scn.StartGame();

		// Dwarves: a companion and an ally.
		assertEquals(1, scn.GetTwilightCost(gimli));
		assertEquals(0, scn.GetTwilightCost(grimir));
		// [gandalf] Men: a companion and an ally.
		assertEquals(1, scn.GetTwilightCost(dasron));
		assertEquals(1, scn.GetTwilightCost(erland));
		// A [gandalf] companion who is no Man, a Man who is not [gandalf], a [dwarven] card that is no Dwarf.
		assertEquals(4, scn.GetTwilightCost(gandalf));
		assertEquals(3, scn.GetTwilightCost(boromir));
		assertEquals(2, scn.GetTwilightCost(lordofmoria));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void DoesNotDiscountTheOtherPlayersDwarf() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var dain = scn.GetFreepsCard("dain");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(dain);
		scn.MoveCardsToHand(gimli);

		var shadowGimli = scn.GetShadowCard("gimli");
		scn.MoveCardsToHand(shadowGimli);

		scn.StartGame();

		assertEquals(1, scn.GetTwilightCost(gimli));
		assertEquals(2, scn.GetTwilightCost(shadowGimli));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AtTheStartOfTheRegroupPhaseAndNotOfTheManeuverPhaseDiscardsAConditionToDiscardAMinion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var dain = scn.GetFreepsCard("dain");
		var gimli = scn.GetFreepsCard("gimli");
		var lordofmoria = scn.GetFreepsCard("lordofmoria");
		var watch = scn.GetFreepsCard("watch");
		var armor = scn.GetFreepsCard("armor");
		var foes = scn.GetFreepsCard("foes");
		scn.MoveCompanionsToTable(dain, gimli);
		scn.MoveCardsToSupportArea(lordofmoria, foes);
		scn.AttachCardsTo(gimli, watch);
		scn.AttachCardsTo(dain, armor);

		var troop = scn.GetShadowCard("troop");
		var brigand = scn.GetShadowCard("brigand");
		var poison = scn.GetShadowCard("poison");

		scn.StartGame();
		scn.MoveMinionsToTable(troop, brigand);
		scn.MoveCardsToSupportArea(poison);

		scn.SkipToPhase(Phase.MANEUVER);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(dain));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());

		scn.SkipToPhase(Phase.REGROUP);
		assertInZone(Zone.SUPPORT, lordofmoria, foes, poison);
		assertAttachedTo(watch, gimli);
		assertAttachedTo(armor, dain);
		assertInZone(Zone.SHADOW_CHARACTERS, troop, brigand);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(dain));
		scn.FreepsAcceptOptionalTrigger();

		// A [dwarven] condition in the support area and a [gandalf] one borne by a companion; not a [dwarven]
		// possession, nor a condition of another culture.
		assertTrue(scn.FreepsHasCardChoiceAvailable(lordofmoria, watch));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(armor, foes));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(watch);
		assertInZone(Zone.DISCARD, watch);
		assertInZone(Zone.SUPPORT, lordofmoria, foes);
		assertAttachedTo(armor, dain);

		// Minions of two cultures and races; not a Shadow card that is no minion.
		assertTrue(scn.FreepsHasCardChoiceAvailable(troop, brigand));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(poison));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(brigand);

		assertInZone(Zone.DISCARD, brigand);
		assertInZone(Zone.SHADOW_CHARACTERS, troop);
		assertInZone(Zone.SUPPORT, poison);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void TheDwarvenConditionPaysAsWell() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var dain = scn.GetFreepsCard("dain");
		var gimli = scn.GetFreepsCard("gimli");
		var lordofmoria = scn.GetFreepsCard("lordofmoria");
		var watch = scn.GetFreepsCard("watch");
		scn.MoveCompanionsToTable(dain, gimli);
		scn.MoveCardsToSupportArea(lordofmoria);
		scn.AttachCardsTo(gimli, watch);

		var troop = scn.GetShadowCard("troop");
		var brigand = scn.GetShadowCard("brigand");

		scn.StartGame();
		scn.MoveMinionsToTable(troop, brigand);

		scn.SkipToPhase(Phase.REGROUP);
		assertAttachedTo(watch, gimli);
		assertInZone(Zone.SHADOW_CHARACTERS, troop, brigand);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(dain));
		scn.FreepsAcceptOptionalTrigger();

		scn.FreepsChooseCard(lordofmoria);
		assertInZone(Zone.DISCARD, lordofmoria);
		assertAttachedTo(watch, gimli);

		scn.FreepsChooseCard(troop);
		assertInZone(Zone.DISCARD, troop);
		assertInZone(Zone.SHADOW_CHARACTERS, brigand);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void IsNotOfferedWithNoDwarvenOrGandalfConditionInPlay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var dain = scn.GetFreepsCard("dain");
		var armor = scn.GetFreepsCard("armor");
		var foes = scn.GetFreepsCard("foes");
		scn.MoveCompanionsToTable(dain);
		scn.MoveCardsToSupportArea(foes);
		scn.AttachCardsTo(dain, armor);

		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.MoveMinionsToTable(troop);

		scn.SkipToPhase(Phase.REGROUP);

		assertInZone(Zone.SHADOW_CHARACTERS, troop);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(dain));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void DecliningKeepsTheConditionAndTheMinion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var dain = scn.GetFreepsCard("dain");
		var lordofmoria = scn.GetFreepsCard("lordofmoria");
		scn.MoveCompanionsToTable(dain);
		scn.MoveCardsToSupportArea(lordofmoria);

		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.MoveMinionsToTable(troop);

		scn.SkipToPhase(Phase.REGROUP);
		assertInZone(Zone.SUPPORT, lordofmoria);
		assertInZone(Zone.SHADOW_CHARACTERS, troop);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(dain));
		scn.FreepsDeclineOptionalTrigger();

		assertInZone(Zone.SUPPORT, lordofmoria);
		assertInZone(Zone.SHADOW_CHARACTERS, troop);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
