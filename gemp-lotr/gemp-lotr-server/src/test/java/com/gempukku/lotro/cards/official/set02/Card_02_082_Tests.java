package com.gempukku.lotro.cards.official.set02;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_02_082_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("attea", "2_82");

					// Allies of three cultures and races.
					put("albert", "1_69");      // Albert Dreary, Entertainer from Bree: [gandalf] Man ally, vitality 3; a maneuver ability used only on purpose
					put("orophin", "1_56");     // Orophin, Lórien Bowman: [elven] Elf ally, vitality 3; an archery ability, and no archery phase is reached
					put("bounder", "1_286");    // Bounder: [shire] Hobbit ally, vitality 2; a skirmish ability used only on purpose
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a companion, the character who is no ally

					// Morgul Hound: a [wraith] Orc minion of cost 2 whose text only reads wounds on the Ring-bearer.
					put("hound", "7_192");
					// Úlairë Nelya, Ringwraith in Twilight: a twilight Nazgûl of cost 5, site 2, who plays with no text
					// of her own; her fierce and damage bonuses come only with burdens or wounds on the Ring-bearer.
					put("nelya", "2_84");

					// Easterling Footman: "Maneuver: If you cannot spot 2 burdens, spot another Easterling and discard
					// a card from hand to add a burden."
					put("footman", "7_143");
					// Easterling Axeman: an Easterling, strength +3 and fierce only at 2 burdens.
					put("axeman", "4_224");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void UlaireAtteaStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 2
		 * Name: Úlairë Attëa, The Easterling
		 * Unique: True
		 * Side: Shadow
		 * Culture: Ringwraith
		 * Twilight Cost: 6
		 * Type: Minion
		 * Subtype: Nazgûl
		 * Strength: 12
		 * Vitality: 3
		 * Site Number: 3
		 * Game Text: <b>Twilight</b>.<br>When you play Úlairë Attëa, you may wound an ally.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("attea");

		assertEquals("Úlairë Attëa", card.getBlueprint().getTitle());
		assertEquals("The Easterling", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.WRAITH, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.NAZGUL, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.TWILIGHT));
		assertEquals(6, card.getBlueprint().getTwilightCost());
		assertEquals(12, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(3, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void WhenPlayedMayWoundAnyAllyAndAnExhaustedOneIsKilled() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var albert = scn.GetFreepsCard("albert");
		var orophin = scn.GetFreepsCard("orophin");
		var bounder = scn.GetFreepsCard("bounder");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveAlliesToTable(albert, orophin, bounder);

		var attea = scn.GetShadowCard("attea");
		scn.MoveCardsToHand(attea);
		assertInHand(attea);

		scn.StartGame();
		// Vitality 2 with 1 wound: one more kills him.
		scn.AddWoundsToChar(bounder, 1);
		assertEquals(1, scn.GetWoundsOn(bounder));
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertEquals(1, scn.GetWoundsOn(bounder));
		scn.ShadowPlayCard(attea);

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(attea));
		scn.ShadowAcceptOptionalTrigger();

		// Any ally, whatever his culture or race, the exhausted one too; not a companion.
		assertTrue(scn.ShadowHasCardChoiceAvailable(albert, orophin, bounder));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(aragorn));
		assertEquals(1, scn.ShadowGetChoiceMin());
		assertEquals(0, scn.GetWoundsOn(albert));
		assertEquals(0, scn.GetWoundsOn(orophin));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		scn.ShadowChooseCard(bounder);

		// A wound and not an exertion: it falls on him exhausted, and kills him.
		assertInZone(Zone.DEAD, bounder);
		assertEquals(0, scn.GetWoundsOn(albert));
		assertEquals(0, scn.GetWoundsOn(orophin));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertInPlay(attea);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void DecliningTheWoundLeavesEveryAllyUnwounded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var albert = scn.GetFreepsCard("albert");
		var orophin = scn.GetFreepsCard("orophin");
		scn.MoveAlliesToTable(albert, orophin);

		var attea = scn.GetShadowCard("attea");
		scn.MoveCardsToHand(attea);
		assertInHand(attea);

		scn.StartGame();
		scn.SetTwilight(10);
		scn.FreepsPass();

		scn.ShadowPlayCard(attea);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(attea));
		scn.ShadowDeclineOptionalTrigger();

		assertEquals(0, scn.GetWoundsOn(albert));
		assertEquals(0, scn.GetWoundsOn(orophin));
		assertInPlay(attea);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void NotOfferedWithNoAllyInPlay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var attea = scn.GetShadowCard("attea");
		scn.MoveCardsToHand(attea);
		assertInHand(attea);

		scn.StartGame();
		scn.SetTwilight(10);
		scn.FreepsPass();

		scn.ShadowPlayCard(attea);

		assertFalse(scn.ShadowHasOptionalTriggerAvailable(attea));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertInPlay(attea);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AnotherNazgulPlayedAfterHerOffersNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var albert = scn.GetFreepsCard("albert");
		scn.MoveAlliesToTable(albert);

		var attea = scn.GetShadowCard("attea");
		var nelya = scn.GetShadowCard("nelya");
		scn.MoveCardsToHand(nelya);
		assertInHand(nelya);

		scn.StartGame();
		scn.MoveMinionsToTable(attea);
		scn.SetTwilight(10);
		scn.FreepsPass();

		scn.ShadowPlayCard(nelya);

		assertInPlay(nelya);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(attea));
		assertEquals(0, scn.GetWoundsOn(albert));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void SheIsNoEasterlingForACardThatSpotsOne() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var attea = scn.GetShadowCard("attea");
		var footman = scn.GetShadowCard("footman");
		var axeman = scn.GetShadowCard("axeman");
		var hound = scn.GetShadowCard("hound");
		// A card in hand for the Footman's cost.
		scn.MoveCardsToHand(hound);
		assertInHand(hound);

		scn.StartGame();
		scn.MoveMinionsToTable(attea, footman);

		// "The Easterling" is her subtitle, and no keyword.
		assertFalse(scn.HasKeyword(attea, Keyword.EASTERLING));

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		// No burden, a card in hand, and no other Easterling to spot than her: the Footman's action is not offered.
		assertEquals(0, scn.GetBurdens());
		assertEquals(1, scn.GetShadowHandCount());
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertFalse(scn.ShadowActionAvailable(footman));

		// An Easterling beside them: it is.
		scn.MoveMinionsToTable(axeman);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowActionAvailable(footman));
	}
}
