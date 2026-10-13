package com.gempukku.lotro.cards.official.set04;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_04_117_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("faramir", "4_117");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: the companion of the other skirmish
					put("bow", "4_131");        // Ranger's Bow: on a Ring-bound Man, "Skirmish: If bearer is skirmishing a Man or a roaming minion, exert bearer to wound that minion." (Faramir's Bow would make him an archer)
					put("valor", "4_135");      // War and Valor: Free Peoples Skirmish event, "Make a [gondor] Man strength +2 (or +3 and damage +1 if skirmishing a roaming minion)."

					put("trooper1", "4_199");   // Uruk Trooper: Uruk-hai, strength 9, damage +1; its regroup ability is never reached
					put("trooper2", "4_199");
					put("ferocity", "4_151");   // Ferocity: Shadow Skirmish event, cost 0, "Make an Uruk-hai strength +2 (or +3 if at a battleground)."
					put("gollum", "6_40");      // Gollum, Old Villain: "Skirmish: Discard a card from hand to make Gollum strength +1 (or +2 if skirmishing a Ring-bound companion)."
					put("chaff1", "1_3");       // Axe Strike: only cards in a hand
					put("chaff2", "1_3");
					put("shaman", "3_59");      // Isengard Shaman: [isengard] Orc, site 4; "Response: If an [isengard] Orc is about to take a wound, remove (2) to prevent that wound."
					put("meats", "7_75");       // Sweeter Meats: Shadow Response event, "If a minion is about to take a wound, spot Gollum to prevent that wound."
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void FaramirStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Faramir, Son of Denethor
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 3
		 * Type: Companion
		 * Subtype: Man
		 * Strength: 7
		 * Vitality: 3
		 * Resistance: 6
		 * Signet: Frodo
		 * Game Text: <b>Ring-bound</b>. <b>Ranger</b>.<br>An opponent may not play skirmish events or use skirmish special abilities during skirmishes involving Faramir.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("faramir");

		assertEquals("Faramir", card.getBlueprint().getTitle());
		assertEquals("Son of Denethor", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.RING_BOUND));
		assertTrue(scn.HasKeyword(card, Keyword.RANGER));
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(7, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getResistance());
		assertEquals(Signet.FRODO, card.getBlueprint().getSignet());
	}

	@Test
	public void ShadowSkirmishEventIsRefusedInFaramirsSkirmishAndPlayedInAnother() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var faramir = scn.GetFreepsCard("faramir");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(faramir, aragorn);

		var trooper1 = scn.GetShadowCard("trooper1");
		var trooper2 = scn.GetShadowCard("trooper2");
		var ferocity = scn.GetShadowCard("ferocity");
		scn.MoveCardsToHand(ferocity);

		scn.StartGame();
		scn.MoveMinionsToTable(trooper1, trooper2);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{faramir, trooper1},
				new PhysicalCardImpl[]{aragorn, trooper2});

		scn.FreepsResolveSkirmish(faramir);
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(ferocity));
		scn.ShadowPass();

		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertTrue(scn.ShadowPlayAvailable(ferocity));
		assertEquals(9, scn.GetStrength(trooper2));
		scn.ShadowPlayCard(ferocity);
		scn.ShadowChooseCard(trooper2);
		assertEquals(11, scn.GetStrength(trooper2));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void ShadowSkirmishAbilityIsRefusedInFaramirsSkirmishAndUsedInAnother() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var faramir = scn.GetFreepsCard("faramir");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(faramir, aragorn);

		var gollum = scn.GetShadowCard("gollum");
		var trooper1 = scn.GetShadowCard("trooper1");
		var chaff1 = scn.GetShadowCard("chaff1");
		var chaff2 = scn.GetShadowCard("chaff2");
		scn.MoveCardsToHand(chaff1, chaff2);

		scn.StartGame();
		scn.MoveMinionsToTable(gollum, trooper1);
		scn.SkipToAssignments();
		// Gollum fights Faramir; his ability, which needs no skirmish of his own, is refused there and used in the other.
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{faramir, gollum},
				new PhysicalCardImpl[]{aragorn, trooper1});

		scn.FreepsResolveSkirmish(faramir);
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertFalse(scn.ShadowActionAvailable(gollum));
		scn.ShadowPass();

		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertTrue(scn.ShadowActionAvailable(gollum));
		int strength = scn.GetStrength(gollum);
		assertEquals(2, scn.GetShadowHandCount());
		scn.ShadowUseCardAction(gollum);
		scn.ShadowChooseCard(chaff1);
		// Not skirmishing a Ring-bound companion now: +1.
		assertEquals(strength + 1, scn.GetStrength(gollum));
		assertInDiscard(chaff1);
		assertEquals(1, scn.GetShadowHandCount());
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void InFaramirsSkirmishTheFreePeoplesPlayerAndShadowResponsesAreNotBarred() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var faramir = scn.GetFreepsCard("faramir");
		var aragorn = scn.GetFreepsCard("aragorn");
		var bow = scn.GetFreepsCard("bow");
		var valor = scn.GetFreepsCard("valor");
		scn.MoveCompanionsToTable(faramir, aragorn);
		scn.AttachCardsTo(faramir, bow);
		scn.MoveCardsToHand(valor);

		var shaman = scn.GetShadowCard("shaman");
		var gollum = scn.GetShadowCard("gollum");
		var meats = scn.GetShadowCard("meats");
		scn.MoveCardsToHand(meats);

		scn.StartGame();
		scn.MoveMinionsToTable(shaman, gollum);
		scn.SkipToAssignments();
		// The Shaman (site 4) is roaming at site 2; Gollum is the one Sweeter Meats spots.
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{faramir, shaman},
				new PhysicalCardImpl[]{aragorn, gollum});
		scn.SetTwilight(2);

		scn.FreepsResolveSkirmish(faramir);

		// His own player's skirmish event: +3 against a roaming minion.
		assertEquals(7, scn.GetStrength(faramir));
		assertTrue(scn.FreepsPlayAvailable(valor));
		scn.FreepsPlayCard(valor);
		assertTrue(scn.FreepsHasCardChoiceAvailable(faramir, aragorn));
		scn.FreepsChooseCard(faramir);
		assertEquals(10, scn.GetStrength(faramir));
		scn.ShadowPass();

		// His own player's skirmish special ability.
		assertTrue(scn.FreepsActionAvailable(bow));
		assertEquals(0, scn.GetWoundsOn(faramir));
		scn.FreepsUseCardAction(bow);
		assertEquals(1, scn.GetWoundsOn(faramir));

		// The Shadow player's Response ability and Response event, in Faramir's skirmish.
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(shaman));
		assertTrue(scn.ShadowPlayAvailable(meats));
		assertEquals(0, scn.GetWoundsOn(shaman));
		scn.ShadowChooseOptionalTrigger(shaman);
		assertEquals(0, scn.GetTwilight());
		assertEquals(0, scn.GetWoundsOn(shaman));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}
}
