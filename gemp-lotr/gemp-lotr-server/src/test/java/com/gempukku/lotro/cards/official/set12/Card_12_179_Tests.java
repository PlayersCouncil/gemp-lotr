package com.gempukku.lotro.cards.official.set12;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_12_179_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("otsea", "12_179");
					// Drawn to Its Power: [wraith] support-area condition; answers only a companion killed, and none is
					put("drawn", "1_211");
					// Helpless: [wraith] condition that plays on Sam; only ever in hand here
					put("helpless", "2_76");
					// Strength Born of Fear: a [sauron] condition, the condition of another culture; only ever in hand here
					put("fear", "1_278");
					// Nazgûl Sword: a [wraith] possession, the [wraith] card that is no condition; only ever in hand here
					put("sword", "1_218");
					// Lord of Moria: Free Peoples [dwarven] support-area condition; each Dwarf is damage +1
					put("lord", "1_21");
					// The Saga of Elendil: Free Peoples [gondor] condition on a [gondor] companion; a skirmish ability used
					// only on purpose
					put("saga", "1_114");
					// Dwarven Axe: a Free Peoples possession, no condition; answers only a minion losing to Gimli
					put("axe", "1_9");
					// Black Breath: a Shadow condition borne by a companion; its transfer is used only on purpose, and its
					// bars on healing and on removing burdens reach no reading
					put("breath", "1_207");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a [gondor] companion; a maneuver ability used only on purpose
					put("gimli", "1_13");       // Gimli, Son of Glóin: a skirmish ability used only on purpose
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void UlaireOtseaStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 12
		 * Name: Úlairë Otsëa, Black Specter
		 * Unique: True
		 * Side: Shadow
		 * Culture: Wraith
		 * Twilight Cost: 4
		 * Type: Minion
		 * Subtype: Nazgûl
		 * Strength: 9
		 * Vitality: 3
		 * Site Number: 3
		 * Game Text: <b>Shadow:</b> Exert Úlairë Otsëa and discard a [wraith] condition from hand to discard a Free Peoples condition from play.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("otsea");

		assertEquals("Úlairë Otsëa", card.getBlueprint().getTitle());
		assertEquals("Black Specter", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.WRAITH, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.NAZGUL, card.getBlueprint().getRace());
		assertEquals(4, card.getBlueprint().getTwilightCost());
		assertEquals(9, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(3, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void ShadowAbilityExertsHerAndDiscardsAWraithConditionFromHandToDiscardAFreePeoplesCondition() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var otsea = scn.GetShadowCard("otsea");
		var drawn = scn.GetShadowCard("drawn");
		var helpless = scn.GetShadowCard("helpless");
		var fear = scn.GetShadowCard("fear");
		var sword = scn.GetShadowCard("sword");
		var breath = scn.GetShadowCard("breath");
		var lord = scn.GetFreepsCard("lord");
		var saga = scn.GetFreepsCard("saga");
		var axe = scn.GetFreepsCard("axe");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(aragorn, gimli);
		scn.MoveCardsToSupportArea(lord);
		scn.AttachCardsTo(aragorn, saga);
		scn.AttachCardsTo(gimli, axe, breath);
		scn.MoveCardsToHand(drawn, helpless, fear, sword);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, lord);
		assertAttachedTo(saga, aragorn);
		assertAttachedTo(axe, gimli);
		assertAttachedTo(breath, gimli);
		assertInHand(drawn, helpless, fear, sword);
		scn.MoveMinionsToTable(otsea);
		scn.FreepsPass();

		assertEquals(0, scn.GetWoundsOn(otsea));
		assertTrue(scn.ShadowActionAvailable(otsea));
		scn.ShadowUseCardAction(otsea);

		assertEquals(1, scn.ShadowGetChoiceMin());
		assertTrue(scn.ShadowHasCardChoiceAvailable(drawn, helpless));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(fear, sword));
		scn.ShadowChooseCard(helpless);
		assertInDiscard(helpless);
		assertInHand(drawn, fear, sword);
		assertEquals(1, scn.GetWoundsOn(otsea));

		assertEquals(1, scn.ShadowGetChoiceMin());
		assertTrue(scn.ShadowHasCardChoiceAvailable(lord, saga));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(axe, breath));
		scn.ShadowChooseCard(saga);
		assertInDiscard(saga);
		assertInZone(Zone.SUPPORT, lord);
		assertAttachedTo(axe, gimli);
		assertAttachedTo(breath, gimli);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void NotOfferedWithNoWraithConditionInHandToDiscard() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var otsea = scn.GetShadowCard("otsea");
		var drawn = scn.GetShadowCard("drawn");
		var fear = scn.GetShadowCard("fear");
		var sword = scn.GetShadowCard("sword");
		var lord = scn.GetFreepsCard("lord");
		scn.MoveCardsToSupportArea(lord);
		scn.MoveCardsToHand(fear, sword);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, lord);
		assertInHand(fear, sword);
		scn.MoveMinionsToTable(otsea);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowActionAvailable(otsea));

		scn.MoveCardsToHand(drawn);
		scn.ShadowRefreshDecision();
		assertInHand(drawn);
		assertTrue(scn.ShadowActionAvailable(otsea));
	}

	@Test
	public void NotOfferedWhileSheIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var otsea = scn.GetShadowCard("otsea");
		var drawn = scn.GetShadowCard("drawn");
		var lord = scn.GetFreepsCard("lord");
		scn.MoveCardsToSupportArea(lord);
		scn.MoveCardsToHand(drawn);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, lord);
		assertInHand(drawn);
		scn.MoveMinionsToTable(otsea);
		// Vitality 3 with 2 wounds: she cannot exert.
		scn.AddWoundsToChar(otsea, 2);
		assertEquals(2, scn.GetWoundsOn(otsea));
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowActionAvailable(otsea));
	}

	@Test
	public void WithNoFreePeoplesConditionInPlayTheCostIsPaidForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var otsea = scn.GetShadowCard("otsea");
		var drawn = scn.GetShadowCard("drawn");
		var breath = scn.GetShadowCard("breath");
		var axe = scn.GetFreepsCard("axe");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(gimli);
		scn.AttachCardsTo(gimli, axe, breath);
		scn.MoveCardsToHand(drawn);

		scn.StartGame();
		assertAttachedTo(axe, gimli);
		assertAttachedTo(breath, gimli);
		assertInHand(drawn);
		scn.MoveMinionsToTable(otsea);
		scn.FreepsPass();

		assertEquals(0, scn.GetWoundsOn(otsea));
		assertTrue(scn.ShadowActionAvailable(otsea));
		scn.ShadowUseCardAction(otsea);

		assertEquals(1, scn.GetWoundsOn(otsea));
		assertInDiscard(drawn);
		assertAttachedTo(axe, gimli);
		assertAttachedTo(breath, gimli);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
