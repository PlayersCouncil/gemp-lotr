package com.gempukku.lotro.cards.official.set13;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_13_180_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("east", "13_180");
					put("otsea", "12_179");      // Úlairë Otsëa, Black Specter: [wraith] Nazgûl, vitality 3; a Shadow ability used only on purpose
					put("khamul", "103_61");     // Úlairë Attëa, Khamûl of the East: [raider] Nazgûl; the rest needs 3 burdens
					put("predator", "7_196");    // Morgul Predator: [wraith] Orc and no Nazgûl; a skirmish ability used only on purpose
					put("steed", "1_208");       // Black Steed: [wraith] mount borne by a Nazgûl, strength +2 at a plains (site 2)

					put("destroyed", "1_220");   // Not Easily Destroyed: [wraith] support-area condition; an ability used only on purpose
					put("crown", "101_39");      // Pale Crown: [wraith] support-area artifact; its text reaches only a bearer
					put("spires", "1_249");      // Gleaming Spires Will Crumble: [sauron] support-area condition; skirmishes only

					put("aragorn", "1_89");      // Aragorn, Ranger of the North: vitality 4; a maneuver ability only
					put("elrond", "1_40");       // Elrond, Lord of Rivendell: an ally, vitality 4; heals allies at the start of his owner's turns
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void ShadowintheEastStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 13
		 * Name: Shadow in the East
		 * Unique: False
		 * Side: Shadow
		 * Culture: Wraith
		 * Twilight Cost: 4
		 * Type: Event
		 * Subtype: Shadow
		 * Game Text: <b>Toil 1</b>. <helper>(For each [wraith] character you exert when playing this, its twilight cost is -1.)</helper><br>To play, spot a [wraith] card in your support area.<br>Spot a companion to draw a card for each wound on that companion (and draw an additional card if you can spot a Nazgûl).
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("east");

		assertEquals("Shadow in the East", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.WRAITH, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.TOIL));
		assertEquals(1, scn.GetKeywordCount(card, Keyword.TOIL));
		assertTrue(scn.HasTimeword(card, Timeword.SHADOW));
		assertEquals(4, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void PlayableOnlyWithAWraithCardInYourSupportAreaAndDrawsForEachWoundAndOneMoreForTheNazgul() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var east = scn.GetShadowCard("east");
		var otsea = scn.GetShadowCard("otsea");
		var steed = scn.GetShadowCard("steed");
		var destroyed = scn.GetShadowCard("destroyed");
		var spires = scn.GetShadowCard("spires");
		scn.MoveCardsToHand(east);
		scn.MoveCardsToSupportArea(spires);

		scn.StartGame();
		scn.AddWoundsToChar(aragorn, 3);
		assertEquals(3, scn.GetWoundsOn(aragorn));
		assertInZone(Zone.SUPPORT, spires);
		scn.MoveMinionsToTable(otsea);
		scn.AttachCardsTo(otsea, steed);
		assertAttachedTo(steed, otsea);
		scn.SetTwilight(4);
		scn.FreepsPass();

		// A [wraith] Nazgûl in play bearing a [wraith] possession, and a [sauron] condition in the support area: none
		// of them is a [wraith] card in the support area.
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(east));

		scn.MoveCardsToSupportArea(destroyed);
		assertInZone(Zone.SUPPORT, destroyed);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowPlayAvailable(east));
		assertEquals(1, scn.GetShadowHandCount());
		scn.ShadowPlayCard(east);
		// Toil: the pool pays in full, so the [wraith] Nazgûl is offered to exert, from 0, and is not.
		assertEquals(0, scn.ShadowGetChoiceMin());
		scn.ShadowDeclineChoosing();
		assertEquals(0, scn.GetWoundsOn(otsea));
		scn.ShadowChooseCard(aragorn);

		// 3 wounds on Aragorn, and one more for the Nazgûl.
		assertInDiscard(east);
		assertEquals(4, scn.GetShadowHandCount());
		assertEquals(3, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AWraithArtifactThereWillDoAndWithNoNazgulTheWoundsAloneAreDrawn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var elrond = scn.GetFreepsCard("elrond");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveAlliesToTable(elrond);
		var frodo = scn.GetRingBearer();

		var east = scn.GetShadowCard("east");
		var predator = scn.GetShadowCard("predator");
		var crown = scn.GetShadowCard("crown");
		scn.MoveCardsToHand(east);
		scn.MoveCardsToSupportArea(crown);

		scn.StartGame();
		scn.AddWoundsToChar(frodo, 1);
		scn.AddWoundsToChar(elrond, 2);
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(2, scn.GetWoundsOn(elrond));
		assertInZone(Zone.SUPPORT, crown);
		scn.MoveMinionsToTable(predator);
		scn.AddWoundsToChar(predator, 1);
		assertEquals(1, scn.GetWoundsOn(predator));
		scn.SetTwilight(4);
		scn.FreepsPass();

		assertTrue(scn.ShadowPlayAvailable(east));
		assertEquals(1, scn.GetShadowHandCount());
		scn.ShadowPlayCard(east);
		scn.ShadowDeclineChoosing();

		// Either companion, wounded or not; not the wounded ally, nor the wounded minion.
		assertTrue(scn.ShadowHasCardChoiceAvailable(frodo, aragorn));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(elrond, predator));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(frodo);

		// One wound on Frodo, and no Nazgûl to spot: the Predator is a [wraith] Orc.
		assertInDiscard(east);
		assertEquals(1, scn.GetShadowHandCount());
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ANazgulOfAnotherCultureEarnsTheAdditionalCard() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var east = scn.GetShadowCard("east");
		var khamul = scn.GetShadowCard("khamul");
		var destroyed = scn.GetShadowCard("destroyed");
		scn.MoveCardsToHand(east);
		scn.MoveCardsToSupportArea(destroyed);

		scn.StartGame();
		scn.AddWoundsToChar(aragorn, 2);
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertInZone(Zone.SUPPORT, destroyed);
		scn.MoveMinionsToTable(khamul);
		scn.SetTwilight(4);
		scn.FreepsPass();

		assertTrue(scn.ShadowPlayAvailable(east));
		assertEquals(1, scn.GetShadowHandCount());
		// Khamûl is no [wraith] character: nothing is asked for toil.
		scn.ShadowPlayCard(east);
		scn.ShadowChooseCard(aragorn);

		// Two wounds, and one more for the [raider] Nazgûl.
		assertInDiscard(east);
		assertEquals(3, scn.GetShadowHandCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
